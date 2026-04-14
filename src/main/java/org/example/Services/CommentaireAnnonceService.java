package org.example.Services;

import org.example.Entities.CommentaireAnnonce;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireAnnonceService {
    private Connection cnx;

    public CommentaireAnnonceService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // CREATE
    public void add(CommentaireAnnonce c) throws SQLException {
        String sql = "INSERT INTO commentaire_annonce (description, etat, date_comm, " +
                     "annonce_id, auteur_id, parent_id, moderation_status) " +
                     "VALUES (?, ?, NOW(), ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, c.getDescription());
        ps.setString(2, c.getEtat());
        ps.setInt(3, c.getAnnonceId());
        ps.setInt(4, c.getAuteurId());
        
        if (c.getParentId() != null) {
            ps.setInt(5, c.getParentId());
        } else {
            ps.setNull(5, Types.INTEGER);
        }
        ps.setString(6, c.getModerationStatus());
        ps.executeUpdate();
    }

    // READ - Main comments for an announcement
    public List<CommentaireAnnonce> getByAnnonceId(int annonceId) throws SQLException {
        return getByAnnonceIdForViewer(annonceId, null);
    }

    public List<CommentaireAnnonce> getByAnnonceIdForViewer(int annonceId, Integer viewerId) throws SQLException {
        List<CommentaireAnnonce> list = new ArrayList<>();
        String sql;
        if (viewerId != null) {
            sql = "SELECT c.*, u.name as auteur_nom FROM commentaire_annonce c " +
                  "LEFT JOIN user u ON c.auteur_id = u.id " +
                  "WHERE c.annonce_id = ? AND c.parent_id IS NULL " +
                  "AND (c.moderation_status = 'Approuvé' OR c.auteur_id = ?) " +
                  "ORDER BY c.date_comm DESC";
        } else {
            sql = "SELECT c.*, u.name as auteur_nom FROM commentaire_annonce c " +
                  "LEFT JOIN user u ON c.auteur_id = u.id " +
                  "WHERE c.annonce_id = ? AND c.parent_id IS NULL AND c.moderation_status = 'Approuvé' " +
                  "ORDER BY c.date_comm DESC";
        }
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, annonceId);
        if (viewerId != null) {
            ps.setInt(2, viewerId);
        }
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToCommentaire(rs));
        }
        return list;
    }

    // READ - Replies to a comment
    public List<CommentaireAnnonce> getByParentId(int parentId) throws SQLException {
        return getByParentIdForViewer(parentId, null);
    }

    public List<CommentaireAnnonce> getByParentIdForViewer(int parentId, Integer viewerId) throws SQLException {
        List<CommentaireAnnonce> list = new ArrayList<>();
        String sql;
        if (viewerId != null) {
            sql = "SELECT c.*, u.name as auteur_nom FROM commentaire_annonce c " +
                  "LEFT JOIN user u ON c.auteur_id = u.id " +
                  "WHERE c.parent_id = ? AND (c.moderation_status = 'Approuvé' OR c.auteur_id = ?) " +
                  "ORDER BY c.date_comm ASC";
        } else {
            sql = "SELECT c.*, u.name as auteur_nom FROM commentaire_annonce c " +
                  "LEFT JOIN user u ON c.auteur_id = u.id " +
                  "WHERE c.parent_id = ? AND c.moderation_status = 'Approuvé' " +
                  "ORDER BY c.date_comm ASC";
        }
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, parentId);
        if (viewerId != null) {
            ps.setInt(2, viewerId);
        }
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToCommentaire(rs));
        }
        return list;
    }

    public int countByAnnonceIdForViewer(int annonceId, Integer viewerId) throws SQLException {
        String sql;
        if (viewerId != null) {
            sql = "SELECT COUNT(*) FROM commentaire_annonce c " +
                  "WHERE c.annonce_id = ? AND c.parent_id IS NULL " +
                  "AND (c.moderation_status = 'Approuvé' OR c.auteur_id = ?)";
        } else {
            sql = "SELECT COUNT(*) FROM commentaire_annonce c " +
                  "WHERE c.annonce_id = ? AND c.parent_id IS NULL AND c.moderation_status = 'Approuvé'";
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, annonceId);
            if (viewerId != null) {
                ps.setInt(2, viewerId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countTotalVisibleComments(int annonceId, Integer viewerId) throws SQLException {
        int count = countByAnnonceIdForViewer(annonceId, viewerId);

        String sql;
        if (viewerId != null) {
            sql = "SELECT COUNT(*) FROM commentaire_annonce c " +
                  "WHERE c.annonce_id = ? AND c.parent_id IS NOT NULL " +
                  "AND (c.moderation_status = 'Approuvé' OR c.auteur_id = ?)";
        } else {
            sql = "SELECT COUNT(*) FROM commentaire_annonce c " +
                  "WHERE c.annonce_id = ? AND c.parent_id IS NOT NULL AND c.moderation_status = 'Approuvé'";
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, annonceId);
            if (viewerId != null) {
                ps.setInt(2, viewerId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count += rs.getInt(1);
                }
            }
        }

        return count;
    }

    public int countPendingComments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM commentaire_annonce WHERE moderation_status = 'En attente'";
        try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countApprovedComments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM commentaire_annonce WHERE moderation_status = 'Approuvé'";
        try (PreparedStatement ps = cnx.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // UPDATE - User can edit their comment
    public void update(CommentaireAnnonce c) throws SQLException {
        String sql = "UPDATE commentaire_annonce SET description=?, date_modification=NOW() " +
                     "WHERE id=? AND auteur_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, c.getDescription());
        ps.setInt(2, c.getId());
        ps.setInt(3, c.getAuteurId());
        ps.executeUpdate();
    }

    // DELETE - User can delete their comment
    public void delete(int id, int auteurId) throws SQLException {
        String sql = "DELETE FROM commentaire_annonce WHERE id=? AND auteur_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.setInt(2, auteurId);
        ps.executeUpdate();
    }

    // MODERATION - Approve comment
    public void approve(int id) throws SQLException {
        String sql = "UPDATE commentaire_annonce SET moderation_status='Approuvé', etat='Approuvé' WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // MODERATION - Pending comments for approval
    public List<CommentaireAnnonce> getPending() throws SQLException {
        List<CommentaireAnnonce> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name as auteur_nom FROM commentaire_annonce c " +
                     "LEFT JOIN user u ON c.auteur_id = u.id " +
                     "WHERE c.moderation_status = 'En attente' ORDER BY c.date_comm ASC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(mapResultSetToCommentaire(rs));
        }
        return list;
    }

    // Helper method
    private CommentaireAnnonce mapResultSetToCommentaire(ResultSet rs) throws SQLException {
        CommentaireAnnonce c = new CommentaireAnnonce();
        c.setId(rs.getInt("id"));
        c.setDescription(rs.getString("description"));
        c.setEtat(rs.getString("etat"));
        c.setDateComm(rs.getTimestamp("date_comm").toLocalDateTime());
        c.setAnnonceId(rs.getInt("annonce_id"));
        c.setAuteurId(rs.getInt("auteur_id"));
        c.setAuteurNom(rs.getString("auteur_nom"));
        
        if (rs.getObject("parent_id") != null) {
            c.setParentId(rs.getInt("parent_id"));
        }
        c.setModerationStatus(rs.getString("moderation_status"));
        
        if (rs.getTimestamp("date_modification") != null) {
            c.setDateModification(rs.getTimestamp("date_modification").toLocalDateTime());
        }
        return c;
    }
}

