import React, { useState, useEffect } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  LineChart, Line
} from 'recharts';
import './LifecycleChart.css';

const LifecycleChart = () => {
  const [lifecycleData, setLifecycleData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchLifecycleData();
  }, []);

  const fetchLifecycleData = async () => {
    try {
      const response = await fetch('http://localhost:8089/api/monitoring/lifecycle/analysis');
      if (!response.ok) throw new Error('Erreur lors de la récupération des données');
      const data = await response.json();
      setLifecycleData(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatDuration = (hours) => {
    if (hours < 1) {
      return `${Math.round(hours * 60)} min`;
    } else if (hours < 24) {
      return `${Math.round(hours)} h`;
    } else {
      return `${Math.round(hours / 24)} j`;
    }
  };

  const getSlaColor = (compliant) => compliant ? '#1D9E75' : '#E24B4A';

  if (loading) return <div className="lifecycle-loading">Chargement de l'analyse du cycle de vie...</div>;
  if (error) return <div className="lifecycle-error">Erreur: {error}</div>;
  if (!lifecycleData) return null;

  // Préparer les données pour le chart
  const chartData = lifecycleData.stageDurations.map(stage => ({
    name: stage.stageName,
    duration: stage.averageDurationHours,
    slaThreshold: stage.slaThresholdHours,
    compliant: stage.slaCompliant,
    sampleSize: stage.sampleSize
  }));

  return (
    <div className="lifecycle-container">
      <div className="lifecycle-header">
        <h3>📊 Analyse du Cycle de Vie Portabilité</h3>
        <div className="lifecycle-summary">
          <div className="summary-card">
            <span className="summary-label">Durée totale moyenne</span>
            <span className="summary-value">{formatDuration(lifecycleData.totalAverageDurationHours)}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Goulot d'étranglement</span>
            <span className="summary-value bottleneck">{lifecycleData.bottleneckStage || 'N/A'}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Conformité SLA</span>
            <span className={`summary-value ${lifecycleData.slaCompliancePercentage >= 90 ? 'good' : 'warning'}`}>
              {lifecycleData.slaCompliancePercentage}%
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Instances analysées</span>
            <span className="summary-value">{lifecycleData.totalAnalyzedInstances}</span>
          </div>
        </div>
      </div>

      <div className="lifecycle-content">
        {/* Timeline des étapes */}
        <div className="lifecycle-timeline-section">
          <h4>Timeline des étapes (Durée moyenne)</h4>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" label={{ value: 'Heures', position: 'insideBottom', offset: -5 }} />
              <YAxis type="category" dataKey="name" width={150} tick={{ fontSize: 12 }} />
              <Tooltip 
                formatter={(value, name, props) => {
                  if (name === 'duration') {
                    return [formatDuration(value), 'Durée moyenne'];
                  }
                  if (name === 'slaThreshold') {
                    return [formatDuration(value), 'Seuil SLA'];
                  }
                  return [value, name];
                }}
              />
              <Legend />
              <Bar 
                dataKey="duration" 
                name="Durée moyenne"
                fill="#E8611A"
                radius={[0, 4, 4, 0]}
              />
              <Bar 
                dataKey="slaThreshold" 
                name="Seuil SLA"
                fill="#1D9E75"
                radius={[0, 4, 4, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Tableau détaillé des étapes */}
        <div className="lifecycle-table-section">
          <h4>Détail par étape</h4>
          <table className="lifecycle-table">
            <thead>
              <tr>
                <th>Étape</th>
                <th>Durée moyenne</th>
                <th>Min</th>
                <th>Max</th>
                <th>SLA (48h)</th>
                <th>Échantillon</th>
              </tr>
            </thead>
            <tbody>
              {lifecycleData.stageDurations.map((stage, index) => (
                <tr key={index} className={stage.slaCompliant ? 'compliant' : 'non-compliant'}>
                  <td>{stage.stageName}</td>
                  <td>{formatDuration(stage.averageDurationHours)}</td>
                  <td>{formatDuration(stage.minDurationHours)}</td>
                  <td>{formatDuration(stage.maxDurationHours)}</td>
                  <td>
                    <span className={`sla-badge ${stage.slaCompliant ? 'ok' : 'ko'}`}>
                      {stage.slaCompliant ? '✓ OK' : '✗ Dépassé'}
                    </span>
                  </td>
                  <td>{stage.sampleSize}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Alertes et recommandations */}
        <div className="lifecycle-alerts-section">
          <h4>⚠️ Analyse et recommandations</h4>
          {lifecycleData.bottleneckStage && (
            <div className="alert-card warning">
              <strong>Goulot d'étranglement identifié:</strong> {lifecycleData.bottleneckStage}
              <p>Cette étape prend le plus de temps. Envisagez d'optimiser ce processus.</p>
            </div>
          )}
          {lifecycleData.slaCompliancePercentage < 90 && (
            <div className="alert-card danger">
              <strong>SLA non respecté:</strong> Seulement {lifecycleData.slaCompliancePercentage}% des demandes respectent le délai de 48h.
              <p>Action requise: Analysez les étapes non conformes et identifiez les causes de retard.</p>
            </div>
          )}
          {lifecycleData.slaCompliancePercentage >= 90 && (
            <div className="alert-card success">
              <strong>Performance SLA excellente:</strong> {lifecycleData.slaCompliancePercentage}% des demandes respectent le délai.
              <p>Continuez à surveiller les performances pour maintenir ce niveau.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default LifecycleChart;
