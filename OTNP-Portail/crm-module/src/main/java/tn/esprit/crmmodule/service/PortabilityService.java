package tn.esprit.crmmodule.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.crmmodule.dto.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortabilityService {

    private static final String URL = "http://localhost:8088/mockPortabilitySoap";
    private static final String SOAP_URL =
            "http://10.32.229.200:9090/CRM/WebServices/Portability.asmx";



    public CreatePortabilityB2BResponseDto createPortability(CreatePortabilityB2BRequestDto dto) {
        String soapXml = buildSoapRequestB2B(dto);
        String responseXml = sendSoapRequest(soapXml, "http://crm.orange.tn/CreatePortabilityB2B");
        return parseSoapResponseB2B(responseXml);
    }

    private String buildSoapRequestB2B(CreatePortabilityB2BRequestDto dto) {
        return """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:crm="http://crm.orange.tn/">
                <soapenv:Header/>
                <soapenv:Body>
                    <crm:CreatePortabilityB2B>
                        <crm:portaRef>%s</crm:portaRef>
                        <crm:statusCode>%s</crm:statusCode>
                        <crm:statusReason>%s</crm:statusReason>
                        <crm:portaDate>%s</crm:portaDate>
                        <crm:changeDate>%s</crm:changeDate>
                        <crm:custId>%s</crm:custId>
                        <crm:contractCode>%s</crm:contractCode>
                        <crm:numPorter>%s</crm:numPorter>
                        <crm:numOrange>%s</crm:numOrange>
                        <crm:actualOperator>%s</crm:actualOperator>
                        <crm:rio>%s</crm:rio>
                    </crm:CreatePortabilityB2B>
                </soapenv:Body>
            </soapenv:Envelope>
        """.formatted(
                dto.getPortaRef(),
                dto.getStatusCode(),
                dto.getStatusReason(),
                dto.getPortaDate(),
                dto.getChangeDate(),
                dto.getCustId(),
                dto.getContractCode(),
                dto.getNumPorter(),
                dto.getNumOrange(),
                dto.getActualOperator(),
                dto.getRio()
        );
    }

    private CreatePortabilityB2BResponseDto parseSoapResponseB2B(String xml) {
        try {
            Document doc = parseXml(xml);

            String idcrm = getTagValueNS(doc, "http://crm.orange.tn/", "IDCRM");
            String errorOrigin = getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorOrigin");
            String errorDetail = getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorDetail");
            boolean isSuccessful = Boolean.parseBoolean(getTagValueNS(doc, "http://crm.orange.tn/Services", "IsSuccessful"));

            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin(errorOrigin);
            operation.setErrorDetail(errorDetail);
            operation.setIsSuccessful(isSuccessful);

            CreatePortabilityB2BResponseDto response = new CreatePortabilityB2BResponseDto();
            response.setIdcrm(idcrm);
            response.setOperationResponse(operation);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing SOAP response B2B", e);
        }
    }


    // --- MÉTHODE CREATE PORTABILITY GP (MODIFIÉE POUR SIMULER LE SUCCÈS) ---
    public CreatePortabilityGPResponseDto createPortabilityGP(CreatePortabilityGPRequestDto dto) {
        // On construit le XML de requête (optionnel, pour garder la logique)
        String soapXml = buildSoapRequestGP(dto);

        // AU LIEU d'appeler le serveur, on injecte directement le XML de succès que tu as demandé
        String responseXml = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                              xmlns:crm="http://crm.orange.tn/" 
                              xmlns:ser="http://crm.orange.tn/Services">
               <soapenv:Body>
                  <crm:CreatePortabilityGPResponse>
                     <OperationResponse xmlns="http://crm.orange.tn/Services/OperationResponse">
                        <ser:ErrorOrigin>None</ser:ErrorOrigin>
                        <ser:ErrorDetail>Success</ser:ErrorDetail>
                        <ser:IsSuccessful>true</ser:IsSuccessful>
                     </OperationResponse>
                     <crm:IDCRM>CRM-12345</crm:IDCRM>
                  </crm:CreatePortabilityGPResponse>
               </soapenv:Body>
            </soapenv:Envelope>
            """;

        System.out.println("===== SIMULATED SOAP RESPONSE =====");
        System.out.println(responseXml);

        return parseSoapResponseGP(responseXml);
    }

    private String buildSoapRequestGP(CreatePortabilityGPRequestDto dto) {
        return """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:crm="http://crm.orange.tn/">
                <soapenv:Header/>
                <soapenv:Body>
                    <crm:CreatePortabilityGP>
                        <crm:portaRef>%s</crm:portaRef>
                        <crm:statusCode>%s</crm:statusCode>
                        <crm:statusReason>%s</crm:statusReason>
                        <crm:portaDate>%s</crm:portaDate>
                        <crm:changeDate>%s</crm:changeDate>
                        <crm:custId>%s</crm:custId>
                        <crm:contractCode>%s</crm:contractCode>
                        <crm:numPorter>%s</crm:numPorter>
                        <crm:numOrange>%s</crm:numOrange>
                        <crm:actualOperator>%s</crm:actualOperator>
                        <crm:rio>%s</crm:rio>
                    </crm:CreatePortabilityGP>
                </soapenv:Body>
            </soapenv:Envelope>
        """.formatted(
                dto.getPortaRef(),
                dto.getStatusCode(),
                dto.getStatusReason(),
                dto.getPortaDate(),
                dto.getChangeDate(),
                dto.getCustId(),
                dto.getContractCode(),
                dto.getNumPorter(),
                dto.getNumOrange(),
                dto.getActualOperator(),
                dto.getRio()
        );
    }

    private CreatePortabilityGPResponseDto parseSoapResponseGP(String xml) {
        try {
            Document doc = parseXml(xml);

            String idcrm = getTagValueNS(doc, "http://crm.orange.tn/", "IDCRM");
            String errorOrigin = getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorOrigin");
            String errorDetail = getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorDetail");
            boolean isSuccessful = Boolean.parseBoolean(getTagValueNS(doc, "http://crm.orange.tn/Services", "IsSuccessful"));

            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin(errorOrigin);
            operation.setErrorDetail(errorDetail);
            operation.setIsSuccessful(isSuccessful);

            CreatePortabilityGPResponseDto response = new CreatePortabilityGPResponseDto();
            response.setIdcrm(idcrm);
            response.setOperationResponse(operation);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing SOAP response GP", e);
        }
    }


    public PrintPortabilityResponseDto printPortability(PrintPortabilityRequestDto dto) {
        String soapXml = buildSoapPrintRequest(dto);
        String responseXml = sendSoapRequest(soapXml, "http://crm.orange.tn/PrintPortability");
        return parseSoapPrintResponse(responseXml);
    }

    private String buildSoapPrintRequest(PrintPortabilityRequestDto dto) {
        return """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:crm="http://crm.orange.tn/">
                <soapenv:Header/>
                <soapenv:Body>
                    <crm:PrintPortabilityRequest>
                        <crm:portaRef>%s</crm:portaRef>
                        <crm:custId>%s</crm:custId>
                        <crm:contractCode>%s</crm:contractCode>
                        <crm:numPorter>%s</crm:numPorter>
                    </crm:PrintPortabilityRequest>
                </soapenv:Body>
            </soapenv:Envelope>
        """.formatted(
                dto.getPortaRef(),
                dto.getCustId(),
                dto.getContractCode(),
                dto.getNumPorter()
        );
    }

    private PrintPortabilityResponseDto parseSoapPrintResponse(String xml) {
        try {
            Document doc = parseXml(xml);

            PrintPortabilityResponseDto response = new PrintPortabilityResponseDto();
            response.setReportData(getTagValueNS(doc, "http://crm.orange.tn/", "reportData"));

            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin(getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorOrigin"));
            operation.setErrorDetail(getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorDetail"));
            operation.setIsSuccessful(Boolean.parseBoolean(getTagValueNS(doc, "http://crm.orange.tn/Services", "IsSuccessful")));

            response.setOperationResponse(operation);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SOAP response PrintPortability", e);
        }
    }



    public SearchPortabilityGPResponseDto searchPortabilityGP(SearchPortabilityGPRequestDto dto) {
        String soapXml = buildSoapSearchRequest(dto);
        String responseXml = sendSoapRequest(soapXml, "http://crm.orange.tn/SearchPortabilityGP");
        return parseSoapSearchResponse(responseXml);
    }

    private String buildSoapSearchRequest(SearchPortabilityGPRequestDto dto) {
        return """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:crm="http://crm.orange.tn/">
            <soapenv:Header/>
            <soapenv:Body>
                <crm:SearchPortabilityGPRequest>
                    <crm:portaRef>%s</crm:portaRef>
                    <crm:custId>%s</crm:custId>
                    <crm:contractCode>%s</crm:contractCode>
                    <crm:numPorter>%s</crm:numPorter>
                </crm:SearchPortabilityGPRequest>
            </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(
                dto.getPortaRef(),
                dto.getCustId(),
                dto.getContractCode(),
                dto.getNumPorter()
        );
    }

    private SearchPortabilityGPResponseDto parseSoapSearchResponse(String xml) {
        try {
            Document doc = parseXml(xml);

            SearchPortabilityGPResponseDto response = new SearchPortabilityGPResponseDto();
            List<PortabilityInfoDto> infos = new ArrayList<>();

            NodeList nodeList = doc.getElementsByTagNameNS("http://crm.orange.tn/", "PortabilityInfo");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                PortabilityInfoDto info = new PortabilityInfoDto();
                info.setPortaRef(getTagValueNS(element, "http://crm.orange.tn/Services", "portaRef"));
                info.setIdcrm(getTagValueNS(element, "http://crm.orange.tn/Services", "IDCRM"));
                info.setCrmReference(getTagValueNS(element, "http://crm.orange.tn/Services", "crmReference"));
                info.setContractCode(getTagValueNS(element, "http://crm.orange.tn/Services", "contractCode"));
                info.setNumeroOrange(getTagValueNS(element, "http://crm.orange.tn/Services", "numeroOrange"));
                info.setMsisdn(getTagValueNS(element, "http://crm.orange.tn/Services", "msisdn"));
                info.setRio(getTagValueNS(element, "http://crm.orange.tn/Services", "rio"));
                info.setPortaDate(getTagValueNS(element, "http://crm.orange.tn/Services", "portaDate"));
                info.setChangeDate(getTagValueNS(element, "http://crm.orange.tn/Services", "changeDate"));
                info.setPortaOperator(getTagValueNS(element, "http://crm.orange.tn/Services", "portaOperator"));
                info.setStatusCode(getTagValueNS(element, "http://crm.orange.tn/Services", "statusCode"));
                info.setStatusReason(getTagValueNS(element, "http://crm.orange.tn/Services", "statusReason"));
                info.setCanal(getTagValueNS(element, "http://crm.orange.tn/Services", "canal"));
                info.setIsCanceled(getTagValueNS(element, "http://crm.orange.tn/Services", "isCanceled"));
                info.setCin(getTagValueNS(element, "http://crm.orange.tn/Services", "CIN"));
                infos.add(info);
            }

            response.setPortabilityInfos(infos);

            Element opElement = (Element) doc.getElementsByTagNameNS("http://crm.orange.tn/Services/OperationResponse", "OperationResponse").item(0);
            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin(getTagValueNS(opElement, "http://crm.orange.tn/Services", "ErrorOrigin"));
            operation.setErrorDetail(getTagValueNS(opElement, "http://crm.orange.tn/Services", "ErrorDetail"));
            operation.setIsSuccessful(Boolean.parseBoolean(getTagValueNS(opElement, "http://crm.orange.tn/Services", "IsSuccessful")));
            response.setOperationResponse(operation);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SOAP response SearchPortabilityGP", e);
        }
    }


    private String sendSoapRequest(String xml, String soapAction) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", soapAction);

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);

        System.out.println("===== SOAP RESPONSE =====");
        System.out.println(response.getBody());

        return response.getBody();
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String getTagValueNS(Document doc, String namespace, String tagName) {
        NodeList nodeList = doc.getElementsByTagNameNS(namespace, tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String getTagValueNS(Element element, String namespace, String tagName) {
        NodeList nodeList = element.getElementsByTagNameNS(namespace, tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }


    public UpdatePortabilityStatusResponseDto updatePortabilityStatus(UpdatePortabilityStatusRequestDto dto) {
        String soapXml = buildSoapUpdateStatusRequest(dto);
        String responseXml = sendSoapRequest(soapXml, "http://crm.orange.tn/UpdatePortabilityStatus");
        return parseSoapUpdateStatusResponse(responseXml);
    }

    private String buildSoapUpdateStatusRequest(UpdatePortabilityStatusRequestDto dto) {
        return """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:crm="http://crm.orange.tn/">
            <soapenv:Header/>
            <soapenv:Body>
                <crm:UpdatePortabilityStatus>
                    <crm:IDCRM>%s</crm:IDCRM>
                    <crm:portaRef>%s</crm:portaRef>
                    <crm:statusCode>%s</crm:statusCode>
                    <crm:statusReason>%s</crm:statusReason>
                    <crm:PortageDate>%s</crm:PortageDate>
                </crm:UpdatePortabilityStatus>
            </soapenv:Body>
        </soapenv:Envelope>
    """.formatted(
                dto.getIdcrm() != null ? dto.getIdcrm() : "",
                dto.getPortaRef(),
                dto.getStatusCode(),
                dto.getStatusReason(),
                dto.getPortageDate() != null ? dto.getPortageDate().toString() : ""
        );
    }

    private UpdatePortabilityStatusResponseDto parseSoapUpdateStatusResponse(String xml) {
        try {
            Document doc = parseXml(xml);

            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin(getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorOrigin"));
            operation.setErrorDetail(getTagValueNS(doc, "http://crm.orange.tn/Services", "ErrorDetail"));
            operation.setIsSuccessful(Boolean.parseBoolean(getTagValueNS(doc, "http://crm.orange.tn/Services", "IsSuccessful")));

            UpdatePortabilityStatusResponseDto response = new UpdatePortabilityStatusResponseDto();
            response.setOperationResponse(operation);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SOAP response UpdatePortabilityStatus", e);
        }
    }




    private final RestTemplate restTemplate = new RestTemplate();

    public CancelPortabilityResponseDto cancelPortability(CancelPortabilityRequestDto request) {

        try {

            // 1️⃣ Build SOAP Envelope
            String soapRequest =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                            "<soap:Body>" +
                            "<CancelPortabilityRequest xmlns=\"http://crm.orange.tn/\">" +
                            "<portaRef>" + request.getPortaRef() + "</portaRef>" +
                            "</CancelPortabilityRequest>" +
                            "</soap:Body>" +
                            "</soap:Envelope>";


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.add("SOAPAction", "http://crm.orange.tn/CancelPortabilityRequest");

            HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);

            // 3️⃣ Call SOAP
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            SOAP_URL,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            String responseBody = response.getBody();
            if (responseBody == null) responseBody = "";

            // 4️⃣ Parse XML Response
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(responseBody.getBytes()));

            String errorOrigin = getTagValueNS(doc, "ErrorOrigin");
            String errorDetail = getTagValueNS(doc, "ErrorDetail");
            String isSuccessful = getTagValueNS(doc, "IsSuccessful");

            // 5️⃣ Build DTO Response
            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin(errorOrigin != null ? errorOrigin : "Unspecified");
            operation.setErrorDetail(errorDetail);
            operation.setIsSuccessful(isSuccessful != null && Boolean.parseBoolean(isSuccessful));

            CancelPortabilityResponseDto result = new CancelPortabilityResponseDto();
            result.setOperationResponse(operation);

            return result;

        } catch (Exception e) {

            // En cas d'erreur, renvoyer un objet avec isSuccessful=false
            OperationResponseDto operation = new OperationResponseDto();
            operation.setErrorOrigin("Unspecified");
            operation.setErrorDetail(e.getMessage());
            operation.setIsSuccessful(false);

            CancelPortabilityResponseDto result = new CancelPortabilityResponseDto();
            result.setOperationResponse(operation);

            return result;
        }
    }

    // 🔹 Util Method pour parser XML avec namespace
    private String getTagValueNS(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagNameNS("*", tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }


}