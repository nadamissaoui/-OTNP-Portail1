package tn.esprit.bscsmodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsRequest {

    // On utilise @JsonProperty pour être sûr que Jackson fasse le lien avec le JSON de jBPM
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("message")
    private String message;

    // Constructeur par défaut (Indispensable pour Jackson)
    public SmsRequest() {}

    public SmsRequest(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    // Getters et Setters
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}