from app.chat_service import chat_service

tests = [
    "c qoui jbpm",
    "combiens de demande aujourdhui",
    "comment lancer portability in",
    "ca veut dire quoi rio",
    "cest quoi le SLA",
    "explique moi le recyclage",
    "quest ce que le cin",
    "pourquoi y a des erreurs",
    "quand est ce que ca termine",
    "comment ca va",
    "qui es tu",
]
for t in tests:
    r = chat_service.chat(t)
    print("Q: " + t)
    print("R: " + r["text"][:250])
    print("Src: " + r["source"])
    print()
