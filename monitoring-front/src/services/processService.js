const API_BASE_URL = 'http://localhost:8081/api/monitoring';

export const processService = {
  searchProcesses: async (apiParams = {}) => {
    const params = new URLSearchParams();

    // 1. On définit des valeurs par défaut au cas où elles manquent
    const page = (apiParams.page !== undefined && apiParams.page !== null && apiParams.page !== '') ? apiParams.page : 0;
    const size = (apiParams.size !== undefined && apiParams.size !== null && apiParams.size !== '') ? apiParams.size : 100;

    params.append('page', page.toString());
    params.append('size', size.toString());

    // 2. On ajoute les autres filtres de recherche sans dupliquer page et size
    Object.keys(apiParams).forEach(key => {
      // On ignore page et size ici puisqu'ils sont déjà gérés proprement juste au-dessus
      if (key === 'page' || key === 'size') return;

      const value = apiParams[key];
      if (value !== null && value !== undefined && value !== '') {
        params.append(key, value);
      }
    });

    const url = `${API_BASE_URL}/search?${params.toString()}`;
    console.log("URL finale appelée par React :", url); // Pour que tu puisses vérifier dans la console

    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(
        `monitoring-back (${response.status}) — vérifiez que MonitoringBackApplication tourne sur le port 8081 et que le WS est sur 8089`
      );
    }

    return response.json();
  }
};