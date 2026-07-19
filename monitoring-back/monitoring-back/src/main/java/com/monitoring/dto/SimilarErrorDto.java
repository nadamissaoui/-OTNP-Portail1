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
public class SimilarErrorDto {
    private Long logEntryId;
    private String processId;
    private String processName;
    private String errorType;
    private String message;
    private LocalDateTime timestamp;
    private String appliedSolution;
}
