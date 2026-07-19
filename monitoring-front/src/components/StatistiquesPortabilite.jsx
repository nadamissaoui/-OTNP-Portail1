import React, { useEffect, useState } from 'react';
import LifecycleTimeline from "./LifecycleTimeline";

import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
 CartesianGrid,
  ResponsiveContainer,
  LineChart,
  Line
} from 'recharts';
import './Statistique.css';
import LifecycleChart from './LifecycleChart';
import { logAnalysisService } from '../services/logAnalysisService';

const COLORS_PIE = ['#00C49F', '#FF8042', '#0088FE'];
const STATUS_LABEL = { active: 'Active', completed: 'Complété', aborted: 'Aborted' };
const STATUS_CODE  = { active: 1, completed: 2, aborted: 3 };


export default function StatistiquesPortabilite() {

  const [stats, setStats] = useState(null);
  const [monthlyStats, setMonthly] = useState([]);
  const [seasonalData, setSeasonalData] = useState([]);
const [trendMessage, setTrendMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [drilldown, setDrilldown] = useState(null);
  const [drillInstances, setDrillInstances] = useState([]);
  const [drillLoading, setDrillLoading] = useState(false);

  const [activeInstances, setActiveInstances] = useState([]);
  const [prediction, setPrediction] = useState(null);

  const [currentPage, setCurrentPage] = useState(1);
  const [selectedProcessId, setSelectedProcessId] = useState(null);
  const [selectedProcess, setSelectedProcess] = useState(null);
  const [aiPanel, setAiPanel] = useState(null);
  const [aiLoadingId, setAiLoadingId] = useState(null);
  const [aiError, setAiError] = useState(null);
  const [activeStatusFilter, setActiveStatusFilter] = useState('all');
  const itemsPerPage = 10;

  useEffect(() =>{
    fetch('http://localhost:8089/api/monitoring/statistics/performance')
      .then(r => r.json())
      .then(d => { setStats(d); setLoading(false); })
      .catch(e => { setError(e.message); setLoading(false); });

    fetch('http://localhost:8089/api/monitoring/statistics/monthly')
      .then(r => r.json())
      .then(d => setMonthly(d))
      .catch(e => console.error(e));
      fetch("http://localhost:8089/api/monitoring/statistics/seasonal")
  .then(r => r.json())
  .then(data => {

    setSeasonalData(data);

    const values = data.map(d => d.total);

    const filtered = values.filter(v => v > 0);

    if(filtered.length >= 3){

      const last = filtered.slice(-3);

      if(last[2] > last[1] && last[1] > last[0]){

        setTrendMessage("📈 Les demandes augmentent depuis les 3 derniers mois.");

      }
      else if(last[2] < last[1] && last[1] < last[0]){

        setTrendMessage("📉 Les demandes diminuent depuis les 3 derniers mois.");

      }
      else{

        setTrendMessage("📊 Activité stable.");

      }

    }

  })
  .catch(console.error);
fetch('http://localhost:8089/api/monitoring/instances/by-status?status=1&type=&page=0&size=1000')
.then(r => r.json())
.then(active => {
  setActiveInstances(Array.isArray(active) ? active : []);
})
.catch(e => console.error(e));
fetch('http://localhost:8081/api/monitoring/predict')
.then(r => r.json())
.then(setPrediction)
.catch(() => setPrediction({totalPredicted: 0}));

  }, []);

  const handleCellClick = (statusKey, type) => {
    const statusCode = STATUS_CODE[statusKey];
    const label = `${STATUS_LABEL[statusKey]} — Portability ${type}`;

    setDrilldown({ status: statusCode, type, label });
    setDrillLoading(true);
    setDrillInstances([]);

    fetch(`http://localhost:8089/api/monitoring/instances/by-status?status=${statusCode}&type=${type}&page=0&size=50`)
      .then(r => r.json())
      .then(d => { setDrillInstances(d); setDrillLoading(false); })
      .catch(() => setDrillLoading(false));
  };

  const closeDrilldown = () => setDrilldown(null);

  const prepareData = (obj) => [
    { name: 'Completed', value: Number(obj?.completed || 0) },
    { name: 'Aborted', value: Number(obj?.aborted || 0) },
    { name: 'Active', value: Number(obj?.active || 0) },
  ];

  if (loading) return <div>Chargement...</div>;
  if (error) return <div>{error}</div>;
  if (!stats) return null;

  const IN = stats.PortabilityIN || {};
  const OUT = stats.PortabilityOUT || {};

  // Mois courant vs mois prÃ©cÃ©dent
  const monthsWithData = monthlyStats.filter(m => m.PortabilityIN > 0 || m.PortabilityOUT > 0);
  const currentMonth = monthsWithData.length > 0 ? monthsWithData[monthsWithData.length - 1] : null;
  const prevMonth = monthsWithData.length > 1 ? monthsWithData[monthsWithData.length - 2] : null;
  const compareData = prevMonth && currentMonth ? [
    { name: prevMonth.month, IN: prevMonth.PortabilityIN, OUT: prevMonth.PortabilityOUT },
    { name: currentMonth.month, IN: currentMonth.PortabilityIN, OUT: currentMonth.PortabilityOUT }
  ] : [];

  const rows = [
    { key: 'total', label: 'Total', inVal: IN.total, outVal: OUT.total, clickable: false },
    { key: 'completed', label: 'Complété', inVal: IN.completed, outVal: OUT.completed, clickable: true },
    { key: 'aborted', label: 'Aborted', inVal: IN.aborted, outVal: OUT.aborted, clickable: true },
    { key: 'active', label: 'Active', inVal: IN.active, outVal: OUT.active, clickable: true },
  ];

  // 🔥 ONLY ADD CSV FUNCTION
  const exportNotOkCSV = () => {
    const notOkData = activeInstances.filter(inst =>
      inst.statusTime?.trim().toLowerCase() !== 'ok'
    );

    const csvRows = [
      ["ID", "Process", "Type", "Date", "Node", "Status"].join(","),
      ...notOkData.map(inst =>
        [
          inst.id,
          inst.processId,
          inst.type,
          inst.startDate,
          inst.nodeName,
          inst.statusTime
        ].join(",")
      )
    ];

    const blob = new Blob([csvRows.join("\n")], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = "not_ok_instances.csv";
    a.click();

    URL.revokeObjectURL(url);
  };

  const isNotOk = (inst) =>
    inst.statusTime?.trim().toLowerCase() !== 'ok';

  const isOk = (inst) =>
    inst.statusTime?.trim().toLowerCase() === 'ok';

  const activeStatusCounts = activeInstances.reduce(
    (acc, inst) => {
      if (isOk(inst)) {
        acc.ok += 1;
      } else {
        acc.notOk += 1;
      }
      acc.all += 1;
      return acc;
    },
    { all: 0, ok: 0, notOk: 0 }
  );

  const filteredActiveInstances = activeInstances.filter(inst => {
    if (activeStatusFilter === 'ok') return isOk(inst);
    if (activeStatusFilter === 'not-ok') return isNotOk(inst);
    return true;
  });

  const sortedActiveInstances = [...filteredActiveInstances].sort((a, b) => {
    const aRank = isOk(a) ? 0 : 1;
    const bRank = isOk(b) ? 0 : 1;
    return aRank - bRank;
  });

  const indexOfLast = currentPage * itemsPerPage;
  const indexOfFirst = indexOfLast - itemsPerPage;
  const currentItems = sortedActiveInstances.slice(indexOfFirst, indexOfLast);
  const totalPages = Math.max(1, Math.ceil(sortedActiveInstances.length / itemsPerPage));

  const handleActiveStatusFilter = (filter) => {
    setActiveStatusFilter(filter);
    setCurrentPage(1);
  };

  const buildDiagnosticLog = (inst) => {
    const node = inst.nodeName || 'Noeud non identifie';
    return `Erreur probable: processus bloque ou en retard au noeud ${node}.`;
  };

  const normalizeDiagnostic = (response, inst, sourceMode) => {
    const analysis = Array.isArray(response) ? response[0] : response;

    return {
      processId: inst.processId || inst.id,
      instanceId: inst.id,
      nodeName: inst.nodeName,
      workflowType: inst.type,
      statusTime: inst.statusTime,
      sourceMode,
      errorType: analysis?.errorType || 'PROCESS_NOT_OK',
      errorMessage: `Bloqué au noeud ${inst.nodeName || 'inconnu'}.`,
      severity: analysis?.severity || 'WARN',
      suggestions: analysis?.suggestions || (analysis?.solution ? [analysis.solution] : []),
      similarErrors: analysis?.similarErrors || [],
      googleSearchUrl:
        analysis?.googleSearchUrl ||
        `https://www.google.com/search?q=${encodeURIComponent(`jBPM ${inst.nodeName || ''} ${inst.statusTime || 'process not ok'}`)}`
    };
  };

  const buildLocalDiagnostic = (inst, reason = '') => {
    const node = (inst.nodeName || '').toLowerCase();
    let suggestion = "Verifier les logs jBPM et recycler la tache.";
    let source = "Regle locale";
    let confidenceScore = 0.65;

    if (node.includes('signal') || node.includes('donor_received')) {
      suggestion = "Verifier la reception du signal SOAP et relancer si necessaire.";
      source = "Signal_Donor_Received";
      confidenceScore = 0.82;
    } else if (node.includes('eligibility') || node.includes('check')) {
      suggestion = "Verifier MSISDN, code RIO et service d'eligibilite.";
      source = "eligibility";
      confidenceScore = 0.8;
    } else if (node.includes('validation')) {
      suggestion = "Verifier l'assignation de la tache humaine et la decision saisie.";
      source = "validation";
      confidenceScore = 0.78;
    } else if (node.includes('reject')) {
      suggestion = "Controler le motif de rejet, RIO et statut client.";
      source = "rejet donneur";
      confidenceScore = 0.78;
    }

    return {
      processId: inst.processId || inst.id,
      instanceId: inst.id,
      nodeName: inst.nodeName,
      workflowType: inst.type,
      statusTime: inst.statusTime,
      sourceMode: 'diagnostic-local',
      errorType: 'PROCESS_NOT_OK',
      errorMessage: `Bloqué au noeud ${inst.nodeName || 'inconnu'}.`,
      severity: 'WARN',
      suggestions: [{ suggestion, source, confidenceScore }],
      similarErrors: [],
      googleSearchUrl: `https://www.google.com/search?q=${encodeURIComponent(`jBPM ${inst.nodeName || ''} ${inst.statusTime || 'process not ok'}`)}`
    };
  };

  const analyzeProcessWithAi = async (inst, event) => {
    event.stopPropagation();

    if (!isNotOk(inst)) {
      return;
    }

    setAiError(null);
    setAiPanel(null);
    setAiLoadingId(inst.id);

    const processKey = String(inst.processId || inst.id);

    try {
      let existingErrors = [];

      try {
        existingErrors = await logAnalysisService.getErrorsByProcess(processKey);
      } catch (historyError) {
        existingErrors = [];
      }

      if (existingErrors?.length > 0) {
        const unresolved =
          existingErrors.find(err => err.resolved === false) ||
          existingErrors[0];
        const response = await logAnalysisService.analyzeExisting(unresolved.id);
        setAiPanel(normalizeDiagnostic(response, inst, 'historique'));
        return;
      }

      const fallbackLog = buildDiagnosticLog(inst);
      const response = await logAnalysisService.analyzeError(fallbackLog, {
        processId: processKey,
        workflowType: inst.type || 'Portability',
        activityName: inst.nodeName,
        processInstanceId: String(inst.id)
      });
      setAiPanel(normalizeDiagnostic(response, inst, 'diagnostic-local'));
    } catch (e) {
      setAiPanel(buildLocalDiagnostic(inst, "Le backend IA/logs 8081 n'est pas disponible, donc le dashboard applique le diagnostic local."));
      setAiError(null);
    } finally {
      setAiLoadingId(null);
    }
  };

  const closeAiPanel = () => {
    setAiPanel(null);
    setAiError(null);
  };

  return (
    <div className="stats-panel">

      <h3 className="stats-title">Statistiques des processus</h3>

      {/* KPI cards */}
      <div className="performance-cards">
        <div className="performance-card" style={{minWidth:180,padding:12}}>
          <h4 style={{margin:'0 0 4px 0',fontSize:12}}>Average Completion IN</h4>
          <div className="performance-value in" style={{fontSize:20}}>
            {stats.successRateIN?.toFixed(1) || 0} %
          </div>
        </div>
        <div className="performance-card" style={{minWidth:180,padding:12}}>
          <h4 style={{margin:'0 0 4px 0',fontSize:12}}>Average Completion OUT</h4>
          <div className="performance-value out" style={{fontSize:20}}>
            {stats.successRateOUT?.toFixed(1) || 0} %
          </div>
        </div>
      </div>

      {/* Stats table + Pie + Monthly charts in a row */}
      <div className="stats-layout" style={{marginTop:12,gap:12,alignItems:'stretch'}}>
        <div style={{display:'flex',flexDirection:'column',gap:0,minWidth:300}}>
          <table className="stats-table" style={{fontSize:13}}>
            <thead>
              <tr><th style={{padding:8,fontSize:12}}>&Eacute;tat</th><th style={{padding:8,fontSize:12}}>IN</th><th style={{padding:8,fontSize:12}}>OUT</th></tr>
            </thead>
            <tbody>
              {rows.map(r => (
                <tr key={r.key} style={r.clickable ? {cursor:'pointer'} : {}}>
                  <td style={{padding:'4px 8px'}}>{r.label}</td>
                  <td style={{padding:'4px 8px'}}>{r.inVal}</td>
                  <td style={{padding:'4px 8px'}}>{r.outVal}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {/* Pie charts inline after table */}
          <div style={{display:'flex',gap:8,justifyContent:'center',marginTop:4}}>
            <div style={{textAlign:'center'}}>
              <h4 style={{margin:'0 0 2px 0',fontSize:12}}>IN</h4>
              <PieChart width={140} height={95}>
                <Pie data={prepareData(IN)} dataKey="value" outerRadius={36} innerRadius={12}>
                  {prepareData(IN).map((_, i) => (<Cell key={i} fill={COLORS_PIE[i]} />))}
                </Pie>
                <Tooltip formatter={(v, n) => [v, n === 'Completed' ? 'Compl\u00e9t\u00e9' : n === 'Active' ? 'En cours' : 'Abandonn\u00e9']} />
              </PieChart>
            </div>
            <div style={{textAlign:'center'}}>
              <h4 style={{margin:'0 0 2px 0',fontSize:12}}>OUT</h4>
              <PieChart width={140} height={95}>
                <Pie data={prepareData(OUT)} dataKey="value" outerRadius={36} innerRadius={12}>
                  {prepareData(OUT).map((_, i) => (<Cell key={i} fill={COLORS_PIE[i]} />))}
                </Pie>
                <Tooltip formatter={(v, n) => [v, n === 'Completed' ? 'Compl\u00e9t\u00e9' : n === 'Active' ? 'En cours' : 'Abandonn\u00e9']} />
              </PieChart>
            </div>
          </div>
          <div style={{textAlign:'center',fontSize:11,color:'#333',fontWeight:600,marginTop:2}}>
            R&eacute;partition des statuts
          </div>
        </div>

        <div className="monthly-chart-box" style={{flex:1,padding:12,minWidth:280}}>
          <h4 style={{fontSize:13,textAlign:'center',margin:'0 0 8px 0'}}>R&eacute;partition SLA</h4>
          {(() => {
            const slaCount = { green:0, orange:0, red:0 };
            const now = new Date();
            activeInstances.forEach(inst => {
              if (!inst.startDate) return;
              const start = new Date(inst.startDate);
              const hours = (now - start) / 3600000;
              if (hours < 1) slaCount.green++;
              else if (hours < 4) slaCount.orange++;
              else slaCount.red++;
            });
            const total = slaCount.green + slaCount.orange + slaCount.red;
            const pct = (v) => total > 0 ? Math.round((v/total)*100) : 0;
            return (
              <>
                <div style={{display:'flex',gap:6,marginBottom:8}}>
                  {[
                    {key:'green',label:'< 1h',color:'#16a34a',bg:'#dcfce7'},
                    {key:'orange',label:'< 4h',color:'#ea580c',bg:'#ffedd5'},
                    {key:'red',label:'≥ 4h',color:'#dc2626',bg:'#fee2e2'}
                  ].map(s => (
                    <div key={s.key} style={{flex:1,background:s.bg,borderRadius:6,padding:'6px 4px',textAlign:'center'}}>
                      <div style={{fontSize:18,fontWeight:700,color:s.color}}>{slaCount[s.key]}</div>
                      <div style={{fontSize:9,color:s.color,fontWeight:600}}>{s.label}</div>
                      <div style={{fontSize:9,color:'#666'}}>{pct(s.key)}%</div>
                    </div>
                  ))}
                </div>
                <div style={{display:'flex',height:10,borderRadius:5,overflow:'hidden'}}>
                  {[
                    {key:'green',color:'#16a34a'},
                    {key:'orange',color:'#ea580c'},
                    {key:'red',color:'#dc2626'}
                  ].map(s => slaCount[s.key] > 0 && (
                    <div key={s.key} style={{flex:slaCount[s.key],background:s.color,minWidth:2}} />
                  ))}
                </div>
                <div style={{textAlign:'center',marginTop:6,fontSize:10,color:'#999'}}>
                  {total} instance{total > 1 ? 's' : ''} active{total > 1 ? 's' : ''}
                </div>
              </>
            );
          })()}
        </div>
      </div>

      {/* Line charts row */}
      <div className="stats-layout" style={{marginTop:10,gap:12}}>
        <div className="monthly-chart-box" style={{flex:1,padding:12}}>
          <h4 style={{fontSize:13,textAlign:'center',margin:'0 0 6px 0'}}>Pr&eacute;vision mois prochain</h4>
          {!prediction ? (
            <p style={{textAlign:'center',color:'#999',fontSize:12,marginTop:20}}>Calcul...</p>
          ) : prediction.totalPredicted > 0 ? (
            <div style={{textAlign:'center'}}>
              <div style={{fontSize:11,color:'#666',marginBottom:4}}>{prediction.month}</div>
              <div style={{fontSize:28,fontWeight:700,color:'#222'}}>{prediction.totalPredicted}</div>
              <div style={{fontSize:10,color:'#999',marginBottom:6}}>portabilit&eacute;s pr&eacute;vues</div>
              <div style={{display:'flex',justifyContent:'center',gap:12,fontSize:11}}>
                <span style={{color:'#0088FE',fontWeight:600}}>IN {prediction.predictedIN}</span>
                <span style={{color:'#999'}}>|</span>
                <span style={{color:'#FF8042',fontWeight:600}}>OUT {prediction.predictedOUT}</span>
              </div>
              <div style={{marginTop:6,fontSize:11,fontWeight:600,color:prediction.trend === 'up' ? '#16a34a' : '#dc2626'}}>
                {prediction.trend === 'up' ? '\u2191 +' : '\u2193 '}{Math.round(Math.abs(prediction.totalPredicted - prediction.lastMonthIN - prediction.lastMonthOUT))} vs mois dernier
              </div>
            </div>
          ) : (
            <div style={{textAlign:'center',paddingTop:20}}>
              <div style={{fontSize:28,fontWeight:700,color:'#ccc'}}>~</div>
              <div style={{fontSize:11,color:'#999',marginTop:4}}>Donn&eacute;es insuffisantes</div>
            </div>
          )}
        </div>
        <div className="monthly-chart-box" style={{flex:1,padding:12}}>
          <h4 style={{fontSize:13,textAlign:'center',margin:'0 0 6px 0'}}>Comparaison mois/mois</h4>
          {compareData.length > 0 ? (
            <>
              <div style={{display:'flex',justifyContent:'space-around',marginBottom:6}}>
                {compareData.map(m => {
                  const total = m.IN + m.OUT;
                  return (
                    <div key={m.name} style={{textAlign:'center'}}>
                      <div style={{fontSize:11,color:'#666',fontWeight:600,marginBottom:2}}>{m.name}</div>
                      <div style={{fontSize:20,fontWeight:700,color:'#222'}}>{total}</div>
                      <div style={{fontSize:10,color:'#999'}}>
                        <span style={{color:'#0088FE',fontWeight:600}}>IN {m.IN}</span>
                        {' | '}
                        <span style={{color:'#FF8042',fontWeight:600}}>OUT {m.OUT}</span>
                      </div>
                    </div>
                  );
                })}
                {compareData.length === 2 && (() => {
                  const prev = compareData[0].IN + compareData[0].OUT;
                  const curr = compareData[1].IN + compareData[1].OUT;
                  const diff = curr - prev;
                  const pct = prev > 0 ? ((diff / prev) * 100).toFixed(1) : '+0.0';
                  const up = diff >= 0;
                  return (
                    <div style={{textAlign:'center',display:'flex',alignItems:'center',gap:4}}>
                      <span style={{fontSize:24}}>{up ? '\u2191' : '\u2193'}</span>
                      <div>
                        <div style={{fontSize:18,fontWeight:700,color:up ? '#16a34a' : '#dc2626'}}>{up ? '+' : ''}{pct}%</div>
                        <div style={{fontSize:10,color:'#999'}}>vs mois pr&eacute;c.</div>
                      </div>
                    </div>
                  );
                })()}
              </div>
              <ResponsiveContainer width="100%" height={90}>
                <BarChart data={compareData} margin={{top:0,right:10,left:10,bottom:0}}>
                  <XAxis dataKey="name" tick={{fontSize:9}} axisLine={false} tickLine={false} />
                  <Tooltip />
                  <Bar dataKey="IN" fill="#0088FE" radius={[3,3,0,0]} maxBarSize={40} />
                  <Bar dataKey="OUT" fill="#FF8042" radius={[3,3,0,0]} maxBarSize={40} />
                </BarChart>
              </ResponsiveContainer>
            </>
          ) : <p style={{textAlign:'center',color:'#999',fontSize:12}}>Donn&eacute;es insuffisantes</p>}
        </div>

        <div className="monthly-chart-box" style={{flex:1,padding:10}}>
          <h4 style={{fontSize:13,textAlign:'center',margin:'0 0 4px 0'}}>Tendance IN / OUT</h4>
          <ResponsiveContainer width="100%" height={140}>
            <LineChart data={monthlyStats}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" tick={{fontSize:9}} />
              <YAxis tick={{fontSize:9}} />
              <Tooltip />
              <Legend wrapperStyle={{fontSize:9}} />
              <Line type="monotone" dataKey="PortabilityIN" stroke="#0088FE" strokeWidth={2} name="IN" />
              <Line type="monotone" dataKey="PortabilityOUT" stroke="#FF8042" strokeWidth={2} name="OUT" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="monthly-chart-box" style={{flex:1,padding:10}}>
          <h4 style={{fontSize:13,textAlign:'center',margin:'0 0 4px 0'}}>Saisonnier</h4>
          <ResponsiveContainer width="100%" height={140}>
            <LineChart data={seasonalData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" tick={{fontSize:9}} />
              <YAxis tick={{fontSize:9}} />
              <Tooltip />
              <Line type="monotone" dataKey="total" stroke="#2563eb" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
          <div style={{marginTop:4,textAlign:'center',fontWeight:'bold',fontSize:12}}>{trendMessage}</div>
        </div>
      </div>

      {/* Active instances table */}
      <div style={{marginTop:12}}>
        <div className="active-processes-heading">
          <h4 style={{margin:0,fontSize:14}}>Instances actives globales</h4>
          <div className="active-status-toolbar" style={{margin:0}}>
            <button className={activeStatusFilter === 'all' ? 'active' : ''} onClick={() => handleActiveStatusFilter('all')}>
              Tous ({activeStatusCounts.all})
            </button>
            <button className={activeStatusFilter === 'ok' ? 'active' : ''} onClick={() => handleActiveStatusFilter('ok')}>
              OK ({activeStatusCounts.ok})
            </button>
            <button className={activeStatusFilter === 'not-ok' ? 'active' : ''} onClick={() => handleActiveStatusFilter('not-ok')}>
              NOT OK ({activeStatusCounts.notOk})
            </button>
          </div>
        </div>

        {activeStatusCounts.ok === 0 && (
          <div className="active-status-note" style={{fontSize:11,padding:'4px 10px',marginBottom:4,marginTop:4}}>
            Les instances affich&eacute;es sont encore actives mais elles d&eacute;passent le d&eacute;lai attendu sur leur noeud courant.
          </div>
        )}

        <div>
          <table className="drilldown-table">
            <thead>
              <tr><th>ID</th><th>Process</th><th>Type</th><th>Date</th><th>Node</th><th>Status</th><th style={{width:80}}>Diagnostic IA</th></tr>
            </thead>
            <tbody>
              {currentItems.map(inst => (
                <tr key={inst.id} onClick={() => setSelectedProcessId(inst.id)} style={{cursor:'pointer'}}>
                  <td>#{inst.id}</td>
                  <td style={{fontSize:11}}>{inst.processId}</td>
                  <td>{inst.type}</td>
                  <td style={{fontSize:11}}>{inst.startDate}</td>
                  <td style={{fontSize:11}}>{inst.nodeName}</td>
                  <td className={inst.statusTime?.trim().toLowerCase() === 'ok' ? 'status-ok' : 'status-notok'}>{inst.statusTime}</td>
                  <td>
                    {isNotOk(inst) ? (
                      <button className="ai-diagnostic-btn" onClick={(e) => analyzeProcessWithAi(inst, e)} disabled={aiLoadingId === inst.id}>
                        {aiLoadingId === inst.id ? 'Analyse...' : 'Analyser'}
                      </button>
                    ) : (
                      <span className="ai-ok-label">Stable</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="pagination">
            <div className="pagination-inner">
              <button className="page-btn" onClick={() => setCurrentPage(p => Math.max(p-1,1))} disabled={currentPage===1}>&lt;</button>
              <span className="page-info">{currentPage}/{totalPages}</span>
              <button className="page-btn" onClick={() => setCurrentPage(p => Math.min(p+1,totalPages))} disabled={currentPage===totalPages}>&gt;</button>
            </div>
          </div>
        )}

        <div className="csv-container">
          <button className="csv-icon-btn" onClick={exportNotOkCSV} title="Exporter les instances en retard">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
            </svg>
            Exporter PDF
          </button>
        </div>
      </div>
{aiError && (
  <div className="ai-diagnostic-error">
    {aiError}
  </div>
)}

{aiPanel && (
  <div className="ai-diagnostic-panel">
    <div className="ai-diagnostic-header">
      <div>
        <span className="ai-diagnostic-kicker">Assistant de diagnostic integre</span>
        <h4>Diagnostic du processus #{aiPanel.instanceId}</h4>
      </div>
      <button className="ai-diagnostic-close" onClick={closeAiPanel} title="Fermer">&times;</button>
    </div>

    <div className="ai-diagnostic-grid">
      <div>
        <strong>Process</strong>
        <span>{aiPanel.processId}</span>
      </div>
      <div>
        <strong>Type</strong>
        <span>{aiPanel.workflowType || '-'}</span>
      </div>
      <div>
        <strong>Noeud bloque</strong>
        <span>{aiPanel.nodeName || '-'}</span>
      </div>
      <div>
        <strong>Mode d'analyse</strong>
        <span>{aiPanel.sourceMode === 'historique' ? 'Cas similaire deja resolu' : 'Regles metier de diagnostic'}</span>
      </div>
    </div>

    <div className="ai-diagnostic-section">
      <h5>Cause probable</h5>
      <p>{aiPanel.errorMessage}</p>
    </div>

    <div className="ai-diagnostic-section">
      <h5>Solutions recommandees</h5>
      {aiPanel.suggestions?.length > 0 ? (
        <ul className="ai-suggestion-list">
          {aiPanel.suggestions.slice(0, 4).map((suggestion, index) => (
            <li key={`${suggestion.source || 'suggestion'}-${index}`}>
              <p>{suggestion.suggestion || suggestion.description || suggestion.title}</p>
              <span>
                {suggestion.source || 'Regle locale'}
                {suggestion.confidenceScore ? ` - confiance ${Math.round(suggestion.confidenceScore * 100)}%` : ''}
              </span>
            </li>
          ))}
        </ul>
      ) : (
        <p style={{color:'#999',fontStyle:'italic'}}>...</p>
      )}
    </div>

    <div className="ai-diagnostic-section">
      <h5>Cas similaires resolus</h5>
      {aiPanel.similarErrors?.length > 0 ? (
        <ul className="ai-similar-list">
          {aiPanel.similarErrors.slice(0, 3).map((similar, index) => (
            <li key={`${similar.logEntryId || similar.processId}-${index}`}>
              <strong>#{similar.processId || similar.logEntryId}</strong>
              <span>{similar.errorType || similar.message}</span>
              {similar.appliedSolution && <p>{similar.appliedSolution}</p>}
            </li>
          ))}
        </ul>
      ) : (
        <p style={{color:'#999',fontStyle:'italic'}}>...</p>
      )}
    </div>

    <div className="ai-diagnostic-actions">
      <a href={aiPanel.googleSearchUrl} target="_blank" rel="noreferrer">
        Plus +
      </a>
      <button onClick={() => setSelectedProcessId(aiPanel.instanceId)}>
        Voir timeline
      </button>
    </div>
  </div>
)}
{
    selectedProcessId && (
        <LifecycleTimeline
            processId={selectedProcessId}
            onClose={() => setSelectedProcessId(null)}
        />
    )
}
    </div>
  );
}
