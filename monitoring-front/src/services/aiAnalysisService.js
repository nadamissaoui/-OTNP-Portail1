import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081';

const aiAnalysisService = {

    analyzeLog: async (logContent, processId, workflowType) => {
        const response = await axios.post(`${API_BASE_URL}/api/logs/analyze`, {
            logContent,
            processId,
            workflowType
        });
        return response.data;
    },

    getErrors: async () => {
        const response = await axios.get(`${API_BASE_URL}/api/logs/errors`);
        return response.data;
    },

    getErrorsByProcessId: async (processId) => {
        const response = await axios.get(`${API_BASE_URL}/api/logs/errors/process/${processId}`);
        return response.data;
    },

    markAsResolved: async (logEntryId) => {
        const response = await axios.put(`${API_BASE_URL}/api/logs/${logEntryId}/resolve`);
        return response.data;
    },

    markSolutionApplied: async (solutionId) => {
        const response = await axios.put(`${API_BASE_URL}/api/logs/solutions/${solutionId}/apply`);
        return response.data;
    }
};

export default aiAnalysisService;