import json, os, re, requests, subprocess
from typing import Dict, Any, List, Optional
from datetime import datetime, date
from .logger import logger

MONITORING_API = "http://localhost:8089/api/monitoring"

class ChatService:
    def __init__(self):
        self._ollama_available = self._check_ollama()

    def _find_ollama(self):
        candidates = [
            "ollama",
            os.path.join(os.environ.get("LOCALAPPDATA", ""), "Programs", "Ollama", "ollama.exe"),
            os.path.join(os.environ.get("ProgramFiles", ""), "Ollama", "ollama.exe"),
            os.path.join(os.environ.get("USERPROFILE", ""), "AppData", "Local", "Programs", "Ollama", "ollama.exe"),
        ]
        for c in candidates:
            try:
                r = subprocess.run([c, "--version"], capture_output=True, text=True, timeout=5)
                if r.returncode == 0:
                    logger.info(f"Ollama detected: {c} - {r.stdout.strip()}")
                    return c
            except Exception:
                continue
        logger.info("Ollama not installed")
        return None

    def _check_ollama(self):
        self._ollama_path = self._find_ollama()
        return self._ollama_path is not None

    def _load_nv_token(self):
        token = os.environ.get("NV_API_KEY")
        if token:
            return token
        try:
            with open(os.path.join(os.path.dirname(__file__), "..", ".nv_token")) as f:
                return f.read().strip()
        except Exception:
            return None

    def _nvidia_chat(self, message: str, history: List[Dict], context: Optional[Dict] = None) -> Optional[str]:
        token = self._load_nv_token()
        if not token:
            return None
        try:
            system = (
                "Tu es un assistant IA professionnel qui aide les agents d'Orange Tunisie sur le portail OTNP. "
                "Tu reponds TOUJOURS en francais, de maniere claire et structuree."
            )
            messages = [{"role": "system", "content": system}]
            for msg in history[-6:]:
                messages.append({"role": msg.get("role", "user"), "content": msg.get("text", "")})
            messages.append({"role": "user", "content": message})

            resp = requests.post(
                "https://integrate.api.nvidia.com/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {token}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": "meta/llama-3.2-3b-instruct",
                    "messages": messages,
                    "temperature": 0.3,
                    "max_tokens": 500
                },
                timeout=30
            )
            if resp.status_code == 200:
                data = resp.json()
                text = data["choices"][0]["message"]["content"].strip()
                if text:
                    return text
            logger.warning(f"NVIDIA error {resp.status_code}: {resp.text[:200]}")
        except Exception as e:
            logger.warning(f"NVIDIA call failed: {str(e)[:100]}")
        return None

    def _ollama_chat(self, message: str, history: List[Dict], context: Optional[Dict] = None) -> Optional[str]:
        if not self._ollama_available:
            return None
        try:
            system = (
                "Tu es un assistant IA professionnel qui aide les agents d'Orange Tunisie sur le portail OTNP. "
                "Tu reponds TOUJOURS en francais, de maniere claire et structuree. "
                "Quand on te demande une definition ou explication, donne une reponse detaillee et technique. "
                "Pour les questions hors contexte telecom, reponds de maniere naturelle et utile."
            )
            user_content = "Reponds en francais: " + message
            messages = [{"role": "system", "content": system}]
            for msg in history[-6:]:
                messages.append({"role": msg.get("role", "user"), "content": msg.get("text", "")})
            messages.append({"role": "user", "content": user_content})
            payload = {
                "model": "llama3.2:1b",
                "messages": messages,
                "stream": False,
                "options": {"temperature": 0.1, "num_predict": 256}
            }
            resp = requests.post("http://localhost:11434/api/chat", json=payload, timeout=60)
            if resp.status_code == 200:
                data = resp.json()
                return data.get("message", {}).get("content", "").strip()
            logger.warning(f"Ollama error {resp.status_code}: {resp.text[:200]}")
        except requests.exceptions.ConnectionError:
            logger.warning("Ollama not reachable")
        except Exception as e:
            logger.warning(f"Ollama call failed: {str(e)[:100]}")
        return None

    def _fetch_live_data(self) -> Dict[str, Any]:
        data = {}
        for endpoint, key in [
            ("/statistics/performance", "performance"),
            ("/statistics/monthly", "monthly"),
            ("/search", "search"),
            ("/statistics/today", "today"),
        ]:
            try:
                r = requests.get(f"{MONITORING_API}{endpoint}", timeout=3)
                if r.ok:
                    data[key] = r.json()
            except Exception:
                pass
        return data

    def _has_local_match(self, message: str) -> Optional[str]:
        lower = message.lower().strip()
        otnp_keywords = ["jbpm", "kie", "portab", "rio", "msisdn", "sla", "cin",
                         "recycl", "textbelt", "siebel", "crm", "wildfly",
                         "operateur", "ooredoo", "tunisie", "dashboard", "bscs",
                         "soap", "jms", "workflow", "process", "tache", "instance"]
        if not any(k in lower for k in otnp_keywords):
            return None

        topics = [
            (r"\bjbpm\b|\bkie\b", "**jBPM** (Business Process Management) est le moteur de workflow du portail. KIE Server (port 8081) execute les processus de portabilite."),
            (r"portab.*in|portin|port.in", "**Portability IN**: portabilite entrante. Verification MSISDN, RIO, operateur donneur. Instance jBPM creee automatiquement."),
            (r"portab.*out|portout|port.out", "**Portability OUT**: portabilite sortante. Verification contrat BSCS, eligibilite, suivi jusqu'a cloture."),
            (r"portab", "**Portabilite numerique**: changer d'operateur en conservant son numero. OTNP gere les portabilites IN et OUT via workflows jBPM."),
            (r"\brio\b", "**RIO** (Releve d'Identite Operateur): 12 caracteres, valable 28 jours. Obligatoire pour toute portabilite."),
            (r"\bmsisdn\b", "**MSISDN**: numero au format 216XXXXXXXX (8 chiffres). Prefixes: 2162 Ooredoo, 2165 Orange, 2169 Tunisie Telecom."),
            (r"\bsla\b", "**SLA**: Vert (<1h), Orange (1-4h), Rouge (>=4h). Sur les instances actives."),
            (r"\bcin\b", "**CIN**: 8 chiffres. Verification d'unicite automatique."),
            (r"recycl|relance", "**Recyclage**: relance les processus bloques via le menu Recyclage en masse."),
            (r"textbelt|\bsms\b", "**SMS**: notification via TextBelt quand un PortaIN se termine."),
            (r"\bcrm\b|siebel", "**Ref CRM**: NP-{timestamp}-{random}, cle de correlation CRM/jBPM."),
            (r"operateur|ooredoo|tunisie.?telecom", "**Operateurs**: Ooredoo (2162), Orange (2165), Tunisie Telecom (2169)."),
            (r"dashboard", "**Dashboard**: processus actifs, erreurs, SLA. Menus Consultation et Statistiques."),
        ]
        for pattern, answer in topics:
            if re.search(pattern, lower):
                return answer
        return None

    def chat(self, message: str, history: Optional[List[Dict]] = None, context: Optional[Dict] = None) -> Dict[str, Any]:
        if not message or not message.strip():
            return {"text": "Veuillez entrer une question.", "source": "Assistant OTNP", "success": True}
        history = history or []
        context = context or {}

        kb = self._has_local_match(message)
        if kb:
            return {"text": kb, "source": "Assistant OTNP", "success": True, "model": "kb"}

        nvidia_text = self._nvidia_chat(message, history, context)
        if nvidia_text:
            return {"text": nvidia_text, "source": "NVIDIA NIM (Cloud)", "success": True, "model": "nvidia"}

        ollama_text = self._ollama_chat(message, history, context)
        if ollama_text:
            return {"text": ollama_text, "source": "Ollama (LLM local)", "success": True, "model": "ollama"}

        local = self._local_answer(message)
        return {"text": local, "source": "Assistant OTNP", "success": True, "model": "local"}

    def _local_answer(self, message: str) -> str:
        lower = message.lower().strip()

        if re.search(r"^(bonjour|salut|bonsoir|hello|coucou|cc|bjr|slt)\b", lower):
            return "Bonjour ! Je suis votre assistant OTNP. Posez-moi une question sur la portabilite, le monitoring, les erreurs ou tout autre sujet."
        if re.search(r"\b(merci|thanks|thx)\b", lower):
            return "Avec plaisir ! N'hesitez pas si vous avez d'autres questions."
        if re.search(r"\b(au revoir|bye|a (plus|bientot))\b", lower):
            return "Au revoir ! Bonne journee."

        topics = [
            (r"\bjbpm\b|\bkie\b", "**jBPM** (Business Process Management) est le moteur de workflow du portail. KIE Server (port 8081) execute les processus de portabilite. Chaque demande (PortaIN/OUT) est une instance jBPM avec des etapes: validation, signaux SOAP/JMS, taches humaines."),
            (r"portab.*in|portin|port.in", "**Portability IN**: portabilite entrante (client -> Orange). Verification MSISDN, RIO, operateur donneur (Ooredoo/TT). Instance jBPM creee automatiquement."),
            (r"portab.*out|portout|port.out", "**Portability OUT**: portabilite sortante (Orange -> autre). Verification contrat BSCS, eligibilite, suivi jusqu'a cloture."),
            (r"portab", "**Portabilite numerique**: changer d'operateur en conservant son numero. OTNP gere les portabilites IN (entrantes) et OUT (sortantes) via workflows jBPM."),
            (r"\brio\b", "**RIO** (Releve d'Identite Operateur): 12 caracteres, valable 28 jours. Obligatoire pour toute portabilite."),
            (r"\bmsisdn\b", "**MSISDN**: numero au format 216XXXXXXXX (8 chiffres). Prefixes: 2162 Ooredoo, 2165 Orange, 2169 Tunisie Telecom."),
            (r"\bsla\b", "**SLA**: Vert (<1h), Orange (1-4h), Rouge (>=4h). Sur les instances actives (state=1)."),
            (r"\bcin\b", "**CIN**: 8 chiffres. Verification d'unicite automatique."),
            (r"recycl|relance", "**Recyclage**: relance les processus bloques via le menu Recyclage en masse."),
            (r"archive|archiv", "**Archive**: masque les demandes anciennes. Toggle 'Voir archives' dans Consultation."),
            (r"\bsms\b|textbelt", "**SMS**: notification automatique via TextBelt quand un PortaIN se termine. 1 gratuit/jour."),
            (r"\bcrm\b|ref.crm|siebel", "**Ref CRM**: identifiant unique NP-{timestamp}-{random} pour correlier CRM et jBPM."),
            (r"\bid.*client", "**ID Client**: TMP-IN-{timestamp} (PortaIN) ou code BSCS (PortaOUT)."),
            (r"operateur|ooredoo|orange.*tunisie|tunisie.?telecom", "**Operateurs**: Ooredoo (2162), Orange (2165), Tunisie Telecom (2169)."),
            (r"dashboard|tableau.*bord|statistique", "**Dashboard**: processus actifs, erreurs, SLA. Menus Consultation et Statistiques."),
            (r"back.?end|api|serveur|wildfly|jboss|8081|8089|5001", "**Backends**: KIE (8081), Monitoring (8089), AI (5001), PostgreSQL (5432)."),
        ]

        for pattern, answer in topics:
            if re.search(pattern, lower):
                return answer

        if re.search(r"(combien|quel.*(nombre|total|volume|etat)|nb |nbr )", lower):
            live = self._fetch_live_data()
            perf = live.get("performance", {})
            total = perf.get("totalProcesses", "N/A")
            active = perf.get("active", "N/A")
            err_cnt = perf.get("error", "N/A")
            today_str = date.today().strftime("%d/%m/%Y")
            if "aujourd" in lower:
                today = live.get("today", {})
                if isinstance(today, dict) and today.get("count") is not None:
                    return f"**Demandes aujourd'hui ({today_str})**: {today['count']}"
                if total != "N/A":
                    return f"**Demandes aujourd'hui ({today_str})**: Total {total}, Actives {active}, Erreurs {err_cnt}"
                return f"**Demandes aujourd'hui ({today_str})**: Donnees indisponibles."
            if "erreur" in lower or "error" in lower:
                return f"**Demandes en erreur**: {err_cnt}"
            if "acti" in lower:
                return f"**Demandes actives**: {active}"
            return f"**Resume demandes ({today_str})**: Total {total}, Actives {active}, En erreur {err_cnt}"

        if re.search(r"(comment|explique|guide|etape|demarche|procedure|pas a pas)", lower):
            if re.search(r"(portab|lance|creer|demarrer)", lower):
                return "Pour lancer une portabilite:\n1. Menu **Demarrer**\n2. Portability IN ou OUT\n3. Remplissez le formulaire (MSISDN, RIO, CIN, nom...)\n4. Soumettez -> Instance jBPM creee"
            if re.search(r"(analys|log|erreur|diagnostic)", lower):
                return "Pour analyser une erreur:\n1. Menu **Analyse IA des logs**\n2. Copiez le message d'erreur\n3. Cliquez Analyser\n4. L'IA propose diagnostics et solutions"
            if re.search(r"(recycl|relance)", lower):
                return "Pour recycler:\n1. Menu **Recyclage en masse**\n2. Cochez les instances\n3. Cliquez Recycler\n4. Taches humaines traitees automatiquement"
            if re.search(r"(recherch|trouv|consult|voir|liste)", lower):
                return "Pour consulter les demandes:\n1. Menu **Consultation**\n2. Filtres (statut, operateur, date, SLA)\n3. Cliquez une ligne pour details, commentaires, logs SMS"
            return "Dites-moi ce que vous voulez faire: lancer une portabilite, analyser une erreur, recycler, consulter, ou autre."

        if re.search(r"(pourquoi|cause|raison|origine|source)", lower):
            if re.search(r"(erreur|echec|blocage|probleme)", lower):
                return "Causes frequentes d'erreur:\n1. KIE Server pas demarre\n2. Signal SOAP/JMS non recu\n3. Tache humaine en attente\n4. Donnees invalides (MSISDN, RIO, CIN)\n\nUtilisez Analyse IA des logs pour un diagnostic precis."
            return "Pour identifier la cause, utilisez le module Analyse IA des logs."

        if re.search(r"(quand|.*heure|temps necessaire|duree|delai|combien.*temps)", lower):
            return "Les durees varient selon le type de portabilite et la charge. Consultez le dashboard Consultation pour le suivi SLA."

        if re.search(r"(qui.*(es.?tu|etes)|tu.*es.*qui|ton.*(nom|role|but)|presente.*toi)", lower):
            return "Je suis l'assistant IA du portail OTNP (Orange Tunisie Number Portability). Je reponds a toutes vos questions: portabilite, monitoring, erreurs, processus, et sujets generaux."
        if re.search(r"(que.*(peux|sais).*tu|capacite|fonctionnalite|aide|help)", lower):
            return "Je peux vous aider sur:\n1. **Portability IN/OUT**: demarrage, suivi\n2. **Monitoring**: stats, SLA, consultation\n3. **Analyse d'erreurs**: logs jBPM, KIE\n4. **Recyclage**: relance de processus\n5. **Questions generales**: telecom, IT, quotidien\n\nPosez votre question !"
        if re.search(r"(bon|mauvais|bien|mal|journee|soiree|ca va)", lower) and not re.search(r"bonjour|bonsoir", lower):
            return "Je vais bien, merci ! Comment puis-je vous aider aujourd'hui ?"

        words = re.findall(r"[a-z]{3,}", lower)
        key_terms = ["portabilite", "jbpm", "kie", "msisdn", "rio", "cin", "sla", "sms", "crm",
                     "recyclage", "monitoring", "dashboard", "contrat", "operateur",
                     "wildfly", "jboss", "textbelt", "siebel", "bscs", "erreur",
                     "processus", "instance", "workflow", "tache", "validation"]
        found = [w for w in words if w in key_terms]
        suggestions = ""
        if found:
            suggestions = "Essayez: Explique " + ", ".join(found[:3]) + " ou C'est quoi " + found[0]

        return (
            "Je n'ai pas de reponse specifique pour cette question.\n\n"
            "**Suggestions:**\n"
            "- \"C'est quoi [sujet]\" (ex: jBPM, RIO, SLA...)\n"
            "- \"Combien de demandes aujourd'hui ?\"\n"
            "- \"Comment lancer une portabilite ?\"\n"
            "- \"Explique le recyclage\"\n"
            "- \"Pourquoi une erreur KIE ?\"\n\n"
            + (suggestions + "\n\n" if suggestions else "") +
            "**Pour des reponses illimitees**: NVIDIA NIM (Llama 3 70B) est integre ! "
            "Creez un compte gratuit sur build.nvidia.com, mettez votre cle dans .nv_token"
        )

chat_service = ChatService()
