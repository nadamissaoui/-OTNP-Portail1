import React, { useState, useEffect } from 'react';

export default function PageTransition({ children, trigger }) {
  const [isTransitioning, setIsTransitioning] = useState(false);
  const [phase, setPhase] = useState('idle');

  useEffect(() => {
    if (trigger) {
      startTransition();
    }
  }, [trigger]);

  const startTransition = () => {
    setIsTransitioning(true);
    setPhase('loading');
    
    setTimeout(() => {
      setPhase('transitioning');
    }, 1500);
    
    setTimeout(() => {
      setIsTransitioning(false);
      setPhase('idle');
    }, 3000);
  };

  if (!isTransitioning) {
    return children;
  }

  return (
    <div className={`page-transition-container ${phase}`}>
      <div className="transition-overlay">
        <div className="transition-content">
          <div className="transition-logo">
            <div className="logo-circle">
              <span className="logo-text">orange™</span>
            </div>
          </div>
          
          <div className="transition-messages">
            <p className="message-1">Authentification réussie</p>
            <p className="message-2">Chargement de votre espace</p>
            <p className="message-3">Préparation du dashboard</p>
          </div>
          
          <div className="progress-container">
            <div className="progress-bar">
              <div className="progress-fill"></div>
            </div>
            <div className="progress-dots">
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}