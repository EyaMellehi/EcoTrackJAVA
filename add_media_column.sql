-- Script pour ajouter la colonne media_path à la table annonce existante
USE ecotrack;

-- Ajouter la colonne media_path si elle n'existe pas
ALTER TABLE annonce ADD COLUMN media_path VARCHAR(500) DEFAULT NULL;

-- Afficher la structure de la table pour vérifier
DESCRIBE annonce;

-- Afficher un message de succès
SELECT "✅ Colonne media_path ajoutée avec succès!" AS Message;

