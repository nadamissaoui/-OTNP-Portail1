package tn.esprit.crmmodule.util;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class SoapClient {

    private final RestTemplate restTemplate;

    public SoapClient() {
        this.restTemplate = new RestTemplate();
    }

    public String sendSoapRequest(String url, String soapAction, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", soapAction);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }
}
