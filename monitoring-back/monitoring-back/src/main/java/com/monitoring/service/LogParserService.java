package com.monitoring.service;


import com.monitoring.entity.LogEntry;
import com.monitoring.entity.LogEntry.LogLevel;
import com.monitoring.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogParserService {

    private final LogEntryRepository logEntryRepository;

    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}[.,]?\\d{0,3})\\s+"
                    + "\\[?([A-Z]+)\\]?\\s+"
                    + "(?:\\[([^\\]]+)\\]\\s+)?"
                    + "(.*)"
    );

    private static final Pattern EXCEPTION_PATTERN = Pattern.compile(
            "^([a-zA-Z][a-zA-Z0-9_.]*(?:Exception|Error|Throwable))(?::\\s*(.*))?$"
    );

    private static final Pattern STACK_TRACE_LINE_PATTERN = Pattern.compile(
            "^\\s+at\\s+(.+)$"
    );

    private static final Pattern CAUSED_BY_PATTERN = Pattern.compile(
            "^Caused by:\\s+([a-zA-Z][a-zA-Z0-9_.]*(?:Exception|Error|Throwable))(?::\\s*(.*))?$"
    );

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    );

    public List<LogEntry> parseAndSave(String logContent, String processId, String workflowType) {
        List<LogEntry> entries = parse(logContent, processId, workflowType);
        return logEntryRepository.saveAll(entries);
    }

    public List<LogEntry> parse(String logContent, String processId, String workflowType) {
        List<LogEntry> entries = new ArrayList<>();
        String[] lines = logContent.split("\\r?\\n");

        LogEntry currentEntry = null;
        StringBuilder currentStackTrace = new StringBuilder();
        boolean inStackTrace = false;

        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            Matcher logMatcher = LOG_LINE_PATTERN.matcher(line);
            if (logMatcher.matches()) {
                if (currentEntry != null) {
                    finalizeEntry(currentEntry, currentStackTrace, entries);
                }

                currentEntry = new LogEntry();
                currentEntry.setTimestamp(parseTimestamp(logMatcher.group(1)));
                currentEntry.setLogLevel(parseLogLevel(logMatcher.group(2)));
                String source = logMatcher.group(3);
                currentEntry.setProcessName(source);
                currentEntry.setMessage(logMatcher.group(4));
                currentEntry.setProcessId(processId != null ? processId : extractProcessId(source));
                currentEntry.setWorkflowType(workflowType);
                currentEntry.setCreatedAt(LocalDateTime.now());
                currentEntry.setResolved(false);

                currentStackTrace = new StringBuilder();
                inStackTrace = false;
                continue;
            }

            Matcher exceptionMatcher = EXCEPTION_PATTERN.matcher(line);
            if (exceptionMatcher.matches()) {
                if (currentEntry == null) {
                    currentEntry = createErrorEntry(processId, workflowType);
                }
                currentEntry.setErrorType(exceptionMatcher.group(1));
                if (exceptionMatcher.group(2) != null) {
                    currentEntry.setMessage(exceptionMatcher.group(2));
                }
                currentEntry.setLogLevel(LogLevel.ERROR);
                inStackTrace = true;
                currentStackTrace.append(line).append("\n");
                continue;
            }

            Matcher causedByMatcher = CAUSED_BY_PATTERN.matcher(line);
            if (causedByMatcher.matches() && currentEntry != null) {
                currentEntry.setErrorType(causedByMatcher.group(1));
                inStackTrace = true;
                currentStackTrace.append(line).append("\n");
                continue;
            }

            Matcher stackMatcher = STACK_TRACE_LINE_PATTERN.matcher(line);
            if (stackMatcher.matches() && currentEntry != null) {
                inStackTrace = true;
                currentStackTrace.append(line).append("\n");
                extractSourceInfo(currentEntry, stackMatcher.group(1));
                continue;
            }

            if (currentEntry != null && inStackTrace) {
                currentStackTrace.append(line).append("\n");
            } else if (currentEntry != null) {
                String existing = currentEntry.getMessage();
                currentEntry.setMessage(existing + " " + line.trim());
            }
        }

        if (currentEntry != null) {
            finalizeEntry(currentEntry, currentStackTrace, entries);
        }

        if (entries.isEmpty() && logContent != null && !logContent.isBlank()) {
            LogEntry freeTextEntry = createErrorEntry(processId, workflowType);
            freeTextEntry.setMessage(logContent.trim());
            freeTextEntry.setErrorType("UnknownWorkflowError");
            entries.add(freeTextEntry);
        }

        return entries;
    }

    private void finalizeEntry(LogEntry entry, StringBuilder stackTrace, List<LogEntry> entries) {
        if (stackTrace.length() > 0) {
            entry.setStackTrace(stackTrace.toString());
        }
        if (entry.getLogLevel() == LogLevel.ERROR || entry.getLogLevel() == LogLevel.FATAL) {
            entries.add(entry);
        } else if (entry.getErrorType() != null) {
            entries.add(entry);
        }
    }

    private LogEntry createErrorEntry(String processId, String workflowType) {
        LogEntry entry = new LogEntry();
        entry.setProcessId(processId != null ? processId : "UNKNOWN");
        entry.setWorkflowType(workflowType);
        entry.setLogLevel(LogLevel.ERROR);
        entry.setTimestamp(LocalDateTime.now());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setResolved(false);
        return entry;
    }

    private void extractSourceInfo(LogEntry entry, String stackTraceLine) {
        if (entry.getSourceFile() != null) {
            return;
        }
        Pattern filePattern = Pattern.compile("\\(([^)]+\\.java):(\\d+)\\)");
        Matcher fileMatcher = filePattern.matcher(stackTraceLine);
        if (fileMatcher.find()) {
            entry.setSourceFile(fileMatcher.group(1));
            entry.setLineNumber(Integer.parseInt(fileMatcher.group(2)));
        }
    }

    private LocalDateTime parseTimestamp(String timestampStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(timestampStr, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        log.warn("Could not parse timestamp: {}", timestampStr);
        return LocalDateTime.now();
    }

    private LogLevel parseLogLevel(String level) {
        try {
            return LogLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LogLevel.INFO;
        }
    }

    private String extractProcessId(String source) {
        if (source == null) {
            return "UNKNOWN";
        }
        Pattern pidPattern = Pattern.compile("PID[=:]?(\\d+)|pid[=:]?(\\d+)");
        Matcher matcher = pidPattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return source;
    }
}


