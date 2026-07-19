package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class DNExportResponseDto {
    private boolean successful;
    private String comment;
    private String errorCode;

    public DNExportResponseDto() {}

    public DNExportResponseDto(boolean successful, String comment, String errorCode) {
        this.successful = successful;
        this.comment = comment;
        this.errorCode = errorCode;
    }

    // Getters & Setters
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
