import React, { useState } from 'react';
import './StartProcess.css'; // On réutilise le même design

export default function PortabilityOutForm() {
  const [formData, setFormData] = useState({
    nom: '', prenom: '', idClient: '', msisdn: '', rio: '', marche: 'Mobile'
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Données envoyées :", formData);
    alert("Demande Portabilité OUT envoyée !");
  };

  return (
    <div className="sp-container">
      <div className="sp-form-card">
        <h2>Demande de Portabilité OUT</h2>
        <form onSubmit={handleSubmit} className="simple-form">
          {['nom', 'prenom', 'idClient', 'msisdn', 'rio'].map(field => (
            <div className="simple-field" key={field}>
              <label>{field.toUpperCase()}</label>
              <input type="text" name={field} onChange={handleChange} className="simple-input" />
            </div>
          ))}
          <div className="simple-field">
            <label>MARCHÉ</label>
            <select name="marche" onChange={handleChange} className="simple-input">
              <option value="Mobile">Mobile</option>
              <option value="GSM">GSM</option>
            </select>
          </div>
          <button type="submit" className="submit-btn">Envoyer</button>
        </form>
      </div>
    </div>
  );
}