package tn.esprit.npservicemodule.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.npservicemodule.dto.NPResponseDto;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class NPServiceClient {

    private static final String SOAP_URL = "http://localhost:8088/mockBasicHttpBinding_NPService";
    private final RestTemplate restTemplate = new RestTemplate();

    public NPResponseDto processMessage(String xmlMessage) {
        try {
            // 1. Construction de la requête SOAP (tns:xmlMessage attendu par ton WSDL)
            String soapRequest =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                            + "xmlns:tns=\"http://npcdb.tn\">"
                            + "<soapenv:Header/>"
                            + "<soapenv:Body>"
                            + "<tns:ProcessMessage>"
                            + "<tns:xmlMessage>" + escapeXml(xmlMessage) + "</tns:xmlMessage>"
                            + "</tns:ProcessMessage>"
                            + "</soapenv:Body>"
                            + "</soapenv:Envelope>";

            // 2. Configuration des Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.add("SOAPAction", "http://npcdb.tn/NPService/ProcessMessage");

            HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

            // 3. Appel au Mock SoapUI
            ResponseEntity<String> response = restTemplate.exchange(
                    SOAP_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String responseBody = response.getBody() != null ? response.getBody() : "";
            System.out.println("DEBUG - SOAP RESPONSE : " + responseBody);

            // 4. Parsing de la réponse XML de SoapUI
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // <--- LIGNE MAGIQUE ACTIVÉE !

            Document doc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));

            // IMPORTANT : On utilise les noms exacts de ton mock
            String statusCodeVal = getTagValueNS(doc, "*", "StatusCode");
            String statusMsg = getTagValueNS(doc, "*", "StatusMessage");

            // 5. Mapping vers le DTO (ce que jBPM va recevoir en JSON)
            NPResponseDto result = new NPResponseDto();

            // On ajoute un .trim() par sécurité pour enlever les espaces invisibles
            if (statusCodeVal != null && "0".equals(statusCodeVal.trim())) {
                result.setStatusCode(0);
                result.setStatusMessage(statusMsg != null ? statusMsg.trim() : "SUCCESS");
            } else {
                result.setStatusCode(-1);
                result.setStatusMessage(statusMsg != null ? statusMsg.trim() : "ECHEC SOAP");
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            NPResponseDto error = new NPResponseDto();
            error.setStatusCode(-1);
            error.setStatusMessage("Erreur Java : " + e.getMessage());
            return error;
        }
    }

    private String getTagValueNS(Document doc, String namespace, String tagName) {
        NodeList nodeList = doc.getElementsByTagNameNS(namespace, tagName);
        if (nodeList.getLength() > 0) return nodeList.item(0).getTextContent();
        return null;
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
