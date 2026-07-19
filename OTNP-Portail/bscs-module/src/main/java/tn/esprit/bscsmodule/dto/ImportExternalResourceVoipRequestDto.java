package tn.esprit.bscsmodule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImportExternalResourceVoipRequestDto extends ImportExternalResourceRequestDto {
    private String hlCode;
    private String scCode;
}