package tn.esprit.crmmodule.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class CreatePortabilityGPRequestDto {

    private String portaRef;
    private PortaStatus statusCode;
    private String statusReason;

    // On enlève @JsonFormat et on laisse faire nos setters personnalisés
    private LocalDateTime portaDate;
    private LocalDateTime changeDate;

    // --- MAGIE ICI : Les Setters qui acceptent du texte ---

    public void setPortaDate(Object value) {
        this.portaDate = convertToLocalDateTime(value);
    }

    public void setChangeDate(Object value) {
        this.changeDate = convertToLocalDateTime(value);
    }

    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        String dateStr = value.toString();
        if (dateStr.isEmpty()) return null;

        // Si jBPM envoie juste YYYY-MM-DD (10 caractères)
        if (dateStr.length() <= 10) {
            return LocalDate.parse(dateStr).atStartOfDay(); // Ajoute 00:00:00
        }
        // Sinon, si c'est déjà un format complet
        return LocalDateTime.parse(dateStr.replace(" ", "T"));
    }

    private String custId;
    private String contractCode;
    private String numPorter;
    private String numOrange;
    private PortaOperator actualOperator;
    private String rio;
}