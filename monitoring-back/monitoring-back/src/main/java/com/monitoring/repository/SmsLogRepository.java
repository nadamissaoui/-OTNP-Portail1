package com.monitoring.repository;

import com.monitoring.entity.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

    List<SmsLog> findByProcessInstanceIdOrderBySentAtDesc(Long processInstanceId);

    boolean existsByProcessInstanceIdAndType(Long processInstanceId, String type);
}
