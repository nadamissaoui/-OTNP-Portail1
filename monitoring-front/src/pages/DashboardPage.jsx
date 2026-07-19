import React, { useState, useEffect, useRef } from 'react';

import Navbar from '../components/layout/Navbar';

import Sidebar from '../components/layout/Sidebar';

import ConsultationPage from '../components/ConsultationPage';

import StatistiquePage from '../components/StatistiquesPortabilite';

import StartProcessPage from '../components/StartProcessPage';

import RecyclageMassePage from '../components/RecyclageMassePage';

import AiAnalysisPage from '../components/AiAnalysisPage';

import NotificationsPage from '../components/NotificationsPage';

import ProfessionalChatbot from '../components/chatbot/ProfessionalChatbot';



export default function DashboardPage({ username, onLogout, defaultPage }) {

  const [activeTab, setActiveTab] = useState(defaultPage || 'consultation');

  const [notifications, setNotifications] = useState(() => {

    try {

      const saved = localStorage.getItem('monitoring_notifications');

      return saved ? JSON.parse(saved) : [];

    } catch (e) {

      return [];

    }

  });



  useEffect(() => {

    localStorage.setItem('monitoring_notifications', JSON.stringify(notifications));

  }, [notifications]);



  const knownErrorIds = useRef(new Set());

  const knownTaskIds  = useRef(new Set());



  const pushBrowserNotif = (title, body) => {

    if (Notification.permission === 'granted') {

      new Notification(title, { body, icon: '/favicon.ico' });

    }

  };



  // ── Polling erreurs jBPM (toutes les 10s) ──

  useEffect(() => {

    const checkErrors = async () => {

      try {

        const res = await fetch('http://localhost:8081/api/kie/errors');

        if (!res.ok) return;

        const errors = await res.json();

        const newNotifs = [];

        errors.forEach(err => {

          const id = err.errorId || `${err.processInstanceId}_${err.errorDate}`;

          if (!knownErrorIds.current.has(id)) {

            knownErrorIds.current.add(id);

            newNotifs.push({

              id,

              type: 'error',

              title: 'Erreur jBPM',

              text: `${err.activityName || err.errorType || 'Process error'} — Instance #${err.processInstanceId}`,

              time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),

              read: false

            });

          }

        });

        if (newNotifs.length > 0) {

          setNotifications(prev => [...newNotifs, ...prev].slice(0, 30));

          pushBrowserNotif(`${newNotifs.length} nouvelle(s) erreur(s) jBPM`, newNotifs[0].text);

        }

      } catch {}

    };



    if (Notification.permission === 'default') Notification.requestPermission();



    // Init : mémoriser les erreurs déjà existantes sans notifier

    const initErrors = async () => {

      try {

        const res = await fetch('http://localhost:8081/api/kie/errors');

        if (!res.ok) return;

        const errors = await res.json();

        errors.forEach(err => {

          const id = err.errorId || `${err.processInstanceId}_${err.errorDate}`;

          knownErrorIds.current.add(id);

        });

      } catch {}

    };

    initErrors();



    const interval = setInterval(checkErrors, 10000);

    return () => clearInterval(interval);

  }, []);



  // ── Polling tâches HumanTask (toutes les 8s) ──

  useEffect(() => {

    const checkTasks = async () => {

      try {

        const res = await fetch('http://localhost:8089/api/monitoring/tasks/recyclable?page=0&size=200');

        if (!res.ok) return;

        const data = await res.json();

        const tasks = Array.isArray(data) ? data : [];

        const newNotifs = [];

        tasks.forEach(task => {

          const id = `task_${task.id}`;

          if (!knownTaskIds.current.has(id)) {

            knownTaskIds.current.add(id);

            newNotifs.push({

              id,

              type: 'task',

              title: 'Tâche en attente',

              text: `${task.name || 'HumanTask'} — Instance #${task.processInstanceId}`,

              time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),

              read: false

            });

          }

        });

        if (newNotifs.length > 0) {

          setNotifications(prev => [...newNotifs, ...prev].slice(0, 30));

          pushBrowserNotif(

            `${newNotifs.length} nouvelle(s) tâche(s) en attente`,

            newNotifs[0].text

          );

        }

      } catch {}

    };



    // Init : mémoriser les tâches déjà existantes sans notifier

    const initTasks = async () => {

      try {

        const res = await fetch('http://localhost:8089/api/monitoring/tasks/recyclable?page=0&size=200');

        if (!res.ok) return;

        const data = await res.json();

        const tasks = Array.isArray(data) ? data : [];

        tasks.forEach(task => knownTaskIds.current.add(`task_${task.id}`));

      } catch {}

    };

    initTasks();



    const interval = setInterval(checkTasks, 8000);

    return () => clearInterval(interval);

  }, []);



  const markAllRead = () => {

    setNotifications(prev => prev.map(n => ({ ...n, read: true })));

  };



  const clearNotifications = () => setNotifications([]);



  const addTestNotification = () => {

    const instanceId = Math.floor(900 + Math.random() * 100);

    const n = {

      id: `test_${Date.now()}`,

      type: 'task',

      title: 'Tâche en attente',

      text: `humanTask donor reject 1 — Instance #${instanceId}`,

      time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),

      read: false

    };

    setNotifications(prev => [n, ...prev]);

  };



  const handlePortabilityNotification = (notification) => {

    setNotifications(prev => [notification, ...prev].slice(0, 30));

  };



  const handlePageChange = (menuId) => {

    setActiveTab(menuId);

  };



  const renderContent = () => {

    switch (activeTab) {

      case 'consultation': return <ConsultationPage />;

      case 'statistique': return <StatistiquePage />;

      case 'recyclage-masse': return <RecyclageMassePage />;

      

      case 'portability-in':

        return <StartProcessPage isOut={false} onNotification={handlePortabilityNotification} />;

        

      case 'portability-out':

        return <StartProcessPage isOut={true} onNotification={handlePortabilityNotification} />;



      case 'ai-analysis':

        return <AiAnalysisPage />;



      case 'notifications':

        return (

          <NotificationsPage

            notifications={notifications}

            onMarkAllRead={markAllRead}

            onClearNotifications={clearNotifications}

          />

        );



      default: return <ConsultationPage />;

    }

  };



  return (

    <div className="dashboard-container">

      <Navbar

        onLogout={onLogout}

        agentName={username}

        notifications={notifications}

        onMarkAllRead={markAllRead}

        onClearNotifications={clearNotifications}

        onAddTestNotif={addTestNotification}

      />

      <div className="main-layout-container">

        <Sidebar

          activeMenu={activeTab}

          onMenuClick={handlePageChange}

          username={username}

        />

        <div className="content-wrapper">

          {renderContent()}

        </div>

        <ProfessionalChatbot activeDashboard={activeTab} username={username} />

      </div>

    </div>

  );

}





