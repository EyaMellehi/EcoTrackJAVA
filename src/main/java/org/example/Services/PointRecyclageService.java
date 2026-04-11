package org.example.Services;

import org.example.Entities.Categorie;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PointRecyclageService {
    private Connection cnx;

    public PointRecyclageService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void addPoint(PointRecyclage p) throws SQLException {
        String sql = "INSERT INTO point_recyclage " +
                "(quantite, date_dec, description, address, latitude, longitude, statut, categorie_id, citoyen_id, assigned_at, ai_score, ai_priority, ai_explanation, ai_estimated_at, agent_terrain_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDouble(1, p.getQuantite());
        ps.setDate(2, Date.valueOf(p.getDateDec()));
        ps.setString(3, p.getDescription());
        ps.setString(4, p.getAddress());
        ps.setDouble(5, p.getLatitude());
        ps.setDouble(6, p.getLongitude());
        ps.setString(7, p.getStatut());
        ps.setInt(8, p.getCategorie().getId());
        ps.setInt(9, p.getCitoyen().getId());

        if (p.getAssignedAt() != null) {
            ps.setTimestamp(10, Timestamp.valueOf(p.getAssignedAt()));
        } else {
            ps.setNull(10, Types.TIMESTAMP);
        }

        if (p.getAiScore() != null) {
            ps.setInt(11, p.getAiScore());
        } else {
            ps.setNull(11, Types.INTEGER);
        }

        ps.setString(12, p.getAiPriority());
        ps.setString(13, p.getAiExplanation());

        if (p.getAiEstimatedAt() != null) {
            ps.setTimestamp(14, Timestamp.valueOf(p.getAiEstimatedAt()));
        } else {
            ps.setNull(14, Types.TIMESTAMP);
        }

        if (p.getAgentTerrain() != null) {
            ps.setInt(15, p.getAgentTerrain().getId());
        } else {
            ps.setNull(15, Types.INTEGER);
        }

        ps.executeUpdate();
    }

    public List<PointRecyclage> getAllPoints() throws SQLException {
        List<PointRecyclage> points = new ArrayList<>();

        String sql = "SELECT p.*, " +
                "c.id AS c_id, c.nom AS c_nom, c.description AS c_description, c.coef_points AS c_coef, " +
                "u.id AS u_id, u.name AS u_name, u.email AS u_email, " +
                "a.id AS a_id, a.name AS a_name, a.email AS a_email " +
                "FROM point_recyclage p " +
                "JOIN categorie c ON p.categorie_id = c.id " +
                "JOIN user u ON p.citoyen_id = u.id " +
                "LEFT JOIN user a ON p.agent_terrain_id = a.id " +
                "ORDER BY p.id DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            points.add(mapResultSetToPoint(rs));
        }

        return points;
    }

    public PointRecyclage getPointById(int id) throws SQLException {
        String sql = "SELECT p.*, " +
                "c.id AS c_id, c.nom AS c_nom, c.description AS c_description, c.coef_points AS c_coef, " +
                "u.id AS u_id, u.name AS u_name, u.email AS u_email, " +
                "a.id AS a_id, a.name AS a_name, a.email AS a_email " +
                "FROM point_recyclage p " +
                "JOIN categorie c ON p.categorie_id = c.id " +
                "JOIN user u ON p.citoyen_id = u.id " +
                "LEFT JOIN user a ON p.agent_terrain_id = a.id " +
                "WHERE p.id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToPoint(rs);
        }

        return null;
    }

    public void updatePoint(PointRecyclage p) throws SQLException {
        String sql = "UPDATE point_recyclage SET " +
                "quantite = ?, date_dec = ?, description = ?, address = ?, latitude = ?, longitude = ?, statut = ?, " +
                "categorie_id = ?, citoyen_id = ?, assigned_at = ?, ai_score = ?, ai_priority = ?, ai_explanation = ?, ai_estimated_at = ?, agent_terrain_id = ? " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDouble(1, p.getQuantite());
        ps.setDate(2, Date.valueOf(p.getDateDec()));
        ps.setString(3, p.getDescription());
        ps.setString(4, p.getAddress());
        ps.setDouble(5, p.getLatitude());
        ps.setDouble(6, p.getLongitude());
        ps.setString(7, p.getStatut());
        ps.setInt(8, p.getCategorie().getId());
        ps.setInt(9, p.getCitoyen().getId());

        if (p.getAssignedAt() != null) {
            ps.setTimestamp(10, Timestamp.valueOf(p.getAssignedAt()));
        } else {
            ps.setNull(10, Types.TIMESTAMP);
        }

        if (p.getAiScore() != null) {
            ps.setInt(11, p.getAiScore());
        } else {
            ps.setNull(11, Types.INTEGER);
        }

        ps.setString(12, p.getAiPriority());
        ps.setString(13, p.getAiExplanation());

        if (p.getAiEstimatedAt() != null) {
            ps.setTimestamp(14, Timestamp.valueOf(p.getAiEstimatedAt()));
        } else {
            ps.setNull(14, Types.TIMESTAMP);
        }

        if (p.getAgentTerrain() != null) {
            ps.setInt(15, p.getAgentTerrain().getId());
        } else {
            ps.setNull(15, Types.INTEGER);
        }

        ps.setInt(16, p.getId());
        ps.executeUpdate();
    }

    public void deletePoint(int id) throws SQLException {
        String sql = "DELETE FROM point_recyclage WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<PointRecyclage> searchPoints(String keyword, String status) throws SQLException {
        List<PointRecyclage> points = new ArrayList<>();

        String sql = "SELECT p.*, " +
                "c.id AS c_id, c.nom AS c_nom, c.description AS c_description, c.coef_points AS c_coef, " +
                "u.id AS u_id, u.name AS u_name, u.email AS u_email, " +
                "a.id AS a_id, a.name AS a_name, a.email AS a_email " +
                "FROM point_recyclage p " +
                "JOIN categorie c ON p.categorie_id = c.id " +
                "JOIN user u ON p.citoyen_id = u.id " +
                "LEFT JOIN user a ON p.agent_terrain_id = a.id " +
                "WHERE (p.address LIKE ? OR p.description LIKE ? OR c.nom LIKE ? OR p.statut LIKE ?) " +
                "AND (? IS NULL OR ? = '' OR p.statut = ?) " +
                "ORDER BY p.id DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        String like = "%" + keyword + "%";
        ps.setString(1, like);
        ps.setString(2, like);
        ps.setString(3, like);
        ps.setString(4, like);

        ps.setString(5, status);
        ps.setString(6, status);
        ps.setString(7, status);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            points.add(mapResultSetToPoint(rs));
        }

        return points;
    }

    public int countAllPoints() throws SQLException {
        String sql = "SELECT COUNT(*) FROM point_recyclage";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM point_recyclage WHERE statut = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public List<PointRecyclage> getPointsForMunicipal(User municipalUser) throws SQLException {
        List<PointRecyclage> allPoints = getAllPoints();
        List<PointRecyclage> result = new ArrayList<>();

        if (municipalUser == null) {
            return result;
        }

        String delegation = normalizeText(municipalUser.getDelegation());

        for (PointRecyclage p : allPoints) {
            String address = normalizeText(p.getAddress());

            if (!delegation.isEmpty() && address.contains(delegation)) {
                result.add(p);
            }
        }

        return result;
    }

    public void assignPointToFieldAgent(int pointId, int fieldAgentId) throws SQLException {
        String sql = "UPDATE point_recyclage " +
                "SET agent_terrain_id = ?, statut = ?, assigned_at = ? " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, fieldAgentId);
        ps.setString(2, "IN_PROGRESS");
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(4, pointId);
        ps.executeUpdate();
    }

    public void refusePointByMunicipal(int pointId) throws SQLException {
        String sql = "UPDATE point_recyclage " +
                "SET statut = ?, agent_terrain_id = NULL, assigned_at = NULL " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "REFUSE");
        ps.setInt(2, pointId);
        ps.executeUpdate();
    }

    public List<PointRecyclage> getPointsForFieldAgent(User fieldAgent) throws SQLException {
        List<PointRecyclage> points = new ArrayList<>();

        String sql = "SELECT p.*, " +
                "c.id AS c_id, c.nom AS c_nom, c.description AS c_description, c.coef_points AS c_coef, " +
                "u.id AS u_id, u.name AS u_name, u.email AS u_email, " +
                "a.id AS a_id, a.name AS a_name, a.email AS a_email " +
                "FROM point_recyclage p " +
                "JOIN categorie c ON p.categorie_id = c.id " +
                "JOIN user u ON p.citoyen_id = u.id " +
                "LEFT JOIN user a ON p.agent_terrain_id = a.id " +
                "WHERE p.agent_terrain_id = ? " +
                "ORDER BY p.id DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, fieldAgent.getId());

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            points.add(mapResultSetToPoint(rs));
        }

        return points;
    }

    public void markPointCollected(int pointId) throws SQLException {
        String sql = "UPDATE point_recyclage SET statut = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "COLLECTE");
        ps.setInt(2, pointId);
        ps.executeUpdate();
    }

    private PointRecyclage mapResultSetToPoint(ResultSet rs) throws SQLException {
        Categorie categorie = new Categorie();
        categorie.setId(rs.getInt("c_id"));
        categorie.setNom(rs.getString("c_nom"));
        categorie.setDescription(rs.getString("c_description"));
        categorie.setCoefPoints(rs.getDouble("c_coef"));

        User citoyen = new User();
        citoyen.setId(rs.getInt("u_id"));
        citoyen.setName(rs.getString("u_name"));
        citoyen.setEmail(rs.getString("u_email"));

        User agentTerrain = null;
        int agentId = rs.getInt("a_id");
        if (!rs.wasNull()) {
            agentTerrain = new User();
            agentTerrain.setId(agentId);
            agentTerrain.setName(rs.getString("a_name"));
            agentTerrain.setEmail(rs.getString("a_email"));
        }

        PointRecyclage p = new PointRecyclage();
        p.setId(rs.getInt("id"));
        p.setQuantite(rs.getDouble("quantite"));

        Date dateDec = rs.getDate("date_dec");
        if (dateDec != null) {
            p.setDateDec(dateDec.toLocalDate());
        }

        p.setDescription(rs.getString("description"));
        p.setAddress(rs.getString("address"));
        p.setLatitude(rs.getDouble("latitude"));
        p.setLongitude(rs.getDouble("longitude"));
        p.setStatut(rs.getString("statut"));
        p.setCategorie(categorie);
        p.setCitoyen(citoyen);
        p.setAgentTerrain(agentTerrain);

        Timestamp assignedAt = rs.getTimestamp("assigned_at");
        if (assignedAt != null) {
            p.setAssignedAt(assignedAt.toLocalDateTime());
        }

        int aiScore = rs.getInt("ai_score");
        if (!rs.wasNull()) {
            p.setAiScore(aiScore);
        }

        p.setAiPriority(rs.getString("ai_priority"));
        p.setAiExplanation(rs.getString("ai_explanation"));

        Timestamp aiEstimatedAt = rs.getTimestamp("ai_estimated_at");
        if (aiEstimatedAt != null) {
            p.setAiEstimatedAt(aiEstimatedAt.toLocalDateTime());
        }

        return p;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.toLowerCase().trim();
        normalized = normalized.replaceAll("[^a-z0-9 ]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }
    public int countInProgressPointsForFieldAgent(int fieldAgentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM point_recyclage WHERE agent_terrain_id = ? AND statut = 'IN_PROGRESS'";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, fieldAgentId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }
}