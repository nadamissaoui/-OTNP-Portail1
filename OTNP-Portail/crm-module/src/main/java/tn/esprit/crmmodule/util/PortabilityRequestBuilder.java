package tn.esprit.crmmodule.util;

import tn.esprit.crmmodule.dto.*;

public class PortabilityRequestBuilder {

    private static final String SOAP_ENVELOPE_START = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:crm=\"http://crm.orange.tn/\">";
    private static final String SOAP_ENVELOPE_END = "</soapenv:Envelope>";

    // 1️ CreatePortabilityB2B
    public String buildCreatePortabilityB2B(CreatePortabilityB2BRequestDto dto) {
        return SOAP_ENVELOPE_START +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<crm:CreatePortabilityB2B>" +
                "<crm:portaRef>%s</crm:portaRef>" +
                "<crm:statusCode>%s</crm:statusCode>" +
                "<crm:statusReason>%s</crm:statusReason>" +
                "<crm:portaDate>%s</crm:portaDate>" +
                "<crm:changeDate>%s</crm:changeDate>" +
                "<crm:custId>%s</crm:custId>" +
                "<crm:contractCode>%s</crm:contractCode>" +
                "<crm:numPorter>%s</crm:numPorter>" +
                "<crm:numOrange>%s</crm:numOrange>" +
                "<crm:actualOperator>%s</crm:actualOperator>" +
                "<crm:rio>%s</crm:rio>" +
                "</crm:CreatePortabilityB2B>" +
                "</soapenv:Body>" +
                SOAP_ENVELOPE_END
                        .formatted(
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

    // 2️⃣ CreatePortabilityGP
    public String buildCreatePortabilityGP(CreatePortabilityGPRequestDto dto) {
        return SOAP_ENVELOPE_START +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<crm:CreatePortabilityGP>" +
                "<crm:portaRef>%s</crm:portaRef>" +
                "<crm:statusCode>%s</crm:statusCode>" +
                "<crm:statusReason>%s</crm:statusReason>" +
                "<crm:portaDate>%s</crm:portaDate>" +
                "<crm:changeDate>%s</crm:changeDate>" +
                "<crm:custId>%s</crm:custId>" +
                "<crm:contractCode>%s</crm:contractCode>" +
                "<crm:numPorter>%s</crm:numPorter>" +
                "<crm:numOrange>%s</crm:numOrange>" +
                "<crm:actualOperator>%s</crm:actualOperator>" +
                "<crm:rio>%s</crm:rio>" +
                "</crm:CreatePortabilityGP>" +
                "</soapenv:Body>" +
                SOAP_ENVELOPE_END
                        .formatted(
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

    // 3️⃣ PrintPortability
    public String buildPrintPortability(PrintPortabilityRequestDto dto) {
        return SOAP_ENVELOPE_START +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<crm:PrintPortabilityRequest>" +
                "<crm:portaRef>%s</crm:portaRef>" +
                "<crm:custId>%s</crm:custId>" +
                "<crm:contractCode>%s</crm:contractCode>" +
                "<crm:numPorter>%s</crm:numPorter>" +
                "</crm:PrintPortabilityRequest>" +
                "</soapenv:Body>" +
                SOAP_ENVELOPE_END
                        .formatted(dto.getPortaRef(), dto.getCustId(), dto.getContractCode(), dto.getNumPorter());
    }

    // 4️⃣ CancelPortability
    public String buildCancelPortability(CancelPortabilityRequestDto dto) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<CancelPortabilityRequest xmlns=\"http://crm.orange.tn/\">" +
                "<portaRef>%s</portaRef>" +
                "</CancelPortabilityRequest>" +
                "</soap:Body>" +
                "</soap:Envelope>"
                        .formatted(dto.getPortaRef());
    }

    // 5️⃣ SearchPortabilityGP
    public String buildSearchPortabilityGP(SearchPortabilityGPRequestDto dto) {
        return SOAP_ENVELOPE_START +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<crm:SearchPortabilityGPRequest>" +
                "<crm:portaRef>%s</crm:portaRef>" +
                "<crm:custId>%s</crm:custId>" +
                "<crm:contractCode>%s</crm:contractCode>" +
                "<crm:numPorter>%s</crm:numPorter>" +
                "</crm:SearchPortabilityGPRequest>" +
                "</soapenv:Body>" +
                SOAP_ENVELOPE_END
                        .formatted(dto.getPortaRef(), dto.getCustId(), dto.getContractCode(), dto.getNumPorter());
    }

    // 6️⃣ UpdatePortabilityStatus
    public String buildUpdatePortabilityStatus(UpdatePortabilityStatusRequestDto dto) {
        return SOAP_ENVELOPE_START +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<crm:UpdatePortabilityStatus>" +
                "<crm:IDCRM>%s</crm:IDCRM>" +
                "<crm:portaRef>%s</crm:portaRef>" +
                "<crm:statusCode>%s</crm:statusCode>" +
                "<crm:statusReason>%s</crm:statusReason>" +
                "<crm:PortageDate>%s</crm:PortageDate>" +
                "</crm:UpdatePortabilityStatus>" +
                "</soapenv:Body>" +
                SOAP_ENVELOPE_END
                        .formatted(
                                dto.getIdcrm() != null ? dto.getIdcrm() : "",
                                dto.getPortaRef(),
                                dto.getStatusCode(),
                                dto.getStatusReason(),
                                dto.getPortageDate() != null ? dto.getPortageDate().toString() : ""
                        );
    }
}
