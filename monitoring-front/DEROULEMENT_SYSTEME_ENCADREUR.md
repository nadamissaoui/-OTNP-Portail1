# Déroulement du Système - Selon la Logique de l'Encadreur

## 🎯 Scénario: Client à la Boutique Orange

### Étape 1: Client arrive à la boutique
```
Client → Boutique Orange
```
- Le client veut changer d'opérateur (portabilité IN)
- Il parle à un agent Orange

---

### Étape 2: Agent utilise Monitoring Front
```
Agent → Monitoring Frontend
```
- L'agent se connecte au **Monitoring Frontend**
- Il accède à l'interface de création de demande
- Il remplit les informations du client:
  - Nom, Prénom
  - Numéro de téléphone (MSISDN)
  - Code RIO
  - Type d'offre

---

### Étape 3: Monitoring Front communique avec CRM
```
Monitoring Frontend → CRM
```
- Monitoring Frontend envoie les données au **CRM**
- Le CRM:
  - Vérifie si le client existe dans la base
  - Valide les informations du client
  - Crée/ met à jour le dossier client
  - Retourne un ID CRM

---

### Étape 4: CRM communique avec WS Back (OTNP_WS1)
```
CRM → OTNP_WS1
```
- Le CRM transmet la demande validée à **OTNP_WS1**
- OTNP_WS1:
  - Prépare les variables du processus
  - Formate les données pour jBPM
  - Appelle KIE Server

---

### Étape 5: WS Back communique avec jBPM (KIE Server)
```
OTNP_WS1 → KIE Server (jBPM)
```
- OTNP_WS1 envoie les variables à **KIE Server**
- KIE Server:
  - Charge la définition du workflow BPMN
  - Crée une instance de processus
  - Démarre le workflow de portabilité
  - Stocke dans la base de données jBPM

---

### Étape 6: jBPM communique avec Portail (OTNP-Portail)
```
KIE Server → OTNP-Portail
```
- jBPM notifie **OTNP-Portail**:
  - Que le processus a démarré
  - Avec l'ID de l'instance
- OTNP-Portail:
  - Met à jour l'état dans le système
  - Peut envoyer des notifications au client

---

## 🔄 Flux Complet (Selon Encadreur)

```
┌─────────────┐
│   Client     │
│  (Boutique)  │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│    Agent    │
└──────┬──────┘
       │
       ↓
┌─────────────────────┐
│ Monitoring Frontend │
│   (React)           │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│        CRM          │
│  (Gestion Client)  │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│   OTNP_WS1          │
│  (WS Backend)       │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│  KIE Server         │
│   (jBPM)            │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│  OTNP-Portail       │
│  (SOAP Service)     │
└─────────────────────┘
```

---

## 📋 Détail de Chaque Étape

