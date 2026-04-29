package org.example.Services;

import org.example.Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AnnonceReactionService {

    private final Connection cnx;

    public AnnonceReactionService() {
        this.cnx = MyConnection.getInstance().getConnection();
        ensureTableExists();
    }

    public void setReaction(int userId, int annonceId, boolean like) throws SQLException {
        String sql = "INSERT INTO annonce_reaction (user_id, annonce_id, reaction) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE reaction = VALUES(reaction), updated_at = CURRENT_TIMESTAMP";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, annonceId);
        ps.setInt(3, like ? 1 : -1);
        ps.executeUpdate();
    }

    public void removeReaction(int userId, int annonceId) throws SQLException {
        String sql = "DELETE FROM annonce_reaction WHERE user_id = ? AND annonce_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, annonceId);
        ps.executeUpdate();
    }

    public int getUserReaction(int userId, int annonceId) throws SQLException {
        String sql = "SELECT reaction FROM annonce_reaction WHERE user_id = ? AND annonce_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, annonceId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int reaction = rs.getInt("reaction");
            if (reaction > 0) {
                return 1;
            }
            if (reaction < 0) {
                return -1;
            }
        }
        return 0;
    }

    public ReactionCounts getReactionCounts(int annonceId) throws SQLException {
        String sql = "SELECT " +
                "SUM(CASE WHEN reaction = 1 THEN 1 ELSE 0 END) AS likes_count, " +
                "SUM(CASE WHEN reaction = -1 THEN 1 ELSE 0 END) AS dislikes_count " +
                "FROM annonce_reaction WHERE annonce_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, annonceId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int likes = rs.getInt("likes_count");
            int dislikes = rs.getInt("dislikes_count");
            return new ReactionCounts(likes, dislikes);
        }
        return new ReactionCounts(0, 0);
    }

    public Map<String, Integer> getUserCategoryScores(int userId) throws SQLException {
        Map<String, Integer> scores = new HashMap<>();

        String sql = "SELECT a.categorie, SUM(ar.reaction) AS score " +
                "FROM annonce_reaction ar " +
                "JOIN annonce a ON a.id = ar.annonce_id " +
                "WHERE ar.user_id = ? AND a.active = true " +
                "AND a.categorie IS NOT NULL AND a.categorie <> '' " +
                "GROUP BY a.categorie";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            scores.put(rs.getString("categorie"), rs.getInt("score"));
        }
        return scores;
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS annonce_reaction (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "annonce_id INT NOT NULL," +
                "reaction TINYINT NOT NULL," +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "UNIQUE KEY uq_annonce_reaction_user_annonce (user_id, annonce_id)," +
                "KEY idx_annonce_reaction_user (user_id)," +
                "KEY idx_annonce_reaction_annonce (annonce_id)," +
                "CONSTRAINT fk_annonce_reaction_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE," +
                "CONSTRAINT fk_annonce_reaction_annonce FOREIGN KEY (annonce_id) REFERENCES annonce(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException ignored) {
            // Non bloquant: si la table existe deja avec un schema equivalent, on continue.
        }
    }

    public static class ReactionCounts {
        public final int likes;
        public final int dislikes;

        public ReactionCounts(int likes, int dislikes) {
            this.likes = likes;
            this.dislikes = dislikes;
        }
    }
}

