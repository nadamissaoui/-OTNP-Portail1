package com.monitoring.repository;

import com.monitoring.entity.ErrorSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorSolutionRepository extends JpaRepository<ErrorSolution, Long> {

    List<ErrorSolution> findByLogEntryId(Long logEntryId);

    @Query("SELECT es FROM ErrorSolution es WHERE es.logEntry.errorType = :errorType "
            + "ORDER BY es.confidenceScore DESC")
    List<ErrorSolution> findByErrorType(@Param("errorType") String errorType);

    @Query("SELECT es FROM ErrorSolution es WHERE es.applied = true "
            + "AND es.logEntry.errorType = :errorType "
            + "ORDER BY es.confidenceScore DESC")
    List<ErrorSolution> findAppliedSolutionsByErrorType(@Param("errorType") String errorType);
}

