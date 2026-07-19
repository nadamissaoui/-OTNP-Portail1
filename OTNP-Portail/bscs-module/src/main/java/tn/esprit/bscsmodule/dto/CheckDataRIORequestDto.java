package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class CheckDataRIORequestDto {

    private String dnNum;
    private String operatorCode;
    private String rioCode;

    public String getDnNum() { return dnNum; }
    public void setDnNum(String dnNum) { this.dnNum = dnNum; }

    public String getOperatorCode() { return operatorCode; }
    public void setOperatorCode(String operatorCode) { this.operatorCode = operatorCode; }

    public String getRioCode() { return rioCode; }
    public void setRioCode(String rioCode) { this.rioCode = rioCode; }
}
