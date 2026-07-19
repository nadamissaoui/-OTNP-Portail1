import axios from 'axios';

const API_URL = 'http://localhost:8081/api/monitoring/search';

export const searchDemandes = async (filters) => {
  try {
    // Crée les query params dynamiquement pour l'URL
    const params = {};
    if (filters.processInstanceId) params.processInstanceId = filters.processInstanceId;
    if (filters.crmId) params.crmId = filters.crmId;
    if (filters.status) params.status = filters.status;
    if (filters.msisdn) params.msisdn = filters.msisdn;
    if (filters.phoneNumber) params.phoneNumber = filters.phoneNumber;
    if (filters.contractCode) params.contractCode = filters.contractCode;
    if (filters.dateDebut) params.dateDebut = filters.dateDebut;
    if (filters.dateFin) params = params.dateFin = filters.dateFin;

    const response = await axios.get(API_URL, { params });
    return response.data;
  } catch (error) {
    console.error("Erreur lors de la récupération des demandes", error);
    throw error;
  }
};