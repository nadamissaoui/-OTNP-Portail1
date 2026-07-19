package tn.esprit.bscsmodule.service;
import tn.esprit.bscsmodule.dto.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.bind.annotation.*;
import tn.esprit.bscsmodule.dto.DeactivateContractRequestDto;
@Service
public class BscsSoapClient {

    private final String SOAP_URL = "http://localhost:8088/mockNumberPortabilityHandlingSoapBinding";

    // Méthode existante pour importResource
    public String callSoap(ImportRequest req) {
        String soapRequest = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:alu="http://alu.services.ws.lhs.com">
               <soapenv:Header/>
               <soapenv:Body>
                  <alu:importExternalResourceRequest>
                     <contractReference>
                        <coId>%s</coId>
                     </contractReference>
                     <dnTarget>%s</dnTarget>
                     <operatorCode>%s</operatorCode>
                  </alu:importExternalResourceRequest>
               </soapenv:Body>
            </soapenv:Envelope>
        """.formatted(
                req.getContractId(),
                req.getMsisdn(),
                req.getOperatorCode()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(SOAP_URL, entity, String.class);
        return response.getBody();
    }

    // Nouvelle méthode pour changeDN
    public String callChangeDN(ChangeDNRequestDto req) {
        String soapRequest = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:alu="http://alu.services.ws.lhs.com">
               <soapenv:Header/>
               <soapenv:Body>
                  <alu:changeDNRequest>
                     <DNTarget>%s</DNTarget>
                     <contractReference>
                         <coId>%s</coId>
                         <coIdPub>%s</coIdPub>
                         <dirNum>%s</dirNum>
                     </contractReference>
                     <retention>%s</retention>
                     <service>%s</service>
                  </alu:changeDNRequest>
               </soapenv:Body>
            </soapenv:Envelope>
        """.formatted(
                req.getDnTarget(),
                req.getContractId(),
                req.getCoIdPub(),
                req.getDirNum(),
                req.getRetention(),
                req.getService()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(SOAP_URL, entity, String.class);
        return response.getBody();
    }

    public String callCheckCommercialEligibility(CheckCommercialEligibilityRequestDto req) {

        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:alu="http://alu.services.ws.lhs.com">
           <soapenv:Header/>
           <soapenv:Body>
              <alu:checkCommercialEligibilityRequest>
                 <contractReference>
                    <coId>%s</coId>
                    <coIdPub>%s</coIdPub>
                 </contractReference>
              </alu:checkCommercialEligibilityRequest>
           </soapenv:Body>
        </soapenv:Envelope>
    """.formatted(
                req.getCoId(),
                req.getCoIdPub()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(SOAP_URL, entity, String.class);

        return response.getBody();
    }
    public String callCheckDataRIO(CheckDataRIORequestDto req) {
        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:alu="http://alu.services.ws.lhs.com">
           <soapenv:Header/>
           <soapenv:Body>
              <alu:checkDataRIORequest>
                 <alu:dnNum>%s</alu:dnNum>
                 <alu:operatorCode>%s</alu:operatorCode>
                 <alu:rioCode>%s</alu:rioCode>
                 <alu:sessionChange/>
              </alu:checkDataRIORequest>
           </soapenv:Body>
        </soapenv:Envelope>
    """.formatted(
                req.getDnNum(),
                req.getOperatorCode(),
                req.getRioCode()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(SOAP_URL, entity, String.class);

        return response.getBody();
    }
    public String callDeactivateContract(DeactivateContractRequestDto req) {
        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:alu="http://alu.services.ws.lhs.com">
           <soapenv:Header/>
           <soapenv:Body>
              <alu:deactivateContractRequest>
                 <contractReference>
                    <coId>%s</coId>
                    <coIdPub>%s</coIdPub>
                 </contractReference>
                 <sessionChange/>
              </alu:deactivateContractRequest>
           </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(req.getCoId(), req.getCoIdPub());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(SOAP_URL, entity, String.class);

        return response.getBody();
    }
    public String callDNExport(DNExportRequestDto req) {

        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:alu="http://alu.services.ws.lhs.com">
           <soapenv:Header/>
           <soapenv:Body>
              <alu:dNExportRequest>
                 <alu:destPLCodePub>%s</alu:destPLCodePub>
                 <alu:dnTarget>%s</alu:dnTarget>
                 <alu:sessionChange/>
              </alu:dNExportRequest>
           </soapenv:Body>
        </soapenv:Envelope>
    """.formatted(
                req.getDestPLCodePub(),
                req.getDnTarget()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(SOAP_URL, entity, String.class);

        return response.getBody();
    }
    // Dans BscsSoapClient.java
    public String callGetMsisdnStatusInfo(GetMsisdnStatusInfoRequestDto req) {
        String soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:alu="http://alu.services.ws.lhs.com">
           <soapenv:Header/>
           <soapenv:Body>
              <alu:getMsisdnStatusInfoRequest>
                 <alu:msisdn>%s</alu:msisdn>
              </alu:getMsisdnStatusInfoRequest>
           </soapenv:Body>
        </soapenv:Envelope>
    """.formatted(req.getMsisdn());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(SOAP_URL, entity, String.class);
        return response.getBody();
    }

    public String callGetSingleRIO(GetSingleRIORequestDto req) {

        String soapRequest = """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                      xmlns:alu="http://alu.services.ws.lhs.com">
       <soapenv:Header/>
       <soapenv:Body>
          <alu:getSingleRIORequest>
             <alu:dnNum>%s</alu:dnNum>
             <alu:operatorCode>%s</alu:operatorCode>
             <alu:sessionChange/>
          </alu:getSingleRIORequest>
       </soapenv:Body>
    </soapenv:Envelope>
    """.formatted(
                req.getDnNum(),
                req.getOperatorCode()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(SOAP_URL, entity, String.class);

        return response.getBody();
    }





}
