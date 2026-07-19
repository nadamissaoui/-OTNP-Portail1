package com.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portability_requests")
public class PortabilityRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_instance_id")
    private Long processInstanceId;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "cin_number")
    private String cinNumber;

    private String msisdn;

    @Column(name = "rio_code")
    private String rioCode;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "id_client")
    private String idClient;

    @Column(name = "type_identite")
    private String typeIdentite;

    @Column(name = "ref_crm")
    private String refCrm;

    private String marche;

    @Column(name = "numero_orange")
    private String numeroOrange;

    private String status;

    @Column(name = "workflow_type")
    private String workflowType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(Long processInstanceId) { this.processInstanceId = processInstanceId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getCinNumber() { return cinNumber; }
    public void setCinNumber(String cinNumber) { this.cinNumber = cinNumber; }

    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }

    public String getRioCode() { return rioCode; }
    public void setRioCode(String rioCode) { this.rioCode = rioCode; }

    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }

    public String getIdClient() { return idClient; }
    public void setIdClient(String idClient) { this.idClient = idClient; }

    public String getTypeIdentite() { return typeIdentite; }
    public void setTypeIdentite(String typeIdentite) { this.typeIdentite = typeIdentite; }

    public String getRefCrm() { return refCrm; }
    public void setRefCrm(String refCrm) { this.refCrm = refCrm; }

    public String getMarche() { return marche; }
    public void setMarche(String marche) { this.marche = marche; }

    public String getNumeroOrange() { return numeroOrange; }
    public void setNumeroOrange(String numeroOrange) { this.numeroOrange = numeroOrange; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
