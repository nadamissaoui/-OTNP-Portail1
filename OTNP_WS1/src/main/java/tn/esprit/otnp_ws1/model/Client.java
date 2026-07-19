package tn.esprit.otnp_ws1.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "client", propOrder = {
        "clientName",
        "cinNumber",
        "contractType",
        "idClient",
        "typeIdentite",
        "refCrm",
        "marche",
        "numeroOrange"
})
public class Client {
    private String clientName;
    private String cinNumber;
    private String contractType;

    // Nouveaux attributs correspondants au formulaire
    private String idClient;
    private String typeIdentite;
    private String refCrm;
    private String marche;
    private String numeroOrange;

    // --- GETTERS ET SETTERS ---
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getCinNumber() { return cinNumber; }
    public void setCinNumber(String cinNumber) { this.cinNumber = cinNumber; }

    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }

    public String getIdClient() { return idClient; }
    public void setIdClient(String idClient) { this.idClient = idClient; }

    public String getTypeIdentite() { return typeIdentite; }
    public void setTypeIdentite(String typeIdentite) { this.typeIdentite = typeIdentite; }

    public String getRefCrm() { return refCrm; }
    public void setRefCrm(String refCrm) { this.refCrm = refCrm; }

    public String getMarche() { return marche; }
    public void setMarche(String marche) { this.marche = marche; }

    public String getNumeroOrange() { return numeroOrange; }
    public void setNumeroOrange(String numeroOrange) { this.numeroOrange = numeroOrange; }
}