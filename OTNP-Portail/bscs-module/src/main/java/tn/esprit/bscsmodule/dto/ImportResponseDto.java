package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class ImportResponseDto {

    private boolean isSuccessful;
    private String comment;
    private String dnId;

    public ImportResponseDto() {
    }

    public ImportResponseDto(boolean isSuccessful, String comment, String dnId) {
        this.isSuccessful = isSuccessful;
        this.comment = comment;
        this.dnId = dnId;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDnId() {
        return dnId;
    }

    public void setDnId(String dnId) {
        this.dnId = dnId;
    }
}
