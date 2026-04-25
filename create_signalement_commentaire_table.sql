CREATE TABLE IF NOT EXISTS signalement_commentaire (
  id INT AUTO_INCREMENT PRIMARY KEY,
  commentaire_id INT NOT NULL,
  citoyen_id INT NOT NULL,
  raison VARCHAR(100) NOT NULL,
  statut VARCHAR(20) DEFAULT 'en_attente',
  date_signalement DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sc_commentaire FOREIGN KEY (commentaire_id) REFERENCES commentaire_annonce(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_sc_citoyen FOREIGN KEY (citoyen_id) REFERENCES user(id)
    ON DELETE CASCADE
);
