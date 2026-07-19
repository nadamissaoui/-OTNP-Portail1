import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/login/LoginPage';
import DashboardPage from './pages/DashboardPage';
import PageTransition from './components/PageTransition';
import './App.css';
import AiAnalysisPage from './components/AiAnalysisPage';
import ErrorReportPage from './components/ErrorReportPage';
import FeedbackPage from './components/FeedbackPage';
function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [username, setUsername] = useState('');
  const [triggerTransition, setTriggerTransition] = useState(false);
  const [defaultPage, setDefaultPage] = useState('consultation');

  const handleLoginSuccess = (user) => {
    setUsername(user);
    setTriggerTransition(true);
    
    // Transition fluide vers le dashboard
    setTimeout(() => {
      setIsAuthenticated(true);
      setTriggerTransition(false);
    }, 3000);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setUsername('');
    setTriggerTransition(false);
    setDefaultPage('consultation');
  };

  return (
    <Router>
      <div className="App">
        <PageTransition trigger={triggerTransition}>
          <Routes>
            {/* Route Connexion : La Navbar n'est plus appelée ici */}
            <Route 
              path="/login" 
              element={
                isAuthenticated ? 
                <Navigate to="/dashboard" /> : 
                <LoginPage onLoginSuccess={handleLoginSuccess} />
              } 
            />
            
            <Route path="/ai-analysis" element={<AiAnalysisPage />} />

            <Route path="/feedback/:processInstanceId" element={<FeedbackPage />} />

            
            {/* Route Tableau de Bord : La Navbar sera appelée à l'intérieur de DashboardPage */}
            <Route 
              path="/dashboard" 
              element={
                isAuthenticated ? 
                <DashboardPage 
                  username={username} 
                  onLogout={handleLogout}
                  defaultPage={defaultPage}
                /> : 
                <Navigate to="/login" />
              } 
            />
            
            {/* Redirection par défaut */}
            <Route path="/" element={<Navigate to="/login" />} />
          </Routes>
        </PageTransition>
      </div>
    </Router>
  );
}

export default App;