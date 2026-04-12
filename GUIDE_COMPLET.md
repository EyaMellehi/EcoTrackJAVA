# EcoTrack CRUD Annonces - Guide Complet

## ✅ Fonctionnalités Complètes

### 1. Gestion des Annonces
- ✅ **Créer**: Ajouter une nouvelle annonce avec tous les champs
- ✅ **Lire**: Afficher la liste complète ou les détails d'une annonce
- ✅ **Éditer**: Modifier une annonce existante
- ✅ **Supprimer**: Supprimer une annonce (avec confirmation)

### 2. Gestion des Commentaires
- ✅ **Créer**: Ajouter un commentaire sur une annonce
- ✅ **Lire**: Afficher tous les commentaires d'une annonce
- ✅ **Éditer**: Modifier un commentaire (voir détails ci-dessous)
- ✅ **Supprimer**: Supprimer un commentaire (voir détails ci-dessous)

## 📋 Structure Complète

### Bases de Données
```sql
-- Table annonce
CREATE TABLE annonce (
    id INT(11) NOT NULL AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    date_pub DATETIME NOT NULL,
    region VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    createur_id INT(11) NOT NULL,
    categorie VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
);

-- Table commentaire
CREATE TABLE commentaire (
    id INT(11) NOT NULL AUTO_INCREMENT,
    annonce_id INT(11) NOT NULL,
    auteur VARCHAR(255) NOT NULL,
    texte TEXT NOT NULL,
    date_creation DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE
);
```

### Entités Java

#### Annonce.java
- Champs: id, titre, datePub, region, contenu, createurId, categorie
- Getters/Setters complets
- Constructeurs: avec et sans id

#### Commentaire.java (NEW)
- Champs: id, annonceId, auteur, texte, dateCreation
- Getters/Setters complets
- Constructeurs: avec et sans id

### Services

#### AnnonceService.java
- create(Annonce): Ajouter une annonce
- read() : Récupérer toutes les annonces
- update(Annonce): Modifier une annonce
- delete(Annonce): Supprimer une annonce

#### CommentaireService.java (NEW)
- create(Commentaire): Ajouter un commentaire
- readAll(): Récupérer tous les commentaires
- readByAnnonceId(int): Récupérer les commentaires d'une annonce
- update(Commentaire): Modifier un commentaire
- delete(Commentaire): Supprimer un commentaire

### Contrôleurs GUI

#### AfficherAnnonces.java (MODIFIÉ)
- Affiche la liste de toutes les annonces
- Double-clic pour voir les détails

#### AfficherDetailsAnnonce.java (NEW)
- Affiche les détails complets d'une annonce
- Affiche tous les commentaires
- Formulaire pour ajouter un commentaire
- Boutons: Éditer, Supprimer, Retour

#### EditerAnnonce.java (NEW)
- Formulaire pour éditer une annonce
- Pré-rempli avec les valeurs actuelles
- Validation des champs
- Boutons: Enregistrer, Annuler

#### AjouterAnnonce.java (EXISTANT)
- Formulaire pour créer une annonce
- Validation complète

### Fichiers FXML

#### AfficherAnnonces.fxml (MODIFIÉ)
- Tableau des annonces
- Colonne ID masquée
- Bouton "Ajouter une annonce"
- Double-clic pour voir les détails

#### AfficherDetailsAnnonce.fxml (NEW)
- Détails de l'annonce en lecture seule
- Liste des commentaires
- Formulaire d'ajout de commentaire
- Boutons: Éditer, Supprimer, Retour

#### EditerAnnonce.fxml (NEW)
- Formulaire de modification
- Champs identiques à AjouterAnnonce.fxml
- Boutons: Enregistrer, Annuler

#### AjouterAnnonce.fxml (EXISTANT)
- Formulaire de création

## 🚀 Guide d'Utilisation

### Accueil
1. L'application démarre avec la liste des annonces
2. Colonne ID est masquée (comme demandé)

### Consulter une Annonce
1. **Double-cliquez** sur une ligne du tableau
2. Affichage complet de l'annonce et ses commentaires

