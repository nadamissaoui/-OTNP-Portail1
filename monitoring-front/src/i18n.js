import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = {
  fr: {
    translation: {
      "consultation": "Consultation",
      "statistique": "Statistique",
      "recyclage": "Recyclages en masse",
      "startProcess": "Start Process",
      "deconnexion": "Déconnexion",
      "demandesToday": "Demandes aujourd'hui",
      "tauxSucces": "Taux de succès",
      "tempsMoyen": "Temps moyen de traitement",
      "erreurs": "Bloquées / En erreur",
      "total": "Total",
      "performance": "Performance",
      "duree": "Durée",
      "critique": "Critique",
      "actualiser": "Actualiser les données",
      "demandesParJour": "Demandes par jour",
      "repartitionStatut": "Répartition par statut",
      "parOperateur": "Par opérateur",
      "enCours": "En cours",
      "termine": "Terminé",
      "annule": "Annulé"
    }
  },
  en: {
    translation: {
      "consultation": "Consultation",
      "statistique": "Statistics",
      "recyclage": "Mass Recycling",
      "startProcess": "Start Process",
      "deconnexion": "Logout",
      "demandesToday": "Today's Requests",
      "tauxSucces": "Success Rate",
      "tempsMoyen": "Average Processing Time",
      "erreurs": "Blocked / Errors",
      "total": "Total",
      "performance": "Performance",
      "duree": "Duration",
      "critique": "Critical",
      "actualiser": "Refresh Data",
      "demandesParJour": "Requests per day",
      "repartitionStatut": "Status Distribution",
      "parOperateur": "By Operator",
      "enCours": "In Progress",
      "termine": "Completed",
      "annule": "Cancelled"
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'fr',
    fallbackLng: 'fr',
    interpolation: { escapeValue: false }
  });

export default i18n;