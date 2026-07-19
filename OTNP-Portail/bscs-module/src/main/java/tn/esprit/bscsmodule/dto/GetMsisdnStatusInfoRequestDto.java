package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class GetMsisdnStatusInfoRequestDto {
    private String msisdn;

    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }
}
