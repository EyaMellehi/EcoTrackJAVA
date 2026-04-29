
package org.example.Services;

import org.example.Entities.PointRecyclage;
import org.example.Entities.RapportRecyc;
import org.example.Entities.User;
import org.example.Utils.MyConnection;

import java.sql.*;

public class RapportRecycService {

    private Connection cnx;

    public RapportRecycService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void createRapport(RapportRecyc rapport) throws SQLException {
        String sql = "INSERT INTO rapport_recyc " +
                "(date_collect, quantite_collecte, commentaire, point_attribue, point_recy_id, agent_terrain_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setTimestamp(1, Timestamp.valueOf(rapport.getDateCollect()));
        ps.setDouble(2, rapport.getQuantiteCollecte());
        ps.setString(3, rapport.getCommentaire());
        ps.setInt(4, rapport.getPointAttribue());
        ps.setInt(5, rapport.getPointRecy().getId());
        ps.setInt(6, rapport.getAgentTerrain().getId());

        ps.executeUpdate();
    }

    public RapportRecyc getRapportByPointId(int pointId) throws SQLException {
        String sql = "SELECT r.*, " +
                "u.id AS u_id, u.name AS u_name, u.email AS u_email, " +
                "p.id AS p_id " +
                "FROM rapport_recyc r " +
                "LEFT JOIN user u ON r.agent_terrain_id = u.id " +
                "LEFT JOIN point_recyclage p ON r.point_recy_id = p.id " +
                "WHERE r.point_recy_id = ? " +
                "LIMIT 1";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, pointId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            RapportRecyc r = new RapportRecyc();
            r.setId(rs.getInt("id"));

            Timestamp t = rs.getTimestamp("date_collect");
            if (t != null) {
                r.setDateCollect(t.toLocalDateTime());
            }

            r.setQuantiteCollecte(rs.getDouble("quantite_collecte"));
            r.setCommentaire(rs.getString("commentaire"));
            r.setPointAttribue(rs.getInt("point_attribue"));

            User agent = null;
            int agentId = rs.getInt("u_id");
            if (!rs.wasNull()) {
                agent = new User();
                agent.setId(agentId);
                agent.setName(rs.getString("u_name"));
                agent.setEmail(rs.getString("u_email"));
                r.setAgentTerrain(agent);
            }

            PointRecyclage point = new PointRecyclage();
            point.setId(rs.getInt("p_id"));
            r.setPointRecy(point);

            return r;
        }

        return null;
    }

    public void createRapportAndRewardCitizen(RapportRecyc rapport, int citizenId) throws SQLException {
        boolean oldAutoCommit = cnx.getAutoCommit();

        try {
            cnx.setAutoCommit(false);

            String insertRapport = "INSERT INTO rapport_recyc " +
                    "(date_collect, quantite_collecte, commentaire, point_attribue, point_recy_id, agent_terrain_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = cnx.prepareStatement(insertRapport)) {
                ps.setTimestamp(1, Timestamp.valueOf(rapport.getDateCollect()));
                ps.setDouble(2, rapport.getQuantiteCollecte());
                ps.setString(3, rapport.getCommentaire());
                ps.setInt(4, rapport.getPointAttribue());
                ps.setInt(5, rapport.getPointRecy().getId());
                ps.setInt(6, rapport.getAgentTerrain().getId());
                ps.executeUpdate();
            }

            String updateCitizenPoints = "UPDATE user SET points = COALESCE(points, 0) + ? WHERE id = ?";

            try (PreparedStatement ps = cnx.prepareStatement(updateCitizenPoints)) {
                ps.setInt(1, rapport.getPointAttribue());
                ps.setInt(2, citizenId);
                ps.executeUpdate();
            }

            cnx.commit();

        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(oldAutoCommit);
        }
    }
}













