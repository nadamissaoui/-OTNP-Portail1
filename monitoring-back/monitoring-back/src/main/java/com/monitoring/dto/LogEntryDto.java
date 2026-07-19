package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntryDto {
    private Long id;
    private String processId;
    private String processName;
    private String workflowType;
    private String logLevel;
    private String message;
    private String stackTrace;
    private String errorType;
    private String sourceFile;
    private Integer lineNumber;
    private LocalDateTime timestamp;
    private Boolean resolved;
}
