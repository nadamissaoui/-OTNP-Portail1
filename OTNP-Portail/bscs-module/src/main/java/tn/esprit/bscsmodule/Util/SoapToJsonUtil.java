package tn.esprit.bscsmodule.Util;

import org.json.JSONObject;
import org.json.XML;

import java.util.function.Function; // ⚠️ IMPORTANT

public class SoapToJsonUtil {

    public static JSONObject extractBody(String soapXml, String returnTag) {

        if (soapXml == null || soapXml.trim().isEmpty()) {
            throw new RuntimeException("Empty SOAP response");
        }

        System.out.println("\n\n=== ATTENTION BINGO : VOICI CE QUE SOAPUI A RENVOYE ===");
        System.out.println(soapXml);
        System.out.println("========================================================\n\n");


        System.out.println("\n\n=== REPONSE DIRECTE DE SOAPUI ===");
        System.out.println(soapXml);
        System.out.println("=================================\n\n");

        String cleanXml = soapXml
                .replaceAll("(<\\/?)\\w+:", "$1")
                .replaceAll("xmlns(:\\w+)?=\"[^\"]*\"", "");

        JSONObject json = XML.toJSONObject(cleanXml);

        JSONObject body = json
                .getJSONObject("Envelope")
                .getJSONObject("Body");

        if (!body.has(returnTag)) {
            throw new RuntimeException("Element " + returnTag + " missing in SOAP response");
        }

        return body.getJSONObject(returnTag);
    }


    public static <T> T mapSoapResponse(
            String soapXml,
            String returnTag,
            Function<JSONObject, T> mapper) {

        JSONObject obj = extractBody(soapXml, returnTag);
        return mapper.apply(obj);
    }
}