package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class GetSingleRIOResponseDto {

    private boolean isSuccessful;
    private String comment;
    private String errorCode;

    private String companyName;
    private String rioCode;
    private String firstName;
    private String lastName;
    private String smsSendText;
    private String endOfCommitement;

    public GetSingleRIOResponseDto() {}

    public GetSingleRIOResponseDto(boolean isSuccessful,
                                   String comment,
                                   String errorCode,
                                   String companyName,
                                   String rioCode,
                                   String firstName,
                                   String lastName,
                                   String smsSendText,
                                   String endOfCommitement) {

        this.isSuccessful = isSuccessful;
        this.comment = comment;
        this.errorCode = errorCode;
        this.companyName = companyName;
        this.rioCode = rioCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.smsSendText = smsSendText;
        this.endOfCommitement = endOfCommitement;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }    // Getters & Setters
}