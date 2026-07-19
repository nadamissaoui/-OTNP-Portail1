package tn.esprit.bscsmodule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportExternalResourceRequestDto {
    //private ContractReferenceDto contractReference;
    private CustomerReferenceDto customerReference;
    private String dnTarget;
    private String operatorCode;
    private SessionChangeDto sessionChange;
    private String smSerialNum;
}