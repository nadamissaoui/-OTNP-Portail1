const AI_SERVICE_URL = process.env.REACT_APP_AI_SERVICE_URL || 'http://localhost:5001';

export const chatbotService = {
  async ask(message, options = {}) {
    const cleanMessage = message.trim();
    if (!cleanMessage) return { text: 'Veuillez entrer une question.', source: 'Assistant' };

    try {
      const history = (options.history || []).slice(-10).map(msg => ({
        role: msg.role,
        text: msg.text
      }));

      const resp = await fetch(`${AI_SERVICE_URL}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: cleanMessage,
          history,
          context: {
            activeDashboard: options.activeDashboard || '',
            username: options.username || 'Agent'
          }
        }),
        signal: AbortSignal.timeout(30000)
      });

      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

      const data = await resp.json();
      if (data.success && data.text) {
        return { text: data.text, source: data.source || 'AI Service' };
      }
      throw new Error('Reponse invalide');
    } catch (error) {
      return {
        text: "Je n'arrive pas a contacter le service IA pour le moment. Veuillez verifier que le backend AI (port 5001) est demarre, puis reessayer.",
        source: 'Erreur connexion'
      };
    }
  }
};
