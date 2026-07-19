package tn.esprit.bscsmodule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportExternalResourceResponseDto {

    private String comment;
    private boolean isSuccessful;
    private Long dnId;
}