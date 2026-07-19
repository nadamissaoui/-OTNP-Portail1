package com.monitoring.controller;

import com.monitoring.service.LogAnalysisService;
import com.monitoring.service.LogAnalysisService.LogError;
import com.monitoring.service.LogAnalysisService.ErrorSolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour l'analyse de logs jBPM
 * Permet d'analyser les logs et de suggérer des solutions basées sur l'historique
 */
@RestController
@RequestMapping("/api/log-analysis")
@CrossOrigin(origins = "*")
public class LogAnalysisController {
    
    @Autowired
    private LogAnalysisService logAnalysisService;
    
    /**
     * Analyse un fichier de log et retourne les erreurs trouvées
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyzeLogFile(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String logFilePath = request.get("logFilePath");
        if (logFilePath == null || logFilePath.isEmpty()) {
            response.put("error", "logFilePath est requis");
            return response;
        }
        
        try {
            List<LogError> errors = logAnalysisService.analyzeLogFile(logFilePath);
            
            response.put("success", true);
            response.put("errors", errors);
            response.put("totalErrors", errors.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Suggère une solution pour une erreur spécifique
     */
    @PostMapping("/suggest-solution")
    public Map<String, Object> suggestSolution(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LogError error = new LogError();
            error.setErrorMessage((String) request.get("errorMessage"));
            error.setProcessInstanceId((String) request.get("processInstanceId"));
            error.setActivityName((String) request.get("activityName"));
            error.setErrorType((String) request.get("errorType"));
            
            // Pour l'instant, on passe une liste vide d'erreurs historiques
            // Dans une vraie implémentation, on chargerait depuis la base de données
            List<LogError> historicalErrors = List.of();
            
            ErrorSolution solution = logAnalysisService.suggestSolution(error, historicalErrors);
            
            response.put("success", true);
            response.put("solution", Map.of(
                "title", solution.getTitle(),
                "description", solution.getDescription(),
                "documentationUrl", solution.getDocumentationUrl()
            ));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Analyse une erreur à partir de son texte et suggère une solution
     */
    @PostMapping("/analyze-error")
    public Map<String, Object> analyzeError(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String errorText = request.get("errorText");
        if (errorText == null || errorText.isEmpty()) {
            response.put("error", "errorText est requis");
            return response;
        }
        
        try {
            // Créer un LogError à partir du texte
            LogError error = new LogError();
            error.setErrorMessage(errorText);
            error.setErrorType(logAnalysisService.classifyError(errorText));
            
            // Suggérer une solution
            List<LogError> historicalErrors = List.of();
            ErrorSolution solution = logAnalysisService.suggestSolution(error, historicalErrors);
            
            response.put("success", true);
            response.put("errorType", error.getErrorType());
            response.put("solution", Map.of(
                "title", solution.getTitle(),
                "description", solution.getDescription(),
                "documentationUrl", solution.getDocumentationUrl()
            ));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Endpoint de test pour analyser les logs par défaut
     */
    @GetMapping("/test")
    public Map<String, Object> testAnalysis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test avec un fichier de log par défaut
            String defaultLogPath = "c:\\Users\\lenovo\\Desktop\\logs fr\\PBI_JBPM.log";
            List<LogError> errors = logAnalysisService.analyzeLogFile(defaultLogPath);
            
            response.put("success", true);
            response.put("message", "Analyse terminée avec succès");
            response.put("totalErrors", errors.size());
            response.put("sampleErrors", errors.subList(0, Math.min(5, errors.size())));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
