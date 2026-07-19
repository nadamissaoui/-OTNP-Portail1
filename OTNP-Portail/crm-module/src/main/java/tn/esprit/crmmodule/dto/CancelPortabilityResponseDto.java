package tn.esprit.crmmodule.dto;

import lombok.Data;

@Data
public class CancelPortabilityResponseDto {

    private OperationResponseDto operationResponse;

    public OperationResponseDto getOperationResponse() {
        return operationResponse;
    }

    public void setOperationResponse(OperationResponseDto operationResponse) {
        this.operationResponse = operationResponse;
    }
}