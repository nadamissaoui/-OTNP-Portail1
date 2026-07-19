package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class CheckCommercialEligibilityResponseDto {

    private boolean successful;
    private String comment;

    public CheckCommercialEligibilityResponseDto() {}

    public CheckCommercialEligibilityResponseDto(boolean successful, String comment) {
        this.successful = successful;
        this.comment = comment;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
