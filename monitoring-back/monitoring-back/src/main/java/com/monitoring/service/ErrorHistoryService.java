package com.monitoring.service;


import com.monitoring.dto.LogEntryDto;
import com.monitoring.entity.LogEntry;
import com.monitoring.repository.ErrorSolutionRepository;
import com.monitoring.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorHistoryService {

    private final LogEntryRepository logEntryRepository;
    private final ErrorSolutionRepository errorSolutionRepository;

    public List<LogEntryDto> getAllErrors() {
        return logEntryRepository.findAllErrors().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LogEntryDto> getErrorsByProcessId(String processId) {
        return logEntryRepository.findErrorsByProcessId(processId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LogEntryDto> getErrorsByType(String errorType) {
        return logEntryRepository.findByErrorType(errorType).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LogEntryDto> getLogsByProcessId(String processId) {
        return logEntryRepository.findByProcessId(processId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<LogEntryDto> searchErrors(String keyword) {
        return logEntryRepository.findSimilarErrors(keyword).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void markAsResolved(Long logEntryId) {
        LogEntry entry = logEntryRepository.findById(logEntryId)
                .orElseThrow(() -> new RuntimeException("LogEntry not found: " + logEntryId));
        entry.setResolved(true);
        logEntryRepository.save(entry);
    }

    public void markSolutionAsApplied(Long solutionId) {
        var solution = errorSolutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("ErrorSolution not found: " + solutionId));
        solution.setApplied(true);
        errorSolutionRepository.save(solution);
    }

    private LogEntryDto toDto(LogEntry entry) {
        return LogEntryDto.builder()
                .id(entry.getId())
                .processId(entry.getProcessId())
                .processName(entry.getProcessName())
                .workflowType(entry.getWorkflowType())
                .logLevel(entry.getLogLevel().name())
                .message(entry.getMessage())
                .stackTrace(entry.getStackTrace())
                .errorType(entry.getErrorType())
                .sourceFile(entry.getSourceFile())
                .lineNumber(entry.getLineNumber())
                .timestamp(entry.getTimestamp())
                .resolved(entry.getResolved())
                .build();
    }
}
