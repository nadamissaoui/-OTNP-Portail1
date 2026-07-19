package com.monitoring.repository;

import com.monitoring.model.PortabilityRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortabilityRequestRepository extends JpaRepository<PortabilityRequest, Long> {

    Optional<PortabilityRequest> findByProcessInstanceId(Long processInstanceId);

    List<PortabilityRequest> findByStatus(String status);

    List<PortabilityRequest> findByClientNameContainingIgnoreCase(String clientName);

    List<PortabilityRequest> findByMsisdn(String msisdn);

    List<PortabilityRequest> findByCinNumber(String cinNumber);
}
