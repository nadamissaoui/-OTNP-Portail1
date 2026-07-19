package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class CheckCommercialEligibilityRequestDto {

    private String coId;
    private String coIdPub;

    public CheckCommercialEligibilityRequestDto() {}

    public CheckCommercialEligibilityRequestDto(String coId, String coIdPub) {
        this.coId = coId;
        this.coIdPub = coIdPub;
    }

    public String getCoId() {
        return coId;
    }

    public void setCoId(String coId) {
        this.coId = coId;
    }

    public String getCoIdPub() {
        return coIdPub;
    }

    public void setCoIdPub(String coIdPub) {
        this.coIdPub = coIdPub;
    }
}
