package com.monitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class KieServerService {

    private static final Logger log = LoggerFactory.getLogger(KieServerService.class);

    @Value("${jbpm.server.url:http://localhost:8080/kie-server/services/rest/server}")
    private String jbpmServerUrl;

    @Value("${jbpm.server.user:wbadmin}")
    private String kieServerUsername;

    @Value("${jbpm.server.password:wbadmin}")
    private String kieServerPassword;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getBaseUrl() {
        String url = jbpmServerUrl;
        if (url.endsWith("/server")) {
            return url;
        }
        if (!url.contains("/services/rest/server")) {
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            return url + "/services/rest/server";
        }
        return url;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = kieServerUsername + ":" + kieServerPassword;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + new String(encodedAuth));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Map<String, Object> checkConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = getBaseUrl();
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            result.put("connected", true);
            result.put("url", jbpmServerUrl);
            result.put("status", response.getStatusCode().value());
        } catch (Exception e) {
            result.put("connected", false);
            result.put("url", jbpmServerUrl);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getContainers() {
        try {
            String url = getBaseUrl() + "/containers";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseContainers(response.getBody());
            }
        } catch (Exception e) {
            log.error("Erreur containers: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Recupere tous les processus en etat ERROR (status=3) via l'API queries
     */
    public List<Map<String, Object>> getExecutionErrors() {
        try {
            String url = getBaseUrl() + "/queries/processes/instances?status=3&page=0&pageSize=50&sortBy=start_date&sortOrder=false";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> instances = parseProcessInstances(response.getBody());
                // Pour chaque instance en erreur, recuperer les details des noeuds
                for (Map<String, Object> instance : instances) {
                    enrichWithNodeDetails(instance);
                }
                return instances;
            }
        } catch (Exception e) {
            log.error("Erreur recuperation processus en erreur: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Recupere les processus en erreur pour un container specifique
     */
    public List<Map<String, Object>> getErrorsByContainer(String containerId) {
        try {
            String url = getBaseUrl() + "/queries/containers/" + containerId + "/process/instances?status=3&page=0&pageSize=50&sortBy=start_date&sortOrder=false";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> instances = parseProcessInstances(response.getBody());
                for (Map<String, Object> instance : instances) {
                    enrichWithNodeDetails(instance);
                }
                return instances;
            }
        } catch (Exception e) {
            log.error("Erreur recuperation erreurs container {}: {}", containerId, e.getMessage());
            // Fallback: filtrer depuis la liste globale
            return filterByContainer(getExecutionErrors(), containerId);
        }
        return Collections.emptyList();
    }

    public List<Map<String, Object>> getProcessInstancesInError(String containerId) {
        return getErrorsByContainer(containerId);
    }

    /**
     * Enrichit une instance avec les details du noeud en erreur
     */
    private void enrichWithNodeDetails(Map<String, Object> instance) {
        try {
            Long processInstanceId = (Long) instance.get("processInstanceId");
            String containerId = (String) instance.get("containerId");
            if (processInstanceId == null || containerId == null) return;

            String url = getBaseUrl() + "/containers/" + containerId
                    + "/processes/instances/" + processInstanceId + "/nodes/instances";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode nodes = root.path("node-instance");
                if (nodes.isArray()) {
                    // Trouver le dernier noeud actif (non complete) = le noeud en erreur
                    String failedNodeName = null;
                    String failedNodeType = null;
                    long latestDate = 0;

                    for (JsonNode node : nodes) {
                        boolean completed = node.path("node-completed").asBoolean(true);
                        if (!completed) {
                            long startDate = node.path("start-date").path("java.util.Date").asLong(0);
                            String nodeName = node.path("node-name").asText(null);
                            if (startDate >= latestDate && nodeName != null) {
                                latestDate = startDate;
                                failedNodeName = nodeName;
                                failedNodeType = node.path("node-type").asText("");
                            }
                        }
                    }

                    if (failedNodeName != null) {
                        instance.put("activityName", failedNodeName);
                        instance.put("activityType", failedNodeType);
                    }

                    // Construire un message d'erreur descriptif
                    String processId = (String) instance.getOrDefault("processId", "");
                    String processName = (String) instance.getOrDefault("processName", "");
                    StringBuilder errorMsg = new StringBuilder();
                    errorMsg.append("Process ").append(processName)
                            .append(" (Instance #").append(processInstanceId).append(")")
                            .append(" en etat ERREUR.");
                    if (failedNodeName != null) {
                        errorMsg.append(" Bloque au noeud: ").append(failedNodeName)
                                .append(" (type: ").append(failedNodeType).append(")");
                    }
                    errorMsg.append(" | Container: ").append(containerId);
                    instance.put("errorMessage", errorMsg.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Impossible de recuperer les details du noeud pour l'instance {}: {}",
                    instance.get("processInstanceId"), e.getMessage());
            // Message d'erreur par defaut
            instance.putIfAbsent("errorMessage",
                    "Process " + instance.getOrDefault("processName", "") +
                            " (Instance #" + instance.getOrDefault("processInstanceId", "") +
                            ") en etat ERREUR dans le container " + instance.getOrDefault("containerId", ""));
        }
    }

    private List<Map<String, Object>> filterByContainer(List<Map<String, Object>> all, String containerId) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : all) {
            if (containerId.equals(item.get("containerId"))) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<Map<String, Object>> parseProcessInstances(String json) {
        List<Map<String, Object>> instances = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("process-instance");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    Map<String, Object> instance = new HashMap<>();
                    long pid = item.path("process-instance-id").asLong(0);
                    instance.put("processInstanceId", pid);
                    instance.put("processId", item.path("process-id").asText(""));
                    instance.put("processName", item.path("process-name").asText(""));
                    instance.put("containerId", item.path("container-id").asText(""));
                    instance.put("state", item.path("process-instance-state").asInt(0));
                    instance.put("errorDate", item.path("start-date").path("java.util.Date").asLong(0));
                    instance.put("errorId", "ERR-" + pid);
                    instance.put("errorType", "Process Error (state=3)");
                    instance.put("acknowledged", false);
                    instances.add(instance);
                }
            }
        } catch (Exception e) {
            log.error("Erreur parsing instances: {}", e.getMessage());
        }
        return instances;
    }

    private List<Map<String, Object>> parseContainers(String json) {
        List<Map<String, Object>> containers = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("result").path("kie-containers").path("kie-container");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    Map<String, Object> container = new HashMap<>();
                    container.put("containerId", item.path("container-id").asText(""));
                    container.put("status", item.path("status").asText(""));
                    container.put("groupId", item.path("release-id").path("group-id").asText(""));
                    container.put("artifactId", item.path("release-id").path("artifact-id").asText(""));
                    container.put("version", item.path("release-id").path("version").asText(""));
                    containers.add(container);
                }
            }
        } catch (Exception e) {
            log.error("Erreur parsing containers: {}", e.getMessage());
        }
        return containers;
    }
}