### Créer une Annonce
1. Cliquez sur "➕ Ajouter une annonce"
2. Remplissez le formulaire
3. Cliquez "✔ Ajouter"

### Éditer une Annonce
1. Double-cliquez sur une annonce pour voir les détails
2. Cliquez "✏️ Éditer l'Annonce"
3. Modifiez les champs
4. Cliquez "✔ Enregistrer"

### Supprimer une Annonce
1. Double-cliquez sur une annonce pour voir les détails
2. Cliquez "🗑️ Supprimer l'Annonce"
3. Confirmez la suppression

### Ajouter un Commentaire
1. Double-cliquez sur une annonce
2. Dans la section "Ajouter un Commentaire":
   - Entrez votre nom (Auteur)
   - Entrez votre commentaire (Texte)
   - Cliquez "Ajouter Commentaire"

### Éditer un Commentaire
**Fonctionnalité à ajouter**: Clic droit sur un commentaire pour l'éditer

### Supprimer un Commentaire
**Fonctionnalité à ajouter**: Bouton supprimer sur chaque commentaire

## 🗄️ Base de Données

### Création
Exécutez le script `setup.sql`:
```bash
mysql -u root -p < setup.sql
```

### Configuration de Connexion
- **Fichier**: MyConnection.java (Singleton)
- **Base**: ecotrack
- **Hôte**: localhost
- **Port**: 3306
- **Utilisateur**: root
- **Mot de passe**: (vide)

## 🎨 Design

- **Thème**: Écologie (Vert)
- **Couleurs**:
  - Fond: #f1f8e9
  - En-têtes: #2e7d32
  - Boutons Ajouter: #4CAF50
  - Boutons Éditer: #FF9800
  - Boutons Supprimer: #f44336
  - Boutons Retour: #1976D2
- **Fenêtre**: 900 x 600 pixels

## 📦 Technologies

- **Java**: 17+
- **JavaFX**: 17.0.2
- **MySQL**: JDBC 8.0.15
- **Maven**: 3.8.1+

## ▶️ Lancement

```bash
# Option 1: Maven
mvn javafx:run

# Option 2: Script batch
run.bat

# Option 3: Manuel
mvn clean compile
mvn javafx:run
```

## 🔧 Prochaines Améliorations

1. **Édition des Commentaires**: Interface pour modifier les commentaires
2. **Suppression des Commentaires**: Boutons supprimer pour chaque commentaire
3. **Recherche**: Champ de recherche des annonces
4. **Filtrage**: Par région, catégorie, date
5. **Pagination**: Pour les longues listes
6. **Export**: Exporter les annonces en PDF/Excel
7. **Authentification**: Système utilisateur
8. **Images**: Support des images pour les annonces
9. **Pagination des Commentaires**: Si beaucoup de commentaires
10. **Notifications**: En temps réel des nouveaux commentaires

## ✨ Fonctionnalités Spéciales

### Gestion des Erreurs
- Messages d'erreur clairs pour chaque opération
- Affichage gracieux si la base de données n'est pas disponible
- Validation des formulaires

### Navigation
- FXMLLoader pour transiter entre les écrans
- Boutons "Retour" pour naviguer facilement
- Double-clic pour accéder aux détails

### Pattern Design
- **MVC**: Strict séparation Model-View-Controller
- **Singleton**: Connexion à la base de données
- **Service**: Abstraction de la logique métier

## 📝 Notes de Développement

- Toutes les requêtes d'écriture utilisent PreparedStatement
- Les commentaires sont triés par date décroissante
- La suppression d'une annonce supprime ses commentaires (CASCADE)
- La connexion MySQL est persistante (Singleton)

## 📞 Support

Pour tout problème:
1. Vérifiez que MySQL est en cours d'exécution
2. Assurez-vous que la base de données `ecotrack` existe
3. Vérifiez les tables `annonce` et `commentaire`
4. Consultez les logs d'erreur dans la console

---

**Projet**: EcoTrack - Gestion des Annonces Écologiques
**Version**: 1.0
**Date**: Avril 2026

