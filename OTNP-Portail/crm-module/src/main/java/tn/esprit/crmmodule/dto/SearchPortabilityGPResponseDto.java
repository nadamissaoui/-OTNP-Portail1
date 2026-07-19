package tn.esprit.crmmodule.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchPortabilityGPResponseDto {
    private List<PortabilityInfoDto> portabilityInfos;
    private OperationResponseDto operationResponse;

}