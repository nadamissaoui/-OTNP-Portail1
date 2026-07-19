package tn.esprit.crmmodule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CreatePortabilityB2BRequestDto {

    @NotNull(message = "portaRef is required")
    private String portaRef;

    @NotNull(message = "statusCode is required")
    private PortaStatus statusCode;
    private String statusReason;
    private LocalDateTime portaDate;
    private LocalDateTime changeDate;
    private String custId;
    private String contractCode;
    private String numPorter;
    private String numOrange;
    private PortaOperator actualOperator;
    private String rio;

    // Getters & Setters
}