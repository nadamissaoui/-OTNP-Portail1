import React, { useState } from 'react';
import './Sidebar.css';

export default function Sidebar({
  activeMenu,
  onMenuClick
}) {
  const [openStartProcess, setOpenStartProcess] =
    useState(false);

  const handleStartProcessClick = () => {
    setOpenStartProcess(
      !openStartProcess
    );

    onMenuClick('portability-in');
  };

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">

        <button
          className={`sidebar-item ${
            activeMenu ===
            'consultation'
              ? 'active'
              : ''
          }`}
          onClick={() =>
            onMenuClick(
              'consultation'
            )
          }
        >
          <span className="sidebar-icon">
            🔍
          </span>
          Consultation
        </button>

        <button
          className={`sidebar-item ${
            activeMenu ===
            'statistique'
              ? 'active'
              : ''
          }`}
          onClick={() =>
            onMenuClick(
              'statistique'
            )
          }
        >
          <span className="sidebar-icon">
            📊
          </span>
          Statistique
        </button>

        <button
          className={`sidebar-item ${
            activeMenu ===
            'recyclage-masse'
              ? 'active'
              : ''
          }`}
          onClick={() =>
            onMenuClick(
              'recyclage-masse'
            )
          }
        >
          <span className="sidebar-icon">
            ♻️
          </span>
          Recyclages en masse
        </button>

        {/* START PROCESS */}
        <button
          className={`sidebar-item ${
            activeMenu ===
              'portability-in' ||
            activeMenu ===
              'portability-out'
              ? 'active'
              : ''
          }`}
          onClick={
            handleStartProcessClick
          }
        >
          <span className="sidebar-icon">
            ➕
          </span>

          Start Process

          <span className="submenu-arrow">
            {openStartProcess
              ? '▼'
              : '▶'}
          </span>
        </button>

        {/* SUBMENU */}
        {openStartProcess && (
          <div className="submenu">
            <button
              className={`submenu-item ${
                activeMenu ===
                'portability-in'
                  ? 'active'
                  : ''
              }`}
              onClick={() =>
                onMenuClick(
                  'portability-in'
                )
              }
            >
              📥 Portability IN
            </button>

            <button
              className={`submenu-item ${
                activeMenu ===
                'portability-out'
                  ? 'active'
                  : ''
              }`}
              onClick={() =>
                onMenuClick(
                  'portability-out'
                )
              }
            >
              📤 Portability OUT
            </button>
          </div>
        )}

        {/* NOTIFICATIONS */}
        <button
          className={`sidebar-item ${
            activeMenu ===
            'notifications'
              ? 'active'
              : ''
          }`}
          onClick={() =>
            onMenuClick(
              'notifications'
            )
          }
        >
          <span className="sidebar-icon">
            🔔
          </span>
          Notifications
        </button>

      </nav>
    </aside>
  );
}
