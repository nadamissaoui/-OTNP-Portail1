package com.monitoring.dto;

import java.util.List;

public class DashboardKpiDTO {
    private long totalToday;
    private double successRate;
    private String avgProcessingTime;
    private long criticalErrors;
    private List<PendingTaskDTO> pendingTasks; // Ajout de la liste des tâches bloquées

    public DashboardKpiDTO() {}

    public DashboardKpiDTO(long totalToday, double successRate, String avgProcessingTime, long criticalErrors, List<PendingTaskDTO> pendingTasks) {
        this.totalToday = totalToday;
        this.successRate = successRate;
        this.avgProcessingTime = avgProcessingTime;
        this.criticalErrors = criticalErrors;
        this.pendingTasks = pendingTasks;
    }

    // Getters et Setters
    public long getTotalToday() { return totalToday; }
    public void setTotalToday(long totalToday) { this.totalToday = totalToday; }
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public String getAvgProcessingTime() { return avgProcessingTime; }
    public void setAvgProcessingTime(String avgProcessingTime) { this.avgProcessingTime = avgProcessingTime; }
    public long getCriticalErrors() { return criticalErrors; }
    public void setCriticalErrors(long criticalErrors) { this.criticalErrors = criticalErrors; }
    public List<PendingTaskDTO> getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(List<PendingTaskDTO> pendingTasks) { this.pendingTasks = pendingTasks; }

    // Sous-classe DTO interne pour mapper proprement chaque tâche
    public static class PendingTaskDTO {
        private String taskName;
        private long count;

        public PendingTaskDTO(String taskName, long count) {
            this.taskName = taskName;
            this.count = count;
        }

        public String getTaskName() { return taskName; }
        public long getCount() { return count; }
    }
}