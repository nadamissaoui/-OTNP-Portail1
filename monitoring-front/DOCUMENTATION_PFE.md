# Documentation du Projet PFE - Système de Monitoring de Portabilité

## 📋 Table des Matières

1. [Vue d'ensemble du projet](#vue-densemble)
2. [Architecture du système](#architecture)
3. [Composants détaillés](#composants)
4. [Flux de données](#flux)
5. [Utilité de chaque composant](#utilite)
6. [Questions possibles du jury](#questions)

---

## 🎯 Vue d'ensemble du Projet

### Contexte
Le projet consiste à développer un **système de monitoring** pour les demandes de portabilité téléphonique chez Orange Tunisie. Les clients peuvent changer d'opérateur tout en gardant leur numéro de téléphone.

### Problème résolu
- **Avant**: Pas de visibilité sur l'état des demandes de portabilité
- **Après**: Dashboard en temps réel pour surveiller, analyser et gérer les demandes

### Objectifs principaux
1. ✅ Surveiller les demandes de portabilité IN (clients qui viennent chez Orange)
2. ✅ Surveiller les demandes de portabilité OUT (clients qui quittent Orange)
3. ✅ Détecter les erreurs et les goulots d'étranglement
4. ✅ Recycler les tâches bloquées
5. ✅ Analyser les performances et tendances

---

## 🏗️ Architecture du Système

```
┌─────────────────┐
│  Portail Web    │
│  (React)        │
└────────┬────────┘
         │ SOAP
         ↓
┌─────────────────┐
│  OTNP-Portail   │
│  (SOAP Service) │
└────────┬────────┘
         │ REST
         ↓
┌─────────────────┐
│  OTNP_WS1       │
│  (WS Backend)   │
└────────┬────────┘
         │ REST
         ↓
┌─────────────────┐
│  KIE Server     │
│  (jBPM Engine)  │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Monitoring     │
│  Frontend       │
└─────────────────┘
```

---

## 📦 Composants Détaillés

### 1. Portail Web (React)
**Emplacement**: `c:\Users\lenovo\Desktop\OTNP-Portail1\OTNP-Portail`

**Utilité**: Interface pour les clients de Orange Tunisie

**Fonctionnalités**:
- Formulaire de demande de portabilité IN
- Formulaire de demande de portabilité OUT
- Saisie des informations client (nom, prénom, RIO, numéro)

**Comment ça marche**:
1. Le client remplit le formulaire
2. Les données sont envoyées via SOAP au backend
3. Le backend crée une instance de processus dans jBPM

---

### 2. OTNP-Portail (SOAP Service)
**Emplacement**: `c:\Users\lenovo\Desktop\OTNP-Portail1\OTNP-Portail\npservice-module`

**Utilité**: Service SOAP qui reçoit les demandes du portail

**Fonctionnalités**:
- Endpoint SOAP pour créer des demandes IN
- Endpoint SOAP pour créer des demandes OUT
- Communication avec KIE Server pour démarrer les processus

**Comment ça marche**:
```java
// Le service SOAP reçoit la demande
@WebMethod
public String startPortabilityIn(DemandeDTO demande) {
    // Envoie le signal à KIE Server
    kieServerClient.startProcess("portabilityin_1.0.0-SNAPSHOT", variables);
    return "Demande créée avec succès";
}
```

---

### 3. OTNP_WS1 (WS Backend)
**Emplacement**: `c:\Users\lenovo\Desktop\OTNP-Portail1\OTNP_WS1`

**Utilité**: Backend REST pour le monitoring

**Fonctionnalités**:
- Récupération des statistiques (IN vs OUT)
- Récupération des tâches recyclables
- Recyclage des tâches bloquées
- Analyse des tendances saisonnières
- Envoi de SMS aux clients

**Comment ça marche**:
```java
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {
    
    // Statistiques générales
    @GetMapping("/statistics/performance")
    public ResponseEntity<?> getStatistiques() {
        return ResponseEntity.ok(monitoringService.getStatistiques());
    }
    
    // Tâches recyclables
    @GetMapping("/tasks/recyclable")
    public ResponseEntity<?> getRecyclableTasks(...) {
        return ResponseEntity.ok(monitoringService.getRecyclableTasks(...));
    }
}
```

---

### 4. KIE Server (jBPM Engine)
**Utilité**: Moteur de workflow pour gérer les processus de portabilité

**Fonctionnalités**:
- Exécution des workflows de portabilité
- Gestion des tâches humaines
- Suivi de l'état des processus
- Historique des nœuds parcourus

**Workflow de portabilité**:
```
Start → Création → Validation RIO → Portage → Activation → End
```

---

### 5. Monitoring Frontend (React)
**Emplacement**: `c:\Users\lenovo\monitoring-front`

**Utilité**: Interface de monitoring pour les superviseurs

**Fonctionnalités**:
- Dashboard avec statistiques en temps réel
- Table des instances actives
- Page de recyclage des tâches
- Graphiques de tendances
- Notifications en temps réel
- Analyse des erreurs avec IA

**Comment ça marche**:
- Le frontend appelle les endpoints REST de OTNP_WS1
- Affiche les données dans des graphiques (Recharts)
- Met à jour les données automatiquement (polling)

---

## 🔄 Flux de Données

### Scénario 1: Client crée une demande de portabilité

```
1. Client remplit le formulaire (Portail Web)
   ↓
2. Données envoyées via SOAP (OTNP-Portail)
   ↓
3. SOAP Service appelle KIE Server
   ↓
4. KIE Server crée une instance de processus
   ↓
5. Le processus avance dans le workflow
   ↓
6. Monitoring Frontend affiche l'état en temps réel
```

### Scénario 2: Superviseur recycle une tâche bloquée

```
1. Superviseur voit une tâche bloquée (Monitoring Frontend)
   ↓
2. Il sélectionne la tâche et clique "Recycle"
   ↓
3. Frontend envoie la demande à OTNP_WS1
   ↓
4. OTNP_WS1 appelle KIE Server pour remettre la tâche en état "Ready"
   ↓
5. La tâche peut être traitée à nouveau
```

---

## 💡 Utilité de Chaque Composant

| Composant | Utilité | Bénéfice |
|-----------|---------|----------|
| **Portail Web** | Interface client | Permet aux clients de créer des demandes facilement |
| **SOAP Service** | Communication inter-systèmes | Standard industriel pour l'intégration |
| **OTNP_WS1** | Backend monitoring | Centralise toutes les données de monitoring |
| **KIE Server** | Moteur de workflow | Automatise les processus de portabilité |
| **Monitoring Frontend** | Interface superviseur | Visibilité en temps réel sur le système |

---

## 🎓 Questions Possibles du Jury

### Questions techniques

**Q1: Pourquoi avoir choisi SOAP pour le portail et REST pour le monitoring?**
- **Réponse**: SOAP est un standard pour les intégrations B2B (Business-to-Business). REST est plus moderne et léger pour les applications web modernes comme le monitoring.

**Q2: Comment jBPM gère-t-il les workflows de portabilité?**
- **Réponse**: jBPM exécute des processus définis en BPMN. Chaque étape (validation RIO, portage, activation) est un nœud dans le workflow. Le moteur gère automatiquement les transitions entre les étapes.

**Q3: Comment le système détecte les erreurs?**
- **Réponse**: Le système interroge régulièrement la base de données jBPM pour trouver les processus en erreur (status = 3 ou 5). Ces erreurs sont affichées dans le dashboard et analysées par l'IA.

**Q4: Pourquoi utiliser React pour le frontend?**
- **Réponse**: React permet de créer des interfaces réactives avec des mises à jour en temps réel. Les composants sont réutilisables et la maintenance est facilitée.

**Q5: Comment fonctionne le recyclage des tâches?**
- **Réponse**: Quand une tâche est bloquée, le superviseur peut la remettre en état "Ready" via l'API. Cela permet de retraiter la tâche sans recréer toute la demande.

### Questions fonctionnelles

**Q6: Quel est l'apport de ce système pour Orange Tunisie?**
- **Réponse**: 
  - Visibilité en temps réel sur les demandes
  - Détection rapide des problèmes
  - Amélioration du taux de succès
  - Réduction du temps de traitement

**Q7: Comment le système aide-t-il à respecter les SLA?**
- **Réponse**: Le système alerte quand une demande dépasse le délai SLA (48h). Les graphiques montrent les goulots d'étranglement pour optimiser le processus.

**Q8: Quelles sont les limites du système actuel?**
- **Réponse**: 
  - Dépendance à KIE Server
  - Pas de prédiction en temps réel (analyse post-hoc)
  - Interface uniquement en français

### Questions d'implémentation

**Q9: Quels défis avez-vous rencontrés?**
- **Réponse**: 
  - Compatibilité Java version (Java 21 vs Spring Boot 3)
  - Compréhension de l'API jBPM
  - Intégration SOAP/REST
  - Gestion des erreurs asynchrones

**Q10: Comment avez-vous testé le système?**
- **Réponse**: 
  - Tests unitaires pour les services
  - Tests d'intégration pour les API
  - Tests manuels sur l'environnement de développement
  - Simulation de scénarios réels

---

## 📊 Statistiques Clés à Présenter

### KPIs du système
- **Taux de succès**: % de demandes terminées avec succès
- **Temps moyen de traitement**: Durée moyenne d'une demande
- **Tâches recyclées**: Nombre de tâches remises en traitement
- **Erreurs détectées**: Nombre d'erreurs identifiées

### Comparaison IN vs OUT
- Les demandes IN sont généralement plus rapides
- Les demandes OUT nécessitent plus de validation
- Le système permet d'identifier les différences

---

## 🚀 Améliorations Futures

1. **Analyse prédictive en temps réel**: Prédire les délais avant qu'ils ne surviennent
2. **Alertes intelligentes**: Notifications automatiques pour les problèmes critiques
3. **Interface multilingue**: Support de l'arabe et de l'anglais
4. **Mobile app**: Application mobile pour les superviseurs
5. **Machine Learning**: Améliorer les recommandations avec plus de données

---

## 📝 Conclusion

Ce projet a permis de créer un système complet de monitoring pour les demandes de portabilité chez Orange Tunisie. Le système offre une visibilité en temps réel, permet de détecter rapidement les problèmes, et fournit des outils d'analyse pour améliorer continuellement le processus.

**Points forts**:
- ✅ Architecture modulaire et extensible
- ✅ Interface utilisateur intuitive
- ✅ Intégration réussie de technologies hétérogènes
- ✅ Fonctionnalités avancées d'analyse

**Impact métier**:
- Amélioration du taux de succès des portabilités
- Réduction du temps de traitement
- Meilleure satisfaction client
- Optimisation des ressources

---

## 🎯 Conseils pour la Présentation

1. **Commencez par le contexte**: Expliquez ce qu'est la portabilité téléphonique
2. **Présentez le problème**: Pourquoi le monitoring était nécessaire
3. **Montrez l'architecture**: Utilisez le diagramme fourni
4. **Démontrez le système**: Montrez le dashboard en action
5. **Expliquez les choix techniques**: Justifiez SOAP, REST, React, jBPM
6. **Soyez prêt aux questions**: Révisez les questions possibles ci-dessus
7. **Terminez avec les perspectives**: Parlez des améliorations futures

**Bonne chance pour votre présentation! 🎓**
