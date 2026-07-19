import React, { useState, useEffect } from 'react';
import { FiDownload, FiCalendar } from 'react-icons/fi';
import { AiOutlineFile } from 'react-icons/ai';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import * as XLSX from 'xlsx';

const CSS = `
:root {
  --nw-orange: #E8611A;
  --nw-orange-light: rgba(232,97,26,0.10);
  --nw-orange-border: rgba(232,97,26,0.35);
  --nw-orange-deep: #C94E10;
  --nw-green: #1D9E75;
  --nw-amber: #BA7517;
  --nw-red: #E24B4A;
}

.report-page {
  padding: 24px 28px;
  background: #f5f6fa;
  min-height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  color: #1f2937;
  box-sizing: border-box;
  width: 100%;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 0 16px;
  border-bottom: 2px solid #e5e7eb;
}

.report-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  border-left: 4px solid var(--nw-orange);
  padding-left: 12px;
}

.report-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-export {
  padding: 10px 16px;
  background: linear-gradient(135deg, var(--nw-orange), var(--nw-orange-deep));
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all .2s;
  box-shadow: 0 2px 6px rgba(232,97,26,.3);
}

.btn-export:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232,97,26,.4);
}

.btn-export:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.btn-pdf {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  box-shadow: 0 2px 6px rgba(220,38,38,.3);
}

.btn-pdf:hover {
  box-shadow: 0 4px 12px rgba(220,38,38,.4);
}

.btn-excel {
  background: linear-gradient(135deg, #16a34a, #15803d);
  box-shadow: 0 2px 6px rgba(22,163,74,.3);
}

.btn-excel:hover {
  box-shadow: 0 4px 12px rgba(22,163,74,.4);
}

.filters-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.filter-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
  background: #fff;
  color: #1f2937;
}

.filter-input:focus {
  outline: none;
  border-color: var(--nw-orange);
  box-shadow: 0 0 0 2px var(--nw-orange-light);
}

.report-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--nw-orange);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
  text-transform: uppercase;
  font-weight: 600;
}

.report-table-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
}

.report-table thead {
  background: #f3f4f6;
  border-bottom: 2px solid #e5e7eb;
}

.report-table th {
  padding: 12px 14px;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: .5px;
}

.report-table td {
  padding: 12px 14px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
  color: #374151;
}

.report-table tr:last-child td {
  border-bottom: none;
}

.report-table tr:hover {
  background: #fafafa;
}

.badge-severity {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.badge-error {
  background: rgba(226,75,74,.1);
  color: #c0392b;
  border: 1px solid rgba(226,75,74,.3);
}

.badge-warn {
  background: rgba(186,117,23,.1);
  color: #92580a;
  border: 1px solid rgba(186,117,23,.3);
}

.badge-info {
  background: rgba(55,138,221,.1);
  color: #185fa5;
  border: 1px solid rgba(55,138,221,.3);
}

.badge-resolved {
  background: rgba(29,158,117,.1);
  color: #0f6e56;
  border: 1px solid rgba(29,158,117,.3);
}

.badge-pending {
  background: rgba(186,117,23,.1);
  color: #92580a;
  border: 1px solid rgba(186,117,23,.3);
}

.time-badge {
  background: #f3f4f6;
  color: #6b7280;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.no-data {
  text-align: center;
  padding: 48px 24px;
  color: #9ca3af;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: .5;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px;
  color: #6b7280;
  font-size: 14px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(232,97,26,.2);
  border-top-color: var(--nw-orange);
  border-radius: 50%;
  animation: spin .8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.export-summary {
  background: var(--nw-orange-light);
  border: 1px solid var(--nw-orange-border);
  border-radius: 6px;
  padding: 12px 16px;
  margin-top: 16px;
  color: var(--nw-orange);
  font-size: 13px;
}

@media (max-width: 768px) {
  .report-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .report-actions {
    width: 100%;
  }
  
  .filters-card {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    width: 100%;
  }
  
  .filter-input {
    width: 100%;
  }
  
  .report-stats {
    grid-template-columns: 1fr;
  }
  
  .report-table {
    font-size: 12px;
  }
  
  .report-table th,
  .report-table td {
    padding: 10px 8px;
  }
}
`;

