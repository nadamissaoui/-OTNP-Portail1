package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class DeactivateContractRequestDto {

    private Long coId;
    private String coIdPub;

    public Long getCoId() { return coId; }
    public void setCoId(Long coId) { this.coId = coId; }

    public String getCoIdPub() { return coIdPub; }
    public void setCoIdPub(String coIdPub) { this.coIdPub = coIdPub; }
}
