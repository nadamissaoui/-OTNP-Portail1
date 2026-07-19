package com.monitoring.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Service d'analyse de logs jBPM pour détecter les erreurs et suggérer des solutions
 * Basé sur l'historique des logs et la recherche de patterns
 */
@Service
public class LogAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogAnalysisService.class);
    
    // Patterns pour extraire les erreurs des logs
    private static final Pattern ERROR_PATTERN = Pattern.compile(
        "(ERROR|Exception|exception|FATAL|WARN|warning).*?(?=\\n\\n|\\d{4}-\\d{2}-\\d{2}|\\[\\d{4}-)",
        Pattern.DOTALL
    );
    
    private static final Pattern PROCESS_ID_PATTERN = Pattern.compile(
        "processInstanceId[=:]\\s*([\\w-]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern ACTIVITY_PATTERN = Pattern.compile(
        "activity[Name]?[=:]\\s*([\\w-]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Base de connaissances des erreurs courantes et leurs solutions
    private static final Map<String, ErrorSolution> ERROR_KNOWLEDGE_BASE = new HashMap<>();
    
    static {
        // Initialiser la base de connaissances avec des erreurs courantes jBPM
        ERROR_KNOWLEDGE_BASE.put("connection", new ErrorSolution(
            "Erreur de connexion base de données",
            "Vérifiez que la base de données est accessible et que les credentials sont corrects. " +
            "Vérifiez aussi que le pool de connexions n'est pas épuisé.",
            "https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/#_database_configuration"
        ));
        
        ERROR_KNOWLEDGE_BASE.put("timeout", new ErrorSolution(
            "Timeout de transaction",
            "Augmentez le timeout de transaction dans la configuration jBPM. " +
            "Vérifiez aussi que les requêtes ne sont pas trop lentes.",
            "https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/#_transaction_management"
        ));
        
        ERROR_KNOWLEDGE_BASE.put("nullpointer", new ErrorSolution(
            "NullPointerException",
            "Vérifiez que toutes les variables de processus sont initialisées avant utilisation. " +
            "Ajoutez des validations dans les scripts de processus.",
            "https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/#_process_variables"
        ));
        
        ERROR_KNOWLEDGE_BASE.put("signal", new ErrorSolution(
            "Erreur de signal jBPM",
            "Vérifiez que le signal existe dans la définition du processus. " +
            "Assurez-vous que le process instance est dans un état qui peut recevoir le signal.",
            "https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/#_signals"
        ));
        
        ERROR_KNOWLEDGE_BASE.put("task", new ErrorSolution(
            "Erreur de tâche humaine",
            "Vérifiez que l'utilisateur assigné existe et a les droits nécessaires. " +
            "Vérifiez aussi que le groupe de tâches est correctement configuré.",
            "https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/#_human_tasks"
        ));
        
        ERROR_KNOWLEDGE_BASE.put("rio", new ErrorSolution(
            "Erreur RIO (Relevé d'Identité Opérateur)",
            "Vérifiez que le RIO est valide (format correct, non expiré). " +
            "Contactez l'opérateur émetteur si nécessaire.",
            "https://www.arcep.fr/portabilite-des-numéros-mobiles"
        ));
        
        ERROR_KNOWLEDGE_BASE.put("portability", new ErrorSolution(
            "Erreur de portabilité",
            "Vérifiez que toutes les informations du client sont correctes (nom, prénom, numéro). " +
            "Assurez-vous que le RIO est valide et que le numéro n'est pas déjà en cours de portabilité.",
            "https://www.arcep.fr/portabilite-des-numéros-mobiles"
        ));
    }
    
    /**
     * Analyse un fichier de log et extrait les erreurs
     */
    public List<LogError> analyzeLogFile(String logFilePath) {
        List<LogError> errors = new ArrayList<>();
        
        try {
            Path path = Paths.get(logFilePath);
            if (!Files.exists(path)) {
                logger.error("Fichier de log non trouvé: {}", logFilePath);
                return errors;
            }
            
            List<String> lines = Files.readAllLines(path);
            String logContent = String.join("\n", lines);
            
            Matcher errorMatcher = ERROR_PATTERN.matcher(logContent);
            while (errorMatcher.find()) {
                String errorText = errorMatcher.group();
                LogError error = extractErrorDetails(errorText);
                if (error != null) {
                    errors.add(error);
                }
            }
            
            logger.info("Analyse du log terminée: {} erreurs trouvées", errors.size());
            
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier de log: {}", logFilePath, e);
        }
        
        return errors;
    }
    
    /**
     * Extrait les détails d'une erreur à partir du texte du log
     */
    private LogError extractErrorDetails(String errorText) {
        LogError error = new LogError();
        error.setErrorMessage(errorText.substring(0, Math.min(200, errorText.length())));
        
        // Extraire le processInstanceId
        Matcher processIdMatcher = PROCESS_ID_PATTERN.matcher(errorText);
        if (processIdMatcher.find()) {
            error.setProcessInstanceId(processIdMatcher.group(1));
        }
        
        // Extraire le nom de l'activité
        Matcher activityMatcher = ACTIVITY_PATTERN.matcher(errorText);
        if (activityMatcher.find()) {
            error.setActivityName(activityMatcher.group(1));
        }
        
        // Classifier l'erreur
        error.setErrorType(classifyError(errorText));
        
        return error;
    }
    
    /**
     * Classifie le type d'erreur basé sur le contenu
     */
    public String classifyError(String errorText) {
        String lower = errorText.toLowerCase();
        
        if (lower.contains("connection") || lower.contains("database") || lower.contains("sql")) {
            return "DATABASE_ERROR";
        } else if (lower.contains("timeout") || lower.contains("timed out")) {
            return "TIMEOUT_ERROR";
        } else if (lower.contains("nullpointer") || lower.contains("null pointer")) {
            return "NULL_POINTER_ERROR";
        } else if (lower.contains("signal")) {
            return "SIGNAL_ERROR";
        } else if (lower.contains("task") || lower.contains("user task")) {
            return "TASK_ERROR";
        } else if (lower.contains("rio") || lower.contains("relevé")) {
            return "RIO_ERROR";
        } else if (lower.contains("portability") || lower.contains("portabilité")) {
            return "PORTABILITY_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }
    
    /**
     * Trouve des erreurs similaires dans l'historique
     */
    public List<LogError> findSimilarErrors(LogError currentError, List<LogError> historicalErrors) {
        return historicalErrors.stream()
            .filter(error -> error.getErrorType().equals(currentError.getErrorType()))
            .filter(error -> similarity(error.getErrorMessage(), currentError.getErrorMessage()) > 0.5)
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * Calcule la similarité entre deux textes (algorithme simple)
     */
    private double similarity(String s1, String s2) {
        String longer = s1.length() > s2.length() ? s1 : s2;
        String shorter = s1.length() > s2.length() ? s2 : s1;
        
        if (longer.length() == 0) return 1.0;
        
        return (longer.length() - editDistance(longer, shorter)) / (double) longer.length();
    }
    
    /**
     * Distance d'édition (Levenshtein) pour calculer la similarité
     */
    private int editDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Suggère une solution pour une erreur
     */
    public ErrorSolution suggestSolution(LogError error, List<LogError> historicalErrors) {
        // 1. Chercher dans la base de connaissances
        for (Map.Entry<String, ErrorSolution> entry : ERROR_KNOWLEDGE_BASE.entrySet()) {
            if (error.getErrorMessage().toLowerCase().contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 2. Chercher des erreurs similaires dans l'historique
        List<LogError> similarErrors = findSimilarErrors(error, historicalErrors);
        if (!similarErrors.isEmpty()) {
            // Retourner une solution basée sur les erreurs similaires
            return new ErrorSolution(
                "Solution basée sur l'historique",
                "Cette erreur s'est produite " + similarErrors.size() + " fois précédemment. " +
                "Vérifiez les logs précédents pour voir comment elle a été résolue. " +
                "Process instances similaires: " + 
                similarErrors.stream()
                    .map(LogError::getProcessInstanceId)
                    .collect(Collectors.joining(", ")),
                null
            );
        }
        
        // 3. Solution générique
        return new ErrorSolution(
            "Solution générique",
            "Erreur non reconnue. Vérifiez les logs détaillés pour plus d'informations. " +
            "Consultez la documentation jBPM ou contactez le support technique.",
            "https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/"
        );
    }
    
    /**
     * Classe interne pour représenter une erreur de log
     */
    public static class LogError {
        private String processInstanceId;
        private String activityName;
        private String errorMessage;
        private String errorType;
        private Date errorDate;
        
        // Getters and Setters
        public String getProcessInstanceId() { return processInstanceId; }
        public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
        
        public String getActivityName() { return activityName; }
        public void setActivityName(String activityName) { this.activityName = activityName; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        
        public Date getErrorDate() { return errorDate; }
        public void setErrorDate(Date errorDate) { this.errorDate = errorDate; }
    }
    
    /**
     * Classe interne pour représenter une solution d'erreur
     */
    public static class ErrorSolution {
        private String title;
        private String description;
        private String documentationUrl;
        
        public ErrorSolution(String title, String description, String documentationUrl) {
            this.title = title;
            this.description = description;
            this.documentationUrl = documentationUrl;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getDocumentationUrl() { return documentationUrl; }
    }
}
