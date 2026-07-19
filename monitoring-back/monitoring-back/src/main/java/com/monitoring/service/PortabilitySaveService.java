package com.monitoring.service;

import com.monitoring.dto.SavePortabilityRequestDto;
import com.monitoring.model.PortabilityRequest;
import com.monitoring.repository.PortabilityRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortabilitySaveService {

    private final PortabilityRequestRepository repository;

    public PortabilitySaveService(PortabilityRequestRepository repository) {
        this.repository = repository;
    }

    public PortabilityRequest save(SavePortabilityRequestDto dto) {
        PortabilityRequest entity = new PortabilityRequest();
        entity.setProcessInstanceId(dto.getProcessInstanceId());
        entity.setClientName(dto.getClientName());
        entity.setCinNumber(dto.getCinNumber());
        entity.setMsisdn(dto.getMsisdn());
        entity.setRioCode(dto.getRioCode());
        entity.setContractType(dto.getContractType());
        entity.setIdClient(dto.getIdClient());
        entity.setTypeIdentite(dto.getTypeIdentite());
        entity.setRefCrm(dto.getRefCrm());
        entity.setMarche(dto.getMarche());
        entity.setNumeroOrange(dto.getNumeroOrange());
        entity.setWorkflowType(dto.getWorkflowType());
        entity.setStatus("ACTIVE");
        return repository.save(entity);
    }

    public PortabilityRequest findByProcessInstanceId(Long processInstanceId) {
        return repository.findByProcessInstanceId(processInstanceId).orElse(null);
    }

    public List<PortabilityRequest> findAll() {
        return repository.findAll();
    }

    public void updateStatus(Long processInstanceId, String status) {
        repository.findByProcessInstanceId(processInstanceId)
                .ifPresent(request -> {
                    request.setStatus(status);
                    repository.save(request);
                });
    }
}
