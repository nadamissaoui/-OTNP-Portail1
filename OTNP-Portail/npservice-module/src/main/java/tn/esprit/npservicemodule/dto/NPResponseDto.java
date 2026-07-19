package tn.esprit.npservicemodule.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NPResponseDto {

    private int statusCode;       // 0 = succès, -1 = erreur
    private String statusMessage; // message retourné (comment SOAP)
}