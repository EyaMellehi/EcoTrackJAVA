# 🌿 EcoTrack - Gestion des Annonces Environnementales

Projet JavaFX + JDBC suivant le pattern MVC pour la gestion d'annonces écologiques en Tunisie.

## 📋 Prérequis

- **Java 17+** (OpenJDK/Oracle JDK)
- **Maven 3.6+**
- **MySQL 5.7+**
- **MySQL Connector/J 8.0.15** (automatique via Maven)

## 🔧 Installation

### 1️⃣ Préparation de la Base de Données

Connectez-vous à MySQL et exécutez:

```sql
CREATE DATABASE IF NOT EXISTS ecotrack;
USE ecotrack;

CREATE TABLE IF NOT EXISTS annonces (
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

### 2️⃣ Configuration de la Connexion

Le fichier `src/main/java/utils/MyConnection.java` utilise:
- **URL:** `jdbc:mysql://localhost:3306/ecotrack`
- **User:** `root`
- **Password:** `` (vide)

Si votre configuration est différente, modifiez les constantes dans `MyConnection.java`.

### 3️⃣ Compilation et Exécution

#### Avec Maven (recommandé):
```bash
# Compiler
mvn clean compile

# Tester la connexion JDBC
mvn exec:java -Dexec.mainClass="main.TestJDBC"

# Lancer l'application JavaFX
mvn javafx:run -Dexec.mainClass="main.TestFX"
```

#### Sans Maven (avec Java 17):
```bash
# Compiler
javac -d target/classes src/main/java/**/*.java

# Exécuter TestJDBC
java -cp "target/classes:lib/*" main.TestJDBC

# Exécuter l'application JavaFX
java -cp "target/classes:lib/*" main.TestFX
```

## 🏗️ Architecture MVC

### Model
- **entities/Annonce.java** - Modèle de données
- **services/IService.java** - Interface générique CRUD
- **services/AnnonceService.java** - Implémentation des opérations CRUD
- **utils/MyConnection.java** - Singleton JDBC

### View
- **src/main/resources/AjouterAnnonce.fxml** - Écran de création
- **src/main/resources/AfficherAnnonces.fxml** - Écran de liste
- **src/main/resources/style.css** - Styling vert écologique

### Controller
- **gui/AjouterAnnonce.java** - Gestion du formulaire
- **gui/AfficherAnnonces.java** - Gestion du tableau

## 📊 Fonctionnalités

✅ **Créer une annonce**
- Formulaire complet avec validation
- DatePicker pour la date de publication
- Sélection de région (24 régions tunisiennes)
- Catégorie prédéfinie
- ID créateur manuel

✅ **Lister les annonces**
- TableView avec tri/filtrage JavaFX
- Affichage de tous les champs
- Navigation fluide entre écrans

✅ **Modifier une annonce**
- Implémentée dans `AnnonceService.update()`
- Modifiable via extension GUI

✅ **Supprimer une annonce**
- Implémentée dans `AnnonceService.delete()`
- Modifiable via extension GUI

## 🌍 Régions Supportées (24)

Tunis, Ariana, Ben Arous, Manouba, Nabeul, Zaghouan, Bizerte, Béja, Jendouba, Le Kef, Siliana, Sousse, Monastir, Mahdia, Sfax, Kairouan, Kasserine, Sidi Bouzid, Gabès, Medenine, Tataouine, Gafsa, Tozeur, Kébili

## 🏷️ Catégories Supportées (4)

- Agriculture
- Collectes de déchets
- Associations et collectifs citoyens
- Environnement

## 📦 Dépendances

Automatiquement téléchargées via Maven:

```xml
<!-- MySQL JDBC Driver -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.15</version>
</dependency>

<!-- JavaFX FXML -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.2</version>
</dependency>

<!-- JavaFX Controls -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version>
</dependency>
```

## 🚀 Points Techniques

- **Pattern:** MVC strict (Model-View-Controller)
- **UI Framework:** JavaFX 17 avec FXML
- **Database:** MySQL via PreparedStatement (protection SQL injection)
- **Design Pattern:** Singleton (MyConnection)
- **Génériques:** IService<T> pour réutilisabilité
- **Navigation:** FXMLLoader pour changement de scène fluide
- **Data Binding:** PropertyValueFactory pour TableView

## 🎨 Thème Visuel

Thème écologique vert cohérent:
- Couleur primaire: #2e7d32 (vert foncé)
- Couleur secondaire: #4CAF50 (vert boutons)
- Fond: #f1f8e9 (vert très clair)
- Bordures: #a5d6a7 (vert moyen)

## 📝 Tests

### TestJDBC.java
Teste la connexion et les opérations CRUD:
```bash
mvn exec:java -Dexec.mainClass="main.TestJDBC"
```

### TestFX.java (MainFX)
Lance l'application complète avec interface graphique.

## 🐛 Dépannage

### Erreur: "Unable to connect to database"
- ✅ Vérifiez que MySQL est démarré
- ✅ Vérifiez les identifiants dans MyConnection.java
- ✅ Assurez-vous que la base "ecotrack" existe

### Erreur: "JavaFX modules not found"
- ✅ Installez les dépendances: `mvn clean install`
- ✅ Utilisez Java 17 minimum

### Erreur: "FXML not found"
- ✅ Vérifiez que les fichiers .fxml sont dans `src/main/resources/`
- ✅ Compilez avec `mvn compile` (copie les ressources)

## 📄 Licence

Projet éducatif - Libre d'utilisation

## 👨‍💻 Auteur

Créé pour démontrer les bonnes pratiques JavaFX + JDBC avec pattern MVC

---

**Version:** 1.0  
**Date:** 2026-04-09  
**Status:** ✅ Production Ready

