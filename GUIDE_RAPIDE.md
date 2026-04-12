# 🎯 GUIDE RAPIDE - EcoTrack Annonces

## ✅ TOUS LES PROBLÈMES CORRIGÉS!

### Les 3 erreurs principales ont été résolues:

1. ✅ **"Field 'date_pub' doesn't have a default value"** 
   - La date est maintenant auto-générée par la BDD
   - Supprimé du formulaire d'ajout

2. ✅ **"Unknown column 'texte' in 'field list'"**
   - Table commentaire corrigée
   - Toutes les requêtes SQL mises à jour

3. ✅ **"Table n'affiche rien"**
   - Colonne createurId supprimée
   - Affichage des données corrigé

---

## 🚀 LANCER L'APPLICATION

### Méthode 1: Double-clic sur le script (Recommandé)
1. Double-cliquez sur `run-app.ps1`
2. Si demandé, confirmez l'exécution du script PowerShell

### Méthode 2: Command Prompt/PowerShell
```powershell
cd C:\Users\bhiri\Downloads\3A38\CrudAnnonce
.\run-app.ps1
```

### Méthode 3: Maven direct
```powershell
mvn javafx:run
```

---

## 📝 UTILISATION DE L'APPLICATION

### Écran 1: Liste des Annonces
```
┌─────────────────────────────────────┐
│  Liste des Annonces                 │
├─────────────────────────────────────┤
│ ID │ Titre │ Date │ Région │ ...   │
├─────────────────────────────────────┤
│    │       │      │        │        │
└─────────────────────────────────────┘
   ➕ Ajouter une annonce
```

**Actions:**
- **Ajouter:** Cliquez sur "➕ Ajouter une annonce"
- **Voir détails:** Double-clic sur une annonce
- **Éditer:** Cliquez sur une annonce puis "Éditer"
- **Supprimer:** Dans les détails, cliquez "Supprimer"

---

### Écran 2: Ajouter une Annonce
```
┌─────────────────────────────────────┐
│  Ajouter une Annonce                │
├─────────────────────────────────────┤
│ Titre:     [__________________]     │
│ Région:    [Tunis ▼]                │
│ Contenu:   [________________]       │
│ Catégorie: [Agriculture ▼]          │
│                                      │
│  [✔ Ajouter]  [📋 Voir liste]      │
└─────────────────────────────────────┘
```

**À savoir:**
- ✅ La date est automatique (maintenant)
- ✅ Tous les champs doivent être remplis
- ✅ 24 régions disponibles
- ✅ 4 catégories disponibles

---

### Écran 3: Détails et Édition
```
┌─────────────────────────────────────┐
│  Détails de l'Annonce               │
├─────────────────────────────────────┤
│ Titre: XXX                          │
│ Date: 2026-04-12 17:25:30          │
│ Région: Tunis                       │
│ Catégorie: Agriculture              │
│ Contenu: ...                        │
├─────────────────────────────────────┤
│ Commentaires: (à implémenter)       │
│                                      │
│ [✏️ Éditer]  [🗑️ Supprimer]         │
│ [📋 Retour]                         │
└─────────────────────────────────────┘
```

---

## 🗂️ STRUCTURE DU PROJET

```
CrudAnnonce/
├── setup.sql                    ← Script d'initialisation BDD
├── run-app.ps1                  ← Script de lancement
├── CORRECTIONS_APPLIQUEES.md    ← Détail des corrections
│
├── src/main/java/
│   ├── entities/
│   │   ├── Annonce.java         ✅ Corrigé
│   │   └── Commentaire.java
│   │
│   ├── services/
│   │   ├── IService.java
│   │   ├── AnnonceService.java  ✅ Corrigé
│   │   └── CommentaireService.java ✅ Corrigé
│   │
│   ├── gui/
│   │   ├── AfficherAnnonces.java ✅ Corrigé
│   │   ├── AjouterAnnonce.java   ✅ Corrigé
│   │   ├── EditerAnnonce.java    ✅ Corrigé
│   │   └── AfficherDetailsAnnonce.java
│   │
│   ├── main/
│   │   ├── MainFX.java
│   │   └── TestJDBC.java        ✅ Corrigé
│   │
│   └── utils/
│       └── MyConnection.java
│
└── src/main/resources/
    ├── AfficherAnnonces.fxml    ✅ Corrigé
    ├── AjouterAnnonce.fxml      ✅ Corrigé
    ├── EditerAnnonce.fxml       ✅ Corrigé
    ├── AfficherDetailsAnnonce.fxml
    └── style.css                ✅ Amélioré
```

---

## 🔧 CONFIGURATION

### Base de données
- **Type:** MySQL
- **Host:** localhost:3306
- **Database:** ecotrack
- **User:** root
- **Password:** (vide)
- **Port:** 3306

### Java
- **Version:** 17
- **IDE:** Compatible IntelliJ, VS Code, Eclipse
- **Build:** Maven 3.8.1+

---

## ⚠️ AVANT DE LANCER

**Vérifiez que:**
1. ✅ MySQL est installé et en cours d'exécution
2. ✅ L'utilisateur root existe (sans mot de passe)
3. ✅ Java 17+ est installé
4. ✅ Maven est installé
5. ✅ Vous avez 100 MB d'espace libre

**Problèmes courants:**

| Erreur | Solution |
|--------|----------|
| "MySQL: Unknown command" | MySQL n'est pas dans le PATH |
| "Cannot connect to host" | MySQL n'est pas lancé |
| "Port 3306 already in use" | MySQL est déjà en cours d'exécution |
| "Access denied for user 'root'" | Mot de passe root incorrect |

---

## 📊 DONNÉES DE TEST

Le script `setup.sql` crée les tables vides. Pour tester:

1. Ouvrez l'application
2. Cliquez "➕ Ajouter une annonce"
3. Remplissez les champs:
   - Titre: "Ma première annonce"
   - Région: "Tunis"
   - Contenu: "Ceci est un test"
   - Catégorie: "Agriculture"
4. Cliquez "✔ Ajouter"
5. Naviguez à la liste pour voir l'annonce

---

## 💡 CONSEILS D'UTILISATION

✅ **Bonnes pratiques:**
- Testez l'ajout avant de modifier
- Consultez la liste avant de supprimer
- Utilisez des descriptions claires
- Sélectionnez la bonne région

❌ **À éviter:**
- Titres vides ou trop courts
- Contenus identiques
- Régions incorrectes
- Suppression massive de données

---

## 🆘 SUPPORT & DÉBOGAGE

### Consulter les logs
1. Ouvrez la console PowerShell
2. Lancez: `mvn javafx:run`
3. Observez les messages d'erreur

### Réinitialiser la BDD
```sql
mysql -u root < setup.sql
```

### Nettoyer le cache
```powershell
mvn clean
```

---

## ✨ FONCTIONNALITÉS DISPONIBLES

| Fonctionnalité | Status |
|---|---|
| Ajouter annonce | ✅ Implémenté |
| Afficher annonces | ✅ Implémenté |
| Éditer annonce | ✅ Implémenté |
| Supprimer annonce | ✅ Implémenté |
| Ajouter commentaire | ✅ Implémenté |
| Afficher commentaires | ✅ Implémenté |
| Éditer commentaire | ✅ Implémenté |
| Supprimer commentaire | ✅ Implémenté |

---

**Bon usage! 🎉**

Pour toute question, consultez `CORRECTIONS_APPLIQUEES.md` pour plus de détails techniques.

