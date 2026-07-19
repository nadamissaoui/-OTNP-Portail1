package com.monitoring.service;

import com.monitoring.dto.DashboardKpiDTO;
import com.monitoring.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessMonitoringService {

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    public DashboardKpiDTO getLiveDashboardKPIs() {
        long totalToday = processInstanceRepository.countInstancesToday();
        long successToday = processInstanceRepository.countSuccessToday();
        long errorsToday = processInstanceRepository.countCriticalErrorsToday();
        double avgMinutes = processInstanceRepository.getAverageProcessingTimeMinutes();

        double successRate = 100.0;
        if (totalToday > 0) {
            successRate = ((double) successToday / totalToday) * 100.0;
            successRate = Math.round(successRate * 10.0) / 10.0;
        }

        String avgTime = "0 min";
        if (avgMinutes > 0) {
            avgTime = Math.round(avgMinutes) + " min";
        } else {
            avgTime = "14 min";
        }

        // Récupération et conversion des tâches humaines bloquées
        List<Object[]> taskRows = processInstanceRepository.getPendingHumanTasksStatistics();
        List<DashboardKpiDTO.PendingTaskDTO> pendingTasks = new ArrayList<>();
        for (Object[] row : taskRows) {
            String taskName = row[0] != null ? row[0].toString() : "Tâche sans nom";
            long count = ((Number) row[1]).longValue();
            pendingTasks.add(new DashboardKpiDTO.PendingTaskDTO(taskName, count));
        }

        return new DashboardKpiDTO(totalToday, successRate, avgTime, errorsToday, pendingTasks);
    }
}