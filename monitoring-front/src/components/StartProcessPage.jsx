import React, { useState, useEffect } from 'react';
import './StartProcess.css';

// ── Enveloppe SOAP IN ────────────────────────────────────────────────────────
const buildSoapEnvelopeIn = (msisdn, rioCode, client) => `
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.otnp_ws1.esprit.tn/">
  <soapenv:Header/>
  <soapenv:Body>
    <ser:startPortability>
      <msisdn>${msisdn}</msisdn>
      <rioCode>${rioCode}</rioCode>
      <client>
        <clientName>${client.clientName}</clientName>
        <cinNumber>${client.cinNumber}</cinNumber>
        <contractType>${client.contractType}</contractType>
        <idClient>${client.idClient}</idClient>
        <typeIdentite>${client.typeIdentite}</typeIdentite>
        <refCrm>${client.refCrm}</refCrm>
        <marche>${client.marche}</marche>
        <numeroOrange>${client.numeroOrange}</numeroOrange>
      </client>
    </ser:startPortability>
  </soapenv:Body>
</soapenv:Envelope>`.trim();

// ── Enveloppe SOAP OUT ───────────────────────────────────────────────────────
const buildSoapEnvelopeOut = (msisdn, rioCode) => `
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.otnp_ws1.esprit.tn/">
  <soapenv:Header/>
  <soapenv:Body>
    <ser:startPortabilityOut>
      <msisdn>${msisdn}</msisdn>
      <rioCode>${rioCode}</rioCode>
    </ser:startPortabilityOut>
  </soapenv:Body>
</soapenv:Envelope>`.trim();

// ── Parse réponse SOAP ───────────────────────────────────────────────────────
const parseSoapResponse = (xmlText) => {
  const parser = new DOMParser();
  const xml = parser.parseFromString(xmlText, 'text/xml');
  const fault = xml.querySelector('Fault');
  if (fault) {
    const faultString = fault.querySelector('faultstring')?.textContent || 'Erreur SOAP';
    const faultDetail = fault.querySelector('detail')?.textContent || '';
    
    // Détecter erreur RIO
    const combinedMessage = `${faultString} ${faultDetail}`.toLowerCase();
    
    if (combinedMessage.includes('rio') || 
        combinedMessage.includes('invalid') || 
        combinedMessage.includes('rejet')) {
      throw new Error(`RIO invalide : ${faultString}`);
    }
    
    throw new Error(faultString);
  }
  return xml.querySelector('return')?.textContent || xml.querySelector('processId')?.textContent;
};

// ── Icônes SVG inline ────────────────────────────────────────────────────────
const IconArrowRight = () => (
  <svg viewBox="0 0 24 24"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
);
const IconCheck = () => (
  <svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
);
const IconX = () => (
  <svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
);
const IconLock = () => (
  <svg viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
);
const IconPhoneOut = () => (
  <svg viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.22h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L7.91 8.81a16 16 0 0 0 6.29 6.29l.95-.95a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
);
const IconPhoneIn = () => (
  <svg viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.22h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L7.91 8.81a16 16 0 0 0 6.29 6.29l.95-.95a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/><polyline points="21 3 15 3 15 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
);

