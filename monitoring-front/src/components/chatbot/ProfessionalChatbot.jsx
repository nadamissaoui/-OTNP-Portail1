import React, { useEffect, useRef, useState } from 'react';
import { FaRobot, FaPaperPlane, FaTimes, FaChevronUp, FaTrash } from 'react-icons/fa';
import { chatbotService } from '../../services/chatbotService';
import './ProfessionalChatbot.css';

const suggestions = [
  'Explique le dashboard actuel',
  'Aide-moi a comprendre Portability IN',
  'C\'est quoi le SLA ?',
  'Que veut dire RIO ?'
];

const initialMessages = [
  {
    role: 'assistant',
    text: 'Bonjour ! Je suis votre assistant OTNP. Posez-moi n\'importe quelle question : portabilite, monitoring, erreurs, ou autre sujet. Je suis la pour vous aider.'
  }
];

export default function ProfessionalChatbot({ activeDashboard, username }) {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState(initialMessages);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const clearChat = () => setMessages(initialMessages);

  useEffect(() => {
    if (open) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, loading, open]);

  const sendMessage = async (text = input) => {
    const cleanText = text.trim();
    if (!cleanText || loading) return;

    const userMessage = { role: 'user', text: cleanText };
    const nextMessages = [...messages, userMessage];
    setMessages(nextMessages);
    setInput('');
    setLoading(true);

    try {
      const answer = await chatbotService.ask(cleanText, {
        activeDashboard,
        username,
        history: nextMessages
      });
      setMessages(prev => [
        ...prev,
        { role: 'assistant', text: answer.text, source: answer.source }
      ]);
    } catch (error) {
      setMessages(prev => [
        ...prev,
        {
          role: 'assistant',
          text: 'Desole, je n arrive pas a contacter le service IA pour le moment. Verifiez que le backend AI (port 5001) est demarre, puis reessayez.'
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`professional-chatbot ${open ? 'is-open' : ''}`}>
      {!open ? (
        <button className="chatbot-launcher" onClick={() => setOpen(true)} aria-label="Ouvrir l assistant IA">
          <FaRobot aria-hidden="true" />
          <span>Assistant IA</span>
        </button>
      ) : (
        <section className="chatbot-panel" aria-label="Assistant IA monitoring">
          <header className="chatbot-header">
            <div>
              <span className="chatbot-kicker">Assistant OTNP</span>
              <h3>Assistant IA</h3>
            </div>
            <div className="chatbot-header-actions">
              <button className="chatbot-icon-btn" onClick={clearChat} aria-label="Nouvelle conversation" title="Nouvelle conversation">
                <FaTrash aria-hidden="true" />
              </button>
              <button className="chatbot-icon-btn" onClick={() => setOpen(false)} aria-label="Fermer l assistant">
                <FaTimes aria-hidden="true" />
              </button>
            </div>
          </header>

          <div className="chatbot-messages">
            {messages.map((message, index) => (
              <div key={`${message.role}-${index}`} className={`chatbot-message chatbot-message-${message.role}`}>
                <p>{message.text}</p>
                {message.source && <span className="chatbot-source">Source: {message.source}</span>}
              </div>
            ))}
            {loading && (
              <div className="chatbot-message chatbot-message-assistant">
                <div className="chatbot-typing"><span /><span /><span /></div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="chatbot-suggestions">
            {suggestions.map(suggestion => (
              <button key={suggestion} onClick={() => sendMessage(suggestion)} disabled={loading}>
                {suggestion}
              </button>
            ))}
          </div>

          <form className="chatbot-form" onSubmit={(event) => { event.preventDefault(); sendMessage(); }}>
            <textarea
              value={input}
              onChange={(event) => setInput(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault();
                  sendMessage();
                }
              }}
              placeholder="Posez votre question ici..."
              rows={2}
              disabled={loading}
            />
            <button type="submit" disabled={loading || !input.trim()} aria-label="Envoyer">
              {loading ? <FaChevronUp aria-hidden="true" /> : <FaPaperPlane aria-hidden="true" />}
            </button>
          </form>
        </section>
      )}
    </div>
  );
}