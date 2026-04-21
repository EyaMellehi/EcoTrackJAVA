package org.example.Services;

import org.example.Entities.Annonce;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnonceService {
    private Connection cnx;

    public AnnonceService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // CREATE
    public void add(Annonce a) throws SQLException {
        String sql = "INSERT INTO annonce (titre, date_pub, region, contenu, categorie, media_path, auteur_id, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getTitre());
        ps.setTimestamp(2, Timestamp.valueOf(a.getDatePub()));
        ps.setString(3, a.getRegion());
        ps.setString(4, a.getContenu());
        ps.setString(5, a.getCategorie());
        ps.setString(6, a.getMediaPath());
        ps.setInt(7, a.getAuteurId());
        ps.setBoolean(8, true);
        ps.executeUpdate();
    }

    // READ - All active announcements
    public List<Annonce> getAll() throws SQLException {
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as auteur_nom FROM annonce a " +
                     "LEFT JOIN user u ON a.auteur_id = u.id " +
                     "WHERE a.active = true ORDER BY a.date_pub DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(mapResultSetToAnnonce(rs));
        }
        return list;
    }

    // Read by region
    public List<Annonce> getByRegion(String region) throws SQLException {
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as auteur_nom FROM annonce a " +
                     "LEFT JOIN user u ON a.auteur_id = u.id " +
                     "WHERE a.active = true AND a.region = ? ORDER BY a.date_pub DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, region);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToAnnonce(rs));
        }
        return list;
    }

    // Read by category
    public List<Annonce> getByCategorie(String categorie) throws SQLException {
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as auteur_nom FROM annonce a " +
                     "LEFT JOIN user u ON a.auteur_id = u.id " +
                     "WHERE a.active = true AND a.categorie = ? ORDER BY a.date_pub DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, categorie);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToAnnonce(rs));
        }
        return list;
    }

    // Get single annonce
    public Annonce getById(int id) throws SQLException {
        String sql = "SELECT a.*, u.name as auteur_nom FROM annonce a " +
                     "LEFT JOIN user u ON a.auteur_id = u.id WHERE a.id = ? AND a.active = true";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToAnnonce(rs);
        }
        return null;
    }

    // UPDATE
    public void update(Annonce a) throws SQLException {
        String sql = "UPDATE annonce SET titre=?, contenu=?, categorie=?, region=?, " +
                     "media_path=?, date_modification=NOW() WHERE id=? AND auteur_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getTitre());
        ps.setString(2, a.getContenu());
        ps.setString(3, a.getCategorie());
        ps.setString(4, a.getRegion());
        ps.setString(5, a.getMediaPath());
        ps.setInt(6, a.getId());
        ps.setInt(7, a.getAuteurId());
        ps.executeUpdate();
    }

    // DELETE (soft delete)
    public void delete(int id, int auteurId) throws SQLException {
        String sql = "UPDATE annonce SET active = false WHERE id = ? AND auteur_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.setInt(2, auteurId);
        int affected = ps.executeUpdate();
        if (affected == 0) {
            throw new SQLException("Suppression impossible: annonce introuvable ou droits insuffisants.");
        }
    }

    // DELETE admin (hard delete)
    public void deleteById(int annonceId) throws SQLException {
        boolean oldAutoCommit = cnx.getAutoCommit();
        try {
            cnx.setAutoCommit(false);

            // Fallback applicatif si la FK n'est pas encore en ON DELETE CASCADE.
            try (PreparedStatement deleteComments = cnx.prepareStatement(
                    "DELETE FROM commentaire_annonce WHERE annonce_id = ?")) {
                deleteComments.setInt(1, annonceId);
                deleteComments.executeUpdate();
            }

            int affected;
            try (PreparedStatement deleteAnnonce = cnx.prepareStatement(
                    "DELETE FROM annonce WHERE id = ?")) {
                deleteAnnonce.setInt(1, annonceId);
                affected = deleteAnnonce.executeUpdate();
            }

            if (affected == 0) {
                cnx.rollback();
                throw new SQLException("Suppression impossible: annonce introuvable.");
            }

            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(oldAutoCommit);
        }
    }

    // SEARCH
    public List<Annonce> search(String keyword) throws SQLException {
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as auteur_nom FROM annonce a " +
                     "LEFT JOIN user u ON a.auteur_id = u.id " +
                     "WHERE a.active = true AND (a.titre LIKE ? OR a.contenu LIKE ?) " +
                     "ORDER BY a.date_pub DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToAnnonce(rs));
        }
        return list;
    }

    // Helper method
    private Annonce mapResultSetToAnnonce(ResultSet rs) throws SQLException {
        Annonce a = new Annonce();
        a.setId(rs.getInt("id"));
        a.setTitre(rs.getString("titre"));
        a.setDatePub(rs.getTimestamp("date_pub").toLocalDateTime());
        a.setRegion(rs.getString("region"));
        a.setContenu(rs.getString("contenu"));
        a.setCategorie(rs.getString("categorie"));
        a.setMediaPath(rs.getString("media_path"));
        a.setAuteurId(rs.getInt("auteur_id"));
        a.setAuteurNom(rs.getString("auteur_nom"));
        a.setActive(rs.getBoolean("active"));
        if (rs.getTimestamp("date_modification") != null) {
            a.setDateModification(rs.getTimestamp("date_modification").toLocalDateTime());
        }
        return a;
    }
}

