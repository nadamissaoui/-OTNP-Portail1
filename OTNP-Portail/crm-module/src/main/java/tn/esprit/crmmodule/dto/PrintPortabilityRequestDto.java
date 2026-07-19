package tn.esprit.crmmodule.dto;

import lombok.Data;

@Data
public class PrintPortabilityRequestDto {
    private String portaRef;      // référence de portabilité
    private String custId;        // id client
    private String contractCode;  // code du contrat
    private String numPorter;     // numéro à porter
}