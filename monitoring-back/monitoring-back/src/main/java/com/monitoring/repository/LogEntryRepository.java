package com.monitoring.repository;


import com.monitoring.entity.LogEntry;
import com.monitoring.entity.LogEntry.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    List<LogEntry> findByProcessId(String processId);

    List<LogEntry> findByLogLevel(LogLevel logLevel);

    List<LogEntry> findByProcessIdAndLogLevel(String processId, LogLevel logLevel);

    List<LogEntry> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<LogEntry> findByWorkflowType(String workflowType);

    @Query("SELECT l FROM LogEntry l WHERE l.logLevel IN ('ERROR', 'FATAL') ORDER BY l.timestamp DESC")
    List<LogEntry> findAllErrors();

    @Query("SELECT l FROM LogEntry l WHERE l.logLevel IN ('ERROR', 'FATAL') AND l.processId = :processId ORDER BY l.timestamp DESC")
    List<LogEntry> findErrorsByProcessId(@Param("processId") String processId);

    @Query("SELECT l FROM LogEntry l WHERE l.logLevel IN ('ERROR', 'FATAL') "
            + "AND l.errorType = :errorType ORDER BY l.timestamp DESC")
    List<LogEntry> findByErrorType(@Param("errorType") String errorType);

    @Query("SELECT l FROM LogEntry l WHERE l.logLevel IN ('ERROR', 'FATAL') "
            + "AND l.message LIKE CONCAT('%', :keyword, '%') "
            + "ORDER BY l.timestamp DESC")
    List<LogEntry> findSimilarErrors(@Param("keyword") String keyword);

    @Query("SELECT l FROM LogEntry l WHERE l.logLevel IN ('ERROR', 'FATAL') "
            + "AND l.errorType = :errorType AND l.id <> :excludeId "
            + "ORDER BY l.timestamp DESC")
    List<LogEntry> findSimilarByErrorType(@Param("errorType") String errorType,
                                          @Param("excludeId") Long excludeId);
}
