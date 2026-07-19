package tn.esprit.crmmodule.dto;

import lombok.Data;

@Data
public class PrintPortabilityResponseDto {

    private String reportData; // contient cid:467241443636
    private OperationResponseDto operationResponse; // contient ErrorOrigin, ErrorDetail, IsSuccessful
}