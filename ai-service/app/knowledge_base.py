from __future__ import annotations

from typing import Dict, List

KNOWLEDGE_BASE: Dict[str, Dict[str, object]] = {
    "NullPointerException": {
        "cause": "Objet non initialise ou variable de processus absente pendant l'execution jBPM.",
        "steps": [
            "Verifier les variables du processus dans Business Central.",
            "Controler les champs obligatoires envoyes par OTNP_WS1.",
            "Ajouter une validation defensive avant le delegate/service task concerne.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
    "SQLException": {
        "cause": "Incident base de donnees: connexion, timeout, verrou ou requete invalide.",
        "steps": [
            "Verifier MySQL et les credentials du backend monitoring.",
            "Controler les locks et les transactions longues.",
            "Relancer uniquement apres correction de la cause BDD.",
        ],
        "doc": "https://docs.spring.io/spring-data/jpa/reference/",
    },
    "KieServerConnection": {
        "cause": "KIE Server indisponible, mauvais credentials ou container non deploye.",
        "steps": [
            "Tester /kie-server/services/rest/server depuis le backend.",
            "Verifier wbadmin/wbadmin et le container id configure.",
            "Controler WildFly, Business Central et le deploiement du process.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
    "SoapIntegration": {
        "cause": "Echec d'appel SOAP vers un service externe OTNP/BSCS/operateur.",
        "steps": [
            "Verifier l'URL du service et la connectivite reseau.",
            "Comparer la requete SOAP avec le WSDL attendu.",
            "Rejouer l'appel avec un processInstanceId de test.",
        ],
        "doc": "https://cxf.apache.org/docs/index.html",
    },
    "JmsSignalTimeout": {
        "cause": "Signal operateur attendu par jBPM non recu avant expiration du timer.",
        "steps": [
            "Verifier la file JMS et les messages en attente.",
            "Controler le signal attendu dans le diagramme BPMN.",
            "Relancer ou escalader selon le SLA de portabilite.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
    "BusinessRuleRejection": {
        "cause": "Regle metier de portabilite refusee: RIO, MSISDN, engagement ou eligibilite.",
        "steps": [
            "Lire le motif de rejet dans les variables du processus.",
            "Verifier RIO, MSISDN, statut client et engagement.",
            "Mettre a jour CRM puis informer l'agent de traitement.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
    "HumanTaskBlocked": {
        "cause": "Tache humaine non assignee, non reclamee ou bloquee par les droits agent.",
        "steps": [
            "Ouvrir Task Inbox et verifier l'assignation.",
            "Controler les roles agent/validator.",
            "Reassigner la tache ou completer les donnees manquantes.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
    "EligibilityError": {
        "cause": "Echec de verification d'eligibilite ou de format de numero.",
        "steps": [
            "Verifier le format MSISDN et le RIO.",
            "Controler la reponse NUMLEX/CRM.",
            "Corriger les donnees avant de relancer le workflow.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
    "UnknownWorkflowError": {
        "cause": "Incident workflow non classe automatiquement.",
        "steps": [
            "Lire la stack trace complete et le noeud jBPM actif.",
            "Comparer avec les incidents similaires dans l'historique.",
            "Escalader avec processInstanceId, containerId et horodatage.",
        ],
        "doc": "https://docs.jboss.org/jbpm/release/latestFinal/jbpm-docs/html_single/",
    },
}


def solution_for(error_type: str) -> Dict[str, object]:
    return KNOWLEDGE_BASE.get(error_type, KNOWLEDGE_BASE["UnknownWorkflowError"])


def node_specific_steps(node_name: str | None, node_type: str | None) -> List[str]:
    text = f"{node_name or ''} {node_type or ''}".lower()
    if not text.strip():
        return []
    if "signal" in text or "donor" in text:
        return ["Verifier le signal operateur attendu par ce noeud jBPM."]
    if "human" in text or "manual" in text or "validation" in text:
        return ["Verifier l'assignation et les droits de la tache humaine concernee."]
    if "timer" in text:
        return ["Verifier le SLA du timer et le retard de traitement associe."]
    if "eligib" in text or "check" in text:
        return ["Verifier les donnees d'eligibilite utilisees par ce noeud."]
    return [f"Analyser le noeud jBPM '{node_name}' avant de relancer le processus."]
