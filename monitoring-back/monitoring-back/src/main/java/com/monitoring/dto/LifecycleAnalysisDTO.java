package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleAnalysisDTO {
    
    private List<StageDuration> stageDurations;
    private double totalAverageDurationHours;
    private String bottleneckStage;
    private double slaCompliancePercentage;
    private int totalAnalyzedInstances;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageDuration {
        private String stageName;
        private double averageDurationHours;
        private double minDurationHours;
        private double maxDurationHours;
        private double slaThresholdHours;
        private boolean slaCompliant;
        private int sampleSize;
    }
}
