package tn.esprit.otnp_ws1.service;

import org.kie.server.client.ProcessServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.jws.WebService;
import tn.esprit.otnp_ws1.model.Client;

import java.util.HashMap;
import java.util.Map;

@Service
@WebService(
        endpointInterface = "tn.esprit.otnp_ws1.service.OTNPService",
        serviceName = "OTNPServiceImplService"
)
public class OTNPServiceImpl implements OTNPService {

    private final ProcessServicesClient processServicesClient;
    private final RestTemplate restTemplate;

    @Value("${jbpm.container.id}")
    private String containerIdIn;

    @Value("${jbpm.process.in.id:portabilitefinal.Portabilite_Orchestration}")
    private String processIdIn;

    @Value("${jbpm.container.out.id}")
    private String containerIdOut;

    @Value("${jbpm.process.out.id}")
    private String processIdOut;

    @Value("${monitoring.back.url:http://localhost:8081}")
    private String monitoringBackUrl;

    @Autowired
    public OTNPServiceImpl(ProcessServicesClient processServicesClient, RestTemplate restTemplate) {
        this.processServicesClient = processServicesClient;
        this.restTemplate = restTemplate;
    }

    // ── START IN ─────────────────────────────────────────────
    @Override
    public Long startPortability(String msisdn, String rioCode, Client client) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("phoneNumber", msisdn);
            params.put("msisdn", msisdn);
            params.put("rioCode", rioCode);

            if (client != null) {
                params.put("clientName",   client.getClientName());
                params.put("cinNumber",    client.getCinNumber());
                params.put("contractType", client.getContractType());
                params.put("idClient",     client.getIdClient());
                params.put("typeIdentite", client.getTypeIdentite());
                params.put("refCrm",       client.getRefCrm());
                params.put("marche",       client.getMarche());
                params.put("numeroOrange", client.getNumeroOrange());
            }

            Long processInstanceId = processServicesClient.startProcess(
                    containerIdIn,
                    processIdIn,
                    params
            );

            System.out.println(">>> [DEBUT] Porta IN lancee - conteneur=" + containerIdIn
                    + " | Instance ID : " + processInstanceId);

            // Sauvegarder les infos client dans monitoring-back
            saveToMonitoringBack(processInstanceId, msisdn, rioCode, client, "IN");

            return processInstanceId;

        } catch (Exception e) {
            System.err.println(">>> [ERREUR] StartProcess IN : " + e.getMessage());
            throw new RuntimeException("KIE Server Error : " + e.getMessage());
        }
    }

    // ── START OUT ────────────────────────────────────────────
    //@Override
    public Long startPortabilityOut(String msisdn, String rioCode) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("phoneNumber", msisdn);
            params.put("msisdn",      msisdn);
            params.put("rioCode",     rioCode);
            params.put("rio",         rioCode);

            Long processInstanceId = processServicesClient.startProcess(
                    containerIdOut,   // portaout_1.0.0-SNAPSHOT
                    processIdOut,     // portaout.portaout
                    params
            );

            System.out.println(">>> [DEBUT] Porta OUT lancee - conteneur=" + containerIdOut
                    + " | Instance ID : " + processInstanceId);

            saveToMonitoringBack(processInstanceId, msisdn, rioCode, null, "OUT");

            return processInstanceId;

        } catch (Exception e) {
            System.err.println(">>> [ERREUR] StartProcess OUT : " + e.getMessage());
            throw new RuntimeException("KIE Server Error : " + e.getMessage());
        }
    }

    // ── SEND DECISION ────────────────────────────────────────
    @Override
    public boolean sendDecision(Long instanceId, String status) {
        try {
            System.out.println(">>> [DECISION] Instance : " + instanceId + " | Status recu : " + status);

            switch (status) {
                case "ELIGIBILITY_OK":
                    System.out.println(">>> [STEP 2] Eligibilite OK. Orchestration vers Porta OUT...");

                    Map<String, Object> outParams = new HashMap<>();
                    outParams.put("msisdn", "PORTED_NUMBER_" + instanceId);

                    processServicesClient.startProcess(containerIdOut, processIdOut, outParams);
                    processServicesClient.signalProcessInstance(
                            containerIdIn, instanceId, "Signal_Eligibilite_Validee", status);

                    System.out.println(">>> [OK] Signal envoye et Porta OUT demarree avec succes.");
                    return true;

                case "DONOR_ACCEPTED":
                    System.out.println(">>> [STEP 3] Accord donneur recu. Finalisation de l'instance " + instanceId);
                    processServicesClient.signalProcessInstance(
                            containerIdIn, instanceId, "Signal_Donor_Received", status);
                    return true;

                case "REJECTED":
                    System.out.println(">>> [ECHEC] Portabilite rejetee pour l'instance : " + instanceId);
                    processServicesClient.signalProcessInstance(
                            containerIdIn, instanceId, "Signal_Decision", status);
                    return true;

                default:
                    System.out.println(">>> [WARNING] Status inconnu : " + status);
                    return false;
            }

        } catch (Exception e) {
            System.err.println(">>> [ERREUR CRITIQUE] sendDecision : " + e.getMessage());
            return false;
        }
    }

    private void saveToMonitoringBack(Long processInstanceId, String msisdn, String rioCode,
                                       Client client, String workflowType) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("processInstanceId", processInstanceId);
            body.put("msisdn", msisdn);
            body.put("rioCode", rioCode);
            body.put("workflowType", workflowType);

            if (client != null) {
                body.put("clientName",   client.getClientName());
                body.put("cinNumber",    client.getCinNumber());
                body.put("contractType", client.getContractType());
                body.put("idClient",     client.getIdClient());
                body.put("typeIdentite", client.getTypeIdentite());
                body.put("refCrm",       client.getRefCrm());
                body.put("marche",       client.getMarche());
                body.put("numeroOrange", client.getNumeroOrange());
            }

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    monitoringBackUrl + "/api/portability/save",
                    body,
                    Map.class
            );

            System.out.println(">>> [MONITORING] Infos client sauvegardees pour l'instance "
                    + processInstanceId + " : " + response.getBody());
        } catch (Exception e) {
            System.err.println(">>> [WARNING] Impossible de sauvegarder dans monitoring-back : "
                    + e.getMessage());
        }
    }
}