import React, { useState, useEffect } from 'react';
import Sidebar from '../layout/Sidebar';
import ConsultationPage from '../ConsultationPage';
import StatistiquePage from '../StatistiquePage';
import StartProcessPage from './StartProcessPage'; 
import './Dashboard.css';

export default function Dashboard({ username, defaultPage = 'consultation' }) {
  const [loading, setLoading] = useState(true);
  const [currentMenu, setCurrentMenu] = useState(defaultPage);

  useEffect(() => {
    setTimeout(() => setLoading(false), 800);
  }, []);

  const handleMenuChange = (menuId) => {
    setCurrentMenu(menuId);
  };

  if (loading) {
    return (
      <div className="dashboard-wrapper">
        <div className="loading-state">
          <div className="loading-spinner"></div>
          <p>Chargement...</p>
        </div>
      </div>
    );
  }

  const renderContent = () => {
    switch (currentMenu) {
      case 'statistique':
        return <StatistiquePage />;
      case 'consultation':
        return <ConsultationPage />;
      case 'recyclage-masse':
        return (
          <div className="empty-section">
            <div className="empty-icon">♻️</div>
            <h3>Recyclages en masse</h3>
            <p>Module en cours de développement</p>
          </div>
        );

      case 'start-process':
      case 'portability-in':
        return <StartProcessPage activeSubMenu="portability-in" />;
        
      case 'portability-out':
        return <StartProcessPage activeSubMenu="portability-out" />;

      default:
        return <ConsultationPage />;
    }
  };

  return (
    <div className="dashboard-wrapper">
      <Sidebar
        activeMenu={currentMenu}
        onMenuClick={handleMenuChange}
        username={username}
      />
      <div className="dashboard-main">
        {renderContent()}
      </div>
    </div>
  );
}