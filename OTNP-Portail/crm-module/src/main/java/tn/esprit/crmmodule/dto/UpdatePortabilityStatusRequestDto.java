package tn.esprit.crmmodule.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UpdatePortabilityStatusRequestDto {
    private String idcrm;
    private String portaRef;
    private String statusCode;
    private String statusReason;
    private LocalDateTime portageDate;

// getters & setters
}