// ── RÈGLES DE VALIDATION MÉTIER ──────────────────────────────────────────────
const VALIDATION = {
  msisdn: {
    required: true,
    pattern: /^216[0-9]{8}$/,
    message: "Doit commencer par 216 + 8 chiffres (ex: 21658015941)",
    maxLength: 11
  },
  rio: {
    required: true,
    pattern: /^[A-Z]{2}[0-9]{6}$/,
    message: "2 lettres + 6 chiffres (ex: DE123456)",
    maxLength: 8
  },
  nom: {
    required: true,
    minLength: 2,
    maxLength: 50,
    pattern: /^[a-zA-ZÀ-ÿ\s-]+$/,
    message: "2 à 50 caractères, lettres uniquement"
  },
  prenom: {
    required: true,
    minLength: 2,
    maxLength: 50,
    pattern: /^[a-zA-ZÀ-ÿ\s-]+$/,
    message: "2 à 50 caractères, lettres uniquement"
  },
  idClient: {
    required: true,
    minLength: 3,
    maxLength: 20,
    pattern: /^[a-zA-Z0-9-]+$/,
    message: "Min 3 caractères, lettres/chiffres/tirets"
  },
  codeContrat: {
    required: false,
    maxLength: 20,
    message: "Max 20 caractères"
  },
  idIdentite: {
    required: false,
    message: "8 chiffres pour CIN"
  },
  refCrm: {
    required: true,
    maxLength: 30,
    minLength: 3,
    message: "ID unique de la demande CRM (généré automatiquement)"
  },
  numeroProvisoire: {
    required: false,
    pattern: /^[0-9]{8}$/,
    message: "8 chiffres (ex: 36123456)",
    maxLength: 8
  }
};

// ── Validation d'un champ unique ──
const validateField = (name, value, isOut) => {
  const rule = VALIDATION[name];
  if (!rule) return null;

  if (rule.required && (!value || value.trim() === '')) return `Champ obligatoire`;
  if (rule.pattern && value && !rule.pattern.test(value)) return rule.message;
  if (rule.minLength && value && value.trim().length < rule.minLength) return `Min ${rule.minLength} caractères`;
  return null;
};

// ── Composant Field (avec état visuel) ──
const Field = ({ label, required, error, touched, valid, children }) => {
  const cls = touched
    ? error ? 'simple-field field-error-active'
    : valid ? 'simple-field field-valid'
    : 'simple-field'
    : 'simple-field';
  return (
    <div className={cls}>
      <label>{label}{required && <span className="required"> *</span>}</label>
      {children}
      {touched && error && <span className="field-error-msg">{error}</span>}
      {touched && valid && <span className="field-valid-icon">✓</span>}
    </div>
  );
};

