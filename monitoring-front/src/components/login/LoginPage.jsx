import React, { useState } from 'react';
import StepUsername from './StepUsername';
import StepPassword from './StepPassword';
import { checkUser, login } from '../../services/authService';
import maPhoto from '../../images.jpg';
import photo from '../../nada.jpg'; 
import '../../App.css';

export default function LoginPage({ onLoginSuccess }) {
  const [step, setStep] = useState(1);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState({ text: '', type: '' });
  const [currentAvatar, setCurrentAvatar] = useState(maPhoto);

  async function handleNext() {
    if (!username.trim()) {
      setMessage({ text: "Veuillez saisir votre nom d'utilisateur.", type: 'error' });
      return;
    }
    try {
      const data = await checkUser(username);
      if (data.exists) {
        const cleanUsername = username.trim().toLowerCase();
        setCurrentAvatar(cleanUsername === 'admin' ? photo : maPhoto);
        setStep(2);
        setMessage({ text: '', type: '' });
      } else {
        setMessage({ text: data.message, type: 'error' });
      }
    } catch (err) {
      setMessage({ text: "Erreur de connexion au serveur.", type: 'error' });
    }
  }

  async function handleLogin() {
    if (!password.trim()) {
      setMessage({ text: "Veuillez saisir votre mot de passe.", type: 'error' });
      return;
    }
    try {
      const data = await login(username, password);
      if (data.success) {
        onLoginSuccess(username);
      } else {
        setMessage({ text: data.message, type: 'error' });
      }
    } catch (err) {
      setMessage({ text: "Erreur de connexion au serveur.", type: 'error' });
    }
  }

  function handleBack() {
    setStep(1);
    setPassword('');
    setMessage({ text: '', type: '' });
  }

  return (
    <div className="page login-background-cyber">
      {/* Navbar supprimée ici */}

      <div className="login-wrapper-left">
        <div className="card animated-card">
          <div className="glass-brand-header">
            <div className="logo">orange™</div>
            <span className="app-name">SmartPorta</span>
          </div>

          {step === 2 && (
            <div className="avatar">
              <img src={currentAvatar} alt="avatar" />
            </div>
          )}

          {step === 1 ? (
            <StepUsername username={username} setUsername={setUsername} message={message} onNext={handleNext} />
          ) : (
            <StepPassword username={username} password={password} setPassword={setPassword} 
                          message={message} onLogin={handleLogin} onBack={handleBack} />
          )}

          <p className="footer">Billcom consulting © 2026</p>
        </div>

        <div className="welcome-message-side">
          <h1>Bienvenue sur SmartPorta</h1>
          <p>L'espace dédié à la gestion de la portabilité.<br />
              Connectez-vous pour accéder à vos outils de pilotage.</p>
        </div>
      </div>
    </div>
  );
}