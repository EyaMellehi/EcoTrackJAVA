-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS ecotrack;
USE ecotrack;

-- Désactiver les contraintes de clé étrangère
SET FOREIGN_KEY_CHECKS=0;

-- Supprimer les tables si elles existent (pour réinitialiser)
DROP TABLE IF EXISTS commentaire;
DROP TABLE IF EXISTS annonce;

-- Réactiver les contraintes de clé étrangère
SET FOREIGN_KEY_CHECKS=1;

-- Crer la table annonce
CREATE TABLE annonce (
    id INT(11) NOT NULL AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    date_pub DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    region VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    categorie VARCHAR(255) DEFAULT NULL,
    media_path VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Créer la table commentaire
CREATE TABLE commentaire (
    id INT(11) NOT NULL AUTO_INCREMENT,
    annonce_id INT(11) NOT NULL,
    texte TEXT NOT NULL,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


