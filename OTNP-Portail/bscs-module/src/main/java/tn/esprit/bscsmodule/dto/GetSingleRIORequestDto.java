package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class GetSingleRIORequestDto {

    private String dnNum;
    private String operatorCode;

    public GetSingleRIORequestDto() {}

    public GetSingleRIORequestDto(String dnNum, String operatorCode) {
        this.dnNum = dnNum;
        this.operatorCode = operatorCode;
    }

    public String getDnNum() {
        return dnNum;
    }

    public void setDnNum(String dnNum) {
        this.dnNum = dnNum;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }
}