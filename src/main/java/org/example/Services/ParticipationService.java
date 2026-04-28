package org.example.Services;

import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Utils.AttendanceCodeUtils;
import org.example.Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService {
    private final Connection connection;
    private final String eventTable;
    private final String participationTable;
    private final String userTable;
    private final String userPointsColumn;
    private final String eventFkColumn;
    private final String userFkColumn;
    private final String statusColumn;
    private final String dateColumn;
    private String lastError;

    public ParticipationService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.eventTable = resolveExistingTable("events", "event");
        this.participationTable = resolveExistingTable("participations", "participation");
        this.userTable = resolveExistingTable("users", "user");
        this.userPointsColumn = resolveExistingColumn(userTable, "points");
        this.eventFkColumn = resolveExistingColumn(participationTable, "event_id", "eventId");
        this.userFkColumn = resolveExistingColumn(participationTable, "user_id", "userId", "citoyen_id", "citoyenId");
        this.statusColumn = resolveExistingColumn(participationTable, "statut", "status");
        this.dateColumn = resolveExistingColumn(participationTable, "date_inscription", "dateInscription", "joined_at", "created_at");
    }

    public String getLastError() {
        return lastError;
    }

    public List<ParticipationHistoryItem> getParticipationHistory(int userId) {
        List<ParticipationHistoryItem> history = new ArrayList<>();
        lastError = null;

        if (participationTable == null || eventTable == null || eventFkColumn == null || userFkColumn == null) {
            lastError = "Configuration des tables de participation/evenement invalide.";
            return history;
        }

        String statusExpr = statusColumn != null ? "p." + statusColumn : "'inscrit'";
        String dateExpr = dateColumn != null ? "p." + dateColumn : "e.date_deb";
        String pointsExpr = "CASE WHEN LOWER(COALESCE(" + statusExpr + ", '')) = 'present' THEN e.point_gain ELSE 0 END";

        String sql = "SELECT e.id AS event_id, e.titre, e.lieu, e.date_deb, e.date_fin, e.point_gain, " +
                statusExpr + " AS participation_status, " +
                pointsExpr + " AS earned_points, " +
                dateExpr + " AS participation_date " +
                "FROM " + participationTable + " p " +
                "JOIN " + eventTable + " e ON p." + eventFkColumn + " = e.id " +
                "WHERE p." + userFkColumn + " = ? " +
                "ORDER BY participation_date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ParticipationHistoryItem item = new ParticipationHistoryItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitre(rs.getString("titre"));
                    item.setLieu(rs.getString("lieu"));

                    Timestamp eventDebTs = rs.getTimestamp("date_deb");
                    if (eventDebTs != null) {
                        item.setEventDate(eventDebTs.toLocalDateTime());
                    }

                    item.setParticipationStatus(rs.getString("participation_status"));
                    item.setPointGain(rs.getInt("earned_points"));

                    Timestamp participationTs = rs.getTimestamp("participation_date");
                    if (participationTs != null) {
                        item.setParticipationDate(participationTs.toLocalDateTime());
                    }
                    history.add(item);
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }

        return history;
    }

    public List<EventParticipantItem> getEventParticipants(int eventId) {
        List<EventParticipantItem> participants = new ArrayList<>();
        lastError = null;

        if (participationTable == null || userTable == null || eventFkColumn == null || userFkColumn == null) {
            lastError = "Configuration des tables participation/utilisateur invalide.";
            return participants;
        }

        String statusExpr = statusColumn != null ? "p." + statusColumn : "'inscrit'";
        String dateExpr = dateColumn != null ? "p." + dateColumn : "NULL";

        String sql = "SELECT u.id AS user_id, u.name, u.email, u.phone, " +
                statusExpr + " AS participation_status, " +
                dateExpr + " AS participation_date " +
                "FROM " + participationTable + " p " +
                "JOIN " + userTable + " u ON p." + userFkColumn + " = u.id " +
                "WHERE p." + eventFkColumn + " = ? " +
                "ORDER BY participation_date DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    EventParticipantItem item = new EventParticipantItem();
                    item.setUserId(rs.getInt("user_id"));
                    item.setName(rs.getString("name"));
                    item.setEmail(rs.getString("email"));
                    item.setPhone(rs.getString("phone"));
                    item.setStatus(rs.getString("participation_status"));

                    Timestamp ts = rs.getTimestamp("participation_date");
                    if (ts != null) {
                        item.setParticipationDate(ts.toLocalDateTime());
                    }

                    participants.add(item);
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }

        return participants;
    }

    public boolean canParticipate(User user, Event event) {
        if (participationTable == null || eventFkColumn == null || userFkColumn == null) {
            lastError = "Table de participation introuvable ou colonnes manquantes.";
            return false;
        }
        return !hasParticipation(user.getId(), event.getId());
    }

    public boolean hasParticipation(int userId, int eventId) {
        if (participationTable == null || eventFkColumn == null || userFkColumn == null) {
            return false;
        }

        String sql = "SELECT 1 FROM " + participationTable + " WHERE " + eventFkColumn + " = ? AND " + userFkColumn + " = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            statement.setInt(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public boolean registerParticipation(User user, Event event) {
        lastError = null;
        if (participationTable == null || eventFkColumn == null || userFkColumn == null) {
            lastError = "Table de participation introuvable ou colonnes manquantes.";
            return false;
        }

        if (hasParticipation(user.getId(), event.getId())) {
            lastError = "Vous êtes déjà inscrit à cet événement.";
            return false;
        }

        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        columns.add(eventFkColumn);
        values.add(event.getId());
        columns.add(userFkColumn);
        values.add(user.getId());

        if (statusColumn != null) {
            columns.add(statusColumn);
            values.add("inscrit");
        }

        if (dateColumn != null) {
            columns.add(dateColumn);
            values.add(Timestamp.valueOf(java.time.LocalDateTime.now()));
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(participationTable)
                .append(" (")
                .append(String.join(", ", columns))
                .append(") VALUES (")
                .append(String.join(", ", java.util.Collections.nCopies(columns.size(), "?")))
                .append(")");

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    statement.setObject(i + 1, values.get(i));
                }
                if (statement.executeUpdate() <= 0) {
                    connection.rollback();
                    lastError = "Inscription impossible.";
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            lastError = e.getMessage();
            return false;
        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean updateParticipationStatus(Event event, int userId, String newStatus) {
        return updateParticipationStatus(event, userId, newStatus, null);
    }

    public boolean updateParticipationStatus(Event event, int userId, String newStatus, String attendanceCode) {
        lastError = null;

        if (event == null) {
            lastError = "Evenement invalide.";
            return false;
        }
        if (statusColumn == null || participationTable == null || eventFkColumn == null || userFkColumn == null) {
            lastError = "Le statut de participation n'est pas configurable dans la base.";
            return false;
        }
        if (!"present".equals(newStatus) && !"absent".equals(newStatus)) {
            lastError = "Statut invalide.";
            return false;
        }
        if ("present".equals(newStatus) && !isValidAttendanceCode(event, userId, attendanceCode)) {
            lastError = "Code de presence invalide. Verifiez le code a 5 chiffres du citoyen.";
            return false;
        }
        if (event.getDateFin() == null || !LocalDateTime.now().isAfter(event.getDateFin())) {
            lastError = "Le marquage de presence est autorise uniquement apres la date de fin de l'evenement.";
            return false;
        }

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            String currentStatus;
            String readSql = "SELECT " + statusColumn + " FROM " + participationTable +
                    " WHERE " + eventFkColumn + " = ? AND " + userFkColumn + " = ? FOR UPDATE";
            try (PreparedStatement readStatement = connection.prepareStatement(readSql)) {
                readStatement.setInt(1, event.getId());
                readStatement.setInt(2, userId);
                try (ResultSet rs = readStatement.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        lastError = "Participation introuvable pour ce citoyen.";
                        return false;
                    }
                    currentStatus = rs.getString(1);
                }
            }

            String normalizedCurrent = currentStatus == null || currentStatus.isBlank()
                    ? "inscrit"
                    : currentStatus.trim().toLowerCase();

            if (newStatus.equals(normalizedCurrent)) {
                connection.commit();
                return true;
            }

            if ("present".equals(normalizedCurrent) && "absent".equals(newStatus)) {
                connection.rollback();
                lastError = "Ce citoyen est deja confirme present. Le statut ne peut pas revenir a absent.";
                return false;
            }

            String updateSql = "UPDATE " + participationTable + " SET " + statusColumn + " = ?" +
                    " WHERE " + eventFkColumn + " = ? AND " + userFkColumn + " = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, newStatus);
                updateStatement.setInt(2, event.getId());
                updateStatement.setInt(3, userId);
                updateStatement.executeUpdate();
            }

            boolean shouldAwardPoints = "present".equals(newStatus) && !"present".equals(normalizedCurrent);
            int pointsToAdd = Math.max(0, event.getPointGain());
            if (shouldAwardPoints && pointsToAdd > 0 && userTable != null && userPointsColumn != null) {
                String pointsSql = "UPDATE " + userTable +
                        " SET " + userPointsColumn + " = COALESCE(" + userPointsColumn + ", 0) + ? WHERE id = ?";
                try (PreparedStatement pointsStatement = connection.prepareStatement(pointsSql)) {
                    pointsStatement.setInt(1, pointsToAdd);
                    pointsStatement.setInt(2, userId);
                    pointsStatement.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            lastError = e.getMessage();
            return false;
        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    public String generateAttendanceCode(Event event, int userId) {
        return AttendanceCodeUtils.generateFiveDigitCode(event, userId);
    }

    public String buildAttendanceQrPayload(Event event, int userId) {
        return AttendanceCodeUtils.buildQrPayload(event, userId);
    }

    private boolean isValidAttendanceCode(Event event, int userId, String attendanceCode) {
        if (attendanceCode == null || attendanceCode.isBlank()) {
            return false;
        }

        String normalized = attendanceCode.trim();
        if (!normalized.matches("\\d{5}")) {
            return false;
        }

        return AttendanceCodeUtils.matches(event, userId, normalized);
    }


    private String resolveExistingTable(String... candidates) {
        for (String candidate : candidates) {
            try (PreparedStatement statement = connection.prepareStatement("SHOW TABLES LIKE ?")) {
                statement.setString(1, candidate);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return candidate;
                    }
                }
            } catch (SQLException e) {
                lastError = e.getMessage();
            }
        }
        return null;
    }

    private String resolveExistingColumn(String table, String... candidates) {
        if (table == null) {
            return null;
        }
        for (String candidate : candidates) {
            String sql = "SHOW COLUMNS FROM " + table + " LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, candidate);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return candidate;
                    }
                }
            } catch (SQLException e) {
                lastError = e.getMessage();
            }
        }
        return null;
    }

    public static class ParticipationHistoryItem {
        private int eventId;
        private String titre;
        private String lieu;
        private LocalDateTime eventDate;
        private LocalDateTime participationDate;
        private String participationStatus;
        private int pointGain;

        public int getEventId() {
            return eventId;
        }

        public void setEventId(int eventId) {
            this.eventId = eventId;
        }

        public String getTitre() {
            return titre;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public String getLieu() {
            return lieu;
        }

        public void setLieu(String lieu) {
            this.lieu = lieu;
        }

        public LocalDateTime getEventDate() {
            return eventDate;
        }

        public void setEventDate(LocalDateTime eventDate) {
            this.eventDate = eventDate;
        }

        public LocalDateTime getParticipationDate() {
            return participationDate;
        }

        public void setParticipationDate(LocalDateTime participationDate) {
            this.participationDate = participationDate;
        }

        public String getParticipationStatus() {
            return participationStatus;
        }

        public void setParticipationStatus(String participationStatus) {
            this.participationStatus = participationStatus;
        }

        public int getPointGain() {
            return pointGain;
        }

        public void setPointGain(int pointGain) {
            this.pointGain = pointGain;
        }
    }

    public static class EventParticipantItem {
        private int userId;
        private String name;
        private String email;
        private String phone;
        private String status;
        private LocalDateTime participationDate;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getParticipationDate() {
            return participationDate;
        }

        public void setParticipationDate(LocalDateTime participationDate) {
            this.participationDate = participationDate;
        }
    }
}

