package com.monitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoring.dto.ErrorSuggestionDto;
import com.monitoring.dto.LogAnalysisResponse;
import com.monitoring.dto.SimilarErrorDto;
import com.monitoring.entity.ErrorSolution;
import com.monitoring.entity.LogEntry;
import com.monitoring.repository.ErrorSolutionRepository;
import com.monitoring.repository.LogEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiAnalysisService {

    private final LogEntryRepository logEntryRepository;
    private final ErrorSolutionRepository errorSolutionRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.python.service-url:http://localhost:5000}")
    private String pythonServiceUrl;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    public AiAnalysisService(LogEntryRepository logEntryRepository,
                             ErrorSolutionRepository errorSolutionRepository,
                             WebClient.Builder webClientBuilder,
                             ObjectMapper objectMapper) {
        this.logEntryRepository = logEntryRepository;
        this.errorSolutionRepository = errorSolutionRepository;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Analyse une erreur SANS info de noeud (mode classique/manuel).
     */
    public LogAnalysisResponse analyzeError(LogEntry logEntry) {
        return analyzeErrorWithNode(logEntry, null, null);
    }

    /**
     * Analyse une erreur AVEC info de noeud jBPM (mode automatique).
     * Le nom du noeud permet au service Python de retourner des solutions specifiques.
     */
    public LogAnalysisResponse analyzeErrorWithNode(LogEntry logEntry, String nodeName, String nodeType) {
        List<ErrorSuggestionDto> suggestions = new ArrayList<>();
        List<SimilarErrorDto> similarErrors = new ArrayList<>();

        // 1. Appeler le microservice Python IA
        if (aiEnabled) {
            try {
                Map<String, Object> pythonResponse = callPythonAiService(logEntry, nodeName, nodeType);
                if (pythonResponse != null) {
                    suggestions.addAll(extractSuggestions(pythonResponse));
                    similarErrors.addAll(extractSimilarErrors(pythonResponse));
                }
            } catch (Exception e) {
                log.warn("Microservice Python IA indisponible, fallback local: {}", e.getMessage());
            }
        }

        // 2. Fallback: solutions locales si le service Python est indisponible
        if (suggestions.isEmpty()) {
            suggestions.addAll(getLocalSuggestions(logEntry, nodeName, nodeType));
        }

        // 3. Fallback: erreurs similaires depuis la BDD locale
        if (similarErrors.isEmpty()) {
            similarErrors.addAll(findLocalSimilarErrors(logEntry));
        }

        String googleUrl = buildGoogleSearchUrl(logEntry, nodeName);

        // 4. Persister les suggestions
        for (ErrorSuggestionDto dto : suggestions) {
            ErrorSolution solution = ErrorSolution.builder()
                    .logEntry(logEntry)
                    .suggestion(dto.getSuggestion())
                    .googleSearchUrl(dto.getGoogleSearchUrl() != null ? dto.getGoogleSearchUrl() : googleUrl)
                    .source(dto.getSource())
                    .confidenceScore(dto.getConfidenceScore())
                    .createdAt(LocalDateTime.now())
                    .applied(false)
                    .build();
            errorSolutionRepository.save(solution);
        }

        return LogAnalysisResponse.builder()
                .logEntryId(logEntry.getId())
                .processId(logEntry.getProcessId())
                .errorType(logEntry.getErrorType())
                .errorMessage(logEntry.getMessage())
                .severity(logEntry.getLogLevel().name())
                .suggestions(suggestions)
                .similarErrors(similarErrors)
                .googleSearchUrl(googleUrl)
                .totalSimilarErrorsCount(similarErrors.size())
                .build();
    }

    public LogAnalysisResponse analyzeErrorById(Long logEntryId) {
        LogEntry logEntry = logEntryRepository.findById(logEntryId)
                .orElseThrow(() -> new RuntimeException("LogEntry not found: " + logEntryId));
        return analyzeError(logEntry);
    }

    /**
     * Appelle le microservice Python (FastAPI) pour l'analyse IA.
     * Envoie node_name et node_type pour obtenir des solutions specifiques au noeud.
     */
    private Map<String, Object> callPythonAiService(LogEntry logEntry, String nodeName, String nodeType) {
        String logContent = logEntry.getMessage();
        if (logEntry.getStackTrace() != null) {
            logContent += "\n" + logEntry.getStackTrace();
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("log_content", logContent);
        requestBody.put("process_id", logEntry.getProcessId());
        requestBody.put("workflow_type", logEntry.getWorkflowType());

        // NOUVEAU : envoyer le noeud jBPM pour solutions specifiques
        if (nodeName != null && !nodeName.isEmpty()) {
            requestBody.put("node_name", nodeName);
        }
        if (nodeType != null && !nodeType.isEmpty()) {
            requestBody.put("node_type", nodeType);
        }

        String response = webClient.post()
                .uri(pythonServiceUrl + "/api/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null) {
            try {
                JsonNode root = objectMapper.readTree(response);
                Map<String, Object> result = new HashMap<>();
                JsonNode payload = root.isArray() && root.size() > 0 ? root.get(0) : root;
                result.put("json", payload);
                return result;
            } catch (Exception e) {
                log.error("Erreur parsing reponse Python: {}", e.getMessage());
            }
        }
        return null;
    }

    private List<ErrorSuggestionDto> extractSuggestions(Map<String, Object> pythonResponse) {
        List<ErrorSuggestionDto> suggestions = new ArrayList<>();
        JsonNode json = (JsonNode) pythonResponse.get("json");
        JsonNode suggestionsNode = json.path("suggestions");

        if (suggestionsNode.isArray()) {
            for (JsonNode s : suggestionsNode) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion(s.path("suggestion").asText())
                        .googleSearchUrl(s.path("google_search_url").asText())
                        .source(s.path("source").asText())
                        .confidenceScore(s.path("confidence_score").asDouble(0.5))
                        .build());
            }
        }
        return suggestions;
    }

    private List<SimilarErrorDto> extractSimilarErrors(Map<String, Object> pythonResponse) {
        List<SimilarErrorDto> similarErrors = new ArrayList<>();
        JsonNode json = (JsonNode) pythonResponse.get("json");
        JsonNode similarNode = json.path("similar_errors");

        if (similarNode.isArray()) {
            for (JsonNode s : similarNode) {
                similarErrors.add(SimilarErrorDto.builder()
                        .processId(s.path("process_id").asText())
                        .errorType(s.path("error_type").asText())
                        .message(s.path("message").asText())
                        .appliedSolution(s.path("applied_solution").asText(null))
                        .build());
            }
        }
        return similarErrors;
    }

    /**
     * Solutions locales PRO (fallback si Python indisponible).
     * Utilise le nom du noeud pour proposer des solutions specifiques.
     */
    private List<ErrorSuggestionDto> getLocalSuggestions(LogEntry logEntry, String nodeName, String nodeType) {
        List<ErrorSuggestionDto> suggestions = new ArrayList<>();
        String errorType = logEntry.getErrorType();
        String message = logEntry.getMessage() != null ? logEntry.getMessage().toLowerCase() : "";
        String googleUrl = buildGoogleSearchUrl(logEntry, nodeName);

        // Solutions specifiques par noeud jBPM
        if (nodeName != null && !nodeName.isEmpty()) {
            String nodeNameLower = nodeName.toLowerCase();

            if (nodeNameLower.contains("signal") || nodeNameLower.contains("donor_received")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Signal non recu de l'operateur donneur.\n\nEtapes:\n" +
                                "  1. Verifier si l'operateur donneur a envoye le signal\n" +
                                "  2. Consulter les logs OTNP_WS1 pour le message SOAP\n" +
                                "  3. Verifier la file JMS pour les messages en attente\n" +
                                "  4. Verifier la connectivite vers le gateway inter-operateur\n" +
                                "  5. Relancer le signal via Business Central si necessaire")
                        .source("Solutions noeud '" + nodeName + "' (JBPM_SIGNAL)")
                        .confidenceScore(0.85)
                        .googleSearchUrl(googleUrl)
                        .build());
            } else if (nodeNameLower.contains("manual_validation") || nodeNameLower.contains("update crm")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Tache de validation CRM bloquee.\n\nEtapes:\n" +
                                "  1. Verifier dans Business Central si la tache est assignee (Task Inbox)\n" +
                                "  2. Verifier les droits de l'agent (role 'agent' ou 'validator')\n" +
                                "  3. Verifier la connexion au CRM\n" +
                                "  4. Reassigner la tache si bloquee trop longtemps\n" +
                                "  5. Verifier les champs obligatoires du formulaire")
                        .source("Solutions noeud '" + nodeName + "' (JBPM_HUMAN_TASK)")
                        .confidenceScore(0.85)
                        .googleSearchUrl(googleUrl)
                        .build());
            } else if (nodeNameLower.contains("reject") || nodeNameLower.contains("donor reject")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Demande rejetee par l'operateur donneur.\n\nEtapes:\n" +
                                "  1. Consulter le motif de rejet dans les variables du processus\n" +
                                "  2. Motifs frequents : RIO invalide, numero inexistant, engagement actif\n" +
                                "  3. Verifier la validite du code RIO (demander au client de verifier)\n" +
                                "  4. Si rejet injustifie : contacter l'operateur donneur\n" +
                                "  5. Mettre a jour le statut CRM : 'Rejetee par donneur'")
                        .source("Solutions noeud '" + nodeName + "' (JBPM_HUMAN_TASK)")
                        .confidenceScore(0.85)
                        .googleSearchUrl(googleUrl)
                        .build());
            } else if (nodeNameLower.contains("check") || nodeNameLower.contains("eligib")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Verification d'eligibilite echouee.\n\nEtapes:\n" +
                                "  1. Verifier que le service d'eligibilite est accessible\n" +
                                "  2. Verifier le format du MSISDN (format E.164)\n" +
                                "  3. Consulter la base NUMLEX pour la tranche\n" +
                                "  4. Verifier si le numero est dans la liste noire\n" +
                                "  5. Tester manuellement le service d'eligibilite")
                        .source("Solutions noeud '" + nodeName + "' (JBPM_SERVICE_TASK)")
                        .confidenceScore(0.85)
                        .googleSearchUrl(googleUrl)
                        .build());
            } else if (nodeNameLower.contains("timer")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Timer expire - delai reglementaire depasse.\n\nEtapes:\n" +
                                "  1. Identifier quel delai a expire (J+1, J+3)\n" +
                                "  2. Verifier la reponse de l'operateur donneur\n" +
                                "  3. Envoyer une relance si necessaire\n" +
                                "  4. Escalader au superviseur si delai depasse\n" +
                                "  5. Contacter l'ARCEP si blocage prolonge")
                        .source("Solutions noeud '" + nodeName + "' (JBPM_TIMER)")
                        .confidenceScore(0.85)
                        .googleSearchUrl(googleUrl)
                        .build());
            } else if (nodeNameLower.contains("humantask") || (nodeType != null && nodeType.toLowerCase().contains("humantask"))) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Tache humaine non completee.\n\nEtapes:\n" +
                                "  1. Verifier l'assignation dans Business Central > Task Inbox\n" +
                                "  2. Verifier les droits de l'agent assigne\n" +
                                "  3. Reassigner si l'agent n'est pas disponible\n" +
                                "  4. Verifier le formulaire de la tache\n" +
                                "  5. Verifier le deadline/escalation configure")
                        .source("Solutions noeud '" + nodeName + "' (JBPM_HUMAN_TASK)")
                        .confidenceScore(0.80)
                        .googleSearchUrl(googleUrl)
                        .build());
            } else {
                // Noeud inconnu mais on a quand meme le type
                String nodeTypeDesc = nodeType != null ? nodeType : "inconnu";
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Erreur au noeud '" + nodeName + "' (type: " + nodeTypeDesc + ").\n\nEtapes:\n" +
                                "  1. Consulter Business Central > Process Instances > Diagram\n" +
                                "  2. Verifier les variables du processus\n" +
                                "  3. Consulter les logs du serveur jBPM\n" +
                                "  4. Verifier les WorkItemHandlers si c'est un service task\n" +
                                "  5. Relancer le processus apres correction")
                        .source("Solutions noeud '" + nodeName + "' (type: " + nodeTypeDesc + ")")
                        .confidenceScore(0.70)
                        .googleSearchUrl(googleUrl)
                        .build());
            }
        }

        // Solutions generiques si pas de noeud specifique
        if (suggestions.isEmpty()) {
            if (errorType != null) {
                if (errorType.contains("NullPointerException")) {
                    suggestions.add(ErrorSuggestionDto.builder()
                            .suggestion("NullPointerException. Verifiez que l'objet n'est pas null.")
                            .source("Regle locale: NullPointerException")
                            .confidenceScore(0.7)
                            .googleSearchUrl(googleUrl)
                            .build());
                }
                if (errorType.contains("SQLException") || errorType.contains("DataAccessException")) {
                    suggestions.add(ErrorSuggestionDto.builder()
                            .suggestion("Erreur SQL. Verifiez la connexion BDD.")
                            .source("Regle locale: SQL Error")
                            .confidenceScore(0.7)
                            .googleSearchUrl(googleUrl)
                            .build());
                }
            }

            if (message.contains("port-in") || message.contains("portin")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Erreur Port-In. Verifiez le RIO et le statut aupres de l'operateur donneur.")
                        .source("Regle locale: Port-In")
                        .confidenceScore(0.6)
                        .googleSearchUrl(googleUrl)
                        .build());
            }
            if (message.contains("port-out") || message.contains("portout")) {
                suggestions.add(ErrorSuggestionDto.builder()
                        .suggestion("Erreur Port-Out. Verifiez le contrat client dans BSCS.")
                        .source("Regle locale: Port-Out")
                        .confidenceScore(0.6)
                        .googleSearchUrl(googleUrl)
                        .build());
            }
        }

        // Toujours ajouter le lien Google
        suggestions.add(ErrorSuggestionDto.builder()
                .suggestion("Recherche Google pour cette erreur")
                .source("Google Search")
                .confidenceScore(0.5)
                .googleSearchUrl(googleUrl)
                .build());

        return suggestions;
    }

    private List<SimilarErrorDto> findLocalSimilarErrors(LogEntry logEntry) {
        List<SimilarErrorDto> result = new ArrayList<>();
        if (logEntry.getErrorType() != null) {
            Long excludeId = logEntry.getId() != null ? logEntry.getId() : -1L;
            List<LogEntry> similar = logEntryRepository.findSimilarByErrorType(
                    logEntry.getErrorType(), excludeId);

            for (LogEntry s : similar.stream().limit(10).collect(Collectors.toList())) {
                String appliedSolution = null;
                List<ErrorSolution> solutions = errorSolutionRepository.findByLogEntryId(s.getId());
                if (!solutions.isEmpty()) {
                    appliedSolution = solutions.stream()
                            .filter(sol -> Boolean.TRUE.equals(sol.getApplied()))
                            .findFirst()
                            .map(ErrorSolution::getSuggestion)
                            .orElse(solutions.get(0).getSuggestion());
                }

                result.add(SimilarErrorDto.builder()
                        .logEntryId(s.getId())
                        .processId(s.getProcessId())
                        .processName(s.getProcessName())
                        .errorType(s.getErrorType())
                        .message(s.getMessage())
                        .timestamp(s.getTimestamp())
                        .appliedSolution(appliedSolution)
                        .build());
            }
        }
        return result;
    }

    public String buildGoogleSearchUrl(LogEntry logEntry, String nodeName) {
        StringBuilder query = new StringBuilder();
        if (nodeName != null && !nodeName.isEmpty()) {
            query.append("jBPM ").append(nodeName).append(" ");
        }
        if (logEntry.getErrorType() != null) {
            query.append(logEntry.getErrorType());
        }
        if (logEntry.getMessage() != null) {
            String msg = logEntry.getMessage();
            if (msg.length() > 80) {
                msg = msg.substring(0, 80);
            }
            query.append(" ").append(msg);
        }
        if (logEntry.getWorkflowType() != null) {
            query.append(" ").append(logEntry.getWorkflowType());
        }
        String encoded = URLEncoder.encode(query.toString().trim(), StandardCharsets.UTF_8);
        return "https://www.google.com/search?q=" + encoded;
    }

    /**
     * Notifie le service Python quand une solution est appliquee.
     */
    public void notifyPythonSolutionApplied(LogEntry logEntry, String solution) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("process_id", logEntry.getProcessId());
            body.put("error_type", logEntry.getErrorType());
            body.put("message", logEntry.getMessage());
            body.put("workflow_type", logEntry.getWorkflowType());
            body.put("timestamp", logEntry.getTimestamp().toString());
            body.put("solution", solution);

            webClient.post()
                    .uri(pythonServiceUrl + "/api/history")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.warn("Impossible de notifier le service Python: {}", e.getMessage());
        }
    }
}

