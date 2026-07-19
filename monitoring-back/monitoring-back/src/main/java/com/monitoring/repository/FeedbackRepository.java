package com.monitoring.repository;

import com.monitoring.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByProcessInstanceId(Long processInstanceId);

    boolean existsByProcessInstanceId(Long processInstanceId);
}
