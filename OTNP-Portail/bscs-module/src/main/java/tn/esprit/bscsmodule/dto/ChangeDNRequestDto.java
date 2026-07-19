package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class ChangeDNRequestDto {
    private String dnTarget;
    private String contractId;
    private String coIdPub;
    private String dirNum;
    private String retention;
    private String service;

    // Constructeurs, getters, setters
    public ChangeDNRequestDto() {}

    public ChangeDNRequestDto(String dnTarget, String contractId, String coIdPub, String dirNum, String retention, String service) {
        this.dnTarget = dnTarget;
        this.contractId = contractId;
        this.coIdPub = coIdPub;
        this.dirNum = dirNum;
        this.retention = retention;
        this.service = service;
    }

    public String getDnTarget() { return dnTarget; }
    public void setDnTarget(String dnTarget) { this.dnTarget = dnTarget; }
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }
    public String getCoIdPub() { return coIdPub; }
    public void setCoIdPub(String coIdPub) { this.coIdPub = coIdPub; }
    public String getDirNum() { return dirNum; }
    public void setDirNum(String dirNum) { this.dirNum = dirNum; }
    public String getRetention() { return retention; }
    public void setRetention(String retention) { this.retention = retention; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}
