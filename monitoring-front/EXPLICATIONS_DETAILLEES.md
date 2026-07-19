# Explications Détaillées du Système - Pour Présentation PFE

## 📚 Table des Matières

1. [OTNP Portail en SOAP](#otnp-portail-soap)
2. [Relation OTNP Portail ↔ jBPM](#relation-otnp-jbpm)
3. [Relation WS Back ↔ OTNP Portail](#relation-ws-otnp)
4. [Relation Monitoring Front ↔ Monitoring Back](#relation-monitoring)
5. [Déroulement Complet du Système](#deroulement)

---

## 1. OTNP Portail en SOAP

### 🤔 Pourquoi SOAP?

**SOAP (Simple Object Access Protocol)** a été choisi pour le portail pour plusieurs raisons:

#### Raison 1: Standard d'Entreprise
- SOAP est le standard **B2B (Business-to-Business)** le plus utilisé
- Les opérateurs téléphoniques utilisent SOAP pour les intégrations
- Garantit l'interopérabilité entre systèmes hétérogènes

#### Raison 2: Sécurité et Fiabilité
- **WS-Security**: Support natif pour la sécurité (signature, encryption)
- **WS-ReliableMessaging**: Garantit que les messages sont livrés
- **Transaction support**: Gestion des transactions distribuées

#### Raison 3: Contrat WSDL
- **WSDL (Web Service Description Language)**: Définit précisément le service
- Les clients savent exactement quoi envoyer et quoi recevoir
- Validation automatique des données

#### Raison 4: Legacy Systems
- Les systèmes existants chez Orange Tunisie utilisent SOAP
- Facilite l'intégration avec les systèmes CRM, facturation, etc.

---

### 🔧 Comment ça marche?

#### Architecture SOAP du Portail

```
┌─────────────────────────────────────────┐
│         Portail Web (React)            │
│  - Formulaire de demande               │
│  - Validation des données              │
└──────────────┬──────────────────────────┘
               │
               │ HTTP POST
               │ Envelope SOAP
               ↓
┌─────────────────────────────────────────┐
│      OTNP-Portail (SOAP Service)        │
│  - Endpoint: /ws/portability           │
│  - Reçoit l'envelope SOAP              │
│  - Extrait les données                  │
│  - Valide le RIO                       │
└──────────────┬──────────────────────────┘
               │
               │ Appel KIE Server
               │ REST API
               ↓
┌─────────────────────────────────────────┐
│         KIE Server (jBPM)               │
│  - Démarre le processus                 │
│  - Crée l'instance de workflow          │
└─────────────────────────────────────────┘
```

#### Exemple de Requête SOAP

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:startPortabilityIn xmlns:ns2="http://tn.esprit/">
            <demande>
                <nom>Ben Ali</nom>
                <prenom>Ahmed</prenom>
                <msisdn>21620123456</msisdn>
                <rio>12345678901234</rio>
                <marche>PREPAID</marche>
            </demande>
        </ns2:startPortabilityIn>
    </soap:Body>
</soap:Envelope>
```

#### Exemple de Réponse SOAP

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:startPortabilityInResponse xmlns:ns2="http://tn.esprit/">
            <return>SUCCESS</return>
            <processId>12345</processId>
            <message>Demande créée avec succès</message>
        </ns2:startPortabilityInResponse>
    </soap:Body>
</soap:Envelope>
```

---

### 📁 Structure du Projet OTNP-Portail

```
OTNP-Portail/
├── OTNP-Portail/              # Application Web (React)
│   ├── src/
│   │   ├── components/        # Composants React
│   │   ├── services/          # Services SOAP
│   │   └── App.jsx            # Application principale
│
├── npservice-module/          # Module SOAP Service
│   ├── src/main/java/
│   │   └── tn/esprit/
│   │       └── npservicemodule/
│   │           ├── service/
│   │           │   └── WorkflowService.java  # Service SOAP
│   │           └── config/
│   │               └── KieServerConfig.java   # Configuration KIE
│
└── crm-module/                # Module CRM (intégration)
    └── src/main/java/
        └── tn/esprit/
            └── crmmodule/
                └── dto/
                    └── ProcessVariablesDTO.java
```

---

### 💻 Code Clé - WorkflowService.java

```java
@Service
public class WorkflowService {
    
    @Autowired
    private KieServicesClient kieServicesClient;
    
    /**
     * Démarre un processus de portabilité IN
     * @param variables Variables du processus (nom, prenom, RIO, etc.)
     * @return ID de l'instance de processus
     */
    public Long startPortabilityIn(Map<String, Object> variables) {
        // 1. Récupérer le client de processus KIE
        ProcessServicesClient processClient = 
            kieServicesClient.getServicesClient(ProcessServicesClient.class);
        
        // 2. Démarrer le processus avec les variables
        Long processInstanceId = processClient.startProcess(
            "portabilityin_1.0.0-SNAPSHOT",  // ID du conteneur
            "PortabilityIN",                 // ID du processus
            variables                        // Variables du processus
        );
        
        // 3. Logger le démarrage
        log.info("Processus IN démarré: ID = {}", processInstanceId);
        
        return processInstanceId;
    }
}
```

---

## 2. Relation OTNP Portail ↔ jBPM

### 🔗 Pourquoi la relation est nécessaire?

jBPM (Java Business Process Management) est le **moteur de workflow** qui exécute les processus de portabilité. OTNP Portail est l'**interface** qui permet de démarrer ces processus.

### 📊 Diagramme de la Relation

```
┌──────────────────────┐
│  OTNP Portail        │
│  (SOAP Service)     │
└──────────┬───────────┘
           │
           │ 1. Reçoit la demande SOAP
           │ 2. Extrait les variables
           │ 3. Appelle KIE Server
           ↓
┌──────────────────────┐
│  KIE Server          │
│  (jBPM Engine)       │
└──────────┬───────────┘
           │
           │ 4. Crée l'instance de processus
           │ 5. Exécute le workflow
           │ 6. Retourne l'ID d'instance
           ↓
┌──────────────────────┐
│  Base de données     │
│  jBPM                │
└──────────────────────┘
```

### 🔄 Flux de Communication

#### Étape 1: Client envoie la demande
```
Client → Portail Web → SOAP Service
```

#### Étape 2: SOAP Service traite la demande
```java
// WorkflowService.java
public Long startPortabilityIn(DemandeDTO demande) {
    // Convertir DTO en Map de variables
    Map<String, Object> variables = new HashMap<>();
    variables.put("nom", demande.getNom());
    variables.put("prenom", demande.getPrenom());
    variables.put("msisdn", demande.getMsisdn());
    variables.put("rio", demande.getRio());
    variables.put("marche", demande.getMarche());
    
    // Appeler KIE Server
    return startProcess(variables);
}
```

#### Étape 3: KIE Server crée le processus
```
KIE Server reçoit la demande
  ↓
Charge la définition BPMN
  ↓
Crée une instance de processus
  ↓
Exécute le premier nœud (Start)
  ↓
Avance vers le nœud suivant (Validation RIO)
  ↓
Retourne l'ID de l'instance
```

#### Étape 4: jBPM stocke les données
```
Base de données jBPM:
  - processinstancelog: Historique des instances
  - task: Tâches humaines
  - nodeinstancelog: Historique des nœuds
  - variableinstancelog: Variables du processus
```

### 🎯 Pourquoi jBPM?

#### Raison 1: Automatisation des Workflows
- Les processus de portabilité sont complexes
- Plusieurs étapes: Validation → Portage → Activation
- jBPM automatise les transitions entre étapes

#### Raison 2: Gestion des Tâches Humaines
- Certaines étapes nécessitent une intervention humaine
- jBPM gère l'assignation des tâches aux utilisateurs
- Suivi de l'état des tâches (Ready, Reserved, InProgress, Completed)

#### Raison 3: Visibilité et Traçabilité
- Historique complet de chaque processus
- Temps passé dans chaque étape
- Identification des goulots d'étranglement

#### Raison 4: Flexibilité
- Modification des workflows sans changer le code
- Ajout de nouvelles étapes facilement
- Support des processus complexes (parallélisme, conditions)

---

### 📝 Exemple de Workflow BPMN

```xml
<process id="PortabilityIN" name="Portabilité IN">
    <!-- Start Node -->
    <startEvent id="start" name="Début"/>
    
    <!-- Task: Validation RIO -->
    <userTask id="validateRio" name="Validation RIO">
        <potentialOwner>admin</potentialOwner>
    </userTask>
    
    <!-- Task: Portage -->
    <serviceTask id="porting" name="Portage">
        <extensionElements>
            <onEntry-script>
                System.out.println("Début du portage");
            </onEntry-script>
        </extensionElements>
    </serviceTask>
    
    <!-- Task: Activation -->
    <serviceTask id="activation" name="Activation"/>
    
    <!-- End Node -->
    <endEvent id="end" name="Fin"/>
    
    <!-- Transitions -->
    <sequenceFlow source="start" target="validateRio"/>
    <sequenceFlow source="validateRio" target="porting"/>
    <sequenceFlow source="porting" target="activation"/>
    <sequenceFlow source="activation" target="end"/>
</process>
```

---

## 3. Relation WS Back ↔ OTNP Portail

### 🔗 Nature de la Relation

**WS Back (OTNP_WS1)** et **OTNP Portail** sont deux applications **indépendantes** qui communiquent avec le même **KIE Server**.

```
┌─────────────────┐
│  OTNP Portail   │
│  (SOAP)         │
└────────┬────────┘
         │
         │ REST API
         ↓
┌─────────────────┐
│  KIE Server     │
│  (jBPM)         │
└────────┬────────┘
         ↑
         │ REST API
         │
┌─────────────────┐
│  OTNP_WS1       │
│  (REST)         │
└─────────────────┘
```

### 🤔 Pourquoi deux applications séparées?

#### OTNP Portail: Création des demandes
- **Rôle**: Interface pour créer des demandes
- **Protocole**: SOAP (standard B2B)
- **Utilisateurs**: Clients Orange Tunisie
- **Fonction principale**: Démarrer les processus

#### OTNP_WS1: Monitoring des demandes
- **Rôle**: Interface pour surveiller les demandes
- **Protocole**: REST (moderne, léger)
- **Utilisateurs**: Superviseurs, administrateurs
- **Fonction principale**: Analyser et gérer les processus

### 📊 Comparaison des Deux Applications

| Caractéristique | OTNP Portail | OTNP_WS1 |
|----------------|--------------|----------|
| **Protocole** | SOAP | REST |
| **Objectif** | Créer des demandes | Surveiller les demandes |
| **Utilisateurs** | Clients | Superviseurs |
| **Interface** | Web (React) | Dashboard (React) |
| **Actions** | Start Process | Read, Recycle, Analyze |

### 🔗 Communication Indirecte

Les deux applications ne communiquent **pas directement** entre elles. Elles communiquent **indirectement** via KIE Server:

```
Scénario: Client crée une demande → Superviseur la surveille

1. Client → OTNP Portail → KIE Server
   (Création de la demande)

2. OTNP_WS1 → KIE Server
   (Lecture de l'état de la demande)

3. OTNP_WS1 → Monitoring Frontend
   (Affichage des données)
```

### 💡 Pourquoi cette architecture?

#### Avantage 1: Séparation des responsabilités
- OTNP Portail se concentre sur la création
- OTNP_WS1 se concentre sur le monitoring
- Chaque application a une responsabilité unique

#### Avantage 2: Indépendance
- Si OTNP Portail est en maintenance, OTNP_WS1 continue de fonctionner
- Les deux applications peuvent évoluer indépendamment

#### Avantage 3: Sécurité
- Les clients n'ont accès qu'à OTNP Portail
- Les superviseurs ont accès à OTNP_WS1
- Séparation des droits d'accès

---

## 4. Relation Monitoring Front ↔ Monitoring Back

### 🔗 Architecture Client-Serveur

```
┌─────────────────────────┐
│  Monitoring Frontend    │
│  (React Application)    │
│  - Dashboard            │
│  - Graphiques           │
│  - Tables               │
└──────────┬──────────────┘
           │
           │ HTTP/REST
           │ JSON
           ↓
┌─────────────────────────┐
│  Monitoring Back       │
│  (Spring Boot)         │
│  - REST Controllers     │
│  - Services            │
│  - Repositories        │
└──────────┬──────────────┘
           │
           │ REST API
           ↓
┌─────────────────────────┐
│  KIE Server             │
│  (jBPM)                 │
└─────────────────────────┘
```

### 🔄 Communication REST

#### Format des Requêtes

```http
GET /api/monitoring/statistics/performance
Host: localhost:8089
Accept: application/json
```

#### Format des Réponses

```json
{
  "PortabilityIN": {
    "total": 150,
    "completed": 120,
    "aborted": 20,
    "active": 10
  },
  "PortabilityOUT": {
    "total": 100,
    "completed": 80,
    "aborted": 15,
    "active": 5
  },
  "successRateIN": 80.0,
  "successRateOUT": 80.0
}
```

### 📁 Structure Monitoring Frontend

```
monitoring-front/
├── src/
│   ├── components/
│   │   ├── StatistiquesPortabilite.jsx  # Dashboard principal
│   │   ├── RecyclageMassePage.jsx       # Page de recyclage
│   │   ├── AiAnalysisPage.jsx           # Analyse IA
│   │   ├── NotificationsPage.jsx        # Page de notifications
│   │   └── layout/
│   │       ├── Navbar.jsx               # Barre de navigation
│   │       └── Sidebar.jsx              # Menu latéral
│   ├── pages/
│   │   └── DashboardPage.jsx            # Page principale
│   └── services/
│       └── aiAnalysisService.js          # Service IA
```

### 📁 Structure Monitoring Back

```
monitoring-back/
└── monitoring-back/
    └── src/main/java/
        └── com/monitoring/
            ├── controller/
            │   ├── MonitoringController.java    # Endpoints REST
            │   └── KPIController.java          # KPIs
            ├── service/
            │   ├── ProcessMonitoringService.java # Service monitoring
            │   └── WsMonitoringClient.java      # Client WS
            └── repository/
                └── ProcessInstanceRepository.java # Accès données
```

### 💻 Exemple de Code

#### MonitoringController.java (Backend)

```java
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    @Autowired
    private ProcessMonitoringService monitoringService;
    
    /**
     * Récupère les statistiques de performance
     */
    @GetMapping("/statistics/performance")
    public ResponseEntity<?> getStatistiques() {
        return ResponseEntity.ok(monitoringService.getStatistiques());
    }
    
    /**
     * Récupère les tâches recyclables
     */
    @GetMapping("/tasks/recyclable")
    public ResponseEntity<?> getRecyclableTasks(
            @RequestParam(required = false) String containerId,
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        List<Map<String, Object>> tasks = monitoringService.getRecyclableTasks(
                containerId, processInstanceId, dateDebut, dateFin, page, size
        );
        return ResponseEntity.ok(tasks);
    }
}
```

#### StatistiquesPortabilite.jsx (Frontend)

```jsx
export default function StatistiquesPortabilite() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    // Appel au backend
    fetch('http://localhost:8089/api/monitoring/statistics/performance')
      .then(r => r.json())
      .then(d => { 
        setStats(d); 
        setLoading(false); 
      })
      .catch(e => console.error(e));
  }, []);
  
  if (loading) return <div>Chargement...</div>;
  
  // Affichage des statistiques
  return (
    <div className="stats-panel">
      <h3>Statistiques des processus</h3>
      <div className="performance-cards">
        <div className="performance-card">
          <h4>Success Rate IN</h4>
          <div className="performance-value in">
            {stats.successRateIN?.toFixed(1) || 0} %
          </div>
        </div>
        {/* ... */}
      </div>
    </div>
  );
}
```

### 🔄 Mise à jour en Temps Réel

Le frontend utilise le **polling** pour mettre à jour les données automatiquement:

```jsx
useEffect(() => {
  // Polling toutes les 30 secondes
  const interval = setInterval(() => {
    fetch('http://localhost:8089/api/monitoring/statistics/performance')
      .then(r => r.json())
      .then(d => setStats(d));
  }, 30000);
  
  return () => clearInterval(interval);
}, []);
```

---

## 5. Déroulement Complet du Système

### 🎬 Scénario Complet: Client crée une demande → Superviseur la surveille

#### Étape 1: Client remplit le formulaire (Portail Web)

```
Client accède au portail Orange Tunisie
  ↓
Remplit le formulaire de portabilité IN
  ↓
Saisit: nom, prénom, numéro, RIO, type d'offre
  ↓
Clique sur "Soumettre"
```

#### Étape 2: Portail Web envoie la demande (SOAP)

```javascript
// Portail Web (React)
const submitForm = async (formData) => {
  const soapEnvelope = buildSoapEnvelope(formData);
  
  const response = await fetch('http://localhost:8080/ws/portability', {
    method: 'POST',
    headers: { 'Content-Type': 'text/xml' },
    body: soapEnvelope
  });
  
  const result = await response.text();
  return parseSoapResponse(result);
};
```

#### Étape 3: OTNP Portail traite la demande (SOAP Service)

```java
// WorkflowService.java
@WebMethod
public String startPortabilityIn(@WebParam(name = "demande") DemandeDTO demande) {
    try {
        // 1. Valider les données
        if (!validateRio(demande.getRio())) {
            return "ERROR: RIO invalide";
        }
        
        // 2. Préparer les variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("nom", demande.getNom());
        variables.put("prenom", demande.getPrenom());
        variables.put("msisdn", demande.getMsisdn());
        variables.put("rio", demande.getRio());
        variables.put("marche", demande.getMarche());
        
        // 3. Démarrer le processus KIE
        Long processId = startProcess(variables);
        
        // 4. Retourner le succès
        return "SUCCESS: Process ID = " + processId;
        
    } catch (Exception e) {
        return "ERROR: " + e.getMessage();
    }
}
```

#### Étape 4: KIE Server crée le processus (jBPM)

```
KIE Server reçoit la demande
  ↓
Charge la définition BPMN "PortabilityIN"
  ↓
Crée une instance de processus (ID: 12345)
  ↓
Exécute le nœud "Start"
  ↓
Avance vers "Validation RIO"
  ↓
Crée une tâche humaine pour l'administrateur
  ↓
Stocke dans la base de données jBPM
  ↓
Retourne l'ID 12345 au SOAP Service
```

#### Étape 5: jBPM stocke les données

```
Base de données jBPM:

Table: processinstancelog
- id: 12345
- processid: PortabilityIN
- status: 1 (Active)
- date: 2024-06-29 10:30:00

Table: task
- id: 1001
- name: Validation RIO
- status: Ready
- processinstanceid: 12345

Table: variableinstancelog
- processinstanceid: 12345
- variablename: nom
- value: Ben Ali
```

#### Étape 6: Monitoring Frontend affiche les données

```jsx
// StatistiquesPortabilite.jsx
useEffect(() => {
  // Récupérer les statistiques
  fetch('http://localhost:8089/api/monitoring/statistics/performance')
    .then(r => r.json())
    .then(d => setStats(d));
  
  // Récupérer les instances actives
  fetch('http://localhost:8089/api/monitoring/instances/by-status?status=1')
    .then(r => r.json())
    .then(d => setActiveInstances(d));
}, []);
```

#### Étape 7: Monitoring Back interroge KIE Server

```java
// ProcessMonitoringService.java
public DashboardKpiDTO getLiveDashboardKPIs() {
    // 1. Compter les instances
    long totalToday = processInstanceRepository.countInstancesToday();
    long successToday = processInstanceRepository.countSuccessToday();
    long errorsToday = processInstanceRepository.countCriticalErrorsToday();
    
    // 2. Calculer le taux de succès
    double successRate = (successToday * 100.0) / totalToday;
    
    // 3. Récupérer les tâches en attente
    List<Object[]> taskRows = processInstanceRepository.getPendingHumanTasksStatistics();
    
    // 4. Retourner les KPIs
    return new DashboardKpiDTO(totalToday, successRate, avgTime, errorsToday, pendingTasks);
}
```

#### Étape 8: Superviseur voit la demande en temps réel

```
Dashboard Monitoring Frontend
  ↓
Affiche: "Demande #12345 - En cours"
  ↓
Statut: "Validation RIO"
  ↓
Temps écoulé: "2h 30m"
  ↓
SLA: "OK" (< 48h)
```

#### Étape 9: Superviseur recycle une tâche bloquée

```
Superviseur voit une tâche bloquée
  ↓
Sélectionne la tâche
  ↓
Clique sur "Recycle"
  ↓
Frontend envoie: POST /api/monitoring/tasks/recycle
  ↓
Monitoring Back appelle KIE Server
  ↓
KIE Server remet la tâche en état "Ready"
  ↓
La tâche peut être traitée à nouveau
```

---

## 🎯 Résumé pour Présentation

### Points Clés à Présenter

1. **SOAP pour le portail**: Standard B2B, sécurisé, fiable
2. **jBPM pour le workflow**: Automatisation, visibilité, flexibilité
3. **REST pour le monitoring**: Moderne, léger, facile à consommer
4. **Architecture modulaire**: Séparation des responsabilités
5. **Communication indirecte**: Via KIE Server (hub central)

### Schéma à Dessiner au Tableau

```
                    ┌─────────────┐
                    │   Client    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ Portail Web │
                    │   (React)   │
                    └──────┬──────┘
                           │ SOAP
                    ┌──────▼──────┐
                    │ OTNP Portail│
                    │ (SOAP Svc)  │
                    └──────┬──────┘
                           │ REST
                    ┌──────▼──────┐
                    │  KIE Server │
                    │   (jBPM)    │
                    └──────┬──────┘
                           │ REST
                    ┌──────▼──────┐
                    │  OTNP_WS1   │
                    │ (REST Svc)  │
                    └──────┬──────┘
                           │ REST
                    ┌──────▼──────┐
                    │Monitoring F  │
                    │  (React)     │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ Superviseur │
                    └─────────────┘
```

### Questions à Anticiper

**Q: Pourquoi ne pas utiliser REST partout?**
- R: SOAP est requis pour l'intégration B2B avec les systèmes existants. REST est plus adapté pour les applications web modernes comme le monitoring.

**Q: Pourquoi jBPM et pas un moteur de workflow custom?**
- R: jBPM est une solution mature, open-source, avec une communauté active. Il offre des fonctionnalités avancées (BPMN designer, console d'administration) qui seraient coûteuses à développer.

**Q: Comment garantir la cohérence des données entre les applications?**
- R: Toutes les applications communiquent avec le même KIE Server, qui est la source de vérité. Les données sont stockées dans la base de données jBPM.

---

## 📚 Conclusion

Ce système utilise une **architecture modulaire** où chaque composant a une responsabilité claire:

- **OTNP Portail**: Création des demandes (SOAP)
- **KIE Server**: Exécution des workflows (jBPM)
- **OTNP_WS1**: Monitoring des demandes (REST)
- **Monitoring Frontend**: Visualisation des données (React)

Cette architecture permet:
- ✅ Séparation des responsabilités
- ✅ Indépendance des composants
- ✅ Évolutivité facile
- ✅ Maintenance simplifiée

**Bonne chance pour votre présentation! 🎓**
