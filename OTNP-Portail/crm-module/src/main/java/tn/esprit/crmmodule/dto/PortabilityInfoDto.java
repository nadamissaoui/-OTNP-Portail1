package tn.esprit.crmmodule.dto;

import lombok.Data;

@Data
public class PortabilityInfoDto {
    private String portaRef;
    private String idcrm;
    private String crmReference;
    private String contractCode;
    private String numeroOrange;
    private String msisdn;
    private String rio;
    private String portaDate;
    private String changeDate;
    private String portaOperator;
    private String statusCode;
    private String statusReason;
    private String canal;
    private String isCanceled;
    private String cin;
}