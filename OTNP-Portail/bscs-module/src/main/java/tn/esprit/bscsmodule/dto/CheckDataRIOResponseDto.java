package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class CheckDataRIOResponseDto {

    private boolean successful;
    private String comment;
    private String errorCode;

    public CheckDataRIOResponseDto() {}

    public CheckDataRIOResponseDto(boolean successful, String comment, String errorCode) {
        this.successful = successful;
        this.comment = comment;
        this.errorCode = errorCode;
    }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
