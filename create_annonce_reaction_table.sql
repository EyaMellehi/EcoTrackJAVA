CREATE TABLE IF NOT EXISTS annonce_reaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    annonce_id INT NOT NULL,
    reaction TINYINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_annonce_reaction_user_annonce (user_id, annonce_id),
    KEY idx_annonce_reaction_user (user_id),
    KEY idx_annonce_reaction_annonce (annonce_id),
    CONSTRAINT fk_annonce_reaction_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_annonce_reaction_annonce FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE,
    CONSTRAINT chk_annonce_reaction_value CHECK (reaction IN (-1, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

