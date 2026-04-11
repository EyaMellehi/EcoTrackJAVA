package org.example.Services;

import org.example.Entities.Signalement;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SignalementService {

    private final Connection cnx;

    public SignalementService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void add(Signalement s) throws SQLException {
        String sql = "INSERT INTO signalement " +
                "(titre, description, type, statut, addresse, latitude, longitude, date_creation, citoyen_id, agent_assigne_id, delegation, assigned_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, s.getTitre());
        ps.setString(2, s.getDescription());
        ps.setString(3, s.getType());
        ps.setString(4, s.getStatut());
        ps.setString(5, s.getAddresse());
        ps.setDouble(6, s.getLatitude());
        ps.setDouble(7, s.getLongitude());
        ps.setTimestamp(8, Timestamp.valueOf(s.getDateCreation()));

        if (s.getCitoyenId() != null) {
            ps.setInt(9, s.getCitoyenId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }

        if (s.getAgentAssigneId() != null) {
            ps.setInt(10, s.getAgentAssigneId());
        } else {
            ps.setNull(10, Types.INTEGER);
        }

        ps.setString(11, s.getDelegation());

        if (s.getAssignedAt() != null) {
            ps.setTimestamp(12, Timestamp.valueOf(s.getAssignedAt()));
        } else {
            ps.setNull(12, Types.TIMESTAMP);
        }

        ps.executeUpdate();
    }

    public List<Signalement> getAll() throws SQLException {
        List<Signalement> list = new ArrayList<>();
        String sql = "SELECT * FROM signalement";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(mapResultSetToSignalement(rs));
        }

        return list;
    }

    public List<Signalement> getByCitoyenId(int citoyenId) throws SQLException {
        List<Signalement> list = new ArrayList<>();
        String sql = "SELECT * FROM signalement WHERE citoyen_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, citoyenId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToSignalement(rs));
        }

        return list;
    }

    public Signalement getById(int id) throws SQLException {
        String sql = "SELECT * FROM signalement WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToSignalement(rs);
        }

        return null;
    }

    public void update(Signalement s) throws SQLException {
        String sql = "UPDATE signalement SET titre=?, description=?, type=?, statut=?, addresse=?, latitude=?, longitude=?, citoyen_id=?, agent_assigne_id=?, delegation=?, assigned_at=? WHERE id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, s.getTitre());
        ps.setString(2, s.getDescription());
        ps.setString(3, s.getType());
        ps.setString(4, s.getStatut());
        ps.setString(5, s.getAddresse());
        ps.setDouble(6, s.getLatitude());
        ps.setDouble(7, s.getLongitude());

        if (s.getCitoyenId() != null) {
            ps.setInt(8, s.getCitoyenId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }

        if (s.getAgentAssigneId() != null) {
            ps.setInt(9, s.getAgentAssigneId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }

        ps.setString(10, s.getDelegation());

        if (s.getAssignedAt() != null) {
            ps.setTimestamp(11, Timestamp.valueOf(s.getAssignedAt()));
        } else {
            ps.setNull(11, Types.TIMESTAMP);
        }

        ps.setInt(12, s.getId());

        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM signalement WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private Signalement mapResultSetToSignalement(ResultSet rs) throws SQLException {
        Timestamp dateCreationTs = rs.getTimestamp("date_creation");
        Timestamp assignedAtTs = rs.getTimestamp("assigned_at");

        Integer citoyenId = rs.getObject("citoyen_id") != null ? rs.getInt("citoyen_id") : null;
        Integer agentAssigneId = rs.getObject("agent_assigne_id") != null ? rs.getInt("agent_assigne_id") : null;

        return new Signalement(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("description"),
                rs.getString("type"),
                rs.getString("statut"),
                rs.getString("addresse"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                dateCreationTs != null ? dateCreationTs.toLocalDateTime() : null,
                citoyenId,
                agentAssigneId,
                rs.getString("delegation"),
                assignedAtTs != null ? assignedAtTs.toLocalDateTime() : null
        );
    }

    public List<Signalement> getByAgentAssigneId(int agentId) throws SQLException {
        List<Signalement> list = new ArrayList<>();
        String sql = "SELECT * FROM signalement WHERE agent_assigne_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, agentId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToSignalement(rs));
        }

        return list;
    }

    public void updateStatut(int signalementId, String statut) throws SQLException {
        String sql = "UPDATE signalement SET statut = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, statut);
        ps.setInt(2, signalementId);
        ps.executeUpdate();
    }

    public List<Signalement> getByDelegation(String delegation) throws SQLException {
        List<Signalement> list = new ArrayList<>();
        String sql = "SELECT * FROM signalement WHERE delegation = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, delegation);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToSignalement(rs));
        }

        return list;
    }

    public void assignAgent(int signalementId, int agentId) throws SQLException {
        String sql = "UPDATE signalement SET agent_assigne_id = ?, statut = ?, assigned_at = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, agentId);
        ps.setString(2, "EN_COURS");
        ps.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
        ps.setInt(4, signalementId);
        ps.executeUpdate();
    }

    public int addAndReturnId(Signalement s) throws SQLException {
        String sql = "INSERT INTO signalement " +
                "(titre, description, type, statut, addresse, latitude, longitude, date_creation, citoyen_id, agent_assigne_id, delegation, assigned_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, s.getTitre());
        ps.setString(2, s.getDescription());
        ps.setString(3, s.getType());
        ps.setString(4, s.getStatut());
        ps.setString(5, s.getAddresse());
        ps.setDouble(6, s.getLatitude());
        ps.setDouble(7, s.getLongitude());
        ps.setTimestamp(8, Timestamp.valueOf(s.getDateCreation()));

        if (s.getCitoyenId() != null) {
            ps.setInt(9, s.getCitoyenId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }

        if (s.getAgentAssigneId() != null) {
            ps.setInt(10, s.getAgentAssigneId());
        } else {
            ps.setNull(10, Types.INTEGER);
        }

        ps.setString(11, s.getDelegation());

        if (s.getAssignedAt() != null) {
            ps.setTimestamp(12, Timestamp.valueOf(s.getAssignedAt()));
        } else {
            ps.setNull(12, Types.TIMESTAMP);
        }

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }

        throw new SQLException("Failed to retrieve generated signalement ID.");
    }
}