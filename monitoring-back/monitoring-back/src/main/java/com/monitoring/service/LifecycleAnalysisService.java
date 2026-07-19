package com.monitoring.service;

import com.monitoring.dto.LifecycleAnalysisDTO;
import com.monitoring.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LifecycleAnalysisService {

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    // SLA standard: 2 jours = 48 heures
    private static final double SLA_THRESHOLD_HOURS = 48.0;

    public LifecycleAnalysisDTO getLifecycleAnalysis() {
        List<Object[]> stageData = processInstanceRepository.getLifecycleStageDurations();
        List<LifecycleAnalysisDTO.StageDuration> stages = new ArrayList<>();
        
        double totalDuration = 0;
        double maxDuration = 0;
        String bottleneck = null;
        int totalSampleSize = 0;

        for (Object[] row : stageData) {
            String stageName = row[0] != null ? row[0].toString() : "Unknown Stage";
            double avgDuration = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
            double minDuration = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
            double maxDurationStage = row[3] != null ? ((Number) row[3]).doubleValue() : 0;
            int sampleSize = row[4] != null ? ((Number) row[4]).intValue() : 0;

            // Déterminer le goulot d'étranglement (étape la plus longue)
            if (avgDuration > maxDuration) {
                maxDuration = avgDuration;
                bottleneck = stageName;
            }

            totalDuration += avgDuration;
            totalSampleSize += sampleSize;

            // Vérifier la conformité SLA pour cette étape
            boolean slaCompliant = avgDuration <= SLA_THRESHOLD_HOURS;

            stages.add(new LifecycleAnalysisDTO.StageDuration(
                stageName,
                avgDuration,
                minDuration,
                maxDurationStage,
                SLA_THRESHOLD_HOURS,
                slaCompliant,
                sampleSize
            ));
        }

        // Calculer le pourcentage de conformité SLA global
        long slaCompliant = processInstanceRepository.countSlaCompliantInstances();
        long totalCompleted = processInstanceRepository.countTotalCompletedInstances();
        
        double slaCompliancePercentage = 0;
        if (totalCompleted > 0) {
            slaCompliancePercentage = ((double) slaCompliant / totalCompleted) * 100.0;
            slaCompliancePercentage = Math.round(slaCompliancePercentage * 10.0) / 10.0;
        }

        return new LifecycleAnalysisDTO(
            stages,
            totalDuration,
            bottleneck,
            slaCompliancePercentage,
            totalSampleSize
        );
    }
}
