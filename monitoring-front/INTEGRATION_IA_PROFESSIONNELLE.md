# Intégration IA Professionnelle - Analyse de Logs jBPM

## 🎯 Objectif

Créer une vraie intégration IA professionnelle pour l'analyse des erreurs jBPM basée sur:
- **Vos logs réels** du dossier `logs fr`
- **Analyse de patterns** basée sur l'historique des erreurs
- **Base de connaissances** des erreurs courantes jBPM
- **Solutions documentées** avec liens vers la documentation officielle

---

## 🏗️ Architecture

### Backend (Monitoring Back)

#### 1. LogAnalysisService.java
**Emplacement**: `monitoring-back/src/main/java/com/monitoring/service/LogAnalysisService.java`

**Fonctionnalités**:
- Analyse les fichiers de logs jBPM
- Extrait les erreurs avec regex patterns
- Classifie les erreurs par type (DATABASE_ERROR, TIMEOUT_ERROR, etc.)
- Calcule la similarité entre erreurs (algorithme Levenshtein)
- Suggère des solutions basées sur:
  - Base de connaissances des erreurs courantes
  - Historique des erreurs similaires
  - Documentation officielle jBPM

**Base de connaissances des erreurs**:
- Erreurs de connexion base de données
- Timeouts de transaction
- NullPointerException
- Erreurs de signal jBPM
- Erreurs de tâches humaines
- Erreurs RIO (portabilité)
- Erreurs de portabilité

#### 2. LogAnalysisController.java
**Emplacement**: `monitoring-back/src/main/java/com/monitoring/controller/LogAnalysisController.java`

**Endpoints REST**:
- `POST /api/log-analysis/analyze` - Analyse un fichier de log
- `POST /api/log-analysis/suggest-solution` - Suggère une solution pour une erreur
- `POST /api/log-analysis/analyze-error` - Analyse une erreur à partir de son texte
- `GET /api/log-analysis/test` - Test d'analyse avec logs par défaut

### Frontend (Monitoring Front)

#### 3. logAnalysisService.js
**Emplacement**: `monitoring-front/src/services/logAnalysisService.js`

**Fonctions**:
- `analyzeLogFile(logFilePath)` - Analyse un fichier de log
- `suggestSolution(errorData)` - Suggère une solution
- `analyzeError(errorText)` - Analyse une erreur à partir de son texte
- `testAnalysis()` - Test d'analyse

#### 4. AiAnalysisPage.jsx
**Emplacement**: `monitoring-front/src/components/AiAnalysisPage.jsx`

**Modifications**:
- Intégration du service `logAnalysisService`
- Modification du chatbot pour utiliser l'analyse de logs
- Détection automatique des questions sur les erreurs
- Affichage des solutions avec documentation

---

## 🔧 Comment ça marche

### 1. Analyse de Logs

```java
// Le service lit le fichier de log
List<LogError> errors = logAnalysisService.analyzeLogFile(logFilePath);

// Pour chaque erreur trouvée:
// - Extrait le processInstanceId
// - Extrait le nom de l'activité
// - Classifie le type d'erreur
// - Suggère une solution
```

### 2. Classification des Erreurs

```java
// Classification basée sur le contenu de l'erreur
if (errorText.contains("connection") || errorText.contains("database")) {
    return "DATABASE_ERROR";
} else if (errorText.contains("timeout")) {
    return "TIMEOUT_ERROR";
} else if (errorText.contains("rio")) {
    return "RIO_ERROR";
} // ... etc
```

### 3. Similarité entre Erreurs

```java
// Algorithme de Levenshtein pour calculer la similarité
double similarity = editDistance(error1, error2);

// Si similarité > 0.5, considère comme erreurs similaires
List<LogError> similarErrors = findSimilarErrors(currentError, historicalErrors);
```

### 4. Suggestion de Solutions

```java
// 1. Chercher dans la base de connaissances
if (errorText.contains("connection")) {
    return new ErrorSolution(
        "Erreur de connexion base de données",
        "Vérifiez que la base de données est accessible...",
        "https://docs.jboss.org/jbpm/docs/..."
    );
}

// 2. Chercher des erreurs similaires dans l'historique
List<LogError> similar = findSimilarErrors(error, historicalErrors);
if (!similar.isEmpty()) {
    return new ErrorSolution(
        "Solution basée sur l'historique",
        "Cette erreur s'est produite X fois précédemment...",
        null
    );
}

// 3. Solution générique
return new ErrorSolution(
    "Solution générique",
    "Erreur non reconnue. Consultez la documentation...",
    "https://docs.jboss.org/jbpm/docs/..."
);
```

---

## 🚀 Utilisation

### Backend

