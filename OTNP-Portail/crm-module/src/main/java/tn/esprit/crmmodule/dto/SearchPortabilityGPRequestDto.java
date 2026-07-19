package tn.esprit.crmmodule.dto;

import lombok.Data;

@Data
public class SearchPortabilityGPRequestDto {
    private String portaRef;
    private String custId;
    private String contractCode;
    private String numPorter;
}