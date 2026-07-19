package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorSuggestionDto {
    private String suggestion;
    private String googleSearchUrl;
    private String source;
    private Double confidenceScore;
}
