package tn.esprit.bscsmodule.dto;

public class DNExportRequestDto {
    private String destPLCodePub;
    private String dnTarget;

    // Getters & Setters
    public String getDestPLCodePub() {
        return destPLCodePub;
    }

    public void setDestPLCodePub(String destPLCodePub) {
        this.destPLCodePub = destPLCodePub;
    }

    public String getDnTarget() {
        return dnTarget;
    }

    public void setDnTarget(String dnTarget) {
        this.dnTarget = dnTarget;
    }
}
