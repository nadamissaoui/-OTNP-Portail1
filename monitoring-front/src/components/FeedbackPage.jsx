import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

export default function FeedbackPage() {
  const { processInstanceId } = useParams();
  const [loading, setLoading] = useState(true);
  const [instance, setInstance] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!processInstanceId) return;
    const load = async () => {
      try {
        const [fbRes, searchRes] = await Promise.all([
          fetch(`http://localhost:8081/api/feedback/${processInstanceId}`),
          fetch(`http://localhost:8081/api/monitoring/search?processId=${processInstanceId}`)
        ]);
        if (fbRes.ok) {
          const fb = await fbRes.json();
          if (fb.exists !== false) setFeedback(fb);
        }
        if (searchRes.ok) {
          const results = await searchRes.json();
          if (Array.isArray(results) && results.length > 0) setInstance(results[0]);
        }
      } catch {}
      setLoading(false);
    };
    load();
  }, [processInstanceId]);

  const handleSubmit = async () => {
    if (rating === 0) return;
    setSubmitting(true);
    setError(null);
    try {
      const res = await fetch('http://localhost:8081/api/feedback/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          processInstanceId: Number(processInstanceId),
          msisdn: instance?.variables?.msisdn || instance?.variables?.phoneNumber || '',
          rating,
          comment,
          source: 'client'
        })
      });
      if (res.ok) {
        setSubmitted(true);
      } else {
        const err = await res.json();
        setError(err.error || 'Erreur lors de l\'envoi');
      }
    } catch {
      setError('Erreur réseau');
    }
    setSubmitting(false);
  };

  const styles = {
    page: {
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      padding: 20
    },
    card: {
      background: '#fff',
      borderRadius: 16,
      padding: 40,
      maxWidth: 480,
      width: '100%',
      boxShadow: '0 20px 60px rgba(0,0,0,0.3)',
      textAlign: 'center'
    },
    icon: {
      width: 64,
      height: 64,
      background: '#ff6600',
      borderRadius: 16,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontSize: 28,
      margin: '0 auto 16px'
    },
    title: { fontSize: 22, fontWeight: 700, color: '#1a1a2e', margin: '0 0 4px' },
    subtitle: { fontSize: 14, color: '#8a8580', margin: '0 0 20px' },
    info: { fontSize: 13, color: '#555', margin: '0 0 20px', lineHeight: 1.6 },
    stars: { display: 'flex', gap: 6, justifyContent: 'center', marginBottom: 20 },
    star: { fontSize: 32, cursor: 'pointer', transition: 'color 0.15s', background: 'none', border: 'none', padding: 0 },
    starFilled: { color: '#f5a623' },
    starEmpty: { color: '#ddd' },
    textarea: {
      width: '100%', padding: 10, border: '1px solid #d0d0d0', borderRadius: 8,
      fontSize: 14, resize: 'vertical', boxSizing: 'border-box', marginBottom: 16
    },
    btn: {
      width: '100%', padding: '12px 0', background: '#ff6600', color: '#fff',
      border: 'none', borderRadius: 8, fontSize: 15, fontWeight: 600,
      cursor: 'pointer', transition: 'background 0.15s'
    },
    btnDisabled: { background: '#ccc', cursor: 'default' },
    error: { color: '#dc2626', fontSize: 13, marginTop: 8 },
    thanks: { fontSize: 16, color: '#1a6b3c', margin: '16px 0 0' }
  };

  if (loading) {
    return (
      <div style={styles.page}>
        <div style={styles.card}>
          <p style={{ color: '#999' }}>Chargement...</p>
        </div>
      </div>
    );
  }

  if (feedback || submitted) {
    return (
      <div style={styles.page}>
        <div style={styles.card}>
          <div style={{ fontSize: 64, marginBottom: 12 }}>🎉</div>
          <h1 style={styles.title}>Merci !</h1>
          <p style={styles.info}>
            Votre avis a bien été enregistré. Il nous aide à améliorer notre service.
          </p>
          {feedback && (
            <div>
              <div style={{ ...styles.stars, cursor: 'default' }}>
                {[1,2,3,4,5].map(s => (
                  <span key={s} style={{ ...styles.star, ...(s <= feedback.rating ? styles.starFilled : styles.starEmpty), cursor: 'default' }}>★</span>
                ))}
              </div>
              {feedback.comment && <p style={{ fontStyle: 'italic', color: '#555' }}>"{feedback.comment}"</p>}
            </div>
          )}
        </div>
      </div>
    );
  }

  const msisdn = instance?.variables?.msisdn || instance?.variables?.phoneNumber || '';
  const clientName = instance?.variables?.clientName || '';

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.icon}>⭐</div>
        <h1 style={styles.title}>Votre avis compte</h1>
        <p style={styles.subtitle}>Comment s'est passée votre portabilité chez Orange ?</p>
        {clientName && <p style={styles.info}>Bonjour <strong>{clientName}</strong></p>}
        {msisdn && <p style={styles.info}>Numéro : <strong>{msisdn}</strong></p>}

        <div style={styles.stars}>
          {[1,2,3,4,5].map(s => (
            <button key={s} style={{ ...styles.star, ...(s <= rating ? styles.starFilled : styles.starEmpty) }}
              onClick={() => setRating(s)}>★</button>
          ))}
        </div>

        <textarea style={styles.textarea} rows={3} placeholder="Votre commentaire (optionnel)..."
          value={comment} onChange={e => setComment(e.target.value)} />

        <button style={{ ...styles.btn, ...(rating === 0 || submitting ? styles.btnDisabled : {}) }}
          onClick={handleSubmit} disabled={rating === 0 || submitting}>
          {submitting ? 'Envoi...' : 'Envoyer mon avis'}
        </button>

        {error && <p style={styles.error}>{error}</p>}
      </div>
    </div>
  );
}
