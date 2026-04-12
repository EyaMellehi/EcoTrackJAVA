# EcoTrack - CRUD Annonces

Application JavaFX pour gérer les annonces écologiques avec une base de données MySQL.

## Architecture MVC

- **Model**: `services/` - Classes de service et entités
- **View**: `resources/` - Fichiers FXML
- **Controller**: `gui/` - Classes contrôleur JavaFX

## Structure du Projet

```
src/main/java/
├── entities/
│   └── Annonce.java              # Modèle d'annonce
├── services/
│   ├── IService.java             # Interface générique
│   └── AnnonceService.java        # Implémentation service
├── utils/
│   └── MyConnection.java          # Singleton MySQL
├── gui/
│   ├── AjouterAnnonce.java        # Contrôleur ajout
│   └── AfficherAnnonces.java      # Contrôleur affichage
└── main/
    ├── MainFX.java               # Point d'entrée JavaFX
    └── TestJDBC.java             # Test JDBC
```

## Configuration de la Base de Données

### Base de Données
- **Nom**: `ecotrack`
- **Table**: `annonce`

### Créer la Base de Données

Exécutez le script SQL fourni:
```bash
mysql -u root -p < setup.sql
```

Ou manuellement:
```sql
CREATE DATABASE IF NOT EXISTS ecotrack;
USE ecotrack;

CREATE TABLE IF NOT EXISTS annonce (
    id INT(11) NOT NULL AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    date_pub DATETIME NOT NULL,
    region VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    createur_id INT(11) NOT NULL,
    categorie VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
);
```

## Démarrage de l'Application

### Option 1: Avec Maven (Recommandé)
```bash
mvn javafx:run
```

### Option 2: Script Windows
Double-cliquez sur `run.bat`

### Option 3: Compilation manuelle
```bash
mvn clean compile
mvn javafx:run
```

## Fonctionnalités

### 1. Afficher les Annonces
- Page d'accueil affiche la liste de toutes les annonces
- La colonne ID est masquée
- Colonnes affichées: Titre, Date Publication, Région, Contenu, Créateur ID, Catégorie
- Bouton "Ajouter une annonce" pour créer une nouvelle annonce

### 2. Ajouter une Annonce
- Formulaire avec les champs:
  - **Titre**: Texte libre
  - **Date Publication**: Date picker
  - **Région**: ComboBox (24 régions tunisiennes)
  - **Contenu**: TextArea
  - **Créateur ID**: Numéro entier
  - **Catégorie**: ComboBox (Agriculture, Collectes de déchets, Associations, Environnement)
- Boutons:
  - "✔ Ajouter" - Enregistre l'annonce et affiche un message de succès
  - "📋 Voir liste" - Retourne à la page d'affichage

## Régions Disponibles

Tunis, Ariana, Ben Arous, Manouba, Nabeul, Zaghouan, Bizerte, Béja, 
Jendouba, Le Kef, Siliana, Sousse, Monastir, Mahdia, Sfax, Kairouan, 
Kasserine, Sidi Bouzid, Gabès, Medenine, Tataouine, Gafsa, Tozeur, Kébili

## Catégories Disponibles

- Agriculture
- Collectes de déchets
- Associations et collectifs citoyens
- Environnement

## Design Visuel

- **Thème**: Vert (Écologie)
- **Couleurs**:
  - Fond: #f1f8e9 (Vert clair)
  - En-têtes: #2e7d32 (Vert foncé)
  - Boutons: #4CAF50 (Vert) et #1976D2 (Bleu)
- **Taille de la Fenêtre**: 900 x 600 pixels
- **Titre**: "EcoTrack - Annonces"

## Gestion des Erreurs

- Si la base de données n'est pas disponible:
  - La page d'affichage montre un tableau vide
  - L'ajout affiche un message d'erreur détaillé
- Validation des formulaires:
  - Vérification que tous les champs sont remplis
  - Vérification que le Créateur ID est un nombre

## Technologies

- **Java**: 17+
- **JavaFX**: 17.0.2
- **MySQL JDBC**: 8.0.15
- **Maven**: 3.8.1+

## Notes de Développement

- La connexion MySQL utilise le pattern Singleton
- Les requêtes d'écriture (Create, Update, Delete) utilisent PreparedStatement
- Toutes les exceptions sont catchées et affichées à l'utilisateur
- Navigation entre les écrans avec FXMLLoader

## Fichiers Importants

- `pom.xml` - Configuration Maven
- `setup.sql` - Script d'initialisation BD
- `src/main/resources/style.css` - Styling global
- `src/main/resources/*.fxml` - Interfaces utilisateur

## License

Projet de démonstration - EcoTrack

