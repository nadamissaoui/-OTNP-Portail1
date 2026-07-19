package com.monitoring.dto;

public class SavePortabilityRequestDto {

    private Long processInstanceId;
    private String clientName;
    private String cinNumber;
    private String msisdn;
    private String rioCode;
    private String contractType;
    private String idClient;
    private String typeIdentite;
    private String refCrm;
    private String marche;
    private String numeroOrange;
    private String workflowType;

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

    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }
}
