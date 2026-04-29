package org.example.Services;

import org.example.Entities.RapportSignalement;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RapportSignalementService {

    private final Connection cnx;

    public RapportSignalementService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void add(RapportSignalement rapport) throws SQLException {
        String sql = "INSERT INTO rapport_signalement (date_intervention, commentaire, signalement_id, agent_terrain_id) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setTimestamp(1, Timestamp.valueOf(rapport.getDateIntervention()));
        ps.setString(2, rapport.getCommentaire());
        ps.setInt(3, rapport.getSignalementId());
        ps.setInt(4, rapport.getAgentTerrainId());

        ps.executeUpdate();
    }

    public List<RapportSignalement> getAll() throws SQLException {
        List<RapportSignalement> list = new ArrayList<>();
        String sql = "SELECT * FROM rapport_signalement";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(mapResultSetToRapport(rs));
        }

        return list;
    }

    public RapportSignalement getById(int id) throws SQLException {
        String sql = "SELECT * FROM rapport_signalement WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToRapport(rs);
        }

        return null;
    }

    public RapportSignalement getBySignalementId(int signalementId) throws SQLException {
        String sql = "SELECT * FROM rapport_signalement WHERE signalement_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, signalementId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToRapport(rs);
        }

        return null;
    }

    public List<RapportSignalement> getByAgentTerrainId(int agentTerrainId) throws SQLException {
        List<RapportSignalement> list = new ArrayList<>();
        String sql = "SELECT * FROM rapport_signalement WHERE agent_terrain_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, agentTerrainId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToRapport(rs));
        }

        return list;
    }

    public void update(RapportSignalement rapport) throws SQLException {
        String sql = "UPDATE rapport_signalement SET date_intervention = ?, commentaire = ?, signalement_id = ?, agent_terrain_id = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setTimestamp(1, Timestamp.valueOf(rapport.getDateIntervention()));
        ps.setString(2, rapport.getCommentaire());
        ps.setInt(3, rapport.getSignalementId());
        ps.setInt(4, rapport.getAgentTerrainId());
        ps.setInt(5, rapport.getId());

        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM rapport_signalement WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private RapportSignalement mapResultSetToRapport(ResultSet rs) throws SQLException {
        return new RapportSignalement(
                rs.getInt("id"),
                rs.getTimestamp("date_intervention").toLocalDateTime(),
                rs.getString("commentaire"),
                rs.getInt("signalement_id"),
                rs.getInt("agent_terrain_id")
        );
    }


}