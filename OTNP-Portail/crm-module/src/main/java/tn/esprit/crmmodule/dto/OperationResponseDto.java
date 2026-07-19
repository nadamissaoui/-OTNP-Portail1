package tn.esprit.crmmodule.dto;

import lombok.Data;

@Data
public class OperationResponseDto {

    private String errorOrigin;
    private String errorDetail;
    private Boolean isSuccessful;

    // Getters & Setters
}