1. **Démarrer Monitoring Back**:
```bash
cd monitoring-back
mvn spring-boot:run
```

2. **Tester l'API**:
```bash
curl http://localhost:8089/api/log-analysis/test
```

### Frontend

1. **Démarrer Monitoring Front**:
```bash
cd monitoring-front
npm start
```

2. **Utiliser le chatbot**:
- Allez sur la page "Analyse IA"
- Cliquez sur le chatbot
- Posez une question sur une erreur:
  - "Comment résoudre une erreur de connexion?"
  - "Quelle est la solution pour une erreur RIO?"
  - "Problème de timeout, que faire?"

---

## 📊 Exemples de Réponses

### Question: "Comment résoudre une erreur de connexion?"

```
🤖 **Analyse IA basée sur vos logs réels**

**Type d'erreur:** DATABASE_ERROR

**Solution:** Erreur de connexion base de données

Vérifiez que la base de données est accessible et que les credentials sont corrects. 
Vérifiez aussi que le pool de connexions n'est pas épuisé.

📚 Documentation: https://docs.jboss.org/jbpm/docs/7.0.0.Final/jbpm-docs/html_single/#_database_configuration
```

### Question: "Erreur RIO invalide"

```
🤖 **Analyse IA basée sur vos logs réels**

**Type d'erreur:** RIO_ERROR

**Solution:** Erreur RIO (Relevé d'Identité Opérateur)

Vérifiez que le RIO est valide (format correct, non expiré). 
Contactez l'opérateur émetteur si nécessaire.

📚 Documentation: https://www.arcep.fr/portabilite-des-numéros-mobiles
```

---

## 🎓 Pour la Présentation PFE

### Points à mettre en avant:

1. **Approche professionnelle**:
   - Analyse de logs réels (pas de fausse IA)
   - Base de connaissances des erreurs courantes
   - Algorithmes de similarité (Levenshtein)
   - Documentation officielle jBPM

2. **Architecture solide**:
   - Service backend pour l'analyse
   - API REST pour la communication
   - Service frontend pour l'intégration
   - Chatbot intelligent avec détection automatique

3. **Avantages**:
   - Pas besoin d'API externe (OpenAI)
   - Solutions basées sur l'historique
   - Documentation officielle intégrée
   - Extensible (ajouter de nouvelles erreurs)

4. **Démonstration**:
   - Montrer l'analyse de logs réels
   - Montrer la classification des erreurs
   - Montrer les solutions suggérées
   - Montrer les liens vers la documentation

### Questions possibles du jury:

**Q: Pourquoi ne pas utiliser OpenAI?**
R: Notre approche est plus professionnelle car elle:
- Utilise vos logs réels
- Ne dépend pas d'un service externe
- Est basée sur la documentation officielle
- Peut être étendue avec votre propre base de connaissances

**Q: Comment fonctionne la classification?**
R: Nous utilisons des patterns regex pour extraire les erreurs et les classer par type (DATABASE_ERROR, TIMEOUT_ERROR, etc.) basé sur le contenu de l'erreur.

**Q: Comment trouvez-vous des solutions similaires?**
R: Nous utilisons l'algorithme de Levenshtein pour calculer la similarité entre erreurs et trouver des erreurs similaires dans l'historique.

**Q: Comment étendre la base de connaissances?**
R: Il suffit d'ajouter de nouvelles entrées dans la map `ERROR_KNOWLEDGE_BASE` dans `LogAnalysisService.java`.

---

## 🔮 Améliorations Futures

1. **Base de données des erreurs**:
   - Stocker les erreurs dans une base de données
   - Permettre la recherche avancée
   - Statistiques sur les erreurs

2. **Machine Learning**:
   - Entraîner un modèle sur vos logs
   - Prédire les erreurs avant qu'elles ne surviennent
   - Classification automatique avec ML

3. **Interface d'administration**:
   - Interface pour ajouter des solutions
   - Gestion de la base de connaissances
   - Export des rapports

4. **Alertes automatiques**:
   - Détecter les patterns d'erreurs
   - Envoyer des alertes proactives
   - Suggérer des actions préventives

---

## 📝 Conclusion

Cette intégration IA professionnelle est:
- ✅ **Basée sur vos logs réels** (pas de fausse IA)
- ✅ **Indépendante** (pas besoin d'OpenAI)
- ✅ **Documentée** (liens vers la documentation officielle)
- ✅ **Extensible** (base de connaissances évolutive)
- ✅ **Professionnelle** (algorithme de similarité, classification)

**C'est une approche solide pour un PFE qui démontre des compétences en:**
- Analyse de logs
- Pattern matching
- Algorithmes de similarité
- Architecture REST
- Intégration frontend/backend

---

**Bonne chance pour votre présentation! 🎓**
