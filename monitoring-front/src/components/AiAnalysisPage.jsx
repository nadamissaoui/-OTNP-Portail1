import React, { useState, useEffect } from 'react';
import { FcGoogle } from "react-icons/fc";
import * as XLSX from 'xlsx';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import { logAnalysisService } from '../services/logAnalysisService';
const CSS = `
:root {
  --nw-orange: #E8611A;
  --nw-orange-light: rgba(232,97,26,0.10);
  --nw-orange-border: rgba(232,97,26,0.35);
  --nw-orange-deep: #C94E10;
  --nw-orange-soft: #F2762E;
  --nw-green: #1D9E75;
  --nw-amber: #BA7517;
  --nw-red: #E24B4A;
}

/* ── Layout global ── */
.ai-page {
  padding: 24px 28px;
  background: #f5f6fa;
  min-height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  color: #1f2937;
  box-sizing: border-box;
  width: 100%;
}

/* ── Header ── */
.ai-page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px;
  border-left: 4px solid var(--nw-orange);
  padding-left: 12px;
}

/* ── KIE status ── */
.kie-status-row { display:flex; align-items:center; gap:8px; margin-bottom:20px; padding-left:4px; }
.kie-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }
.kie-ok      { background: #1D9E75; }
.kie-ko      { background: #E24B4A; }
.kie-pending { background: #BA7517; }
.kie-label   { color:#6b7280; font-size:13px; }
.btn-reconnect {
  padding: 4px 12px;
  background: transparent;
  color: var(--nw-orange);
  border: 1px solid var(--nw-orange-border);
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

/* ── Carte principale (comme le tableau de consultation) ── */
.ai-main-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 20px 24px;
  width: 100%;
  box-sizing: border-box;
}

/* ── Onglets ── */
.tabs-row { display:flex; margin-bottom:20px; }
.tab-btn {
  padding: 9px 24px;
  border: 1px solid #d1d5db;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  background: #f9fafb;
  color: #6b7280;
  transition: all .15s;
}
.tab-btn-left  { border-radius: 6px 0 0 6px; border-right: none; }
.tab-btn-right { border-radius: 0 6px 6px 0; }
.tab-active    { background: var(--nw-orange)!important; color:#fff!important; border-color: var(--nw-orange)!important; }

/* ── Filtres ── */
.filters-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.form-input {
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #1f2937;
  font-size: 13px;
  box-sizing: border-box;
}
.form-input:focus {
  outline: none;
  border-color: var(--nw-orange);
  box-shadow: 0 0 0 2px var(--nw-orange-light);
}
.btn-refresh {
  padding: 8px 18px;
  background: #1D9E75;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: opacity .15s;
}
.btn-refresh:hover    { opacity: .88; }
.btn-refresh:disabled { opacity: .5; cursor: not-allowed; }
.errors-count { color: #9ca3af; font-size: 13px; }
.btn-toggle-resolved {
  background: transparent;
  border: none;
  color: var(--nw-orange);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
}
.btn-toggle-resolved:hover { color: var(--nw-orange-deep); }

/* ── Rapport ── */
.report-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-wrap: wrap;
}
.report-label { color:#6b7280; font-size:13px; font-weight:500; }
.report-select { min-width: 130px; }
.btn-report {
  padding: 7px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  transition: opacity .15s;
}
.btn-report:hover { opacity: .88; }
.btn-excel { background: #1D6F42; }
.btn-pdf  { background: #D32F2F; }

/* ── Résolution manuelle ── */
.btn-resolve {
  padding: 6px 12px;
  background: transparent;
  color: var(--nw-green);
  border: 1px solid rgba(29,158,117,0.4);
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all .15s;
}
.btn-resolve:hover { background: rgba(29,158,117,0.08); }
.resolved-badge {
  padding: 5px 11px;
  background: rgba(29,158,117,0.12);
  color: var(--nw-green);
  border: 1px solid rgba(29,158,117,0.35);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

/* ── Carte erreur ── */
.error-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-left: 4px solid var(--nw-orange);
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 12px;
}
.error-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.error-info { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.error-date { color:#9ca3af; font-size:12px; white-space:nowrap; }
.error-message {
  background: #fef2f2;
  border-left: 3px solid rgba(226,75,74,0.4);
  border-radius: 0 6px 6px 0;
  padding: 10px 12px;
  color: #1a1a1a;
  font-family: monospace;
  font-size: 12px;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 130px;
  overflow-y: auto;
}
.error-actions-row { display:flex; gap:10px; align-items:center; }
.analysis-done-label { color: #1D9E75; font-size:12px; font-weight:500; }

/* ── Badges ── */
.badge {
  padding: 3px 9px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}
.badge-error { background:rgba(226,75,74,.1); color:#c0392b; border:1px solid rgba(226,75,74,.3); }
.badge-warn  { background:rgba(186,117,23,.1); color:#92580a; border:1px solid rgba(186,117,23,.3); }
.badge-info  { background:rgba(55,138,221,.1); color:#185fa5; border:1px solid rgba(55,138,221,.3); }
.badge-type  { background:var(--nw-orange-light); color:var(--nw-orange); border:1px solid var(--nw-orange-border); }
.process-id    { color:#9ca3af; font-size:12px; }
.activity-name { color:#BA7517; font-size:12px; font-weight:500; }

/* ── Boutons ── */
.btn-primary {
  padding: 9px 20px;
  background: linear-gradient(135deg, var(--nw-orange-soft), var(--nw-orange) 55%, var(--nw-orange-deep));
  color: #fff;
  border: none;
  border-radius: 7px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: .2px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 6px rgba(232,97,26,.35), inset 0 1px 0 rgba(255,255,255,.18);
  transition: transform .15s ease, box-shadow .15s ease, filter .15s ease;
}
.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232,97,26,.45), inset 0 1px 0 rgba(255,255,255,.18);
  filter: brightness(1.04);
}
.btn-primary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(232,97,26,.35), inset 0 1px 0 rgba(255,255,255,.12);
}
.btn-primary:disabled { opacity: .5; cursor: not-allowed; box-shadow:none; }
.btn-reanalyze {
  background: linear-gradient(135deg, #7c8696, #6b7280)!important;
  box-shadow: 0 2px 6px rgba(107,114,128,.35), inset 0 1px 0 rgba(255,255,255,.12)!important;
}
.btn-full { width:100%; padding:11px; font-size:14px; justify-content:center; }

.google-link {
  color: var(--nw-orange);
  font-size: 12px;
  padding: 5px 10px;
  border: 1px solid var(--nw-orange-border);
  border-radius: 6px;
  text-decoration: none;
  display: inline-block;
  margin-top: 6px;
}
.google-link:hover { background: var(--nw-orange-light); }
.google-link-btn {
  display: inline-block;
  margin-top: 10px;
  padding: 8px 18px;
  background: var(--nw-orange);
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
}

/* ── Bloc résultats IA ── */
.analysis-results-block {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid #f0f0f0;
  background: #fafafa;
  border-radius: 0 0 6px 6px;
  padding: 16px;
  margin: 12px -18px -16px;
}
.analysis-results-title {
  color: var(--nw-orange);
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px;
}
.result-badges-row { display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin-bottom:12px; }

/* ── Suggestions ── */
.suggestions-section  { margin-bottom:12px; }
.suggestions-title    { color:#1D9E75; font-size:13px; font-weight:600; margin:0 0 8px; }
.suggestion-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
}
.suggestion-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; flex-wrap:wrap; gap:6px; }
.suggestion-source { color:#6b7280; font-size:11px; font-weight:600; text-transform:uppercase; }
.conf-wrap { display:flex; align-items:center; gap:6px; }
.conf-bg   { width:80px; height:5px; background:#e5e7eb; border-radius:3px; overflow:hidden; }
.conf-fill { height:100%; background:#1D9E75; border-radius:3px; }
.conf-text { color:#9ca3af; font-size:11px; }
.suggestion-text { color:#374151; font-size:13px; line-height:1.6; white-space:pre-wrap; }

/* ── Erreurs similaires ── */
.similar-errors-section  { margin-bottom:12px; }
.similar-errors-title    { color:#BA7517; font-size:13px; font-weight:600; margin:0 0 8px; }
.similar-error-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
}
.similar-error-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.similar-process    { color:#6b7280; font-size:12px; }
.similarity-score   { color:var(--nw-orange); font-size:12px; font-weight:600; }
.applied-solution {
  margin-top: 6px;
  padding: 6px 10px;
  background: rgba(29,158,117,.08);
  border-left: 3px solid #1D9E75;
  color: #0f6e56;
  font-size: 12px;
  border-radius: 0 6px 6px 0;
}
.similar-error-message { color:#6b7280; font-size:12px; font-family:monospace; white-space:pre-wrap; word-break:break-word; }

/* ── Formulaire manuel ── */
.form-subtitle { color:#6b7280; font-size:13px; margin:0 0 16px; }
.form-row   { display:flex; gap:14px; margin-bottom:14px; }
.form-group { flex:1; display:flex; flex-direction:column; gap:5px; }
.form-group label { font-size:13px; color:#555; font-weight:500; }
.log-textarea {
  width: 100%;
  padding: 10px 12px;
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #1f2937;
  font-size: 12px;
  font-family: monospace;
  resize: vertical;
  min-height: 160px;
  box-sizing: border-box;
  margin-bottom: 14px;
}
.log-textarea:focus { outline:none; border-color:var(--nw-orange); }

/* ── Misc ── */
.spinner {
  width: 15px; height: 15px;
  border: 2px solid rgba(255,255,255,.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform:rotate(360deg); } }
.error-banner {
  background: rgba(226,75,74,.08);
  border: 1px solid rgba(226,75,74,.3);
  color: #c0392b;
  padding: 10px 14px;
  border-radius: 8px;
  margin-top: 14px;
  font-size: 13px;
}
.no-errors-card {
  text-align: center;
  padding: 48px 24px;
  color: #9ca3af;
  font-size: 14px;
}
.no-errors-card p:first-child { color:#1D9E75; font-size:15px; font-weight:500; margin-bottom:6px; }
.kie-disconnected-card { text-align:center; padding:48px 24px; }
.kie-disconnected-title { color:#BA7517; font-size:15px; font-weight:500; margin-bottom:8px; }
.kie-disconnected-sub   { color:#9ca3af; font-size:13px; margin-bottom:20px; }

/* ── Chat IA ── */
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
  font-family: 'Segoe UI', sans-serif;
}
.chat-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: linear-gradient(135deg, var(--nw-orange-soft), var(--nw-orange));
  color: #fff;
  border: none;
  border-radius: 28px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(232,97,26,.4);
  transition: transform .15s, box-shadow .15s;
}
.chat-toggle-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(232,97,26,.5); }
.chat-toggle-icon { font-size: 18px; }
.chat-window {
  width: 360px;
  height: 520px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(0,0,0,.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--nw-orange-soft), var(--nw-orange));
  color: #fff;
}
.chat-title { font-weight: 600; font-size: 15px; }
.chat-close {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}
.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f9fafb;
}
.chat-bubble {
  max-width: 85%;
  padding: 10px 13px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}
.chat-bubble-user {
  align-self: flex-end;
  background: var(--nw-orange);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.chat-bubble-agent {
  align-self: flex-start;
  background: #fff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
}
.chat-bubble-text { white-space: pre-wrap; }
.chat-typing {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.chat-typing span {
  width: 7px;
  height: 7px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.2s infinite ease-in-out;
}
.chat-typing span:nth-child(2) { animation-delay: .2s; }
.chat-typing span:nth-child(3) { animation-delay: .4s; }
@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: .5; }
  40% { transform: scale(1); opacity: 1; }
}
.chat-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 14px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}
.chat-suggestion {
  padding: 5px 10px;
  background: var(--nw-orange-light);
  color: var(--nw-orange);
  border: 1px solid var(--nw-orange-border);
  border-radius: 14px;
  cursor: pointer;
  font-size: 11px;
  transition: background .15s;
}
.chat-suggestion:hover { background: rgba(232,97,26,.15); }
.chat-input-row {
  display: flex;
  gap: 8px;
  padding: 12px 14px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}
.chat-input {
  flex: 1;
  padding: 9px 12px;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  font-size: 13px;
  outline: none;
}
.chat-input:focus { border-color: var(--nw-orange); box-shadow: 0 0 0 2px var(--nw-orange-light); }
.chat-send {
  padding: 9px 16px;
  background: var(--nw-orange);
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
}
.chat-send:disabled { opacity: .5; cursor: not-allowed; }
`;

