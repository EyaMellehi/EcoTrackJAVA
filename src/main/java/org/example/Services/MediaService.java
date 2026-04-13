package org.example.Services;

import org.example.Entities.Media;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaService {

    private final Connection cnx;

    public MediaService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void add(Media media) throws SQLException {
        String sql = "INSERT INTO media " +
                "(filename, type, url, created_at, user_id, signalement_id, rapport_signalement_id, annonce_id, event_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, media.getFilename());
        ps.setString(2, media.getType());
        ps.setString(3, media.getUrl());
        ps.setTimestamp(4, Timestamp.valueOf(media.getCreatedAt()));

        if (media.getUserId() != null) {
            ps.setInt(5, media.getUserId());
        } else {
            ps.setNull(5, Types.INTEGER);
        }

        if (media.getSignalementId() != null) {
            ps.setInt(6, media.getSignalementId());
        } else {
            ps.setNull(6, Types.INTEGER);
        }

        if (media.getRapportSignalementId() != null) {
            ps.setInt(7, media.getRapportSignalementId());
        } else {
            ps.setNull(7, Types.INTEGER);
        }

        if (media.getAnnonceId() != null) {
            ps.setInt(8, media.getAnnonceId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }

        if (media.getEventId() != null) {
            ps.setInt(9, media.getEventId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }

        ps.executeUpdate();
    }

    public List<Media> getBySignalementId(int signalementId) throws SQLException {
        List<Media> list = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE signalement_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, signalementId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToMedia(rs));
        }

        return list;
    }

    public List<Media> getByRapportSignalementId(int rapportSignalementId) throws SQLException {
        List<Media> list = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE rapport_signalement_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, rapportSignalementId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToMedia(rs));
        }

        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM media WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private Media mapResultSetToMedia(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("created_at");

        Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null;
        Integer signalementId = rs.getObject("signalement_id") != null ? rs.getInt("signalement_id") : null;
        Integer rapportSignalementId = rs.getObject("rapport_signalement_id") != null ? rs.getInt("rapport_signalement_id") : null;
        Integer annonceId = rs.getObject("annonce_id") != null ? rs.getInt("annonce_id") : null;
        Integer eventId = rs.getObject("event_id") != null ? rs.getInt("event_id") : null;

        return new Media(
                rs.getInt("id"),
                rs.getString("filename"),
                rs.getString("type"),
                rs.getString("url"),
                createdAtTs != null ? createdAtTs.toLocalDateTime() : null,
                userId,
                signalementId,
                rapportSignalementId,
                annonceId,
                eventId
        );
    }

    public Media getFirstBySignalementId(int signalementId) throws SQLException {
        String sql = "SELECT * FROM media WHERE signalement_id = ? ORDER BY created_at ASC LIMIT 1";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, signalementId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToMedia(rs);
        }

        return null;
    }

    public Media getFirstByRapportSignalementId(int rapportSignalementId) throws SQLException {
        String sql = "SELECT * FROM media WHERE rapport_signalement_id = ? ORDER BY created_at ASC LIMIT 1";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, rapportSignalementId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToMedia(rs);
        }

        return null;
    }
}