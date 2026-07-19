package tn.esprit.smsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SmsRequest {
    @NotNull(message = "Le numéro est obligatoire")
    @Pattern(regexp = "^5[0-9]{7}$", message = "Le numéro doit être un numéro Orange valide (8 chiffres commençant par 5)")    private String phoneNumber;

    @NotBlank(message = "Le message ne peut pas être vide")
    private String message;

    // Constructeur vide (obligatoire pour Jackson/JSON)
    public SmsRequest() {}

    // GETTERS
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    // SETTERS
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}