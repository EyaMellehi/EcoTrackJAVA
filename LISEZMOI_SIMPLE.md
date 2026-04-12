# 🌱 GUIDE SIMPLE - LANCER L'APPLICATION

## ✅ LE PROBLÈME EST RÉSOLU!

La cause: **Un caractère invisible (BOM) au début du fichier FXML**
La solution: **Suppression du BOM**

---

## 🚀 COMMENT LANCER L'APP (2 MÉTHODES)

### MÉTHODE 1: PowerShell (Recommandée)

Ouvrez PowerShell et tapez:
```powershell
cd "C:\Users\bhiri\Downloads\3A38\CrudAnnonce"
mvn javafx:run
```

Puis appuyez sur **ENTRÉE**.

### MÉTHODE 2: Fichier .BAT (Double-clic)

Double-cliquez sur:
```
C:\Users\bhiri\Downloads\3A38\CrudAnnonce\test-app.bat
```

---

## ⏳ TEMPS D'ATTENTE

L'application prend **30-45 secondes** à démarrer. 
**C'est normal!** JavaFX a besoin de temps.

---

## 🎯 CE QUI DEVRAIT S'AFFICHER

```
┌─────────────────────────────────┐
│  EcoTrack - Annonces            │
├─────────────────────────────────┤
│ Liste des Annonces              │
├─────────────────────────────────┤
│ ID │ Titre │ Date │ Région │... │
├─────────────────────────────────┤
│ 1  │ Test1 │ ...  │ Tunis  │... │
│ 2  │ Test2 │ ...  │ Ariana │... │
│ ... (plus d'annonces)            │
├─────────────────────────────────┤
│ ➕ Ajouter une annonce          │
└─────────────────────────────────┘
```

---

## 🔘 COMMENT UTILISER

### 1️⃣ Voir les Détails d'une Annonce

- Cliquer sur le bouton **"👁️ Voir"** à droite de chaque ligne

### 2️⃣ Ajouter un Commentaire

- Dans la page de détails
- Écrire dans la zone: "Votre commentaire:"
- Cliquer **"Ajouter"**

### 3️⃣ Retourner à la Liste

- Cliquer **"Retour à la liste"** (bouton bleu)

---

## 🆘 SI RIEN N'APPARAÎT

**Vérifiez:**

1. **MySQL est démarré?**
   - Ouvrir Services Windows (`services.msc`)
   - Chercher "MySQL"
   - Status = "Running" ✅

2. **Vous voyez des logs rouges?**
   - C'est une connexion BD, c'est OK
   - Attendez 30-45 secondes
   - L'interface devrait arriver

3. **Erreur "Erreur lors de l'affichage"?**
   - C'est RÉSOLU! Le BOM a été supprimé
   - Réessayez avec: `mvn clean compile` puis `mvn javafx:run`

---

## 📊 DONNÉES DISPONIBLES

10 annonces dans la base:
- ID 1-10
- Titres: "Test Annonce", "Formation...", etc.
- Régions: Tunis, Sousse, Ariana, etc.

---

## 💡 ASTUCES

- **Fermer l'app:** Clic sur le ❌ en haut à droite de la fenêtre
- **Relancer:** Réexécutez `mvn javafx:run`
- **Modifier une annonce:** À venir dans la prochaine version

---

## ✨ PRÊT? LET'S GO!

```powershell
mvn javafx:run
```

Et cliquez sur "👁️ Voir" pour tester!

---

**Questions?** Consultez les fichiers `.md` dans le dossier du projet.

