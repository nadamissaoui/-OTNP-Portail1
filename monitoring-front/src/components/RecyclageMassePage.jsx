import React, { useEffect, useState } from 'react';
import './RecyclageMasse.css';

const API_BASE_URL = 'http://localhost:8089';

export default function RecyclageMassePage() {
  const [filters, setFilters] = useState({
    containerId: '',
    dateDebut: '',
    dateFin: '',
    processInstanceId: ''
  });

  const [tasks, setTasks] = useState([]);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [recycling, setRecycling] = useState(false);
  const [toast, setToast] = useState(null);

  // ─── ÉTATS AJOUTÉS POUR LA PAGINATION ──────────────────────────────
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10; // Ajustez la taille de page selon vos besoins

  useEffect(() => {
    fetchTasks();
  }, []);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4500);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const normalizeTask = (item) => ({
    id: item.id,
    name: item.name || 'Human Task',
    processInstanceId: item.processInstanceId,
    containerId: item.containerId || '',
    status: item.status,
    createdOn: item.createdOn
  });

  const buildSearchUrl = (currentFilters = filters) => {
    const params = new URLSearchParams();
    if (currentFilters.containerId) {
      params.append('containerId', currentFilters.containerId);
    }
    if (currentFilters.processInstanceId?.trim()) {
      params.append('processInstanceId', currentFilters.processInstanceId.trim());
    }
    if (currentFilters.dateDebut) {
      params.append('dateDebut', currentFilters.dateDebut);
    }
    if (currentFilters.dateFin) {
      params.append('dateFin', currentFilters.dateFin);
    }
    
    // Modification pour envoyer la totalité au besoin, 
    // ou configurer la pagination côté API si le backend le supporte.
    params.append('page', '0');
    params.append('size', '500'); 
    return `${API_BASE_URL}/api/monitoring/tasks/recyclable?${params.toString()}`;
  };

  const fetchTasks = async (overrideFilters = filters) => {
    setLoading(true);
    try {
      const response = await fetch(buildSearchUrl(overrideFilters));
      if (!response.ok) throw new Error('Erreur lors du chargement des données');
      const data = await response.json();
      const result = Array.isArray(data) ? data.map(normalizeTask) : [];
      setTasks(result);
      setSelectedIds(new Set());
      setCurrentPage(1); // Réinitialiser à la première page lors d'une nouvelle recherche
    } catch (error) {
      console.error(error);
      setTasks([]);
      showToast('Impossible de charger les données du serveur.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchTasks();
  };

  const handleSelectRow = (id) => {
    const nextSelected = new Set(selectedIds);
    if (nextSelected.has(id)) {
      nextSelected.delete(id);
    } else {
      nextSelected.add(id);
    }
    setSelectedIds(nextSelected);
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedIds(new Set(tasks.map(t => t.id)));
    } else {
      setSelectedIds(new Set());
    }
  };

  const handleToggleSelectAll = () => {
    if (selectedIds.size === tasks.length && tasks.length > 0) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(tasks.map(t => t.id)));
    }
  };

  const handleRecycle = async () => {
    if (selectedIds.size === 0 || recycling) return;

    setRecycling(true);
    const idsArray = Array.from(selectedIds);

    try {
      const response = await fetch(`${API_BASE_URL}/api/monitoring/tasks/recycle`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids: idsArray })
      });

      if (!response.ok) throw new Error('Erreur recyclage');

      const result = await response.json();
      const succeeded = result.result?.filter(r => r.success) || [];
      const failed = result.result?.filter(r => !r.success) || [];

      if (succeeded.length > 0 && failed.length === 0) {
        showToast(`✓ ${succeeded.length} tâche(s) recyclée(s) avec succès.`, 'success');
      } else if (succeeded.length > 0 && failed.length > 0) {
        showToast(`${succeeded.length} recyclée(s), ${failed.length} en échec.`, 'success');
      } else {
        showToast('Aucune tâche recyclée. Vérifiez la console backend.', 'error');
      }

      setSelectedIds(new Set());
      setTimeout(() => fetchTasks(), 300);

    } catch (error) {
      console.error(error);
      showToast('Erreur pendant le recyclage. Vérifiez la console du backend WS.', 'error');
    } finally {
      setRecycling(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return isNaN(date.getTime()) ? dateString : date.toLocaleDateString('fr-FR');
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'Ready':      return 'status-ready';
      case 'Reserved':   return 'status-reserved';
      case 'InProgress': return 'status-inprogress';
      default:           return '';
    }
  };

  const allSelected = tasks.length > 0 && selectedIds.size === tasks.length;

  // ─── CALCULS LOGIQUES POUR LA PAGINATION APPARENTE ─────────────────
  const totalPages = Math.ceil(tasks.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentTasks = tasks.slice(indexOfFirstItem, indexOfLastItem);

  const handlePageChange = (pageNumber) => {
    if (pageNumber >= 1 && pageNumber <= totalPages) {
      setCurrentPage(pageNumber);
    }
  };

  // Génération du tableau des numéros de page
  const pageNumbers = [];
  for (let i = 1; i <= totalPages; i++) {
    pageNumbers.push(i);
  }

  return (
    <div className="recyclage-container">
      {toast && (
        <div className={`recyclage-toast toast-${toast.type}`}>
          <span>{toast.message}</span>
        </div>
      )}

      <section className="recyclage-card filter-card">
        <form onSubmit={handleSearchSubmit} className="recyclage-filter-form">
          <div className="filter-row">
            <div className="filter-field full-width">
              <label>Process Type:</label>
              <select
                name="containerId"
                value={filters.containerId}
                onChange={handleInputChange}
                className="filter-select"
              >
                <option value="">Tous les processus</option>
                <option value="portabilityin_1.0.0-SNAPSHOT">
                  portabilityin_1.0.0-SNAPSHOT
                </option>
                <option value="portaout_1.0.0-SNAPSHOT">
                  portaout_1.0.0-SNAPSHOT
                </option>
              </select>
            </div>
          </div>

          <div className="filter-row">
            <div className="filter-field">
              <label>Date Debut:</label>
              <input
                type="date"
                name="dateDebut"
                value={filters.dateDebut}
                onChange={handleInputChange}
                className="filter-input"
              />
            </div>
            <div className="filter-field">
              <label>Date Fin:</label>
              <input
                type="date"
                name="dateFin"
                value={filters.dateFin}
                onChange={handleInputChange}
                className="filter-input"
              />
            </div>
            <div className="filter-field">
              <label>Process Instance ID:</label>
              <input
                type="text"
                name="processInstanceId"
                value={filters.processInstanceId}
                onChange={handleInputChange}
                className="filter-input"
                placeholder="Filtrer par instance..."
              />
            </div>
          </div>

          <div className="filter-actions">
            <button type="submit" className="btn-recherche-green">
              Recherche
            </button>
            <button
              type="button"
              className="btn-select-all"
              onClick={handleToggleSelectAll}
              disabled={tasks.length === 0}
            >
              {allSelected ? 'Désélectionner tout' : 'Sélectionner tout'}
            </button>
          </div>
        </form>

        <div className="recyclage-table-wrapper">
          <table className="recyclage-table">
            <thead>
              <tr>
                <th style={{ width: '45px' }}>
                  <input
                    type="checkbox"
                    checked={allSelected}
                    onChange={handleSelectAll}
                    disabled={tasks.length === 0 || recycling}
                    title="Tout sélectionner"
                  />
                </th>
                <th>Task ID</th>
                <th>Name</th>
                <th>Process Instance ID</th>
                <th>Status</th>
                <th>Created On</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" className="empty-row">Chargement...</td>
                </tr>
              ) : currentTasks.length > 0 ? (
                currentTasks.map(task => (
                  <tr
                    key={task.id}
                    className={selectedIds.has(task.id) ? 'row-selected' : ''}
                  >
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedIds.has(task.id)}
                        onChange={() => handleSelectRow(task.id)}
                        disabled={recycling}
                      />
                    </td>
                    <td>{task.id}</td>
                    <td>{task.name}</td>
                    <td>{task.processInstanceId}</td>
                    <td>
                      <span className={`status-badge ${getStatusBadgeClass(task.status)}`}>
                        {task.status}
                      </span>
                    </td>
                    <td>{formatDate(task.createdOn)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="empty-row">Aucune tâche trouvée.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* ─── PAGINATION SIMPLE ────────────── */}
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

        <div className="table-action-footer">
          <span className="selection-count">
            {selectedIds.size > 0 ? `${selectedIds.size} tâche(s) sélectionnée(s)` : ''}
          </span>
          <button
            type="button"
            className="btn-recycle-green"
            onClick={handleRecycle}
            disabled={selectedIds.size === 0 || recycling}
          >
            {recycling ? 'Recyclage...' : 'Recycler'}
          </button>
        </div>
      </section>
    </div>
  );
}