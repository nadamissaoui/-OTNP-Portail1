import axios from "axios";

const LOGS_API_URL = process.env.REACT_APP_LOGS_API_URL || "http://localhost:8081/api/logs";
const LOG_ANALYSIS_API_URL = process.env.REACT_APP_LOG_ANALYSIS_API_URL || "http://localhost:8081/api/log-analysis";

const normalizeSolution = (solution) => {
  if (!solution) {
    return {
      title: "Solution générique",
      description: "Erreur non reconnue. Vérifiez les logs détaillés, le processInstanceId, l'activité jBPM et la configuration du workflow.",
      documentationUrl: "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/"
    };
  }

  if (typeof solution === "string") {
    return {
      title: "Solution proposée",
      description: solution,
      documentationUrl: null
    };
  }

  return {
    title: solution.title || solution.name || "Solution proposée",
    description: solution.description || solution.suggestion || solution.message || "Consultez les détails de l'analyse pour appliquer la correction.",
    steps: solution.steps || [],
    documentationUrl: solution.documentationUrl || solution.documentation_url || solution.url || null
  };
};

const normalizeAnalysisResponse = (data, originalText = "") => {
  const rawPayload = data?.data || data?.result || data || {};
  const payload = Array.isArray(rawPayload) ? (rawPayload[0] || {}) : rawPayload;
  const firstSuggestion = Array.isArray(payload.suggestions) ? payload.suggestions[0] : null;
  const solution = normalizeSolution(payload.solution || payload.suggestedSolution || payload.suggestion || firstSuggestion);

  return {
    ...payload,
    success: payload.success !== false,
    errorType: payload.errorType || payload.error_type || payload.type || "UNKNOWN_ERROR",
    solution,
    originalLog: payload.originalLog || payload.original_log || originalText,
    probableCause: payload.probableCause || payload.probable_cause || null,
    confidenceScore: payload.confidenceScore || payload.confidence_score || firstSuggestion?.confidenceScore || firstSuggestion?.confidence_score || null,
    similarErrors: payload.similarErrors || payload.similar_errors || []
  };
};

export const logAnalysisService = {

  // Analyse d'un log collé par l'utilisateur
  async analyzeLog(logContent, processId, workflowType) {
    try {

      const response = await axios.post(`${LOGS_API_URL}/analyze`, {
        logContent,
        processId,
        workflowType
      });

      return normalizeAnalysisResponse(response.data, logContent);

    } catch (error) {
      console.error(error);
      throw error;
    }
  },

  // Analyse directe d'une erreur ou exception jBPM depuis son texte
  async analyzeError(errorText, context = {}) {
    if (!errorText || !errorText.trim()) {
      throw new Error("Le texte de l'erreur est vide");
    }

    const payload = {
      errorText,
      logContent: errorText,
      processId: context.processId,
      workflowType: context.workflowType,
      activityName: context.activityName,
      processInstanceId: context.processInstanceId
    };

    try {
      const response = await axios.post(`${LOGS_API_URL}/analyze`, payload);
      return normalizeAnalysisResponse(response.data, errorText);
    } catch (primaryError) {
      try {
        const response = await axios.post(`${LOG_ANALYSIS_API_URL}/analyze-error`, payload);
        return normalizeAnalysisResponse(response.data, errorText);
      } catch (fallbackError) {
        console.error("Erreur d analyse de log:", fallbackError);
        throw fallbackError;
      }
    }
  },

  // Analyse d'un fichier de logs côté backend
  async analyzeLogFile(logFilePath) {
    const response = await axios.post(`${LOG_ANALYSIS_API_URL}/analyze`, {
      logFilePath
    });

    return response.data;
  },

  // Suggérer une solution depuis des données d'erreur structurées
  async suggestSolution(errorData) {
    const response = await axios.post(`${LOG_ANALYSIS_API_URL}/suggest-solution`, errorData);

    return normalizeAnalysisResponse(response.data, errorData?.errorText || "");
  },

  // Tester l'analyse backend avec les logs configurés côté serveur
  async testAnalysis() {
    const response = await axios.get(`${LOG_ANALYSIS_API_URL}/test`);

    return response.data;
  },

  // Ré-analyser un log déjà enregistré
  async analyzeExisting(logEntryId) {

    try {

      const response = await axios.get(
        `${LOGS_API_URL}/analyze/${logEntryId}`
      );

      return response.data;

    } catch (error) {
      console.error(error);
      throw error;
    }

  },

  // Toutes les erreurs
  async getAllErrors() {

    const response = await axios.get(`${LOGS_API_URL}/errors`);

    return response.data;

  },

  // Recherche
  async searchErrors(keyword) {

    const response = await axios.get(`${LOGS_API_URL}/search`, {
      params: {
        keyword
      }
    });

    return response.data;

  },

  // Erreurs d'un processus
  async getErrorsByProcess(processId) {

    const response = await axios.get(
      `${LOGS_API_URL}/errors/process/${processId}`
    );

    return response.data;

  },

  // Erreurs par type
  async getErrorsByType(errorType) {

    const response = await axios.get(
      `${LOGS_API_URL}/errors/type/${errorType}`
    );

    return response.data;

  },

  // Marquer résolue
  async resolveError(logEntryId) {

    await axios.put(
      `${LOGS_API_URL}/${logEntryId}/resolve`
    );

  }

};

