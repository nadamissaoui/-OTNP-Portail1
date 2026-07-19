import React, { useState, useEffect } from 'react';
import './Navbar.css';
import logoBillcom from './download.jpg'; 
import maPhoto from '../../images.jpg'; // Photo d'Ahmed
import photo from '../../nada.jpg';       // Ta photo (Nada)

export default function Navbar({ onLogout, agentName, notifications = [], onMarkAllRead, onClearNotifications, onAddTestNotif }) {
  const [showThemeMenu, setShowThemeMenu] = useState(false);
  const [showNotifMenu, setShowNotifMenu] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false); 
  const [currentTheme, setCurrentTheme] = useState('dark');

  const unreadCount = notifications.filter(n => !n.read).length;

  // Synchronisation de la classe globale sur le document body
  useEffect(() => {
    if (currentTheme === 'light') {
      document.body.classList.add('light-theme');
      document.body.classList.remove('dark-theme');
      document.body.setAttribute('data-theme', 'light');
    } else {
      document.body.classList.add('dark-theme');
      document.body.classList.remove('light-theme');
      document.body.setAttribute('data-theme', 'dark');
    }
  }, [currentTheme]);

  const changeTheme = (theme) => {
    setCurrentTheme(theme);
    setShowThemeMenu(false);
  };

  const currentAvatar = agentName && agentName.trim().toLowerCase() === 'admin' ? photo : maPhoto;

  return (
    <nav className={`navbar ${currentTheme === 'light' ? 'light-theme' : 'dark-theme'}`}>
      {/* Zone Gauche : Logo + Texte Billcom */}
      <div className="navbar-left">
        <img src={logoBillcom} alt="Billcom Logo" className="navbar-logo-img" />
        <span className="navbar-brand-text">Billcom Consulting</span>
      </div>
      
      {/* Zone Droite : Outils + Profil Groupé */}
      <div className="navbar-right">
        
        {/* BOUTON LUMINOSITÉ */}
        <div className="navbar-theme-container">
          <button 
            className="navbar-icon-btn" 
            onClick={() => { setShowThemeMenu(!showThemeMenu); setShowNotifMenu(false); setShowProfileMenu(false); }} 
            title="Luminosité"
          >
            {currentTheme === 'dark' ? (
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path></svg>
            ) : (
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364-6.364l-.707.707M6.343 17.657l-.707.707M16.243 17.657l.707.707M7.757 5.636l.707.707M12 8a4 4 0 100 8 4 4 0 000-8z"></path></svg>
            )}
          </button>

          {showThemeMenu && (
            <div className="theme-dropdown">
              <button className={currentTheme === 'light' ? 'active' : ''} onClick={() => changeTheme('light')}>
                <span className="theme-icon">☀️</span> Mode Clair
              </button>
              <button className={currentTheme === 'dark' ? 'active' : ''} onClick={() => changeTheme('dark')}>
                <span className="theme-icon">🌙</span> Mode Sombre
              </button>
            </div>
          )}
        </div>

        {/* BOUTON NOTIFICATIONS */}
        <div className="navbar-theme-container">
          <button 
            className="navbar-icon-btn" 
            onClick={() => { setShowNotifMenu(!showNotifMenu); setShowThemeMenu(false); setShowProfileMenu(false); if (!showNotifMenu && onMarkAllRead) setTimeout(onMarkAllRead, 2000); }} 
            title="Notifications"
          >
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
            {unreadCount > 0 && (
              <span className="notification-badge notif-pulse">{unreadCount}</span>
            )}
          </button>

          {showNotifMenu && (
            <div className="theme-dropdown notif-dropdown" style={{ 
              minWidth: '300px', 
              right: 0, 
              background: currentTheme === 'dark' ? '#ffffff' : '#1a1a1a', 
              border: currentTheme === 'dark' ? '1px solid #e5e7eb' : '1px solid #333',
              color: currentTheme === 'dark' ? '#000000' : '#ffffff'
            }}>
              <div className="dropdown-header" style={{ 
                display:'flex', 
                justifyContent:'space-between', 
                alignItems:'center', 
                background: currentTheme === 'dark' ? '#ffffff' : '#1a1a1a', 
                color: currentTheme === 'dark' ? '#000000' : '#ffffff', 
                borderBottom: currentTheme === 'dark' ? '1px solid #e5e7eb' : '1px solid #333' 
              }}>
                <span style={{ color: currentTheme === 'dark' ? '#000000' : '#ffffff', fontWeight: '600' }}>Notifications ({notifications.length})</span>
                {notifications.length > 0 && (
                  <button
                    onClick={onClearNotifications}
                    style={{ background:'transparent', border:'none', color: currentTheme === 'dark' ? '#000000' : '#ffffff', fontSize:'11px', cursor:'pointer' }}>
                    Tout effacer
                  </button>
                )}
              </div>
              {notifications.length === 0 ? (
                <div className="dropdown-item-text" style={{ 
                  color: currentTheme === 'dark' ? '#374151' : '#e5e7eb', 
                  textAlign:'center', 
                  padding:'16px', 
                  background: currentTheme === 'dark' ? '#ffffff' : '#1a1a1a' 
                }}>
                  Aucune notification
                  {onAddTestNotif && (
                    <div style={{ marginTop: '8px' }}>
                      <button onClick={onAddTestNotif} style={{
                        background:'#E8611A', color:'#fff', border:'none',
                        borderRadius:'6px', padding:'5px 12px', fontSize:'11px',
                        cursor:'pointer', fontWeight:'600'
                      }}>Simuler une erreur (test)</button>
                    </div>
                  )}
                </div>
              ) : (
                notifications.map(n => (
                  <div key={n.id} className={`dropdown-item-text notif-item ${!n.read ? 'notif-unread' : ''}`} style={{ 
                    background: !n.read 
                      ? (currentTheme === 'dark' ? 'rgba(232,97,26,0.1)' : 'rgba(232,97,26,0.2)') 
                      : (currentTheme === 'dark' ? '#ffffff' : '#1a1a1a'), 
                    borderBottom: currentTheme === 'dark' ? '1px solid #e5e7eb' : '1px solid #333' 
                  }}>
                    <span className={`notif-dot ${n.type === 'error' ? 'notif-dot-error' : 'notif-dot-task'}`}></span>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontSize:'11px', fontWeight:'600', color: n.type === 'error' ? '#ef4444' : '#f97316', marginBottom:'2px' }}>
                        {n.type === 'error' ? '⚠ Erreur jBPM' : '⏳ Tâche en attente'}
                      </div>
                      <div style={{ fontSize:'12px', color: currentTheme === 'dark' ? '#000000' : '#ffffff' }}>{n.text}</div>
                      <div style={{ fontSize:'11px', color: currentTheme === 'dark' ? '#6b7280' : '#9ca3af', marginTop:'2px' }}>{n.time}</div>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        {/* ZONE AVATAR DÉROULANT */}
        <div className="navbar-profile-container">
          <img 
            src={currentAvatar} 
            alt="Agent Avatar" 
            className="navbar-avatar-img-clickable" 
            onClick={() => { setShowProfileMenu(!showProfileMenu); setShowThemeMenu(false); setShowNotifMenu(false); }} 
          />

          {showProfileMenu && (
            <div className="profile-dropdown">
              <div className="profile-dropdown-header">
                <span className="profile-role">Connecté en tant que :</span>
                <span className="profile-username">{agentName || 'Agent Orange'}</span>
              </div>
              
              <div className="profile-dropdown-divider"></div>

              {onLogout && (
                <button className="profile-dropdown-logout-btn" onClick={onLogout}>
                  <span className="logout-icon-svg">🚪</span> Déconnexion
                </button>
              )}
            </div>
          )}
        </div>

      </div>
    </nav>
  );
}