### Étape 1: Client à la boutique
- Le client présente sa demande de portabilité
- L'agent collecte les informations:
  - Pièce d'identité
  - Numéro de téléphone actuel
  - Code RIO (fourni par l'opérateur actuel)

### Étape 2: Agent utilise Monitoring Front
```javascript
// Monitoring Frontend
const handleSubmit = async (formData) => {
  // 1. Valider les données
  if (!validateRio(formData.rio)) {
    alert("RIO invalide");
    return;
  }
  
  // 2. Envoyer au CRM
  const crmResponse = await fetch('/api/crm/validate', {
    method: 'POST',
    body: JSON.stringify(formData)
  });
  
  const crmData = await crmResponse.json();
  
  // 3. Envoyer à OTNP_WS1
  const wsResponse = await fetch('http://localhost:8089/api/monitoring/start', {
    method: 'POST',
    body: JSON.stringify({
      ...formData,
      crmId: crmData.id
    })
  });
  
  return wsResponse.json();
};
```

### Étape 3: CRM valide le client
```java
// CRM Service
public CRMResponse validateClient(ClientDTO client) {
    // 1. Vérifier si le client existe
    Client existingClient = clientRepository.findByMsisdn(client.getMsisdn());
    
    if (existingClient == null) {
        // Créer nouveau client
        existingClient = createNewClient(client);
    } else {
        // Mettre à jour client existant
        updateClient(existingClient, client);
    }
    
    // 2. Valider le RIO
    if (!rioService.validate(client.getRio())) {
        throw new ValidationException("RIO invalide");
    }
    
    // 3. Retourner l'ID CRM
    return new CRMResponse(existingClient.getId(), "VALIDE");
}
```

### Étape 4: OTNP_WS1 prépare les variables
```java
// MonitoringService.java
public Long startPortabilityIn(PortabilityDTO dto) {
    // 1. Préparer les variables jBPM
    Map<String, Object> variables = new HashMap<>();
    variables.put("nom", dto.getNom());
    variables.put("prenom", dto.getPrenom());
    variables.put("msisdn", dto.getMsisdn());
    variables.put("rio", dto.getRio());
    variables.put("crmId", dto.getCrmId());
    variables.put("marche", dto.getMarche());
    
    // 2. Appeler KIE Server
    ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
    Long processInstanceId = processClient.startProcess(
        "portabilityin_1.0.0-SNAPSHOT",
        "PortabilityIN",
        variables
    );
    
    // 3. Logger
    log.info("Processus IN démarré: ID={}, CRM ID={}", processInstanceId, dto.getCrmId());
    
    return processInstanceId;
}
```

### Étape 5: KIE Server exécute le workflow
```
KIE Server reçoit la demande
  ↓
Charge le workflow BPMN "PortabilityIN"
  ↓
Crée l'instance de processus
  ↓
Exécute: Start → Validation RIO → Portage → Activation → End
  ↓
Stocke dans la base de données jBPM
  ↓
Notifie OTNP-Portail
```

### Étape 6: OTNP-Portail met à jour le système
```java
// OTNP-Portail SOAP Service
@WebMethod
public void notifyProcessStarted(Long processInstanceId, String crmId) {
    // 1. Mettre à jour le statut dans le système
    portabilityRepository.updateStatus(processInstanceId, "IN_PROGRESS");
    
    // 2. Envoyer une notification au client
    notificationService.sendSMS(
        crmId,
        "Votre demande de portabilité est en cours de traitement"
    );
}
```

---

## 🎯 Pourquoi cette Architecture?

### Avantages:

1. **Séparation des responsabilités**:
   - Monitoring Front: Interface utilisateur
   - CRM: Gestion des clients
   - OTNP_WS1: Communication avec jBPM
   - jBPM: Exécution des workflows
   - OTNP-Portail: Notification et mise à jour

2. **Validation à chaque étape**:
   - Frontend: Validation des champs
   - CRM: Validation du client
   - OTNP_WS1: Validation des variables
   - jBPM: Validation du workflow

3. **Traçabilité complète**:
   - Chaque étape est logguée
   - Historique dans CRM
   - Historique dans jBPM
   - Historique dans OTNP-Portail

4. **Flexibilité**:
   - Peut remplacer CRM sans changer le reste
   - Peut modifier le workflow sans changer le frontend
   - Peut ajouter de nouvelles étapes facilement

---

## 📊 Résumé pour Présentation

### Flux selon l'encadreur:

```
Client (boutique)
    ↓
Agent (Monitoring Front)
    ↓
CRM (validation client)
    ↓
OTNP_WS1 (préparation variables)
    ↓
KIE Server (exécution workflow)
    ↓
OTNP-Portail (notification)
```

### Points clés à expliquer:

1. **Monitoring Front est le point d'entrée** pour l'agent
2. **CRM valide le client** avant de continuer
3. **OTNP_WS1 fait le pont** entre CRM et jBPM
4. **jBPM exécute le workflow** de portabilité
5. **OTNP-Portail notifie** le système

### Questions possibles du jury:

**Q: Pourquoi passer par CRM avant jBPM?**
- R: Pour valider que le client existe et est éligible à la portabilité.

**Q: Quel est le rôle exact de Monitoring Front dans ce flux?**
- R: C'est l'interface que l'agent utilise pour saisir la demande et déclencher le processus.

**Q: Pourquoi OTNP-Portail est à la fin du flux?**
- R: Pour mettre à jour le système et notifier le client une fois le workflow démarré.

---

## 🎓 Conclusion

Selon la logique de votre encadreur, le flux est:

**Agent (Monitoring Front) → CRM → OTNP_WS1 → jBPM → OTNP-Portail**

Cette architecture assure:
- ✅ Validation à chaque étape
- ✅ Traçabilité complète
- ✅ Séparation des responsabilités
- ✅ Flexibilité et maintenance facile

**Utilisez ce schéma pour votre présentation! 🎓**