const ErrorReportPage = () => {
    const [errors, setErrors] = useState([]);
    const [loading, setLoading] = useState(false);
    const [exporting, setExporting] = useState(false);
    const [dateFrom, setDateFrom] = useState(getDateFromToday(7));
    const [dateTo, setDateTo] = useState(getTodayDate());
    const [filterType, setFilterType] = useState('all');

    useEffect(() => {
        fetchErrors();
    }, []);

    function getDateFromToday(days) {
        const date = new Date();
        date.setDate(date.getDate() - days);
        return date.toISOString().split('T')[0];
    }

    function getTodayDate() {
        return new Date().toISOString().split('T')[0];
    }

    const fetchErrors = async () => {
        setLoading(true);
        try {
            const res = await fetch(
                `http://localhost:8081/api/kie/errors?from=${dateFrom}&to=${dateTo}&type=${filterType}`
            );
            const data = await res.json();
            setErrors(data || []);
        } catch (e) {
            console.error('Erreur récupération:', e);
            setErrors([]);
        } finally {
            setLoading(false);
        }
    };

    const handleDateChange = (newFrom, newTo) => {
        setDateFrom(newFrom);
        setDateTo(newTo);
    };

    useEffect(() => {
        if (dateFrom && dateTo) {
            fetchErrors();
        }
    }, [dateFrom, dateTo, filterType]);

    const calculateStats = () => {
        const total = errors.length;
        const resolved = errors.filter(e => e.status === 'RESOLVED').length;
        const pending = errors.filter(e => e.status === 'PENDING').length;
        const avgResolutionTime = errors.length > 0
            ? (errors.reduce((sum, e) => sum + (e.resolutionTime || 0), 0) / errors.length).toFixed(1)
            : 0;

        return { total, resolved, pending, avgResolutionTime };
    };

    const exportPDF = async () => {
        setExporting(true);
        try {
            const doc = new jsPDF();
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();

            // En-tête
            doc.setFillColor(232, 97, 26);
            doc.rect(0, 0, pageWidth, 30, 'F');
            doc.setTextColor(255, 255, 255);
            doc.setFontSize(18);
            doc.text('Rapport d\'Erreurs jBPM', 14, 20);

            // Métadonnées
            doc.setTextColor(100, 100, 100);
            doc.setFontSize(10);
            doc.text(`Période: ${dateFrom} à ${dateTo}`, 14, 40);
            doc.text(`Généré le: ${new Date().toLocaleString('fr-FR')}`, 14, 48);

            // Statistiques
            const stats = calculateStats();
            doc.setTextColor(29, 158, 117);
            doc.setFontSize(11);
            doc.text(`Total d'erreurs: ${stats.total} | Résolues: ${stats.resolved} | En attente: ${stats.pending} | Temps moyen: ${stats.avgResolutionTime}min`, 14, 58);

            // Tableau
            const tableData = errors.map(e => [
                new Date(e.errorDate).toLocaleString('fr-FR'),
                e.processId || 'N/A',
                e.errorType || 'ERROR',
                (e.errorMessage || '').substring(0, 50) + '...',
                e.status === 'RESOLVED' ? 'Résolu' : 'En attente',
                e.resolutionTime ? `${e.resolutionTime}min` : 'N/A'
            ]);

            doc.autoTable({
                head: [['Date', 'Process', 'Type', 'Message', 'Statut', 'Temps']],
                body: tableData,
                startY: 68,
                theme: 'grid',
                headerStyles: {
                    fillColor: [232, 97, 26],
                    textColor: [255, 255, 255],
                    fontStyle: 'bold',
                    fontSize: 10
                },
                bodyStyles: {
                    fontSize: 9,
                    textColor: [50, 50, 50]
                },
                alternateRowStyles: {
                    fillColor: [245, 245, 245]
                },
                margin: { left: 14, right: 14 }
            });

            // Pied de page
            doc.setFontSize(8);
            doc.setTextColor(150, 150, 150);
            doc.text(
                `Page ${doc.internal.getPages().length}`,
                pageWidth / 2,
                pageHeight - 10,
                { align: 'center' }
            );

            doc.save(`rapport-erreurs-jbpm-${dateTo}.pdf`);
        } catch (e) {
            console.error('Erreur PDF:', e);
        } finally {
            setExporting(false);
        }
    };

    const exportExcel = async () => {
        setExporting(true);
        try {
            const stats = calculateStats();

            // Feuille 1: Résumé
            const summary = [
                ['RAPPORT D\'ERREURS JBPM'],
                [],
                ['Période', `${dateFrom} à ${dateTo}`],
                ['Généré le', new Date().toLocaleString('fr-FR')],
                [],
                ['STATISTIQUES'],
                ['Total d\'erreurs', stats.total],
                ['Erreurs résolues', stats.resolved],
                ['Erreurs en attente', stats.pending],
                ['Temps moyen de résolution', `${stats.avgResolutionTime}min`],
            ];

            // Feuille 2: Détails
            const details = [
                ['Date', 'Process', 'Type', 'Message', 'Statut', 'Temps de résolution', 'Container', 'Instance ID'],
                ...errors.map(e => [
                    new Date(e.errorDate).toLocaleString('fr-FR'),
                    e.processId || 'N/A',
                    e.errorType || 'ERROR',
                    e.errorMessage || 'N/A',
                    e.status === 'RESOLVED' ? 'Résolu' : 'En attente',
                    e.resolutionTime ? `${e.resolutionTime}min` : 'N/A',
                    e.containerId || 'N/A',
                    e.processInstanceId || 'N/A'
                ])
            ];

            const wb = XLSX.utils.book_new();
            const ws1 = XLSX.utils.aoa_to_sheet(summary);
            const ws2 = XLSX.utils.aoa_to_sheet(details);

            ws1.A1 = { ...ws1.A1, s: { bold: true, sz: 14 } };
            ws2.A1 = { ...ws2.A1, s: { bold: true, bg: { rgb: 'FFE8611A' }, color: { rgb: 'FFFFFFFF' } } };

            XLSX.utils.book_append_sheet(wb, ws1, 'Résumé');
            XLSX.utils.book_append_sheet(wb, ws2, 'Détails');

            XLSX.writeFile(wb, `rapport-erreurs-jbpm-${dateTo}.xlsx`);
        } catch (e) {
            console.error('Erreur Excel:', e);
        } finally {
            setExporting(false);
        }
    };

    const stats = calculateStats();

    return (
        <>
            <style>{CSS}</style>
            <div className="report-page">
                {/* Header */}
                <div className="report-header">
                    <h1 className="report-title">Rapport d'Erreurs jBPM</h1>
                    <div className="report-actions">
                        <button
                            className="btn-export btn-pdf"
                            onClick={exportPDF}
                            disabled={exporting || errors.length === 0}>
                            <AiOutlineFile size={16} />
                            {exporting ? 'Génération...' : 'Export PDF'}
                        </button>
                        <button
                            className="btn-export btn-excel"
                            onClick={exportExcel}
                            disabled={exporting || errors.length === 0}>
                            <AiOutlineFile size={16} />
                            {exporting ? 'Génération...' : 'Export Excel'}
                        </button>
                    </div>
                </div>

                {/* Filtres */}
                <div className="filters-card">
                    <div className="filter-group">
                        <label className="filter-label">
                            <FiCalendar size={14} style={{ marginRight: '6px', display: 'inline' }} />
                            De:
                        </label>
                        <input
                            type="date"
                            className="filter-input"
                            value={dateFrom}
                            onChange={(e) => setDateFrom(e.target.value)}
                        />
                    </div>
                    <div className="filter-group">
                        <label className="filter-label">À:</label>
                        <input
                            type="date"
                            className="filter-input"
                            value={dateTo}
                            onChange={(e) => setDateTo(e.target.value)}
                        />
                    </div>
                    <div className="filter-group">
                        <label className="filter-label">Type:</label>
                        <select
                            className="filter-input"
                            value={filterType}
                            onChange={(e) => setFilterType(e.target.value)}>
                            <option value="all">Tous les types</option>
                            <option value="ERROR">Erreurs</option>
                            <option value="WARN">Avertissements</option>
                            <option value="INFO">Informations</option>
                        </select>
                    </div>
                </div>

                {/* Statistiques */}
                <div className="report-stats">
                    <div className="stat-card">
                        <div className="stat-value">{stats.total}</div>
                        <div className="stat-label">Erreurs totales</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value" style={{ color: '#1D9E75' }}>{stats.resolved}</div>
                        <div className="stat-label">Résolues</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value" style={{ color: '#BA7517' }}>{stats.pending}</div>
                        <div className="stat-label">En attente</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{stats.avgResolutionTime}</div>
                        <div className="stat-label">Temps moyen (min)</div>
                    </div>
                </div>

                {/* Tableau */}
                <div className="report-table-card">
                    {loading ? (
                        <div className="loading">
                            <div className="spinner" />
                            Chargement des données...
                        </div>
                    ) : errors.length === 0 ? (
                        <div className="no-data">
                            <div className="no-data-icon">📊</div>
                            <p>Aucune erreur trouvée pour cette période</p>
                        </div>
                    ) : (
                        <table className="report-table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Process</th>
                                    <th>Type</th>
                                    <th>Message</th>
                                    <th>Statut</th>
                                    <th>Temps (min)</th>
                                    <th>Container</th>
                                </tr>
                            </thead>
                            <tbody>
                                {errors.map((error, idx) => (
                                    <tr key={idx}>
                                        <td>{new Date(error.errorDate).toLocaleString('fr-FR')}</td>
                                        <td>{error.processId || 'N/A'}</td>
                                        <td>
                                            <span className={`badge-severity badge-${error.errorType?.toLowerCase() || 'error'}`}>
                                                {error.errorType || 'ERROR'}
                                            </span>
                                        </td>
                                        <td>{(error.errorMessage || 'N/A').substring(0, 60)}...</td>
                                        <td>
                                            <span className={`badge-severity badge-${error.status === 'RESOLVED' ? 'resolved' : 'pending'}`}>
                                                {error.status === 'RESOLVED' ? 'Résolu' : 'En attente'}
                                            </span>
                                        </td>
                                        <td>
                                            <span className="time-badge">
                                                {error.resolutionTime ? `${error.resolutionTime}min` : 'N/A'}
                                            </span>
                                        </td>
                                        <td>{error.containerId || 'N/A'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

                {errors.length > 0 && (
                    <div className="export-summary">
                        ✓ {errors.length} erreur(s) prête(s) à l'export • Cliquez sur PDF ou Excel pour télécharger le rapport
                    </div>
                )}
            </div>
        </>
    );
};

export default ErrorReportPage;
