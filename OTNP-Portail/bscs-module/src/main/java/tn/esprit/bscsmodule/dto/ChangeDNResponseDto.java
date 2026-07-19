package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class ChangeDNResponseDto {
    private boolean successful;
    private String comment;
    private String dnId;

    public ChangeDNResponseDto() {}

    public ChangeDNResponseDto(boolean successful, String comment, String dnId) {
        this.successful = successful;
        this.comment = comment;
        this.dnId = dnId;
    }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getDnId() { return dnId; }
    public void setDnId(String dnId) { this.dnId = dnId; }
}
