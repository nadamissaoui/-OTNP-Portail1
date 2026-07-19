import React from 'react';

export default function StepUsername({ username, setUsername, message, onNext }) {
  return (
    <div className="form-group">
      <div className="input-wrapper">
        <span className="input-icon">👤</span>
        <input
          type="text"
          placeholder="Nom d'utilisateur"
          value={username}
          onChange={e => setUsername(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && onNext()}
          autoFocus
        />
      </div>
      {message.text && (
        <p className={`message ${message.type}`}>{message.text}</p>
      )}
      <button className="btn-main" onClick={onNext}>Suivant</button>
    </div>
  );
}