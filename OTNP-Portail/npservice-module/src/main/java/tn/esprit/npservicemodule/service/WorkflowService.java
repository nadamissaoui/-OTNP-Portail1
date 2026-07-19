package tn.esprit.npservicemodule.service;



import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WorkflowService {

    private final RestTemplate restTemplate = new RestTemplate();

    // L'URL de ton KIE Server (Business Central)
    private static final String KIE_SERVER_URL = "http://localhost:8080/kie-server/services/rest/server";

    // Remplace par l'ID exact de ton projet dans Business Central (Deployment ID)
    private static final String CONTAINER_ID = "portabilite-module_1.0.0";

    public void sendDonorSignal(Long processInstanceId) {
        // Construction de l'URL pour envoyer un signal à une instance précise
        String url = KIE_SERVER_URL + "/containers/" + CONTAINER_ID
                + "/processes/instances/" + processInstanceId + "/signal/Signal_DonorOK";

        // Configuration de l'authentification (wbadmin par défaut)
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("wbadmin", "wbadmin");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // On envoie un corps vide "{}" car le signal a juste besoin d'être "activé"
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Signal 'DonorOK' envoyé au workflow pour l'instance : " + processInstanceId);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur Signal : " + e.getMessage());
        }
    }
}