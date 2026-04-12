# ✅ CORRECTIONS EFFECTUÉES - EcoTrack Annonces

## 📋 Résumé des problèmes et solutions

### ❌ Problème 1: Erreur "Field 'date_pub' doesn't have a default value"
**Cause:** La colonne `date_pub` dans la table `annonce` n'avait pas de valeur par défaut.

**Solution:**
- Modifié le SQL pour ajouter `DEFAULT CURRENT_TIMESTAMP` à `date_pub`
- Supprimé le champ DatePicker des formulaires (le client ne doit pas saisir la date)
- La date est maintenant automatiquement générée par la base de données

**Fichiers modifiés:**
- ✅ `setup.sql` - Ajout de DEFAULT CURRENT_TIMESTAMP
- ✅ `src/main/resources/AjouterAnnonce.fxml` - Suppression du DatePicker
- ✅ `src/main/resources/EditerAnnonce.fxml` - Suppression du DatePicker
- ✅ `src/main/java/gui/AjouterAnnonce.java` - Suppression du datePubField
- ✅ `src/main/java/gui/EditerAnnonce.java` - Suppression du datePubField

---

### ❌ Problème 2: Erreur "Unknown column 'texte' in 'field list'"
**Cause:** La table commentaire s'appelait `commentair_annonce` au lieu de `commentaire`.

**Solution:**
- Renommé la table en `commentaire` dans le SQL
- Supprimé le champ inutile `auteur` (commentaires sans auteur)
- Ajouté DEFAULT CURRENT_TIMESTAMP à `date_creation`
- Corrigé toutes les requêtes SQL dans CommentaireService

**Fichiers modifiés:**
- ✅ `setup.sql` - Renommé table et nettoyé le schéma
- ✅ `src/main/java/services/CommentaireService.java` - Mise à jour des requêtes SQL

---

### ❌ Problème 3: Table des annonces n'affiche rien
**Cause:** 
1. Référence à `createurId` qui n'existe plus
2. Problème avec la conversion Timestamp en String pour affichage

**Solution:**
- Supprimé la colonne `createurId` de la table annonces
- Supprimé le champ `createurId` de l'entité Annonce (ancien constructeur)
- Mis à jour AfficherAnnonces.java pour convertir correctement Timestamp
- Corrigé AfficherAnnonces.fxml pour ne pas afficher createurId

**Fichiers modifiés:**
- ✅ `setup.sql` - Suppression du champ createur_id
- ✅ `src/main/java/entities/Annonce.java` - Nouveau constructeur sans date_pub
- ✅ `src/main/java/services/AnnonceService.java` - Mise à jour des requêtes INSERT
- ✅ `src/main/java/gui/AfficherAnnonces.java` - Conversion Timestamp + cellFactory
- ✅ `src/main/resources/AfficherAnnonces.fxml` - Suppression colonne createurId

---

## 🗄️ Schéma de la base de données FINAL

### Table: `annonce`
```
- id (INT, AUTO_INCREMENT, PRIMARY KEY)
- titre (VARCHAR 255, NOT NULL)
- date_pub (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- region (VARCHAR 255, NOT NULL)
- contenu (TEXT, NOT NULL)
- categorie (VARCHAR 255, DEFAULT NULL)
```

### Table: `commentaire`
```
- id (INT, AUTO_INCREMENT, PRIMARY KEY)
- annonce_id (INT, NOT NULL, FOREIGN KEY)
- texte (TEXT, NOT NULL)
- date_creation (DATETIME, DEFAULT CURRENT_TIMESTAMP)
```

---

## 🚀 Comment utiliser

### Option 1: Script PowerShell (Recommandé)
```powershell
.\run-app.ps1
```

### Option 2: Commandes manuelles
```bash
# 1. Initialiser la base de données
mysql -u root < setup.sql

# 2. Compiler et exécuter
mvn clean package -DskipTests
mvn javafx:run
```

---

## 📝 Fonctionnalités de l'application

### ✅ CRUD Annonces
- **Créer:** Ajouter une annonce (titre, région, contenu, catégorie)
- **Lire:** Afficher la liste de toutes les annonces
- **Éditer:** Modifier une annonce (double-clic sur une ligne)
- **Supprimer:** Supprimer une annonce

### ✅ CRUD Commentaires
- **Créer:** Ajouter un commentaire à une annonce
- **Lire:** Afficher les commentaires d'une annonce
- **Éditer:** Modifier un commentaire
- **Supprimer:** Supprimer un commentaire

### ✅ Interface
- Design moderne avec thème vert écologique
- Navigation fluide entre les écrans
- Validation des champs
- Messages d'erreur/succès clairs

---

## 🎨 Design amélioré

Le fichier `style.css` a été amélioré avec:
- Gradient de fond (vert)
- Ombres sur les boutons et inputs
- Effets de hover
- Bordures arrondies modernes
- Police Segoe UI propre
- Couleurs cohérentes avec le thème écologique

---

## 💾 Points importants

⚠️ **AVANT de lancer l'application:**
1. Vérifiez que MySQL est en cours d'exécution
2. L'utilisateur MySQL par défaut est `root` sans mot de passe
3. La base de données `ecotrack` sera créée automatiquement

✅ **Date de publication:**
- Automatiquement définie à la date/heure actuelle
- NON modifiable par l'utilisateur
- Affichée dans le format: `YYYY-MM-DD HH:MM:SS`

✅ **Commentaires:**
- Liés à une annonce via `annonce_id`
- Cascade delete: supprimer une annonce supprime ses commentaires
- Date de création automatique

---

## 📊 Évolutions futures possibles

- [ ] Authentification utilisateur
- [ ] Gestion des utilisateurs (profils)
- [ ] Recherche et filtrage avancés
- [ ] Export en PDF
- [ ] Notifications
- [ ] Photos/images dans les annonces
- [ ] Système de notation (stars)

---

**✅ Projet prêt à être utilisé!**

Pour toute question ou problème, consultez les logs de console lors de l'exécution.

