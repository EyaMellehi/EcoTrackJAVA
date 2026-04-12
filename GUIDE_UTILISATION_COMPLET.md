# 🌱 Guide d'Utilisation - EcoTrack Annonces

## Démarrage de l'Application

### Option 1: Via Maven (Recommandé)
```powershell
cd C:\Users\bhiri\Downloads\3A38\CrudAnnonce
mvn javafx:run
```

### Option 2: Via le script PowerShell
```powershell
C:\Users\bhiri\Downloads\3A38\CrudAnnonce\run-app-console.ps1
```

---

## Fonctionnalités Disponibles

### 1️⃣ **Afficher les Annonces**
- L'écran principal affiche toutes les annonces dans une table
- Colonnes visibles:
  - ID: Identifiant unique de l'annonce
  - Titre: Titre de l'annonce
  - Date Publication: Date de création
  - Région: Région concernée
  - Contenu: Résumé du contenu
  - Catégorie: Type d'annonce

### 2️⃣ **Voir les Détails d'une Annonce**
- Cliquer sur le bouton **"👁️ Voir"** dans la dernière colonne
- Affiche:
  - Titre, Date, Région, Catégorie
  - Contenu complet
  - **Média (photo)** s'il est présent
  - **Commentaires** existants

### 3️⃣ **Ajouter une Annonce**
- Cliquer sur le bouton **"➕ Ajouter une annonce"**
- Remplir le formulaire:
  - Titre: Titre de votre annonce
  - Date de Publication: Sélectionner une date
  - Région: Choisir parmi les 24 régions tunisiennes
  - Contenu: Description détaillée
  - Catégorie: Agriculture, Collectes de déchets, Associations, Environnement
  - Média: Optionnel (image PNG/JPG)
- Cliquer **"✔ Ajouter"** pour confirmer
- Le formulaire se réinitialise automatiquement

### 4️⃣ **Commenter une Annonce**
- Dans la page de détails, descendre jusqu'à **"Ajouter un Commentaire"**
- Entrer votre commentaire
- Cliquer **"Ajouter"**
- Le commentaire apparaît instantanément

### 5️⃣ **Éditer une Annonce**
- Dans la page de détails, cliquer sur le bouton **"Editer"** (orange)
- Modifier les champs désirés
- Optionnellement, sélectionner une nouvelle image
- Cliquer **"Enregistrer"** pour sauvegarder

### 6️⃣ **Supprimer une Annonce**
- Dans la page de détails, cliquer sur le bouton **"Supprimer"** (rouge)
- Confirmer la suppression
- L'annonce est supprimée (attention: irréversible)

### 7️⃣ **Navigation**
- Bouton **"Retour"** (bleu): Retourner à la liste
- Bouton **"📋 Voir liste"**: Retourner à la liste depuis le formulaire d'ajout
- La navigation est fluide entre les différents écrans

---

## Régions Disponibles

Les 24 gouvernorats tunisiens:
- **Nord:** Tunis, Ariana, Ben Arous, Manouba, Nabeul, Zaghouan
- **Nord-Ouest:** Bizerte, Béja, Jendouba, Le Kef, Siliana
- **Centre:** Sousse, Monastir, Mahdia, Sfax, Kairouan
- **Sud-Est:** Kasserine, Sidi Bouzid, Gabès, Medenine, Tataouine
- **Sud-Ouest:** Gafsa, Tozeur, Kébili

---

## Catégories d'Annonces

- 🌾 **Agriculture**: Activités agricoles, formations, échanges
- 🗑️ **Collectes de déchets**: Nettoyage communautaire, recyclage
- 👥 **Associations et collectifs citoyens**: Groupements, associations
- 🌍 **Environnement**: Protection environnementale, initiatives vertes

---

## Base de Données

**Base:** `ecotrack`
**Tables:**
- `annonce`: Stocke les annonces (id, titre, date_pub, region, contenu, categorie, media_path)
- `commentaire_annonce`: Stocke les commentaires (id, id_annonce, texte, date_creation)

---

## Dépannage

### L'application ne démarre pas?
```powershell
# Vérifier la compilation
mvn clean compile

# Vérifier la connexion BD
mvn exec:java "-Dexec.mainClass=main.TestJDBC"
```

### Les données ne s'affichent pas?
1. Vérifier que MySQL est démarré
2. Vérifier la base `ecotrack` existe
3. Vérifier les identifiants dans `utils/MyConnection.java`

### Erreur de média?
- Utiliser un chemin absolu ou vérifié
- Format accepté: PNG, JPG, GIF, BMP

---

## Technologies Utilisées

- **JavaFX 17.0.2**: Interface graphique
- **MySQL 8.0.15**: Base de données
- **Maven 3.8.1**: Gestion de projet
- **Java 17**: Langage de programmation
- **JDBC**: Connexion BD

---

## Fichiers Importants

```
CrudAnnonce/
├── src/main/java/
│   ├── main/MainFX.java (point d'entrée)
│   ├── gui/ (contrôleurs JavaFX)
│   ├── services/ (logique métier)
│   ├── entities/ (modèles de données)
│   └── utils/ (utilitaires BD)
├── src/main/resources/
│   ├── *.fxml (interface utilisateur)
│   └── style.css (feuille de style)
└── pom.xml (configuration Maven)
```

---

**Dernière mise à jour:** 12/04/2026
**Version:** 1.0 STABLE

