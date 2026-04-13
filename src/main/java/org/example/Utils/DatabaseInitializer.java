package org.example.Utils;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void initializeTables() {
        try {
            Connection cnx = MyConnection.getInstance().getConnection();
            Statement st = cnx.createStatement();
            
            // Create annonce table
            String annonceSQL = "CREATE TABLE IF NOT EXISTS annonce (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "titre VARCHAR(255) NOT NULL," +
                    "date_pub DATETIME NOT NULL," +
                    "region VARCHAR(100) NOT NULL," +
                    "contenu LONGTEXT NOT NULL," +
                    "categorie ENUM('Agriculture', 'Associations et collectifs citoyens', 'Collectes de déchets', 'Environnement') NOT NULL," +
                    "media_path VARCHAR(255)," +
                    "auteur_id INT NOT NULL," +
                    "date_modification DATETIME," +
                    "active BOOLEAN DEFAULT true," +
                    "FOREIGN KEY (auteur_id) REFERENCES user(id) ON DELETE CASCADE," +
                    "INDEX idx_region (region)," +
                    "INDEX idx_categorie (categorie)," +
                    "INDEX idx_active (active)," +
                    "INDEX idx_date_pub (date_pub)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            st.executeUpdate(annonceSQL);
            System.out.println("✅ Table 'annonce' créée/vérifiée");
            
            // Create commentaire_annonce table
            String commentaireSQL = "CREATE TABLE IF NOT EXISTS commentaire_annonce (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "description LONGTEXT NOT NULL," +
                    "etat VARCHAR(50) DEFAULT 'En attente'," +
                    "date_comm DATETIME NOT NULL," +
                    "annonce_id INT NOT NULL," +
                    "auteur_id INT NOT NULL," +
                    "parent_id INT," +
                    "moderation_status VARCHAR(50) DEFAULT 'En attente'," +
                    "date_modification DATETIME," +
                    "FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (auteur_id) REFERENCES user(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (parent_id) REFERENCES commentaire_annonce(id) ON DELETE CASCADE," +
                    "INDEX idx_annonce (annonce_id)," +
                    "INDEX idx_auteur (auteur_id)," +
                    "INDEX idx_parent (parent_id)," +
                    "INDEX idx_moderation (moderation_status)," +
                    "INDEX idx_date_comm (date_comm)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            st.executeUpdate(commentaireSQL);
            System.out.println("✅ Table 'commentaire_annonce' créée/vérifiée");
            
            st.close();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation des tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

