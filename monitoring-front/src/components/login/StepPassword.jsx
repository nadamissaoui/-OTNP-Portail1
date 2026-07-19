import React, { useState } from 'react';

export default function StepPassword({ username, password, setPassword, message, onLogin, onBack }) {
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  return (
    <div className="form-group">
      <p className="welcome-user">Bonjour, <strong>{username}</strong></p>
      
      <div className="input-wrapper">
        <span className="input-icon">🔒</span>
        <input
          type={showPassword ? 'text' : 'password'}
          placeholder="Mot de passe"
          value={password}
          onChange={e => setPassword(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && onLogin()}
          autoFocus
        />
        <span className="eye-icon" onClick={() => setShowPassword(!showPassword)}>
          {showPassword ? '🙈' : '👁️'}
        </span>
      </div>

      <div className="remember-forgot-container">
        <label className="remember-me-label">
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={e => setRememberMe(e.target.checked)}
            className="remember-checkbox"
          />
          <span className="checkmark"></span>
          Se souvenir du mot de passe
        </label>
        
        {/* Changé en bouton avec style de lien pour éliminer définitivement le warning ESLint */}
        <button 
          type="button" 
          className="forgot-password-link" 
          style={{ background: 'none', border: 'none', padding: 0, color: '#ff6600', cursor: 'pointer', textDecoration: 'underline' }}
          onClick={() => alert('Fonctionnalité de récupération de mot de passe à implémenter')}
        >
          Mot de passe oublié ?
        </button>
      </div>

      {message.text && (
        <p className={`message ${message.type}`}>{message.text}</p>
      )}
      
      <button className="btn-main" onClick={onLogin}>Se connecter</button>
      <button className="btn-back" onClick={onBack}>← Retour</button>
    </div>
  );
}