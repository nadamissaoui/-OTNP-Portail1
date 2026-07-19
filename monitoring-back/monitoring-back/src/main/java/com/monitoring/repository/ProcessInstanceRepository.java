package com.monitoring.repository;

import com.monitoring.model.ProcessInstanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstanceLog, Long> {

    // Compte TOUTES les instances de l'application pour tes tests (au lieu de CURDATE())
    @Query(value = "SELECT COUNT(*) FROM jbpm.processinstancelog", nativeQuery = true)
    long countInstancesToday();

    // Compte tous les succès réels (status = 2)
    @Query(value = "SELECT COUNT(*) FROM jbpm.processinstancelog WHERE status = 2", nativeQuery = true)
    long countSuccessToday();

    // Compte toutes les erreurs (status = 3 ou 5)
    @Query(value = "SELECT COUNT(*) FROM jbpm.processinstancelog WHERE (status = 3 OR status = 5)", nativeQuery = true)
    long countCriticalErrorsToday();

    // Calcule le temps moyen global en minutes
    @Query(value = "SELECT IFNULL(AVG(duration) / 60000.0, 0.0) FROM jbpm.processinstancelog WHERE duration IS NOT NULL", nativeQuery = true)
    double getAverageProcessingTimeMinutes();
    @Query(value = "SELECT name, COUNT(*) as volume " +
            "FROM jbpm.task " +
            "WHERE status IN ('Ready', 'Reserved') " +
            "GROUP BY name " +
            "ORDER BY volume DESC", nativeQuery = true)
    List<Object[]> getPendingHumanTasksStatistics();

    // Récupère les durées par étape du workflow pour l'analyse du cycle de vie
    @Query(value = "SELECT " +
            "    n.nodename as stage_name, " +
            "    AVG(TIMESTAMPDIFF(SECOND, pi.start_date, n.end_date)) / 3600.0 as avg_duration_hours, " +
            "    MIN(TIMESTAMPDIFF(SECOND, pi.start_date, n.end_date)) / 3600.0 as min_duration_hours, " +
            "    MAX(TIMESTAMPDIFF(SECOND, pi.start_date, n.end_date)) / 3600.0 as max_duration_hours, " +
            "    COUNT(*) as sample_size " +
            "FROM jbpm.processinstancelog pi " +
            "JOIN jbpm.nodeinstancelog n ON pi.processinstanceid = n.processinstanceid " +
            "WHERE pi.status IN (1, 2) " +
            "  AND n.nodetype = 'HumanTaskNode' " +
            "  AND n.end_date IS NOT NULL " +
            "  AND pi.start_date IS NOT NULL " +
            "GROUP BY n.nodename " +
            "ORDER BY avg_duration_hours DESC", nativeQuery = true)
    List<Object[]> getLifecycleStageDurations();

    // Compte les instances qui respectent le SLA (2 jours = 48 heures)
    @Query(value = "SELECT COUNT(*) " +
            "FROM jbpm.processinstancelog " +
            "WHERE status = 2 " + // Completed
            "  AND duration IS NOT NULL " +
            "  AND duration / 3600000.0 <= 48", nativeQuery = true)
    long countSlaCompliantInstances();

    // Compte le total des instances completed
    @Query(value = "SELECT COUNT(*) " +
            "FROM jbpm.processinstancelog " +
            "WHERE status = 2", nativeQuery = true)
    long countTotalCompletedInstances();

    @Query(value = "SELECT MONTH(start_date) as m, processId, COUNT(*) as cnt " +
            "FROM jbpm.processinstancelog " +
            "WHERE start_date IS NOT NULL " +
            "GROUP BY MONTH(start_date), processId " +
            "ORDER BY m, processId", nativeQuery = true)
    List<Object[]> countByMonthWithProcessId();
}