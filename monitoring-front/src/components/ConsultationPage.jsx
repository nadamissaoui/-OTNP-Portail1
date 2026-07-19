import React, { useState, useEffect } from 'react';
import { processService } from '../services/processService';
import './Consultation.css';
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

const OPERATORS = {
  '2': { name: 'Ooredoo', short: 'OOR', color: '#E3000F' },
  '5': { name: 'Orange',  short: 'ORN', color: '#FF6600' },
  '9': { name: 'Tunisie Telecom', short: 'TT', color: '#0066AA' },
};

const getOperator = (msisdn) => {
  if (!msisdn) return null;
  const num = msisdn.replace(/^216/, '');
  const prefix = num[0];
  return OPERATORS[prefix] || { name: 'Inconnu', short: '?', color: '#999' };
};

export default function ConsultationPage() {
  const [searchForm, setSearchForm] = useState({
    processId: '',
    status: '',
    startDate: '',
    endDate: '',
    msisdn: '',
    phoneNumber: '',
    idCrm: '',
    contractCode: ''
  });

  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState(null);
  const [selectedProcess, setSelectedProcess] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [lifecycleSteps, setLifecycleSteps] = useState([]);
  const [loadingLifecycle, setLoadingLifecycle] = useState(false);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [commentAuthor, setCommentAuthor] = useState('Agent');
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [smsLogs, setSmsLogs] = useState([]);
  const [fraudData, setFraudData] = useState(null);
  const [fraudLoading, setFraudLoading] = useState(false);
  const [feedbackData, setFeedbackData] = useState(null);
  const [feedbackRating, setFeedbackRating] = useState(0);
  const [feedbackComment, setFeedbackComment] = useState('');
  const [feedbackSubmitting, setFeedbackSubmitting] = useState(false);
  const [feedbackLoading, setFeedbackLoading] = useState(false);
  const [archivedIds, setArchivedIds] = useState(() => {
    try { return JSON.parse(localStorage.getItem('archived_consultation') || '[]'); }
    catch { return []; }
  });
  const [showArchived, setShowArchived] = useState(false);
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');
  const [slaFilter, setSlaFilter] = useState('all');
  const [operatorFilter, setOperatorFilter] = useState('all');
  const [periodFilter, setPeriodFilter] = useState('all');

  // États pour la pagination (10 items par page)
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // Charger toutes les instances au démarrage de la page
  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async (formFilters = {}) => {
    setIsSearching(true);
    setError(null);
    setCurrentPage(1); // Réinitialise à la première page lors d'une nouvelle recherche

    const rawParams = {
      processInstanceId: formFilters.processId ? parseInt(formFilters.processId, 10) : null,
      status: formFilters.status ? parseInt(formFilters.status, 10) : null,
      dateDebut: formFilters.startDate || null,
      dateFin: formFilters.endDate || null,
      msisdn: formFilters.msisdn || null,
      phoneNumber: formFilters.phoneNumber || null,
      crmId: formFilters.idCrm || null,
      contractCode: formFilters.contractCode || null,
      page: 0,
      size: 1000 // On récupère jusqu'à 100 instances pour les paginer par blocs de 10
    };

    const apiParams = Object.keys(rawParams).reduce((acc, key) => {
      const value = rawParams[key];
      if (value !== null && value !== undefined && value !== '') {
        acc[key] = value;
      }
      return acc;
    }, {});

    try {
      const data = await processService.searchProcesses(apiParams);
      console.log('📊 Données reçues du backend :', data);
      if (data && data.length > 0) {
        console.log('🔍 Exemple de processus (premier) :', data[0]);
        console.log('🔍 Variables disponibles :', data[0].variables);
      }
      setSearchResults(data || []);
    } catch (err) {
      console.error("Erreur lors de la récupération des données :", err);
      setError("Impossible de charger les données du serveur de monitoring (Vérifiez les ports 8081 / 8089).");
      setSearchResults([]); 
    } finally {
      setIsSearching(false);
    }
  };

  const handleSort = (key) => {
    if (sortKey === key) {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      setSortKey(key);
      setSortDir('asc');
    }
  };

  const getSlaLevel = (startDate, state) => {
    if (!startDate || state !== 1) return null;
    const diff = (new Date() - new Date(startDate)) / 60000;
    if (diff < 60) return 'fast';
    if (diff < 240) return 'warn';
    return 'critical';
  };

  const toggleArchive = (id) => {
    const newArchived = archivedIds.includes(id)
      ? archivedIds.filter(a => a !== id)
      : [...archivedIds, id];
    setArchivedIds(newArchived);
    localStorage.setItem('archived_consultation', JSON.stringify(newArchived));
  };

  let filteredResults = showArchived
    ? [...searchResults]
    : searchResults.filter(r => !archivedIds.includes(r.id));

  if (slaFilter !== 'all') {
    filteredResults = filteredResults.filter(r => getSlaLevel(r.date, r.state) === slaFilter);
  }
  if (operatorFilter !== 'all') {
    filteredResults = filteredResults.filter(r => {
      const msisdn = r.variables?.msisdn || r.variables?.phoneNumber || '';
      const op = getOperator(msisdn);
      return op && op.short === operatorFilter;
    });
  }

  // Filtre par période
  if (periodFilter !== 'all') {
    const now = new Date();
    const periods = { today: 0, week: 7, month: 30, year: 365 };
    const days = periods[periodFilter] ?? 0;
    if (days === 0) {
      filteredResults = filteredResults.filter(r => {
        if (!r.date) return false;
        const d = new Date(r.date);
        return d.toDateString() === now.toDateString();
      });
    } else {
      const cutoff = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
      filteredResults = filteredResults.filter(r => {
        if (!r.date) return false;
        return new Date(r.date) >= cutoff;
      });
    }
  }

  if (sortKey) {
    filteredResults.sort((a, b) => {
      let va, vb;
      if (sortKey === 'id') { va = a.id; vb = b.id; }
      else if (sortKey === 'state') { va = a.state; vb = b.state; }
      else if (sortKey === 'date') { va = a.date; vb = b.date; }
      else if (sortKey === 'msisdn') { va = a.variables?.msisdn || ''; vb = b.variables?.msisdn || ''; }
      else if (sortKey === 'phone') { va = a.variables?.phoneNumber || ''; vb = b.variables?.phoneNumber || ''; }
      else if (sortKey === 'contract') { va = a.variables?.contractCode || ''; vb = b.variables?.contractCode || ''; }
      else if (sortKey === 'operator') {
        const aOp = getOperator(a.variables?.msisdn || a.variables?.phoneNumber);
        const bOp = getOperator(b.variables?.msisdn || b.variables?.phoneNumber);
        va = aOp ? aOp.name : ''; vb = bOp ? bOp.name : '';
      }
      else if (sortKey === 'duration') {
        va = a.date && a.state === 1 ? new Date() - new Date(a.date) : 0;
        vb = b.date && b.state === 1 ? new Date() - new Date(b.date) : 0;
      }
      if (va < vb) return sortDir === 'asc' ? -1 : 1;
      if (va > vb) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });
  }

  // Logique de calcul de la pagination
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredResults.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredResults.length / itemsPerPage);

  const handlePageChange = (pageNumber) => {
    if (pageNumber >= 1 && pageNumber <= totalPages) {
      setCurrentPage(pageNumber);
    }
  };

  // --- LOGIQUE D'EXPORTATION CSV ---
  const exportToCSV = () => {
    const data = filteredResults;
    if (!data || data.length === 0) return;

    const headers = ["INSTANCE ID", "STATUS", "OPÉRATEUR", "MSISDN", "PHONE NUMBER", "CODE CONTRAT", "START DATE"];
    
    const rows = data.map(row => [
      row.id,
      getStatusLabel(row.state),
      (() => { const o = getOperator(row.variables?.msisdn || row.variables?.phoneNumber); return o ? o.name : 'N/A'; })(),
      row.variables?.msisdn || row.variables?.phoneNumber || 'N/A',
      row.variables?.phoneNumber || row.variables?.msisdn || 'N/A',
      row.variables?.contractCode || row.variables?.contractType || row.variables?.contract || 'N/A',
      formatDate(row.date)
    ]);

    const csvContent = [
      headers.join(","),
      ...rows.map(row => row.map(val => `"${val}"`).join(","))
    ].join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `demandes_portabilite_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // --- LOGIQUE D'EXPORTATION PDF ---
  const exportToPDF = () => {
    const data = filteredResults;
    if (!data || data.length === 0) return;
    try {

    const doc = new jsPDF({ orientation: "landscape" });

    // En-tête du document
    doc.setFontSize(16);
    doc.setTextColor(51, 51, 51);
    doc.text("Rapport - Demandes de Portabilité", 14, 15);
    
    doc.setFontSize(10);
    doc.setTextColor(102, 102, 102);
    doc.text(`Généré le : ${new Date().toLocaleString()}`, 14, 22);

    const headers = [["INSTANCE ID", "STATUS", "OPÉRATEUR", "MSISDN", "PHONE", "CODE CONTRAT", "START DATE"]];
    
    const rows = data.map(row => [
      row.id,
      getStatusLabel(row.state),
      (() => { const o = getOperator(row.variables?.msisdn || row.variables?.phoneNumber); return o ? o.name : 'N/A'; })(),
      row.variables?.msisdn || row.variables?.phoneNumber || 'N/A',
      row.variables?.phoneNumber || row.variables?.msisdn || 'N/A',
      row.variables?.contractCode || row.variables?.contractType || row.variables?.contract || 'N/A',
      formatDate(row.date)
    ]);

    autoTable(doc, {
      startY: 28,
      head: headers,
      body: rows,
      theme: "striped",
      headStyles: { 
        fillColor: [255, 102, 0],
        textColor: [255, 255, 255],
        fontStyle: "bold"
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245]
      },
      styles: {
        fontSize: 9,
        cellPadding: 4
      }
    });

    doc.save(`demandes_portabilite_${new Date().toISOString().split('T')[0]}.pdf`);
    } catch (err) {
      console.error('Erreur export PDF:', err);
      alert('Erreur lors de l\'export PDF. Voir console pour détails.');
    }
  };

  const extractCrmId = (variables) => {
    if (!variables) return 'N/A';
    
    // Chercher CRM ID dans tous les noms possibles
    if (variables.crmId) return variables.crmId;
    if (variables.idcrm) return variables.idcrm;
    if (variables.refCrm) return variables.refCrm;
    if (variables.crmID) return variables.crmID;

    // Chercher dans tempResponse (parfois le backend met les infos là)
    if (variables.tempResponse) {
      try {
        if (typeof variables.tempResponse === 'string') {
          const parsed = JSON.parse(variables.tempResponse);
          return parsed.idcrm || parsed.crmId || parsed.refCrm || 'N/A';
        }
        if (typeof variables.tempResponse === 'object') {
          return variables.tempResponse.idcrm || variables.tempResponse.crmId || variables.tempResponse.refCrm || 'N/A';
        }
      } catch (e) {
        console.warn("Erreur lors du parsing de tempResponse", e);
      }
    }
    
    return 'N/A';
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchData(searchForm);
  };

  const fetchLifecycle = async (processId) => {
    setLoadingLifecycle(true);
    setLifecycleSteps([]);
    try {
      const res = await fetch(`http://localhost:8089/api/monitoring/instances/${processId}/lifecycle`);
      if (res.ok) {
        const data = await res.json();
        setLifecycleSteps(data || []);
      }
    } catch (e) {
      console.warn('Lifecycle indisponible pour instance', processId);
    } finally {
      setLoadingLifecycle(false);
    }
  };

  const checkFraud = async (process) => {
    const cin = process.variables?.cinNumber || '';
    const name = process.variables?.clientName || '';
    if (!cin) return;
    setFraudLoading(true);
    setFraudData(null);
    try {
      const res = await fetch('http://localhost:8081/api/fraud/check', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cinNumber: cin, clientName: name })
      });
      if (res.ok) setFraudData(await res.json());
    } catch {}
    setFraudLoading(false);
  };

  const openDetails = (process) => {
    setSelectedProcess(process);
    setIsModalOpen(true);
    setFraudData(null);
    fetchLifecycle(process.id);
    fetchComments(process.id);
    fetchSmsLogs(process.id);
    fetchFeedback(process.id);
    checkFraud(process);
  };

  const fetchSmsLogs = async (processId) => {
    try {
      const res = await fetch(`http://localhost:8081/api/sms/logs/${processId}`);
      if (res.ok) setSmsLogs(await res.json());
    } catch {}
  };

  const fetchFeedback = async (processId) => {
    setFeedbackLoading(true);
    setFeedbackData(null);
    setFeedbackRating(0);
    setFeedbackComment('');
    try {
      const res = await fetch(`http://localhost:8081/api/feedback/${processId}`);
      if (res.ok) {
        const data = await res.json();
        if (data.exists !== false) setFeedbackData(data);
      }
    } catch {}
    setFeedbackLoading(false);
  };

  const submitFeedback = async () => {
    if (!selectedProcess || feedbackRating === 0) return;
    setFeedbackSubmitting(true);
    try {
      const res = await fetch(`http://localhost:8081/api/feedback/submit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          processInstanceId: selectedProcess.id,
          msisdn: selectedProcess.variables?.msisdn || selectedProcess.variables?.phoneNumber || '',
          rating: feedbackRating,
          comment: feedbackComment,
          source: 'agent'
        })
      });
      if (res.ok) {
        const saved = await res.json();
        setFeedbackData(saved);
      }
    } catch {}
    setFeedbackSubmitting(false);
  };

  const fetchComments = async (processId) => {
    setCommentsLoading(true);
    try {
      const res = await fetch(`http://localhost:8081/api/comments/${processId}`);
      if (res.ok) setComments(await res.json());
    } catch {}
    setCommentsLoading(false);
  };

  const addComment = async () => {
    if (!newComment.trim() || !selectedProcess) return;
    try {
      const res = await fetch(`http://localhost:8081/api/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ processInstanceId: selectedProcess.id, author: commentAuthor, text: newComment.trim() })
      });
      if (res.ok) {
        const saved = await res.json();
        setComments(prev => [...prev, saved]);
        setNewComment('');
      }
    } catch {}
  };

  const closeDetails = () => {
    setIsModalOpen(false);
    setSelectedProcess(null);
    setLifecycleSteps([]);
    setComments([]);
    setNewComment('');
    setSmsLogs([]);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return isNaN(date.getTime()) ? dateString : date.toLocaleString('fr-FR', {
      day: 'numeric', month: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  const getStatusLabel = (state) => {
    if (state === 1) return 'En cours';
    if (state === 2) return 'Terminé';
    if (state === 3) return 'Annulé';
    return `Statut ${state}`;
  };

  const computeDuration = (startDate, state) => {
    if (!startDate || !state) return null;
    if (state !== 1) return { label: '—', level: 'sla-done' };
    const start = new Date(startDate);
    if (isNaN(start.getTime())) return null;
    const now = new Date();
    const diffMs = now - start;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const mins = diffMins % 60;

    let label, level;
    if (diffHours < 1) {
      label = `${diffMins} min`;
      level = 'sla-fast';
    } else if (diffHours < 4) {
      label = `${diffHours}h ${String(mins).padStart(2, '0')}`;
      level = 'sla-warn';
    } else {
      label = `${diffHours}h ${String(mins).padStart(2, '0')}`;
      level = 'sla-critical';
    }
    return { label, level };
  };

  return (
    <div className="consultation-container">
      <div className="consultation-header">

  <h1 className="consultation-title">
    Consultation demandes de portabilité
  </h1>

  <button
    className="btn-refresh"
    onClick={() => fetchData(searchForm)}
    disabled={isSearching}
  >
    {isSearching
      ? '⏳ Actualisation...'
      : '🔄 Refresheur'}
  </button>

</div>

      {/* Formulaire de Recherche */}
      <div className="search-panel">
        <form className="search-form-inline" onSubmit={handleSearch}>
          <div className="search-row">
            <div className="search-field">
              <label>Process ID</label>
              <input type="text" name="processId" value={searchForm.processId} onChange={handleInputChange} placeholder="PID" />
            </div>
            <div className="search-field">
              <label>Réf CRM</label>
              <input type="text" name="idCrm" value={searchForm.idCrm} onChange={handleInputChange} placeholder="NP-XXXX" />
            </div>
            <div className="search-field">
              <label>Status</label>
              <select name="status" value={searchForm.status} onChange={handleInputChange} className="form-control-select">
                <option value="">Tous</option>
                <option value="1">En cours (1)</option>
                <option value="2">Terminé (2)</option>
                <option value="3">Annulé (3)</option>
              </select>
            </div>
          </div>

          <div className="search-row">
            <div className="search-field">
              <label>MSISDN</label>
              <input type="text" name="msisdn" value={searchForm.msisdn} onChange={handleInputChange} placeholder="216..." />
            </div>
            <div className="search-field">
              <label>phoneNumber</label>
              <input type="text" name="phoneNumber" value={searchForm.phoneNumber} onChange={handleInputChange} placeholder="Numéro" />
            </div>
            <div className="search-field">
              <label>contractCode</label>
              <input type="text" name="contractCode" value={searchForm.contractCode} onChange={handleInputChange} placeholder="CC-99" />
            </div>
          </div>

          <div className="search-row" style={{ alignItems: 'flex-end' }}>
            <div className="search-field">
              <label>Début</label>
              <input type="date" name="startDate" value={searchForm.startDate} onChange={handleInputChange} />
            </div>
            <div className="search-field">
              <label>Fin</label>
              <input type="date" name="endDate" value={searchForm.endDate} onChange={handleInputChange} />
            </div>
            <div className="search-button-container">
              <button type="submit" className="btn-rechercher" disabled={isSearching}>
                {isSearching ? 'Recherche...' : 'Rechercher'}
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Tableau des Résultats */}
      <div className="table-panel">
        {error && <div className="error-message-small">{error}</div>}

        {/* Filtre SLA */}
        <div className="sla-filter-bar">
          <button className={`sla-filter-btn ${slaFilter === 'all' ? 'active' : ''}`} onClick={() => setSlaFilter('all')}>
            Tout <span className="sla-count">{searchResults.length}</span>
          </button>
          <button className={`sla-filter-btn sla-fast ${slaFilter === 'fast' ? 'active' : ''}`} onClick={() => setSlaFilter('fast')}>
            ⚡ Rapide <span className="sla-count">{searchResults.filter(r => getSlaLevel(r.date, r.state) === 'fast').length}</span>
          </button>
          <button className={`sla-filter-btn sla-warn ${slaFilter === 'warn' ? 'active' : ''}`} onClick={() => setSlaFilter('warn')}>
            ⏳ Attention <span className="sla-count">{searchResults.filter(r => getSlaLevel(r.date, r.state) === 'warn').length}</span>
          </button>
          <button className={`sla-filter-btn sla-critical ${slaFilter === 'critical' ? 'active' : ''}`} onClick={() => setSlaFilter('critical')}>
            🔴 Critique <span className="sla-count">{searchResults.filter(r => getSlaLevel(r.date, r.state) === 'critical').length}</span>
          </button>
        </div>

        {/* Filtre Opérateur */}
        <div className="operator-filter-bar">
          {['all', 'ORN', 'OOR', 'TT'].map(op => (
            <button key={op}
              className={`op-filter-btn ${operatorFilter === op ? 'active' : ''}`}
              style={op !== 'all' ? { '--op-color': OPERATORS[op === 'ORN' ? '5' : op === 'OOR' ? '2' : '4']?.color } : {}}
              onClick={() => setOperatorFilter(op)}>
              {op === 'all' ? '📱 Tous' : op === 'ORN' ? '🍊 Orange' : op === 'OOR' ? '🔴 Ooredoo' : '🔵 TT'}
              <span className="op-count">
                {op === 'all' ? searchResults.length
                  : searchResults.filter(r => {
                      const m = r.variables?.msisdn || r.variables?.phoneNumber || '';
                      const o = getOperator(m);
                      return o && o.short === op;
                    }).length}
              </span>
            </button>
          ))}
        </div>

        {/* Filtre Période - supprimé, remplacé par le select en haut à droite */}

        <table className="data-table">
          <thead>
            <tr>
              <th className={sortKey === 'id' ? 'sorted' : ''} onClick={() => handleSort('id')} style={{cursor:'pointer'}}>
                Instance ID {sortKey === 'id' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th className={sortKey === 'state' ? 'sorted' : ''} onClick={() => handleSort('state')} style={{cursor:'pointer'}}>
                Status {sortKey === 'state' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th className={sortKey === 'operator' ? 'sorted' : ''} onClick={() => handleSort('operator')} style={{cursor:'pointer'}}>
                Opérateur {sortKey === 'operator' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th className={sortKey === 'msisdn' ? 'sorted' : ''} onClick={() => handleSort('msisdn')} style={{cursor:'pointer'}}>
                MSISDN {sortKey === 'msisdn' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th onClick={() => handleSort('phone')} style={{cursor:'pointer'}}>
                Phone {sortKey === 'phone' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th onClick={() => handleSort('contract')} style={{cursor:'pointer'}}>
                Code Contrat {sortKey === 'contract' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th className={sortKey === 'date' ? 'sorted' : ''} onClick={() => handleSort('date')} style={{cursor:'pointer'}}>
                Start Date {sortKey === 'date' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th className={sortKey === 'duration' ? 'sorted' : ''} onClick={() => handleSort('duration')} style={{cursor:'pointer'}}>
                Durée / SLA {sortKey === 'duration' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
              </th>
              <th className="details-col">Details</th>
              <th className="archive-col">Archiver</th>
            </tr>
          </thead>
          <tbody>
            {currentItems && currentItems.length > 0 ? (
              currentItems.map((row, index) => (
                <tr key={row.id || index} className={archivedIds.includes(row.id) ? 'row-archived' : ''}>
                  <td>{row.id}</td>
                  <td>
                    <span className={`status-badge-compact status-${row.state}`}>
                      {getStatusLabel(row.state)}
                    </span>
                  </td>
                  <td>
                    {(() => {
                      const op = getOperator(row.variables?.msisdn || row.variables?.phoneNumber);
                      return op ? (
                        <span className="op-badge" style={{ '--op-color': op.color, borderColor: op.color, color: op.color }}>
                          {op.name}
                        </span>
                      ) : <span className="sla-na">—</span>;
                    })()}
                  </td>
                  <td>{row.variables?.msisdn || row.variables?.phoneNumber || 'N/A'}</td>
                  <td>{row.variables?.phoneNumber ? row.variables.phoneNumber.replace(/^216/, '') : (row.variables?.msisdn ? row.variables.msisdn.replace(/^216/, '') : 'N/A')}</td>
                  <td>{row.variables?.contractCode || row.variables?.contractType || row.variables?.contract || 'N/A'}</td>
                  <td>{formatDate(row.date)}</td>
                  <td>
                    {(() => {
                      const dur = computeDuration(row.date, row.state);
                      return dur ? (
                        <span className={`sla-badge ${dur.level}`}>{dur.label}</span>
                      ) : (
                        <span className="sla-na">—</span>
                      );
                    })()}
                  </td>
                  <td className="details-col">
                    <button className="btn-details-arrow" onClick={() => openDetails(row)}>➔</button>
                  </td>
                  <td className="archive-col">
                    <button
                      className={`btn-archive ${archivedIds.includes(row.id) ? 'archived' : ''}`}
                      onClick={() => toggleArchive(row.id)}
                      title={archivedIds.includes(row.id) ? 'Restaurer' : 'Archiver'}
                    >
                      {archivedIds.includes(row.id) ? '✓ Archivé' : 'Archiver'}
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="10" style={{ textAlign: 'center', padding: '15px', color: '#aaa' }}>
                  Aucune donnée réelle trouvée pour cette recherche.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {/* --- ZONE FOOTER ACTIONS (Pagination + Boutons d'exportation) --- */}
        {searchResults.length > 0 && (
          <div className="table-footer-actions">
            
            {/* Pagination simple: < [page] / [total] > */}
            {totalPages > 1 && (
              <div className="pagination">
                <div className="pagination-inner">
                  <button className="page-btn" onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 1}>
                    &lt;
                  </button>
                  <span className="page-info">{currentPage} / {totalPages}</span>
                  <button className="page-btn" onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages}>
                    &gt;
                  </button>
                </div>
              </div>
            )}

            {/* Toggle archives */}
            <button
              className={`btn-archive-toggle ${showArchived ? 'active' : ''}`}
              onClick={() => setShowArchived(!showArchived)}
            >
              {showArchived ? '🙈 Masquer archivés' : '📦 Voir archivés'}
              {archivedIds.length > 0 && <span className="archive-count">{archivedIds.length}</span>}
            </button>

            {/* Filtre période + export */}
            <div className="export-buttons-group">
              <select className="period-select" value={periodFilter} onChange={e => { setPeriodFilter(e.target.value); setCurrentPage(1); }}>
                <option value="all">📅 Toute période</option>
                <option value="today">📅 Aujourd'hui</option>
                <option value="week">📅 7 jours</option>
                <option value="year">📅 365 jours</option>
              </select>
              <button onClick={exportToCSV} className="btn-export csv" title="Exporter en format Excel/CSV">
                📄 Exporter CSV
              </button>
              <button onClick={exportToPDF} className="btn-export pdf" title="Exporter en format PDF">
                📕 Exporter PDF
              </button>
            </div>

          </div>
        )}
      </div>

      {/* Modal de Détails */}
      {isModalOpen && selectedProcess && (
        <div className="modal-overlay" onClick={closeDetails}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>🔍 Détails de la Demande de Portabilité</h3>
              <button className="modal-close" onClick={closeDetails}>&times;</button>
            </div>
            <div className="modal-body">
              
              {/* SECTION 1: Informations du Processus */}
              <div className="detail-section">
                <div className="detail-section-title">📋 Informations du Processus</div>
                <div className="detail-grid">
                  <div className="detail-group">
                    <label>Process ID jBPM :</label>
                    <span className="detail-value">{selectedProcess.id}</span>
                  </div>
                  <div className="detail-group">
                    <label>Status :</label>
                    <span>
                      <span className={`status-badge-compact status-${selectedProcess.state}`}>
                        {getStatusLabel(selectedProcess.state)}
                      </span>
                    </span>
                  </div>
                  <div className="detail-group">
                    <label>Type :</label>
                    <span className="detail-value">
                      {selectedProcess.containerId?.includes('out') ? 'Portabilité OUT' : 'Portabilité IN'}
                    </span>
                  </div>
                  <div className="detail-group">
                    <label>Date Début :</label>
                    <span className="detail-value">{formatDate(selectedProcess.date)}</span>
                  </div>
                  <div className="detail-group">
                    <label>Version :</label>
                    <span className="detail-value">{selectedProcess.processVersion || '1.0'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Conteneur KIE :</label>
                    <span className="detail-value" style={{fontSize: '11px'}}>{selectedProcess.containerId || 'N/A'}</span>
                  </div>
                </div>
              </div>

              {/* SECTION 2: Informations Client */}
              <div className="detail-section">
                <div className="detail-section-title">👤 Informations Client</div>
                <div className="detail-grid">
                  <div className="detail-group">
                    <label>Nom Client :</label>
                    <span className="detail-value">{selectedProcess.variables?.clientName || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>ID Client :</label>
                    <span className="detail-value">{selectedProcess.variables?.idClient || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Réf CRM (corrélation) :</label>
                    <span className="detail-value highlight">{selectedProcess.variables?.refCrm || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Process ID jBPM :</label>
                    <span className="detail-value">{selectedProcess.id}</span>
                  </div>
                  <div className="detail-group">
                    <label>Code Contrat :</label>
                    <span className="detail-value">{selectedProcess.variables?.contractCode || selectedProcess.variables?.contractType || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>CIN :</label>
                    <span className="detail-value">{selectedProcess.variables?.cinNumber || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Type Identité :</label>
                    <span className="detail-value">{selectedProcess.variables?.typeIdentite || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Marché :</label>
                    <span className="detail-value">
                      <span className="market-badge">{selectedProcess.variables?.marche || 'N/A'}</span>
                    </span>
                  </div>
                </div>
              </div>

              {/* SECTION FRAUDE */}
              <div className="detail-section">
                <div className="detail-section-title">⚠️ Détection de Fraude</div>
                {fraudLoading ? (
                  <div className="timeline-loading">Vérification en cours...</div>
                ) : fraudData ? (
                  <div className="fraud-content">
                    {fraudData.fraud ? (
                      <>
                        <div className={`fraud-banner ${fraudData.blocked ? 'fraud-blocked' : 'fraud-warning'}`}>
                          <strong>{fraudData.blocked ? '⛔ RISQUE ÉLEVÉ' : '⚠️ ANOMALIE DÉTECTÉE'}</strong>
                          <span>{fraudData.message}</span>
                        </div>
                        {fraudData.alerts?.map((a, i) => (
                          <div key={i} className="fraud-alert-item">
                            <span className={`fraud-severity fraud-${a.severity?.toLowerCase()}`}>{a.severity}</span>
                            <span>{a.message}</span>
                          </div>
                        ))}
                      </>
                    ) : (
                      <div className="fraud-clean">
                        <span>✅ Aucune anomalie détectée</span>
                      </div>
                    )}
                    <div className="fraud-score">
                      Score de risque: <strong>{fraudData.riskScore || 0}/100</strong>
                    </div>
                  </div>
                ) : (
                  <div className="fraud-clean">
                    <span>Données insuffisantes pour la vérification</span>
                  </div>
                )}
              </div>

              {/* SECTION TIMELINE: Cycle de Vie */}
              <div className="detail-section">
                <div className="detail-section-title">🔄 Cycle de Vie</div>
                {loadingLifecycle ? (
                  <div className="timeline-loading">Chargement du cycle de vie...</div>
                ) : lifecycleSteps.length > 0 ? (
                  <div className="timeline">
                    {lifecycleSteps.map((step, i) => (
                      <div key={i} className={`timeline-item ${step.status === 'ACTIVE' ? 'active' : 'completed'}`}>
                        <div className="timeline-marker">
                          <div className="timeline-dot" />
                          {i < lifecycleSteps.length - 1 && <div className="timeline-line" />}
                        </div>
                        <div className="timeline-content">
                          <div className="timeline-step-name">{step.step}</div>
                          <div className="timeline-step-date">{step.start}</div>
                          <span className={`timeline-badge ${step.status === 'ACTIVE' ? 'badge-active' : 'badge-done'}`}>
                            {step.status === 'ACTIVE' ? '● En cours' : '✓ Terminé'}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="timeline-empty">Aucune étape disponible</div>
                )}
              </div>

              {/* SECTION COMMENTAIRES */}
              <div className="detail-section">
                <div className="detail-section-title">💬 Commentaires</div>
                {commentsLoading ? (
                  <div className="timeline-loading">Chargement...</div>
                ) : (
                  <div className="comments-container">
                    {comments.length === 0 && <div className="comments-empty">Aucun commentaire</div>}
                    {comments.map((c, i) => (
                      <div key={c.id || i} className="comment-item">
                        <div className="comment-header">
                          <strong>{c.author}</strong>
                          <span className="comment-date">{new Date(c.createdAt).toLocaleString('fr-FR')}</span>
                        </div>
                        <div className="comment-text">{c.text}</div>
                      </div>
                    ))}
                    <div className="comment-form">
                      <input className="comment-author-input" value={commentAuthor} onChange={e => setCommentAuthor(e.target.value)}
                        placeholder="Votre nom" maxLength="50" />
                      <div className="comment-input-row">
                        <input className="comment-input" value={newComment} onChange={e => setNewComment(e.target.value)}
                          placeholder="Ajouter un commentaire..." maxLength="2000"
                          onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); addComment(); } }} />
                        <button className="btn-comment-send" onClick={addComment} disabled={!newComment.trim()}>Envoyer</button>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* SECTION SMS AUTO */}
              <div className="detail-section">
                <div className="detail-section-title">📱 SMS Automatique</div>
                <div className="sms-section">
                  <p className="sms-info">
                    Client : <strong>{selectedProcess.variables?.clientName || 'N/A'}</strong>
                    <br />
                    Numéro : <strong>{selectedProcess.variables?.msisdn || selectedProcess.variables?.phoneNumber || 'N/A'}</strong>
                  </p>
                  {smsLogs.length > 0 ? (
                    <div>
                      {smsLogs.map((log, i) => (
                        <div key={i} className={`sms-autosent ${log.type === 'FEEDBACK_REQUEST' ? 'sms-feedback' : ''}`} style={i > 0 ? {marginTop:8} : {}}>
                          <div className="sms-log-header">
                            {log.type === 'FEEDBACK_REQUEST' ? '📝 Demande de feedback envoyée' : '✅ SMS de félicitations envoyé'}
                            <span className="sms-log-time">le {new Date(log.sentAt).toLocaleString('fr-FR')}</span>
                          </div>
                          <div className="sms-log-item" style={{marginTop:4}}>
                            <span className={`sms-status-dot ${log.status === 'SENT' ? 'sent' : 'failed'}`} />
                            <span>{log.msisdn}</span>
                            <span className="sms-msg-preview">{log.message?.substring(0, 50)}...</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : selectedProcess.state === 2 ? (
                    <div className="sms-waiting">⏳ Envoi automatique en cours...</div>
                  ) : (
                    <div className="sms-waiting">⏳ Envoyé automatiquement quand la demande passe à <strong>Terminé</strong></div>
                  )}
                </div>
              </div>

              {/* SECTION FEEDBACK CLIENT */}
              <div className="detail-section">
                <div className="detail-section-title">⭐ Avis Client</div>
                {feedbackLoading ? (
                  <div className="timeline-loading">Chargement...</div>
                ) : feedbackData ? (
                  <div className="feedback-submitted">
                    <div className="feedback-stars-display">
                      {[1,2,3,4,5].map(s => (
                        <span key={s} className={`star ${s <= feedbackData.rating ? 'star-filled' : 'star-empty'}`}>★</span>
                      ))}
                      <span className="feedback-rating-label">
                        {feedbackData.rating === 5 ? 'Excellent' : feedbackData.rating === 4 ? 'Très bien' : feedbackData.rating === 3 ? 'Moyen' : feedbackData.rating === 2 ? 'Médiocre' : 'Très mauvais'}
                      </span>
                    </div>
                    {feedbackData.comment && <p className="feedback-comment">"{feedbackData.comment}"</p>}
                    <p className="feedback-date">
                      Soumis par le client le {new Date(feedbackData.createdAt).toLocaleString('fr-FR')}
                      {feedbackData.source === 'agent' && <span> (agent)</span>}
                    </p>
                  </div>
                ) : selectedProcess.state === 2 ? (
                  <div className="feedback-pending">
                    🔗 Lien de feedback envoyé au client par SMS
                    <br /><small style={{color:'#aaa'}}>Le client peut donner son avis via le lien reçu</small>
                  </div>
                ) : (
                  <div className="feedback-pending">⏳ Feedback disponible quand la demande passe à <strong>Terminé</strong></div>
                )}
              </div>

              {/* SECTION 3: Données de Portabilité */}
              <div className="detail-section">
                <div className="detail-section-title">📞 Données de Portabilité</div>
                <div className="detail-grid">
                  <div className="detail-group">
                    <label>MSISDN (avec 216) :</label>
                    <span className="detail-value highlight">{selectedProcess.variables?.msisdn || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Phone Number (sans 216) :</label>
                    <span className="detail-value">{selectedProcess.variables?.phoneNumber || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Code RIO :</label>
                    <span className="detail-value">{selectedProcess.variables?.rioCode || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Code Contrat :</label>
                    <span className="detail-value">{selectedProcess.variables?.contractCode || selectedProcess.variables?.contractType || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Numéro Orange (provisoire) :</label>
                    <span className="detail-value">{selectedProcess.variables?.numeroOrange || 'N/A'}</span>
                  </div>
                  <div className="detail-group">
                    <label>Nom Process :</label>
                    <span className="detail-value">{selectedProcess.processName || 'N/A'}</span>
                  </div>
                </div>
              </div>

            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={closeDetails}>Fermer</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );  
}