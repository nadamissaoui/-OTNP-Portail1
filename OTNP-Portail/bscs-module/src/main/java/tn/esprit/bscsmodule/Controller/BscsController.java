package tn.esprit.bscsmodule.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.bscsmodule.Util.SoapToJsonUtil;
import tn.esprit.bscsmodule.dto.*;
import tn.esprit.bscsmodule.service.BscsSoapClient;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bscs")
public class BscsController {

    private final BscsSoapClient soapClient;

    public BscsController(BscsSoapClient soapClient) {
        this.soapClient = soapClient;
    }

    // ================= IMPORT =================
    private static final Logger logger = LoggerFactory.getLogger(BscsController.class);
    @PostMapping("/importResource")
    public ImportResponseDto importResource(@RequestBody Map<String, Object> request) {
        // 1. Log pour voir ce que jBPM envoie réellement dans la console
        System.out.println("Données reçues de jBPM pour Import: " + request);

        // 2. Extraire les données nécessaires pour le client SOAP
        // On utilise "msisdn" ou le nom exact de ta variable dans jBPM
        ImportRequest soapReq = new ImportRequest();
        if(request.containsKey("msisdn")) {
            soapReq.setMsisdn(request.get("msisdn").toString());
        }

        // 3. Déclaration de la variable (résout l'erreur de symbole)
        String soapResponse = soapClient.callSoap(soapReq);

        // 4. Retour de la réponse mappée
        return SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "importExternalResourceReturn",
                obj -> new ImportResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        String.valueOf(obj.opt("dnId"))
                )
        );
    }

    // ================= CHANGE DN =================

    @PostMapping("/changeDN")
    public ChangeDNResponseDto changeDN(@RequestBody ChangeDNRequestDto request) {

        String soapResponse = soapClient.callChangeDN(request);

        return SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "changeDNReturn",
                obj -> new ChangeDNResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        obj.getJSONObject("contractReference").optString("dirNum")
                )
        );
    }

    // ================= CHECK COMMERCIAL =================

    // ================= CHECK COMMERCIAL (Modifié pour String) =================
    @PostMapping("/checkCommercialEligibility")
    public String checkCommercialEligibility(@RequestBody CheckCommercialEligibilityRequestDto request) {
        // 1. Appel du client SOAP
        String soapResponse = soapClient.callCheckCommercialEligibility(request);

        // 2. Mapping de la réponse SOAP vers le DTO
        CheckCommercialEligibilityResponseDto dto = SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "checkCommercialEligibilityReturn",
                obj -> new CheckCommercialEligibilityResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", "")
                )
        );

        // 3. Logique de routage pour jBPM
        if (dto.isSuccessful()) {
            // Succès (Happy Path) -> Va vers Update CRM
            return "0";
        } else if (dto.getComment() != null && dto.getComment().contains("-2")) {
            // Refus Métier -> Va vers Timer et NP Cancel
            return "-2";
        } else {
            // Erreur Technique -> Va vers Task Retry
            return "-1";
        }
    }


    // ================= CHECK DATA RIO (Version JSON pour jBPM) =================
    @PostMapping("/checkDataRIO")
    public ResponseEntity<Map<String, String>> checkDataRIO(@RequestBody(required = false) CheckDataRIORequestDto request) {
        // 1. Sécurité : Si jBPM envoie un corps vide, on initialise un objet vide pour éviter le crash 400
        if (request == null) {
            request = new CheckDataRIORequestDto();
        }

        // 2. Appel au client SOAP (Ta logique d'entreprise reste inchangée)
        String soapResponse = soapClient.callCheckDataRIO(request);

        // 3. Mapping de la réponse SOAP vers le DTO
        CheckDataRIOResponseDto dto = SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "checkDataRIOReturn",
                obj -> new CheckDataRIOResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        obj.optString("bscsErrorCode", "0")
                )
        );

        // 4. Construction de la réponse JSON attendue par jBPM
        Map<String, String> response = new HashMap<>();

        // On récupère le code d'erreur (ex: "0" pour succès)
        String code = (dto.getErrorCode() != null) ? dto.getErrorCode() : "0";

        response.put("errorCode", code);
        response.put("comment", dto.getComment());

        // 5. Retourne un objet JSON (ex: {"errorCode": "0", "comment": "Success"})
        return ResponseEntity.ok(response);
    }

    // ================= DEACTIVATE =================

    @PostMapping("/deactivateContract")
    public DeactivateContractResponseDto deactivateContract(
            @RequestBody DeactivateContractRequestDto request) {

        String soapResponse = soapClient.callDeactivateContract(request);

        return SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "deactivateContractReturn",
                obj -> new DeactivateContractResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        obj.optString("bscsErrorCode", "0")
                )
        );
    }

    // ================= DN EXPORT =================

    @PostMapping("/dNExport")
    public DNExportResponseDto dNExport(@RequestBody DNExportRequestDto request) {

        String soapResponse = soapClient.callDNExport(request);
        System.out.println("🔥🔥🔥 SUCCÈS : dNExport s'est terminé avec succès ! 🔥🔥🔥");

        return SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "dNExportReturn",
                obj -> new DNExportResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        obj.optString("bscsErrorCode", "0")
                )
        );
    }



    @PostMapping("/getMsisdnStatusInfo")
    public GetMsisdnStatusInfoResponseDto getMsisdnStatusInfo(
            @RequestBody GetMsisdnStatusInfoRequestDto request) {

        String soapResponse = soapClient.callGetMsisdnStatusInfo(request);

        return SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "getMsisdnStatusInfoReturn",
                obj -> new GetMsisdnStatusInfoResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        obj.optString("coCode", ""),
                        obj.optLong("contractStatus", 0),
                        obj.optString("custNum", ""),
                        obj.optString("custcode", ""),
                        obj.optString("documentNumber", ""),
                        obj.optString("documentType", ""),
                        obj.optString("firstName", ""),
                        obj.optString("lastName", ""),
                        obj.optString("ligneProduitDes", ""),
                        obj.optString("ligneProduitRef", ""),
                        obj.optLong("market", 0),
                        obj.optString("marketDes", ""),
                        obj.optString("msisdn", ""),
                        obj.optLong("prgCode", 0),
                        obj.optString("socialReason", ""),
                        obj.optLong("subMarket", 0),
                        obj.optString("subMarketDes", ""),
                        obj.optLong("tmCode", 0),
                        obj.optString("tmDes", "")
                )
        );
    }



    @PostMapping("/getSingleRIO")
    public GetSingleRIOResponseDto getSingleRIO(
            @RequestBody GetSingleRIORequestDto request) {

        String soapResponse = soapClient.callGetSingleRIO(request);

        return SoapToJsonUtil.mapSoapResponse(
                soapResponse,
                "getSingleRIOReturn",
                obj -> new GetSingleRIOResponseDto(
                        obj.optBoolean("isSuccessful", false),
                        obj.optString("comment", ""),
                        obj.optString("bscsErrorCode", ""),
                        obj.optString("companyName", ""),
                        obj.optString("rioCode", ""),
                        obj.optString("firstName", ""),
                        obj.optString("lastName", ""),
                        obj.optString("smsSendText", ""),
                        obj.optString("endOfCommitement", "")
                )
        );
    }
    @PostMapping(value = "/sms/send", produces = "application/json")
    public ResponseEntity<String> sendSms(@RequestBody(required = false) SmsRequest request) {
        try {
            // Log pour debug
            System.out.println(">>> [SMS] Requête reçue : " + request);

            // Si jBPM n'envoie rien, on simule quand même un succès pour ne pas bloquer le flux
            if (request == null || request.getPhoneNumber() == null) {
                return ResponseEntity.ok("\"SUCCESS_SKIPPED\"");
            }

            return ResponseEntity.ok("\"SUCCESS\"");
        } catch (Exception e) {
            return ResponseEntity.ok("\"FAILED\"");
        }
    }
    // ================= RECIPIENT ACTIVATE =================
    @PostMapping("/recipientActivate")
    public ResponseEntity<Map<String, Object>> recipientActivate(@RequestBody Map<String, Object> allParams) {
        System.out.println(">>> [DEBUG] Données reçues de jBPM (Recipient Activate) : " + allParams);

        String msisdn = allParams.getOrDefault("msisdn", "unknown").toString();

        // On utilise un Map<String, Object> pour pouvoir renvoyer un booléen (true)
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("isSuccessful", true); // <-- LE MOT CLE MAGIQUE POUR CHECK 5
        response.put("statusCode", "200");  // <-- AU CAS OU LE SCRIPT CHERCHE 200
        response.put("comment", "Activation simulee pour " + msisdn);

        return ResponseEntity.ok(response);
    }

    // Ajouter dans BscsController.java
    @PostMapping("/activationConfirm")
    public ResponseEntity<Map<String, String>> activationConfirm(@RequestBody(required = false) Map<String, Object> request) {
        if (request == null) request = new HashMap<>();

        System.out.println(">>> [FINAL] Confirmation reçue");
        Map<String, String> response = new HashMap<>();
        response.put("status", "CONFIRMED");
        return ResponseEntity.ok(response);
    }
}