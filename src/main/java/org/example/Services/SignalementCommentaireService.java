package org.example.Services;

import org.example.Entities.SignalementCommentaire;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SignalementCommentaireService {
    private final Connection cnx;

    public SignalementCommentaireService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void create(SignalementCommentaire s) throws SQLException {
        String sql = "INSERT INTO signalement_commentaire (commentaire_id, citoyen_id, raison, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getCommentaireId());
            ps.setInt(2, s.getCitoyenId());
            ps.setString(3, s.getRaison());
            ps.setString(4, s.getStatut() != null ? s.getStatut() : "en_attente");
            ps.executeUpdate();
        }
    }

    public List<SignalementCommentaire> getAll() throws SQLException {
        List<SignalementCommentaire> list = new ArrayList<>();
        String sql = "SELECT s.*, c.description AS commentaire_description FROM signalement_commentaire s "
                + "INNER JOIN commentaire_annonce c ON s.commentaire_id = c.id "
                + "ORDER BY s.date_signalement DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countEnAttente() throws SQLException {
        String sql = "SELECT COUNT(*) FROM signalement_commentaire WHERE statut = 'en_attente'";
        try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<SignalementCommentaire> getEnAttente() throws SQLException {
        List<SignalementCommentaire> list = new ArrayList<>();
        String sql = "SELECT s.*, c.description AS commentaire_description FROM signalement_commentaire s "
                + "INNER JOIN commentaire_annonce c ON s.commentaire_id = c.id "
                + "WHERE s.statut = 'en_attente' ORDER BY s.date_signalement ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE signalement_commentaire SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean dejaSignale(int commentaireId, int citoyenId) throws SQLException {
        String sql = "SELECT 1 FROM signalement_commentaire WHERE commentaire_id = ? AND citoyen_id = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commentaireId);
            ps.setInt(2, citoyenId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static SignalementCommentaire mapRow(ResultSet rs) throws SQLException {
        SignalementCommentaire s = new SignalementCommentaire();
        s.setId(rs.getInt("id"));
        s.setCommentaireId(rs.getInt("commentaire_id"));
        s.setCitoyenId(rs.getInt("citoyen_id"));
        s.setRaison(rs.getString("raison"));
        s.setStatut(rs.getString("statut"));
        Timestamp ts = rs.getTimestamp("date_signalement");
        if (ts != null) {
            s.setDateSignalement(ts.toLocalDateTime());
        }
        s.setCommentaireDescription(rs.getString("commentaire_description"));
        return s;
    }
}