const AiAnalysisPage = () => {
    const [logContent, setLogContent]         = useState('');
    const [processId, setProcessId]           = useState('');
    const [workflowType, setWorkflowType]     = useState('PORT_IN');
    const [manualResults, setManualResults]   = useState(null);
    const [loading, setLoading]               = useState(false);
    const [error, setError]                   = useState(null);
    const [kieConnected, setKieConnected]     = useState(null);
    const [kieErrors, setKieErrors]           = useState([]);
    const [kieLoading, setKieLoading]         = useState(false);
    const [containers, setContainers]         = useState([]);
    const [selectedContainer, setSelectedContainer] = useState('all');
    const [activeTab, setActiveTab]           = useState('auto');
    const [analyzingErrorId, setAnalyzingErrorId]   = useState(null);
    const [errorResults, setErrorResults]     = useState({});
    const [resolvedErrors, setResolvedErrors] = useState(() => {
        try {
            const saved = localStorage.getItem('jbpm_resolved_errors');
            return saved ? JSON.parse(saved) : {};
        } catch { return {}; }
    });
    const [reportPeriod, setReportPeriod]     = useState('week');
    const [hideResolved, setHideResolved]     = useState(true);

    // ── Chat IA ──
    const [chatOpen, setChatOpen]             = useState(false);
    const [chatMessages, setChatMessages]     = useState([
        { role: 'agent', text: 'Bonjour ! Je suis votre assistant IA basé sur l\'analyse de vos logs jBPM réels. Je peux vous aider à analyser les erreurs et suggérer des solutions. Posez-moi une question sur une erreur ou un problème.' }
    ]);
    const [chatInput, setChatInput]           = useState('');
    const [chatLoading, setChatLoading]       = useState(false);

    useEffect(() => { checkKieConnection(); }, []);

    const formatAiSolution = (analysisResult) => {
        const solution = analysisResult.solution || {};
        const steps = Array.isArray(solution.steps) && solution.steps.length > 0
            ? `Étapes recommandées :\n${solution.steps.map((step, index) => `${index + 1}. ${step}`).join('\n')}`
            : null;
        const confidence = typeof analysisResult.confidenceScore === 'number'
            ? `Confiance IA : ${Math.round(analysisResult.confidenceScore * 100)}%`
            : null;

        return [
            solution.title,
            analysisResult.probableCause ? `Cause probable : ${analysisResult.probableCause}` : null,
            solution.description,
            steps,
            confidence
        ].filter(Boolean).join('\n\n');
    };

    const checkKieConnection = async () => {
        try {
            const res  = await fetch('http://localhost:8081/api/kie/status');
            const data = await res.json();
            setKieConnected(data.connected);
            if (data.connected) { fetchContainers(); fetchKieErrors(); }
        } catch { setKieConnected(false); }
    };

    const fetchContainers = async () => {
        try {
            const res = await fetch('http://localhost:8081/api/kie/containers');
            setContainers(await res.json());
        } catch (e) { console.error(e); }
    };

    const fetchKieErrors = async (containerId) => {
        setKieLoading(true);
        try {
            const url = containerId && containerId !== 'all'
                ? `http://localhost:8081/api/kie/errors/${containerId}`
                : 'http://localhost:8081/api/kie/errors';
            const res = await fetch(url);
            setKieErrors(await res.json());
        } catch (e) { console.error(e); }
        finally { setKieLoading(false); }
    };

    const analyzeKieError = async (kieError) => {

    // cache l'ancienne analyse
    setErrorResults(prev => ({
        ...prev,
        [kieError.errorId]: null
    }));

    setAnalyzingErrorId(kieError.errorId);

    try {
        // Utiliser le nouveau service d'analyse de logs
        const errorText = `Erreur jBPM: ${kieError.errorMessage || kieError.message || 'Pas de message'}, Activité: ${kieError.activityName || 'Inconnue'}, Processus: ${kieError.processId || 'Inconnu'}`;
        
        const analysisResult = await logAnalysisService.analyzeError(errorText, {
            processId: kieError.processId,
            workflowType: kieError.workflowType,
            activityName: kieError.activityName,
            processInstanceId: kieError.processInstanceId
        });
        
        if (analysisResult.success) {
            setErrorResults(prev => ({
                ...prev,
                [kieError.errorId]: [{
                    errorType: analysisResult.errorType,
                    probableCause: analysisResult.probableCause,
                    confidenceScore: analysisResult.confidenceScore,
                    suggestions: [{
                        suggestion: formatAiSolution(analysisResult),
                        documentationUrl: analysisResult.solution.documentationUrl
                    }]
                }]
            }));
        } else {
            setError("Erreur d'analyse : " + (analysisResult.error || "Erreur inconnue"));
        }

    } catch (e) {

        setError("Erreur d'analyse : " + e.message);

    } finally {

        setAnalyzingErrorId(null);

    }
};
    const handleAnalyze = async () => {
        if (!logContent.trim()) { setError('Veuillez coller le contenu du log'); return; }
        setLoading(true); setError(null); setManualResults(null);
        try {
            // Utiliser le nouveau service d'analyse de logs
            const analysisResult = await logAnalysisService.analyzeError(logContent);
            
            if (analysisResult.success) {
                setManualResults([{
                    errorType: analysisResult.errorType,
                    probableCause: analysisResult.probableCause,
                    confidenceScore: analysisResult.confidenceScore,
                    suggestions: [{
                        suggestion: formatAiSolution(analysisResult),
                        documentationUrl: analysisResult.solution.documentationUrl
                    }]
                }]);
            } else {
                setError(analysisResult.error || "Erreur lors de l'analyse");
            }
        } catch (e) { 
            setError("Erreur : " + e.message); 
        } finally { 
            setLoading(false); 
        }
    };

    const mapChatPeriod = (word) => {
        if (word.includes('jour') || word.includes("aujourd'hui")) return 'day';
        if (word.includes('semaine')) return 'week';
        if (word.includes('mois')) return 'month';
        if (word.includes('année') || word.includes('annee')) return 'year';
        return reportPeriod;
    };

    const extractErrorName = (text) => {
        const lower = text.toLowerCase();
        // Liste des noms d'erreurs connus qu'on cherche explicitement
        const knownPatterns = [
            'signal_donor_received', 'signal donor', 'signal_donor',
            'update crm', 'manual_validation', 'manual validation',
            'humantask', 'human task', 'donor reject', 'donor_reject',
            'portabilite', 'portability', 'portout', 'port_in', 'port_out'
        ];
        for (const p of knownPatterns) {
            if (lower.includes(p)) return p;
        }
        // Sinon, extraire les mots techniques (avec underscore, chiffres, ou longueur > 4)
        const stopWords = new Set(['comment','résoudre','resoudre','solution','corriger','fix','erreur','error','problème','problem','aide','help','comment','pourquoi','why','quoi','what','donne','give','liste','list','montre','show','dis','dire','tell','faire','fait','est','les','des','une','un','du','de','la','le','et','ou','ou','pour','par','sur','dans']);
        const tokens = lower.replace(/[?.,;:!]/g, ' ').split(/\s+/).filter(t => t.length > 3 && !stopWords.has(t));
        // Prioriser les tokens qui ressemblent à des identifiants techniques
        const technical = tokens.find(t => /[_0-9]/.test(t));
        return technical || tokens[0] || null;
    };

    const generateLocalReply = (text) => {
        const lower = text.toLowerCase();
        const { start, end } = getPeriodBounds(reportPeriod);
        const periodErrors = kieErrors.filter(ke => ke.errorDate && new Date(ke.errorDate) >= start && new Date(ke.errorDate) <= end);
        const allErrorsCount = kieErrors.length;

        // Salutations
        if (/bonjour|salut|coucou|hello|hey|hi|bnjr|bnj/.test(lower)) {
            return 'Bonjour ! Je suis votre assistant jBPM pour Orange Tunisie. Je peux vous aider à:\n• Analyser les erreurs\n• Expliquer le système\n• Générer des rapports\n• Répondre à vos questions sur jBPM, KIE Server, etc.\n\nQue souhaitez-vous savoir ?';
        }

        // Questions sur l'IA
        if (/ia|ai|intelligence artificielle|artificial intelligence/.test(lower)) {
            return 'L\'IA (Intelligence Artificielle) dans ce système sert à:\n• Analyser automatiquement les erreurs jBPM\n• Suggérer des solutions pour résoudre les problèmes\n• Détecter des patterns dans les erreurs\n• Aider les superviseurs à prendre des décisions\n\nLe chatbot et la page IA passent par monitoring-back, qui centralise l analyse et appelle le microservice Python ML.';
        }

        // Questions sur jBPM
        if (/jbpm|jboss bpm/.test(lower)) {
            return 'jBPM (Java Business Process Management) est un moteur de workflow open-source qui:\n• Automatise les processus métier\n• Gère les tâches humaines\n• Suit l\'exécution des processus\n• Stocke l\'historique de chaque étape\n\nDans ce projet, jBPM gère les workflows de portabilité téléphonique (IN et OUT).';
        }

        // Questions sur Wildfly
        if (/wildfly|jboss|serveur|server/.test(lower)) {
            return 'Wildfly (anciennement JBoss AS) est le serveur d\'application Java qui:\n• Héberge KIE Server (jBPM)\n• Exécute les workflows\n• Gère les connexions HTTP\n• Fournit l\'environnement d\'exécution\n\nKIE Server tourne sur Wildfly pour exécuter les processus de portabilité.';
        }

        // Questions sur KIE Server
        if (/kie|kie server/.test(lower)) {
            return 'KIE Server est le serveur d\'exécution de jBPM qui:\n• Charge et exécute les définitions de processus (BPMN)\n• Gère les instances de processus\n• Fournit des APIs REST pour interagir\n• Communique avec Wildfly\n\nC\'est le moteur qui fait tourner vos workflows de portabilité.';
        }

        // Questions sur la portabilité
        if (/portabilité|portability|portabilite/.test(lower)) {
            return 'La portabilité permet aux clients de changer d\'opérateur en gardant leur numéro:\n\n• Portabilité IN: Client vient chez Orange Tunisie\n• Portabilité OUT: Client quitte Orange Tunisie\n• Processus: Validation RIO → Portage → Activation\n• SLA: 48 heures maximum\n\nLe système jBPM automatise ce workflow.';
        }

        // Questions sur RIO
        if (/rio|relevé|releve/.test(lower)) {
            return 'Le RIO (Relevé d\'Identité Opérateur) est:\n• Un code unique pour chaque numéro\n• Délivré par l\'opérateur actuel\n• Valide 40 jours\n• Nécessaire pour la portabilité\n\nSans RIO valide, la demande de portabilité sera rejetée.';
        }

        // Remerciements / politesse
        if (/merci|thanks|thank you|cool|super|ok|d'accord/.test(lower)) {
            return 'Avec plaisir ! N\'hésitez pas si vous avez d\'autres questions sur le système.';
        }

        // Nombre d'erreurs
        if (/combien|nombre|count|total|nb/.test(lower)) {
            return periodErrors.length === 0
                ? `Aucune erreur n'a été détectée sur la période sélectionnée (${reportPeriod}). Au total, j'ai ${allErrorsCount} erreur(s) en mémoire.`
                : `Sur la période ${reportPeriod}, il y a ${periodErrors.length} erreur(s). Au total, j'ai ${allErrorsCount} erreur(s) chargées.`;
        }

        // Erreurs les plus fréquentes
        if (/fréquentes|frequentes|plus fréquente|plus frequente|top|principales|souvent|répétées/.test(lower)) {
            if (periodErrors.length === 0) return 'Je n\'ai aucune erreur à analyser pour cette période. Essayez de changer la période ou de rafraîchir les erreurs jBPM.';
            const counts = {};
            periodErrors.forEach(ke => {
                const key = ke.activityName || ke.errorType || 'Inconnue';
                counts[key] = (counts[key] || 0) + 1;
            });
            const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1]).slice(0, 5);
            return `Voici les erreurs les plus fréquentes sur la période ${reportPeriod} :\n` + sorted.map(([k, v], i) => `${i + 1}. ${k} — ${v} occurrence(s)`).join('\n');
        }

        // Résumé des erreurs
        if (/résume|résumé|resume|summary|aperçu|overview|situation|statut/.test(lower)) {
            if (periodErrors.length === 0) return 'Je n\'ai aucune erreur à résumer pour cette période.';
            const byProcess = {};
            periodErrors.forEach(ke => {
                const p = ke.processId || 'Inconnu';
                byProcess[p] = (byProcess[p] || 0) + 1;
            });
            const resolvedCount = periodErrors.filter(ke => resolvedErrors[ke.errorId] || ke.resolved).length;
            return `Voici le résumé pour la période ${reportPeriod} :\n• Total erreurs : ${periodErrors.length}\n• Résolues : ${resolvedCount}\n• En attente : ${periodErrors.length - resolvedCount}\n\nRépartition par processus :\n` +
                Object.entries(byProcess).map(([p, c]) => `• ${p} : ${c}`).join('\n');
        }

        // Rapport PDF / Excel
        if (/rapport|pdf|excel|télécharger|telecharger|download|generer|générer|export/.test(lower)) {
            const isPdf = /pdf/.test(lower);
            const isExcel = /excel|xlsx/.test(lower);
            const periodMatch = lower.match(/jour|aujourd'hui|semaine|mois|année|annee/);
            const targetPeriod = periodMatch ? mapChatPeriod(periodMatch[0]) : reportPeriod;
            setReportPeriod(targetPeriod);
            setTimeout(() => isPdf ? downloadPDF() : isExcel ? downloadExcel() : downloadPDF(), 300);
            return `Bien sûr ! Je génère votre ${isExcel ? 'fichier Excel' : 'rapport PDF'} pour la période ${targetPeriod}. Le téléchargement va démarrer dans un instant.`;
        }

        // Résolution / explication d'une erreur
        const isHelpRequest = /résoudre|resoudre|solution|comment|corriger|fix|aide|expliquer|explication|pourquoi|cause/.test(lower);
        if (isHelpRequest) {
            const term = extractErrorName(text);
            if (!term) {
                return 'Je veux bien vous aider ! Pouvez-vous me donner le nom de l\'erreur ou du nœud concerné ? Par exemple : "Comment résoudre Signal_Donor_Received ?"';
            }
            const matching = periodErrors.filter(ke =>
                (ke.activityName || '').toLowerCase().includes(term) ||
                (ke.errorType || '').toLowerCase().includes(term) ||
                (ke.processId || '').toLowerCase().includes(term) ||
                (ke.errorMessage || '').toLowerCase().includes(term)
            );
            if (matching.length === 0) {
                return `Je n'ai trouvé aucune erreur correspondant à "${term}" sur la période ${reportPeriod}. Essayez avec un autre nom, ou changez de période.`;
            }
            const first = matching[0];
            const analysis = errorResults[first.errorId];
            const suggestion = analysis?.[0]?.suggestions?.[0]?.suggestion;
            if (suggestion) {
                return `Voici ce que je peux vous proposer pour l'erreur "${first.activityName || first.errorType}" (Instance #${first.processInstanceId}) :\n\n${suggestion}\n\nN'hésitez pas à me demander un rapport ou une analyse plus détaillée.`;
            }
            return `L'erreur "${first.activityName || first.errorType}" (Instance #${first.processInstanceId}) n'a pas encore été analysée. Cliquez sur "Analyser avec IA" sur sa carte, puis revenez me voir — je pourrai vous donner la solution exacte.`;
        }

        // Lister les erreurs
        if (/liste|lister|voir|afficher|montre|show|quelles|which/.test(lower)) {
            if (periodErrors.length === 0) return 'Aucune erreur à afficher pour cette période.';
            const sample = periodErrors.slice(0, 8).map(ke =>
                `• ${ke.activityName || ke.errorType || 'Erreur'} (Instance #${ke.processInstanceId}) — ${formatShortDate(ke.errorDate)}`
            ).join('\n');
            const more = periodErrors.length > 8 ? `\n... et ${periodErrors.length - 8} autre(s).` : '';
            return `Voici les erreurs de la période ${reportPeriod} :\n${sample}${more}`;
        }

        // Statut KIE
        if (/kie|serveur|connecté|connecte|status|jbpm/.test(lower)) {
            return kieConnected
                ? 'Le KIE Server est connecté. Les erreurs jBPM sont synchronisées en temps réel.'
                : 'Le KIE Server est déconnecté. Vérifiez que jBPM tourne sur localhost:8080, puis cliquez sur "Reconnecter".';
        }

        // Fallback intelligent et amical
        return `Hmm, je n'ai pas bien saisi votre demande. 🤔\n\nVoici ce que je peux faire pour vous :\n• Vous dire combien d'erreurs il y a\n• Lister les erreurs de la période\n• Vous montrer les erreurs les plus fréquentes\n• Résumer la situation actuelle\n• Expliquer comment résoudre une erreur (donnez-moi son nom)\n• Générer un rapport PDF ou Excel\n\nPosez-moi votre question autrement, je ferai de mon mieux !`;
    };

    const sendChatMessage = async (text = chatInput) => {
        if (!text.trim()) return;
        const userMsg = { role: 'user', text: text.trim() };
        setChatMessages(prev => [...prev, userMsg]);
        setChatInput('');
        setChatLoading(true);

        try {
            // Essayer d'analyser avec le service d'analyse de logs si c'est une question sur une erreur
            if (/erreur|error|exception|problème|problem|solution|résoudre|resoudre|fix|corriger/.test(text.toLowerCase())) {
                console.log('� Analyse de log activée pour:', text.trim());
                
                try {
                    const analysisResult = await logAnalysisService.analyzeError(text.trim());
                    
                    if (analysisResult.success && analysisResult.solution) {
                        const solution = analysisResult.solution;
                        const steps = Array.isArray(solution.steps) && solution.steps.length > 0
                            ? `\n\n**Étapes:**\n${solution.steps.map((step, index) => `${index + 1}. ${step}`).join('\n')}`
                            : '';
                        const reply = `🤖 **Analyse IA basée sur vos logs réels**\n\n` +
                            `**Type d'erreur:** ${analysisResult.errorType}\n\n` +
                            (analysisResult.probableCause ? `**Cause probable:** ${analysisResult.probableCause}\n\n` : '') +
                            `**Solution:** ${solution.title}\n\n` +
                            `${solution.description}\n\n` +
                            steps +
                            (typeof analysisResult.confidenceScore === 'number' ? `\n\n**Confiance:** ${Math.round(analysisResult.confidenceScore * 100)}%` : '') +
                            (solution.documentationUrl ? `📚 Documentation: ${solution.documentationUrl}` : '');
                        
                        setChatMessages(prev => [...prev, { role: 'agent', text: reply }]);
                        return;
                    }
                } catch (logError) {
                    console.warn('⚠️ Analyse de log échouée, fallback vers réponse locale:', logError.message);
                }
            }
            
            // Fallback vers la réponse locale
            const reply = generateLocalReply(text.trim());
            setChatMessages(prev => [...prev, { role: 'agent', text: reply }]);
            
        } catch (error) {
            console.error('❌ Erreur:', error);
            const reply = generateLocalReply(text.trim());
            setChatMessages(prev => [...prev, { role: 'agent', text: reply }]);
        } finally {
            setChatLoading(false);
        }
    };

    const chatSuggestions = [
        'Quelles sont les erreurs les plus fréquentes ?',
        'Résume-moi les erreurs de la semaine',
        'Comment résoudre Signal_Donor_Received ?',
        'Génère un rapport PDF du mois'
    ];

    const formatDate = (ts) => {
        if (!ts) return 'N/A';
        try { return new Date(ts).toLocaleString('fr-FR'); } catch { return 'N/A'; }
    };

    const formatShortDate = (ts) => {
        if (!ts) return 'N/A';
        try {
            const d = new Date(ts);
            return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' });
        } catch { return 'N/A'; }
    };

    const getPeriodBounds = (period) => {
        const now = new Date();
        const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
        let start = new Date(end);
        start.setHours(0, 0, 0, 0);
        switch (period) {
            case 'day':
                start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
                break;
            case 'week':
                const day = start.getDay() || 7;
                start.setDate(start.getDate() - day + 1);
                break;
            case 'month':
                start = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
                break;
            case 'year':
                start = new Date(now.getFullYear(), 0, 1, 0, 0, 0, 0);
                break;
            default: break;
        }
        return { start, end };
    };

    const getFilteredErrorsForReport = () => {
        const { start, end } = getPeriodBounds(reportPeriod);
        return kieErrors.filter(ke => {
            if (!ke.errorDate) return false;
            const d = new Date(ke.errorDate);
            return d >= start && d <= end;
        });
    };

    const toggleResolved = (errorId) => {
        setResolvedErrors(prev => {
            const next = { ...prev };
            if (next[errorId]) {
                delete next[errorId];
            } else {
                next[errorId] = new Date().toISOString();
            }
            try { localStorage.setItem('jbpm_resolved_errors', JSON.stringify(next)); } catch {}
            return next;
        });
    };

    const buildReportRows = () => {
        return getFilteredErrorsForReport().map(ke => {
            const analysis = errorResults[ke.errorId];
            const topSuggestion = analysis?.[0]?.suggestions?.[0];
            const confidencePct = topSuggestion
                ? Math.round((topSuggestion.confidenceScore || topSuggestion.confidence_score || 0) * 100)
                : '';
            const manualResolution = resolvedErrors[ke.errorId];
            const isResolved = manualResolution || ke.resolved;
            return {
                Date: formatShortDate(ke.errorDate),
                Process: ke.processId || 'N/A',
                Instance: ke.processInstanceId || 'N/A',
                Erreur: ke.activityName || ke.errorType || 'N/A',
                'Solution IA': topSuggestion?.suggestion || analysis?.[0]?.appliedSolution || analysis?.[0]?.applied_solution || '—',
                Confiance: confidencePct ? `${confidencePct}%` : '—',
                Résolue: isResolved ? 'Oui' : 'Non',
                'Date résolution': isResolved ? formatShortDate(manualResolution || ke.resolutionDate) : '—'
            };
        });
    };

    const downloadExcel = () => {
        const rows = buildReportRows();
        if (rows.length === 0) { setError('Aucune erreur à exporter pour cette période'); return; }
        const ws = XLSX.utils.json_to_sheet(rows);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Rapport');
        const periodLabel = { day: 'jour', week: 'semaine', month: 'mois', year: 'annee' }[reportPeriod];
        XLSX.writeFile(wb, `rapport_erreurs_${periodLabel}_${new Date().toISOString().slice(0, 10)}.xlsx`);
    };

    const downloadPDF = () => {
        const rows = buildReportRows();
        if (rows.length === 0) { setError('Aucune erreur à exporter pour cette période'); return; }
        const doc = new jsPDF({ orientation: 'landscape' });
        const periodLabel = { day: 'Jour', week: 'Semaine', month: 'Mois', year: 'Année' }[reportPeriod];
        doc.setFontSize(16);
        doc.text(`Rapport des erreurs jBPM - ${periodLabel}`, 14, 20);
        doc.setFontSize(10);
        doc.text(`Généré le ${new Date().toLocaleDateString('fr-FR')}`, 14, 28);
        autoTable(doc, {
            startY: 35,
            head: [['Date', 'Process', 'Instance', 'Erreur', 'Solution IA', 'Confiance', 'Résolue', 'Date résolution']],
            body: rows.map(r => [r.Date, r.Process, r.Instance, r.Erreur, r['Solution IA'], r.Confiance, r.Résolue, r['Date résolution']]),
            styles: { fontSize: 9, cellPadding: 2 },
            headStyles: { fillColor: [232, 97, 26], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 246, 250] }
        });
        doc.save(`rapport_erreurs_${periodLabel.toLowerCase()}_${new Date().toISOString().slice(0, 10)}.pdf`);
    };

    const renderResults = (results) => {
        if (!results || results.length === 0) return null;
        return (
            <div className="analysis-results-block">
                <h4 className="analysis-results-title">Résultats de l'analyse IA (basée sur vos logs réels)</h4>
                {results.map((r, i) => (
                    <div key={i}>
                        <div className="result-badges-row">
                            <span className="badge badge-type">{r.errorType || 'Erreur inconnue'}</span>
                        </div>

                        {r.suggestions?.length > 0 && (
                            <div className="suggestions-section">
                                <h5 className="suggestions-title">Solutions proposées :</h5>
                                {r.suggestions.map((s, j) => (
                                    <div key={j} className="suggestion-card">
                                        <div className="suggestion-header">
                                            <span className="suggestion-source">Analyse IA professionnelle</span>
                                        </div>
                                        <div className="suggestion-text">{s.suggestion}</div>
                                        {s.documentationUrl && (
                                            <a
                                                href={s.documentationUrl}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="google-link-btn"
                                                style={{ marginTop: '10px' }}
                                            >
                                                <FcGoogle size={18} />
                                                <span>Documentation officielle</span>
                                            </a>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
            </div>
        );
    };
 
   
                        
    return (
        <>
            <style>{CSS}</style>
            <div className="ai-page">

                {/* ── Titre ── */}
                <h2 className="ai-page-title">Analyse IA </h2>

                {/* ── Statut KIE ── */}
                <div className="kie-status-row">
                    <div className={`kie-dot ${
                        kieConnected === null ? 'kie-pending' : kieConnected ? 'kie-ok' : 'kie-ko'
                    }`} />
                    <span className="kie-label">
                        KIE Server : {kieConnected === null ? 'Vérification...' : kieConnected ? 'Connecté' : 'Non connecté'}
                    </span>
                    {!kieConnected && kieConnected !== null && (
                        <button className="btn-reconnect" onClick={checkKieConnection}>Reconnecter</button>
                    )}
                </div>

                {/* ── Carte principale pleine largeur ── */}
                <div className="ai-main-card">

                    {/* Onglets */}
                    <div className="tabs-row">
                        <button
                            className={`tab-btn tab-btn-left ${activeTab === 'auto' ? 'tab-active' : ''}`}
                            onClick={() => setActiveTab('auto')}>
                            Erreurs jBPM (automatique)
                        </button>
                        <button
                            className={`tab-btn tab-btn-right ${activeTab === 'manual' ? 'tab-active' : ''}`}
                            onClick={() => setActiveTab('manual')}>
                            Analyse manuelle
                        </button>
                    </div>

                    {/* ══ ONGLET AUTO ══ */}
                    {activeTab === 'auto' && (
                        <>
                            {!kieConnected ? (
                                <div className="kie-disconnected-card">
                                    <p className="kie-disconnected-title">KIE Server non connecté</p>
                                    <p className="kie-disconnected-sub">Vérifiez que jBPM tourne sur localhost:8080</p>
                                    <button className="btn-primary" onClick={checkKieConnection}>
                                        Réessayer la connexion
                                    </button>
                                </div>
                            ) : (
                                <>
                                    {/* Filtres */}
                                    <div className="filters-row">
                                        <select
                                            className="form-input"
                                            value={selectedContainer}
                                            onChange={(e) => { setSelectedContainer(e.target.value); fetchKieErrors(e.target.value); }}
                                            style={{ minWidth: '220px' }}>
                                            <option value="all">Tous les containers</option>
                                            {containers.map((c, i) => (
                                                <option key={i} value={c.containerId}>
                                                    {c.containerId} ({c.status})
                                                </option>
                                            ))}
                                        </select>
                                        <button
                                            className="btn-refresh"
                                            onClick={() => fetchKieErrors(selectedContainer)}
                                            disabled={kieLoading}>
                                            {kieLoading ? 'Chargement...' : 'Rafraîchir'}
                                        </button>
                                        <span className="errors-count">
                                            {(() => {
                                                const pending = kieErrors.filter(ke => !(resolvedErrors[ke.errorId] || ke.resolved)).length;
                                                const resolved = kieErrors.length - pending;
                                                return `${pending} erreur(s) en attente`
                                                    + (resolved > 0 ? ` • ${resolved} résolue(s)` : '');
                                            })()}
                                        </span>

                                        {(() => {
                                            const resolvedCount = kieErrors.filter(ke => resolvedErrors[ke.errorId] || ke.resolved).length;
                                            if (resolvedCount === 0) return null;
                                            return (
                                                <button
                                                    className="btn-toggle-resolved"
                                                    onClick={() => setHideResolved(v => !v)}>
                                                    {hideResolved ? `Voir les ${resolvedCount} erreur(s) résolue(s)` : 'Masquer les résolues'}
                                                </button>
                                            );
                                        })()}

                                        {/* ── Rapport ── */}
                                        <div className="report-controls">
                                            <span className="report-label">Rapport :</span>
                                            <select
                                                className="form-input report-select"
                                                value={reportPeriod}
                                                onChange={(e) => setReportPeriod(e.target.value)}>
                                                <option value="day">Aujourd'hui</option>
                                                <option value="week">Cette semaine</option>
                                                <option value="month">Ce mois</option>
                                                <option value="year">Cette année</option>
                                            </select>
                                            <button className="btn-report btn-excel" onClick={downloadExcel}>
                                                Excel
                                            </button>
                                            <button className="btn-report btn-pdf" onClick={downloadPDF}>
                                                PDF
                                            </button>
                                        </div>
                                    </div>

                                    {/* Aucune erreur */}
                                    {(() => {
                                        const displayedErrors = kieErrors.filter(ke => !hideResolved || !(resolvedErrors[ke.errorId] || ke.resolved));
                                        if (displayedErrors.length === 0 && !kieLoading) {
                                            return (
                                                <div className="no-errors-card">
                                                    <p>{kieErrors.length === 0 ? 'Aucune erreur détectée dans jBPM' : 'Aucune erreur visible'}</p>
                                                    <p>{kieErrors.length === 0 ? 'Les processus fonctionnent normalement' : (hideResolved ? 'Toutes les erreurs sont marquées comme résolues.' : 'Aucune erreur à afficher.')}</p>
                                                </div>
                                            );
                                        }
                                        return displayedErrors.map((ke, idx) => (
                                        <div key={idx} className="error-card">
                                            <div className="error-date" style={{ marginBottom: '8px' }}>{formatDate(ke.errorDate)}</div>
                                            <div className="error-message">{ke.errorMessage}</div>

                                            <div className="error-actions-row">
                                                <button
                                                    className={`btn-primary ${errorResults[ke.errorId] ? 'btn-reanalyze' : ''}`}
                                                    onClick={() => analyzeKieError(ke)}
                                                    disabled={analyzingErrorId === ke.errorId}>
                                                    {analyzingErrorId === ke.errorId
                                                        ? <><div className="spinner" /> Analyse en cours...</>
                                                        : errorResults[ke.errorId] ? 'Re-analyser' : 'Analyser avec IA'}
                                                </button>
                                                {errorResults[ke.errorId] && (
                                                    <span className="analysis-done-label">Analyse terminée</span>
                                                )}
                                                {resolvedErrors[ke.errorId] ? (
                                                    <span className="resolved-badge">
                                                        Résolue {formatShortDate(resolvedErrors[ke.errorId])}
                                                    </span>
                                                ) : (
                                                    <button
                                                        className="btn-resolve"
                                                        onClick={() => toggleResolved(ke.errorId)}
                                                        title="Marquer cette erreur comme résolue">
                                                        Marquer comme résolue
                                                    </button>
                                                )}
                                            </div>

                                            {renderResults(errorResults[ke.errorId])}
                                        </div>
                                    )); })()}
                                </>
                            )}
                        </>
                    )}

                    {/* ══ ONGLET MANUEL ══ */}
                    {activeTab === 'manual' && (
                        <>
                            <p className="form-subtitle">
                                Collez manuellement le contenu d'un log pour l'analyser
                            </p>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Process ID</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={processId}
                                        onChange={(e) => setProcessId(e.target.value)}
                                        placeholder="Ex: Portabilite_Orchestration" />
                                </div>
                                <div className="form-group">
                                    <label>Type de workflow</label>
                                    <select
                                        className="form-input"
                                        value={workflowType}
                                        onChange={(e) => setWorkflowType(e.target.value)}>
                                        <option value="PORT_IN">Port-In</option>
                                        <option value="PORT_OUT">Port-Out</option>
                                    </select>
                                </div>
                            </div>
                            <div className="form-group">
                                <label>Contenu du log</label>
                                <textarea
                                    className="log-textarea"
                                    value={logContent}
                                    onChange={(e) => setLogContent(e.target.value)}
                                    placeholder="Collez ici le contenu du log jBPM..."
                                    rows={12} />
                            </div>
                            <button
                                className="btn-primary btn-full"
                                onClick={handleAnalyze}
                                disabled={loading}>
                                {loading
                                    ? <><div className="spinner" /> Analyse en cours...</>
                                    : 'Analyser avec IA'}
                            </button>
                            {renderResults(manualResults)}
                        </>
                    )}

                </div>{/* fin ai-main-card */}

                {error && <div className="error-banner">{error}</div>}

                {/* ── Chat IA flottant ── */}
                <div className={`chat-widget ${chatOpen ? 'chat-open' : ''}`}>
                    {!chatOpen ? (
                        <button
                            className="chat-toggle-btn"
                            onClick={() => setChatOpen(true)}
                            title="Discuter avec l'agent IA">
                            <span className="chat-toggle-icon">💬</span>
                            <span>Assistant IA</span>
                        </button>
                    ) : (
                        <div className="chat-window">
                            <div className="chat-header">
                                <span className="chat-title">Assistant IA</span>
                                <button className="chat-close" onClick={() => setChatOpen(false)}>×</button>
                            </div>
                            <div className="chat-messages">
                                {chatMessages.map((m, i) => (
                                    <div key={i} className={`chat-bubble chat-bubble-${m.role}`}>
                                        <div className="chat-bubble-text">{m.text}</div>
                                    </div>
                                ))}
                                {chatLoading && (
                                    <div className="chat-bubble chat-bubble-agent">
                                        <div className="chat-typing"><span></span><span></span><span></span></div>
                                    </div>
                                )}
                            </div>
                            <div className="chat-suggestions">
                                {chatSuggestions.map((s, i) => (
                                    <button key={i} className="chat-suggestion" onClick={() => sendChatMessage(s)}>
                                        {s}
                                    </button>
                                ))}
                            </div>
                            <div className="chat-input-row">
                                <input
                                    type="text"
                                    className="chat-input"
                                    placeholder="Posez votre question..."
                                    value={chatInput}
                                    onChange={(e) => setChatInput(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && sendChatMessage()}
                                    disabled={chatLoading} />
                                <button
                                    className="chat-send"
                                    onClick={() => sendChatMessage()}
                                    disabled={chatLoading || !chatInput.trim()}>
                                    Envoyer
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </>
    );
};

export default AiAnalysisPage;

