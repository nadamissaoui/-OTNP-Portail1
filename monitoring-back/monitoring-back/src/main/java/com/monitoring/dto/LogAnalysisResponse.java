package com.monitoring.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAnalysisResponse {
    private Long logEntryId;
    private String processId;
    private String errorType;
    private String errorMessage;
    private String severity;
    private List<ErrorSuggestionDto> suggestions;
    private List<SimilarErrorDto> similarErrors;
    private String googleSearchUrl;
    private int totalSimilarErrorsCount;
}