// ── Composant principal ──────────────────────────────────────────────────────
export default function StartProcessPage({ isOut, onNotification }) {
  const generateRefCrm = () => `NP-${Date.now().toString(36).toUpperCase()}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`;

  const [formData, setFormData] = useState({
    nom: '', prenom: '', marche: 'Mobile', idClient: '',
    typeIdentite: '', idIdentite: '', refCrm: generateRefCrm(),
    numeroProvisoire: '', msisdn: '', rio: '', codeContrat: ''
  });

  // ID Client auto-généré pour IN (pas encore de compte Orange)
  const [inIdClient] = useState(() => `TMP-IN-${Date.now().toString(36).toUpperCase()}`);

  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [msisdnChecking, setMsisdnChecking] = useState(false);
  const [msisdnExists, setMsisdnExists] = useState(false);
  const [cinChecking, setCinChecking] = useState(false);
  const [cinExists, setCinExists] = useState(false);
  const [fraudResult, setFraudResult] = useState(null);
  const [fraudChecking, setFraudChecking] = useState(false);

  // ── Vérification unicité CIN ──
  const checkCinUniqueness = async (cin) => {
    if (!cin || cin.length < 8 || formData.typeIdentite !== 'CIN') return;
    setCinChecking(true);
    setCinExists(false);
    try {
      const res = await fetch(`http://localhost:8081/api/portability/all`);
      if (!res.ok) return;
      const list = await res.json();
      const exists = list.some(p => p.cinNumber === cin && p.status !== 'CANCELED');
      setCinExists(exists);
      if (exists) {
        setErrors(prev => ({ ...prev, idIdentite: 'Ce CIN est déjà utilisé pour une autre demande' }));
      }
    } catch {}
    setCinChecking(false);
  };

  // ── Détection de fraude (CIN + nom) ──
  const checkFraud = async (cinNumber, clientName) => {
    if (!cinNumber || cinNumber.length < 8) return;
    setFraudChecking(true);
    try {
      const res = await fetch(`http://localhost:8081/api/fraud/check`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cinNumber, clientName })
      });
      if (res.ok) {
        const data = await res.json();
        setFraudResult(data);
      }
    } catch {}
    setFraudChecking(false);
  };

  // ── Validation d'un champ au blur ──
  const handleBlur = (name) => {
    setTouched(prev => ({ ...prev, [name]: true }));
    const err = validateField(name, formData[name], isOut);
    setErrors(prev => {
      if (err) return { ...prev, [name]: err };
      const n = { ...prev }; delete n[name]; return n;
    });
    if (name === 'msisdn' && formData.msisdn && /^216\d{8}$/.test(formData.msisdn)) {
      checkMsisdnUniqueness(formData.msisdn);
      const prefix = formData.msisdn.replace(/^216/, '')[0];
      if (!isOut && prefix === '5') {
        setErrors(prev => ({ ...prev, msisdn: 'Ce numéro est déjà Orange — impossible de faire un PortaIN' }));
      } else if (isOut && prefix !== '5') {
        setErrors(prev => ({ ...prev, msisdn: 'Ce numéro n\'est pas Orange — impossible de faire un PortaOUT' }));
      }
    }
    if (name === 'idIdentite' && formData.idIdentite && formData.idIdentite.length >= 8) {
      checkCinUniqueness(formData.idIdentite);
      checkFraud(formData.idIdentite, `${formData.nom} ${formData.prenom}`);
    }
  };

  // ── Vérification async MSISDN unique ──
  const checkMsisdnUniqueness = async (msisdn) => {
    setMsisdnChecking(true);
    setMsisdnExists(false);
    try {
      const res = await fetch(`http://localhost:8081/api/portability/all`);
      if (res.ok) {
        const list = await res.json();
        const exists = list.some(p => p.msisdn === msisdn && p.status !== 'CANCELED');
        setMsisdnExists(exists);
        if (exists) {
          setErrors(prev => ({ ...prev, msisdn: 'Ce numéro a déjà une demande en cours' }));
        }
      }
    } catch {}
    setMsisdnChecking(false);
  };

  const handleChange = (e) => {
    setAlert(null);
    let value = e.target.value;
    const name = e.target.name;

    if (name === 'rio') {
      value = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
    }
    if (name === 'msisdn') {
      value = value.replace(/[^0-9]/g, '');
      if (value.length > 11) return;
      setFormData(prev => ({ ...prev, msisdn: value }));
      setMsisdnExists(false);
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }

    if (errors[name]) {
      setErrors(prev => { const n = { ...prev }; delete n[name]; return n; });
    }
  };

  const validateForm = () => {
    const newErrors = {};
    const baseFields = ['msisdn', 'rio', 'nom', 'prenom', 'refCrm'];
    const outFields = [...baseFields, 'idClient'];
    const inFields = [...baseFields, 'codeContrat', 'idIdentite', 'typeIdentite', 'numeroProvisoire'];
    const fields = isOut ? outFields : inFields;

    fields.forEach(name => {
      const err = validateField(name, formData[name], isOut);
      if (err) newErrors[name] = err;
    });

    // Validation conditionnelle CIN
    if (!isOut && formData.typeIdentite === 'CIN' && formData.idIdentite && !/^\d{8}$/.test(formData.idIdentite)) {
      newErrors.idIdentite = 'Le CIN doit contenir exactement 8 chiffres';
    }

    // CIN déjà utilisé
    if (cinExists) {
      newErrors.idIdentite = 'Ce CIN est déjà utilisé pour une autre demande';
    }

    // Validation opérateur MSISDN
    if (formData.msisdn && /^216\d{8}$/.test(formData.msisdn)) {
      const prefix = formData.msisdn.replace(/^216/, '')[0];
      if (!isOut && prefix === '5') {
        newErrors.msisdn = 'Ce numéro est déjà Orange — impossible de faire un PortaIN';
      }
      if (isOut && prefix !== '5') {
        newErrors.msisdn = 'Ce numéro n\'est pas Orange — impossible de faire un PortaOUT';
      }
    }

    setErrors(newErrors);
    setTouched(prev => fields.reduce((acc, f) => ({ ...acc, [f]: true }), prev));
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // VALIDATION AVANT ENVOI
    if (!validateForm()) {
      setAlert({ 
        type: 'error', 
        message: 'Veuillez corriger les erreurs dans le formulaire' 
      });
      return;
    }
    
    // Vérification fraude avant soumission (CIN + nom)
    const fraudData = await (async () => {
      try {
        const res = await fetch(`http://localhost:8081/api/fraud/check`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            cinNumber: formData.idIdentite,
            clientName: `${formData.nom} ${formData.prenom}`
          })
        });
        if (res.ok) return await res.json();
      } catch {}
      return null;
    })();

    if (fraudData && fraudData.blocked) {
      setAlert({ type: 'error', message: '⛔ Demande bloquée — Détection de fraude: ' + (fraudData.message || 'Risque élevé') });
      setFraudResult(fraudData);
      setLoading(false);
      return;
    }
    if (fraudData && fraudData.fraud) {
      setFraudResult(fraudData);
    }

    setLoading(true);
    setAlert(null);

    try {
      let soapBody;

      if (isOut) {
        soapBody = buildSoapEnvelopeOut(formData.msisdn, formData.rio);
      } else {
        const client = {
          clientName:   `${formData.nom} ${formData.prenom}`,
          cinNumber:    formData.idIdentite,
          contractType: formData.codeContrat,
          idClient:     inIdClient,
          typeIdentite: formData.typeIdentite,
          refCrm:       formData.refCrm,
          marche:       formData.marche,
          numeroOrange: formData.numeroProvisoire
        };
        soapBody = buildSoapEnvelopeIn(formData.msisdn, formData.rio, client);
      }

      const response = await fetch('http://localhost:8089/services/OTNPService', {
        method: 'POST',
        headers: { 'Content-Type': 'text/xml;charset=UTF-8', 'SOAPAction': '""' },
        body: soapBody
      });

      const xmlText = await response.text();
      console.log('Réponse SOAP :', xmlText);

      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      const processId = parseSoapResponse(xmlText);

      // Sauvegarder les données client en base
      try {
        await fetch(`http://localhost:8081/api/portability/save`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            processInstanceId: parseInt(processId),
            msisdn:      formData.msisdn,
            clientName:  `${formData.nom} ${formData.prenom}`,
            cinNumber:   formData.idIdentite,
            contractType: formData.codeContrat,
            typeIdentite: formData.typeIdentite,
            idClient:    isOut ? formData.idClient : inIdClient,
            refCrm:      formData.refCrm,
            marche:      formData.marche,
            rioCode:     formData.rio,
            numeroOrange: formData.numeroProvisoire || null
          })
        });
      } catch (e) { console.warn('Save portability failed:', e); }

      setAlert({
        type: 'success',
        message: `Demande ${isOut ? 'OUT' : 'IN'} créée avec succès — ID processus : ${processId}`
      });

      // Envoyer une notification au parent
      if (onNotification) {
        onNotification({
          id: `portability_${Date.now()}`,
          type: 'task',
          title: `Demande ${isOut ? 'OUT' : 'IN'} créée`,
          text: `Processus #${processId} — ${formData.nom} ${formData.prenom}`,
          time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),
          read: false
        });
      }

      setFormData({
        nom: '', prenom: '', marche: 'Mobile', idClient: '',
        typeIdentite: '', idIdentite: '', refCrm: '',
        numeroProvisoire: '', msisdn: '', rio: '', codeContrat: ''
      });
      setErrors({});

    } catch (err) {
      console.error('Erreur SOAP :', err);
      
      let errorMessage = err.message;
      
      // Messages personnalisés selon le type d'erreur
      if (err.message.toLowerCase().includes('rio')) {
        errorMessage = `❌ RIO invalide auprès de l'opérateur donneur.\n\nVérifiez le code RIO saisi et réessayez.\n\nDétail : ${err.message}`;
      } else if (err.message.toLowerCase().includes('msisdn')) {
        errorMessage = `❌ MSISDN invalide.\n\nVérifiez le numéro de téléphone.\n\nDétail : ${err.message}`;
      } else if (err.message.includes('HTTP')) {
        errorMessage = `❌ Erreur de communication avec le service.\n\nVérifiez que le backend (port 8089) est démarré.\n\nDétail : ${err.message}`;
      }
      
      setAlert({ type: 'error', message: errorMessage });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="sp-container">
      <div className="sp-form-card">

        {/* ── HEADER ── */}
        <div className="sp-form-card-header">
          <div className="sp-header-icon">
            {isOut ? <IconPhoneOut /> : <IconPhoneIn />}
          </div>
          <div className="sp-header-text">
            <h2>{isOut ? 'Portabilité Sortante (OUT)' : 'Portabilité Entrante (IN)'}</h2>
            <p>{isOut
              ? 'Initier une demande de transfert vers un autre opérateur'
              : 'Créer une demande de portage de numéro entrant'}
            </p>
          </div>
        </div>

        {/* ── ALERTE ── */}
        {alert && (
          <div className={`sp-alert sp-alert-${alert.type}`}>
            {alert.type === 'success' ? <IconCheck /> : <IconX />}
            <span>{alert.message}</span>
          </div>
        )}

        {/* ── FRAUDE ── */}
        {fraudResult && fraudResult.fraud && (
          <div className={`sp-alert ${fraudResult.blocked ? 'sp-alert-error' : 'sp-alert-warning'}`}>
            <IconX />
            <div style={{ flex: 1 }}>
              <strong>⚠️ Détection de fraude</strong>
              <p style={{ margin: '4px 0 0', fontSize: '13px' }}>{fraudResult.message}</p>
              {fraudResult.alerts?.map((a, i) => (
                <div key={i} style={{ margin: '4px 0', padding: '4px 8px', background: '#fff3e0', borderRadius: 4, fontSize: '12px' }}>
                  <span style={{
                    display: 'inline-block', padding: '1px 6px', borderRadius: 3, fontSize: '11px', fontWeight: 600, marginRight: 6,
                    background: a.severity === 'HIGH' ? '#ff4444' : '#ffa726', color: '#fff'
                  }}>{a.severity}</span>
                  {a.message}
                </div>
              ))}
              {fraudResult.riskScore >= 70 && (
                <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#d32f2f', fontWeight: 600 }}>⛔ Soumission bloquée - Risque élevé</p>
              )}
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="simple-form">

          {/* ── SECTION : Informations client ── */}
          <div className="sp-section">
            <div className="sp-section-title">Informations client</div>
            <div className="sp-grid">
              <Field label="Nom" required error={errors.nom} touched={touched.nom} valid={!errors.nom && touched.nom && formData.nom?.length >= 2}>
                <input name="nom" value={formData.nom} onChange={handleChange} onBlur={() => handleBlur('nom')}
                  className={`simple-input ${errors.nom && touched.nom ? 'input-error' : ''} ${!errors.nom && touched.nom && formData.nom?.length >= 2 ? 'input-valid' : ''}`}
                  placeholder="Ex : Ben Ali" maxLength={VALIDATION.nom.maxLength} required />
              </Field>
              <Field label="Prénom" required error={errors.prenom} touched={touched.prenom} valid={!errors.prenom && touched.prenom && formData.prenom?.length >= 2}>
                <input name="prenom" value={formData.prenom} onChange={handleChange} onBlur={() => handleBlur('prenom')}
                  className={`simple-input ${errors.prenom && touched.prenom ? 'input-error' : ''} ${!errors.prenom && touched.prenom && formData.prenom?.length >= 2 ? 'input-valid' : ''}`}
                  placeholder="Ex : Mohamed" maxLength={VALIDATION.prenom.maxLength} required />
              </Field>
              {isOut ? (
                <Field label="ID Client" required error={errors.idClient} touched={touched.idClient} valid={!errors.idClient && touched.idClient && formData.idClient?.length >= 3}>
                  <input name="idClient" value={formData.idClient} onChange={(e) => {
                    const val = e.target.value.replace(/[^a-zA-Z0-9-]/g, '');
                    setFormData({ ...formData, idClient: val });
                  }} onBlur={() => handleBlur('idClient')}
                    className={`simple-input ${errors.idClient && touched.idClient ? 'input-error' : ''} ${!errors.idClient && touched.idClient && formData.idClient?.length >= 3 ? 'input-valid' : ''}`}
                    placeholder="ID BSCS du client Orange sortant" maxLength="20" />
                  <span className="field-helper">ID BSCS existant (client Orange partant)</span>
                </Field>
              ) : (
                <Field label="ID Client" required>
                  <input name="idClient" value={inIdClient} readOnly
                    className="simple-input input-valid"
                    placeholder="Généré automatiquement" maxLength="20" />
                  <span className="field-helper">ID temporaire — sera remplacé par l'ID BSCS après création du compte Orange</span>
                </Field>
              )}
              <Field label="Référence CRM" required error={errors.refCrm} touched={touched.refCrm} valid={!errors.refCrm && touched.refCrm && formData.refCrm?.length >= 3}>
                <input name="refCrm" value={formData.refCrm} readOnly
                  className={`simple-input input-valid`}
                  placeholder="Généré automatiquement" maxLength="30" />
                <span className="field-helper">Généré par le CRM Siebel lors de la création de la demande (clé de corrélation jBPM)</span>
              </Field>
              <Field label="Marché">
                <select name="marche" value={formData.marche} onChange={handleChange}
                  className="simple-input">
                  <option value="Mobile">Mobile</option>
                  <option value="GSM">GSM</option>
                  <option value="FlyBox">FlyBox</option>
                </select>
              </Field>
            </div>
          </div>

          {/* ── SECTION : Identité (IN seulement) ── */}
          {!isOut && (
            <div className="sp-section">
              <div className="sp-section-title">Identité & Contrat</div>
              <div className="sp-grid">
                <Field label="Type d'identité" error={errors.typeIdentite} touched={touched.typeIdentite}>
                  <select name="typeIdentite" value={formData.typeIdentite} onChange={handleChange} onBlur={() => handleBlur('typeIdentite')}
                    className="simple-input">
                    <option value="">-- Sélectionner --</option>
                    <option value="CIN">CIN (Carte d'Identité Nationale)</option>
                    <option value="PASSPORT">Passeport</option>
                    <option value="CARTE_SEJOUR">Carte de Séjour</option>
                  </select>
                </Field>
                <Field label="N° Identité" error={errors.idIdentite} touched={touched.idIdentite} valid={!errors.idIdentite && touched.idIdentite && formData.idIdentite?.length >= 8}>
                  <div className="field-input-wrapper">
                    <input name="idIdentite" value={formData.idIdentite} onChange={(e) => {
                      const val = e.target.value.replace(/[^a-zA-Z0-9]/g, '');
                      setFormData({ ...formData, idIdentite: val });
                      setCinExists(false);
                    }} onBlur={() => handleBlur('idIdentite')}
                      className={`simple-input ${errors.idIdentite && touched.idIdentite ? 'input-error' : ''} ${!errors.idIdentite && touched.idIdentite && formData.idIdentite?.length >= 8 ? 'input-valid' : ''}`}
                      placeholder={formData.typeIdentite === 'CIN' ? '8 chiffres (ex: 12345678)' : 'Ex : AB123456'} maxLength="12" />
                    {cinChecking && <span className="field-spinner" />}
                    {cinExists && <span className="field-warning">⚠️ CIN déjà utilisé</span>}
                  </div>
                </Field>
                <Field label="Code Contrat" error={errors.codeContrat} touched={touched.codeContrat}>
                  <input name="codeContrat" value={formData.codeContrat} onChange={handleChange} onBlur={() => handleBlur('codeContrat')}
                    className={`simple-input ${errors.codeContrat && touched.codeContrat ? 'input-error' : ''}`}
                    placeholder="Ex : HDGF4" maxLength="20" />
                </Field>
                <Field label="Numéro Orange" error={errors.numeroProvisoire} touched={touched.numeroProvisoire} valid={!errors.numeroProvisoire && touched.numeroProvisoire && formData.numeroProvisoire?.length === 8}>
                  <input name="numeroProvisoire" value={formData.numeroProvisoire} onChange={(e) => {
                    const val = e.target.value.replace(/[^0-9]/g, '');
                    if (val.length <= 8) setFormData({ ...formData, numeroProvisoire: val });
                  }} onBlur={() => handleBlur('numeroProvisoire')}
                    className={`simple-input ${errors.numeroProvisoire && touched.numeroProvisoire ? 'input-error' : ''} ${!errors.numeroProvisoire && touched.numeroProvisoire && formData.numeroProvisoire?.length === 8 ? 'input-valid' : ''}`}
                    placeholder="8 chiffres (ex: 36123456)" maxLength="8" />
                </Field>
              </div>
            </div>
          )}

          {/* ── SECTION : Données de portabilité ── */}
          <div className="sp-section">
            <div className="sp-section-title">Données de portabilité</div>
            <div className="sp-grid">
              <Field label="MSISDN" required error={errors.msisdn} touched={touched.msisdn} valid={!errors.msisdn && touched.msisdn && /^216\d{8}$/.test(formData.msisdn)}>
                <div className="field-input-wrapper">
                  <input name="msisdn" value={formData.msisdn} onChange={handleChange} onBlur={() => handleBlur('msisdn')}
                    className={`simple-input ${errors.msisdn && touched.msisdn ? 'input-error' : ''} ${!errors.msisdn && touched.msisdn && /^216\d{8}$/.test(formData.msisdn) ? 'input-valid' : ''}`}
                    placeholder="216XXXXXXXX" maxLength="11" required />
                  {msisdnChecking && <span className="field-spinner" />}
                  {msisdnExists && <span className="field-warning">⚠️ Déjà utilisé</span>}
                </div>
              </Field>
              <Field label="Code RIO" required error={errors.rio} touched={touched.rio} valid={!errors.rio && touched.rio && /^[A-Z]{2}\d{6}$/.test(formData.rio)}>
                <input name="rio" value={formData.rio} onChange={handleChange} onBlur={() => handleBlur('rio')}
                  className={`simple-input ${errors.rio && touched.rio ? 'input-error' : ''} ${!errors.rio && touched.rio && /^[A-Z]{2}\d{6}$/.test(formData.rio) ? 'input-valid' : ''}`}
                  placeholder="DE123456" maxLength="8" required />
              </Field>
            </div>
          </div>

          {/* ── BOUTON ── */}
          <button type="submit" className="submit-btn" disabled={loading || Object.keys(errors).length > 0}>
            {loading
              ? <><div className="sp-spinner" /> Traitement en cours…</>
              : <>{isOut ? 'Soumettre la demande OUT' : 'Soumettre la demande IN'} <IconArrowRight /></>
            }
          </button>

          {/* ── FOOTER ── */}
          <div className="sp-footer">
            <IconLock />
            Transmission sécurisée via SOAP/CXF — Orange Tunisie BSS
          </div>

        </form>
      </div>
    </div>
  );
}
