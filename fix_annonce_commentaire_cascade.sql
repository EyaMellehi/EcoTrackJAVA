-- Fix FK commentaire_annonce -> annonce avec ON DELETE CASCADE
USE `ecotrack`;

-- 1) Trouver le nom actuel de la contrainte FK
SELECT CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'ecotrack'
  AND TABLE_NAME = 'commentaire_annonce'
  AND COLUMN_NAME = 'annonce_id'
  AND REFERENCED_TABLE_NAME = 'annonce';

-- 2) Remplacer <FK_NAME_TROUVE> par la valeur retournée au-dessus, puis exécuter:
-- Exemple possible: fk_commentaire_annonce_annonce
ALTER TABLE `commentaire_annonce`
  DROP FOREIGN KEY `<FK_NAME_TROUVE>`;

ALTER TABLE `commentaire_annonce`
  ADD CONSTRAINT `fk_commentaire_annonce_annonce`
  FOREIGN KEY (`annonce_id`) REFERENCES `annonce`(`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

