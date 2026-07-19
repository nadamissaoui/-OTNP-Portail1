package tn.esprit.bscsmodule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerReferenceDto {
    private Long csId;
    private String csIdPub;
}