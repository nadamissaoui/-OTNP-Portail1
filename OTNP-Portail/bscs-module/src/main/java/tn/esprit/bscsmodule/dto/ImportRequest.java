package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data

public class ImportRequest {
    private String contractId;
    private String msisdn;
    private String operatorCode;

    // Getters et setters
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }

    public String getOperatorCode() { return operatorCode; }
    public void setOperatorCode(String operatorCode) { this.operatorCode = operatorCode; }

}
