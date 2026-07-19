import React, { useState, useEffect } from 'react';
import './NotificationsPage.css';

export default function NotificationsPage({ notifications = [], onMarkAllRead, onClearNotifications }) {
  const [filter, setFilter] = useState('all'); // all, error, task, unread
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedId, setExpandedId] = useState(null);
  const [archivedIds, setArchivedIds] = useState(() => {
    try { return JSON.parse(localStorage.getItem('archived_notifications') || '[]'); }
    catch { return []; }
  });
  const [showArchived, setShowArchived] = useState(false);

  const toggleArchive = (id) => {
    const newArchived = archivedIds.includes(id)
      ? archivedIds.filter(a => a !== id)
      : [...archivedIds, id];
    setArchivedIds(newArchived);
    localStorage.setItem('archived_notifications', JSON.stringify(newArchived));
  };

  // Filtrer les notifications selon le filtre et la recherche
  const filteredNotifications = notifications
    .filter(n => showArchived ? true : !archivedIds.includes(n.id))
    .filter(n => {
    // Appliquer le filtre par type
    if (filter === 'error' && n.type !== 'error') return false;
    if (filter === 'task' && n.type !== 'task') return false;
    if (filter === 'unread' && n.read) return false;

    // Appliquer la recherche
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      return (
        n.title.toLowerCase().includes(search) ||
        n.text.toLowerCase().includes(search)
      );
    }

    return true;
  });

  // Statistiques
  const stats = {
    total: notifications.length,
    errors: notifications.filter(n => n.type === 'error').length,
    tasks: notifications.filter(n => n.type === 'task').length,
    unread: notifications.filter(n => !n.read).length
  };

  const getIcon = (type) => {
    return type === 'error' ? '❌' : '⏳';
  };

  const getTypeLabel = (type) => {
    return type === 'error' ? 'Erreur jBPM' : 'Tâche en attente';
  };

  const formatTime = (time) => {
    return time || '--:--';
  };

  return (
    <div className="notifications-page">
      {/* En-tête */}
      <div className="notif-header">
        <div className="notif-header-left">
          <div className="notif-header-icon">🔔</div>
          <div>
            <h1>Centre de Notifications</h1>
            <p>Suivez les alertes et tâches en attente</p>
          </div>
        </div>
        <div className="notif-actions">
          {stats.unread > 0 && (
            <button className="notif-btn notif-btn-secondary" onClick={onMarkAllRead}>
              ✓ Marquer tout comme lu
            </button>
          )}
          {archivedIds.length > 0 && (
            <button
              className={`notif-btn notif-archive-toggle-custom ${showArchived ? 'active' : ''}`}
              onClick={() => setShowArchived(!showArchived)}
            >
              {showArchived ? '🙈 Masquer archivés' : '📦 Voir archivés'}
              <span className="archive-count-badge">{archivedIds.length}</span>
            </button>
          )}
          {notifications.length > 0 && (
            <button className="notif-btn notif-btn-danger" onClick={onClearNotifications}>
              🗑 Tout effacer
            </button>
          )}
        </div>
      </div>

        {/* Statistiques */}
        <div className="notif-stats">
          <div className="stat-card stat-total">
            <div className="stat-number">{stats.total}</div>
            <div className="stat-label">Total</div>
          </div>
          <div className="stat-card stat-error">
            <div className="stat-number">{stats.errors}</div>
            <div className="stat-label">Erreurs</div>
          </div>
          <div className="stat-card stat-task">
            <div className="stat-number">{stats.tasks}</div>
            <div className="stat-label">Tâches</div>
          </div>
          <div className="stat-card stat-unread">
            <div className="stat-number">{stats.unread}</div>
            <div className="stat-label">Non lues</div>
          </div>
        </div>

      {/* Barre de recherche et filtres */}
      <div className="notif-controls">
        <div className="notif-search">
          <input
            type="text"
            placeholder="🔍 Rechercher une notification..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="notif-search-input"
          />
        </div>

        <div className="notif-filters">
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            Toutes ({stats.total})
          </button>
          <button
            className={`filter-btn ${filter === 'error' ? 'active error' : ''}`}
            onClick={() => setFilter('error')}
          >
            ❌ Erreurs ({stats.errors})
          </button>
          <button
            className={`filter-btn ${filter === 'task' ? 'active task' : ''}`}
            onClick={() => setFilter('task')}
          >
            ⏳ Tâches ({stats.tasks})
          </button>
          <button
            className={`filter-btn ${filter === 'unread' ? 'active unread' : ''}`}
            onClick={() => setFilter('unread')}
          >
            🔴 Non lues ({stats.unread})
          </button>
        </div>
      </div>

      {/* Liste des notifications */}
      <div className="notif-list">
        {filteredNotifications.length === 0 ? (
          <div className="notif-empty">
            <div className="notif-empty-icon">📭</div>
            <p>Aucune notification</p>
            {notifications.length === 0 && (
              <p className="notif-empty-hint">
                Les nouvelles notifications apparaîtront ici automatiquement
              </p>
            )}
          </div>
        ) : (
          filteredNotifications.map((notif) => (
            <div
              key={notif.id}
              className={`notif-card ${!notif.read ? 'unread' : ''} ${notif.type} ${archivedIds.includes(notif.id) ? 'archived' : ''}`}
              onClick={() => setExpandedId(expandedId === notif.id ? null : notif.id)}
            >
              <div className="notif-card-header">
                <div className="notif-icon">{getIcon(notif.type)}</div>
                <div className="notif-card-content">
                  <div className="notif-card-title">
                    <h3>{notif.title}</h3>
                    {!notif.read && <span className="unread-badge">Nouveau</span>}
                  </div>
                  <p className="notif-card-text">{notif.text}</p>
                </div>
                <div className="notif-card-time">
                  <span className="time">{formatTime(notif.time)}</span>
                  <button
                    className={`notif-archive-btn ${archivedIds.includes(notif.id) ? 'archived' : ''}`}
                    onClick={(e) => { e.stopPropagation(); toggleArchive(notif.id); }}
                    title={archivedIds.includes(notif.id) ? 'Restaurer' : 'Archiver'}
                  >
                    {archivedIds.includes(notif.id) ? '✓' : '📦'}
                  </button>
                  <span className="expand-icon">{expandedId === notif.id ? '▲' : '▼'}</span>
                </div>
              </div>

              {/* Détails expansés */}
              {expandedId === notif.id && (
                <div className="notif-card-details">
                  <div className="detail-row">
                    <strong>Type :</strong> {getTypeLabel(notif.type)}
                  </div>
                  <div className="detail-row">
                    <strong>ID :</strong> {notif.id}
                  </div>
                  <div className="detail-row">
                    <strong>Statut :</strong> {notif.read ? 'Lu' : 'Non lu'}
                  </div>
                  <div className="detail-row">
                    <strong>Heure :</strong> {formatTime(notif.time)}
                  </div>
                </div>
              )}
            </div>
          ))
        )}
        {/* Pied de page */}
        <div className="notif-footer">
          <p>Affichant {filteredNotifications.length} sur {notifications.length} notifications</p>
        </div>
      </div>
    </div>
  );
}
