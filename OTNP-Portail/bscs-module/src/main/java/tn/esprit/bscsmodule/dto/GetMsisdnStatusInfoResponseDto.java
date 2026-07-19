// GetMsisdnStatusInfoResponseDto.java
package tn.esprit.bscsmodule.dto;

import lombok.Data;

@Data
public class GetMsisdnStatusInfoResponseDto {

    private boolean successful;
    private String comment;
    private String coCode;
    private long contractStatus;
    private String custNum;
    private String custcode;
    private String documentNumber;
    private String documentType;
    private String firstName;
    private String lastName;
    private String ligneProduitDes;
    private String ligneProduitRef;
    private long market;
    private String marketDes;
    private String msisdn;
    private long prgCode;
    private String socialReason;
    private long subMarket;
    private String subMarketDes;
    private long tmCode;
    private String tmDes;

    // Constructeur complet
    public GetMsisdnStatusInfoResponseDto(boolean successful, String comment, String coCode, long contractStatus,
                                          String custNum, String custcode, String documentNumber, String documentType,
                                          String firstName, String lastName, String ligneProduitDes, String ligneProduitRef,
                                          long market, String marketDes, String msisdn, long prgCode, String socialReason,
                                          long subMarket, String subMarketDes, long tmCode, String tmDes) {
        this.successful = successful;
        this.comment = comment;
        this.coCode = coCode;
        this.contractStatus = contractStatus;
        this.custNum = custNum;
        this.custcode = custcode;
        this.documentNumber = documentNumber;
        this.documentType = documentType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ligneProduitDes = ligneProduitDes;
        this.ligneProduitRef = ligneProduitRef;
        this.market = market;
        this.marketDes = marketDes;
        this.msisdn = msisdn;
        this.prgCode = prgCode;
        this.socialReason = socialReason;
        this.subMarket = subMarket;
        this.subMarketDes = subMarketDes;
        this.tmCode = tmCode;
        this.tmDes = tmDes;
    }

    // Getters et setters pour tous les champs (omitted ici pour la lisibilité)
}
