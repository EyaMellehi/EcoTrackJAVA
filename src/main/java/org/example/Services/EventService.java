package org.example.Services;

import org.example.Entities.Event;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {
    private final Connection connection;
    private final String eventTable;
    private final String participationTable;
    private final String participationEventFkColumn;
    private final String mediaTable;
    private final String mediaEventFkColumn;
    private final String eventCoverMediaColumn;
    private String lastDeleteError;

    public EventService() {
        this.connection = MyConnection.getInstance().getConnection();
        this.eventTable = resolveExistingTable("events", "event");
        this.participationTable = resolveExistingTable("participations", "participation");
        this.participationEventFkColumn = resolveExistingColumn(participationTable, "event_id", "eventId");
        this.mediaTable = resolveExistingTable("media", "medias");
        this.mediaEventFkColumn = resolveExistingColumn(mediaTable, "event_id", "eventId");
        this.eventCoverMediaColumn = resolveExistingColumn(eventTable, "cover_media_id", "coverMediaId", "cover_mediaid");
        if (this.eventTable == null) {
            System.err.println("Aucune table d'evenements trouvee (events/event).");
        }
    }

    public String getLastDeleteError() {
        return lastDeleteError;
    }

    /**
     * Récupère tous les événements publiés
     */
    public List<Event> getAllPublishedEvents() {
        List<Event> events = new ArrayList<>();
        if (eventTable == null) {
            return events;
        }

        String query = "SELECT e.*" + participationCountSelectSql() + " FROM " + eventTable + " e " +
                "WHERE e.statut = ? " +
                "ORDER BY e.date_deb ASC";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "publie");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = mapResultSetToEvent(resultSet);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * Recherche et filtre les événements
     */
    public List<Event> searchEvents(String query, String statut, String sortBy, String order) {
        List<Event> events = new ArrayList<>();
        if (eventTable == null) {
            return events;
        }
        
        StringBuilder sql = new StringBuilder(
            "SELECT e.*" + participationCountSelectSql() + " FROM " + eventTable + " e WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Filtre sur le statut
        if (statut != null && !statut.isEmpty()) {
            sql.append("AND e.statut = ? ");
            params.add(statut);
        } else {
            sql.append("AND e.statut = ? ");
            params.add("publie");
        }

        // Filtre sur la recherche
        if (query != null && !query.isEmpty()) {
            sql.append("AND (e.titre LIKE ? OR e.lieu LIKE ? OR e.description LIKE ?) ");
            String searchTerm = "%" + query + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        // Tri
        if (sortBy == null || sortBy.isEmpty() || sortBy.equals("date_deb")) {
            sortBy = "e.date_deb";
        } else if (sortBy.equals("titre")) {
            sortBy = "e.titre";
        } else if (sortBy.equals("lieu")) {
            sortBy = "e.lieu";
        }
        
        order = (order != null && order.equals("DESC")) ? "DESC" : "ASC";
        sql.append("ORDER BY ").append(sortBy).append(" ").append(order);

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = mapResultSetToEvent(resultSet);
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * Recherche admin: peut retourner tous les statuts si aucun filtre n'est selectionne.
     */
    public List<Event> searchEventsForManagement(String query, String statut, String sortBy, String order) {
        return searchEventsForManagement(query, statut, sortBy, order, null);
    }

    public List<Event> searchEventsForManagement(String query, String statut, String sortBy, String order, String region) {
        List<Event> events = new ArrayList<>();
        if (eventTable == null) {
            return events;
        }

        StringBuilder sql = new StringBuilder("SELECT e.*" + participationCountSelectSql() + " FROM " + eventTable + " e WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (statut != null && !statut.isBlank() && !"Tous".equalsIgnoreCase(statut)) {
            sql.append("AND e.statut = ? ");
            params.add(statut);
        }

        if (query != null && !query.isBlank()) {
            sql.append("AND (e.titre LIKE ? OR e.lieu LIKE ? OR e.description LIKE ?) ");
            String searchTerm = "%" + query.trim() + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (region != null && !region.isBlank()) {
            sql.append("AND LOWER(TRIM(e.lieu)) = LOWER(TRIM(?)) ");
            params.add(region.trim());
        }

        String sortColumn;
        if ("titre".equals(sortBy)) {
            sortColumn = "e.titre";
        } else if ("lieu".equals(sortBy)) {
            sortColumn = "e.lieu";
        } else {
            sortColumn = "e.date_deb";
        }

        String sortOrder = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";
        sql.append("ORDER BY ").append(sortColumn).append(" ").append(sortOrder);

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(mapResultSetToEvent(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }

    /**
     * Récupère un événement par ID
     */
    public Event getEventById(int id) {
        if (eventTable == null) {
            return null;
        }
        String query = "SELECT e.*" + participationCountSelectSql() + " FROM " + eventTable + " e WHERE e.id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEvent(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Crée un nouvel événement
     */
    public boolean createEvent(Event event) {
        if (eventTable == null) {
            return false;
        }
        String query = "INSERT INTO " + eventTable + " (titre, description, lieu, date_deb, date_fin, capacite_max, point_gain, statut, createur_id, cover_media_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, event.getTitre());
            statement.setString(2, event.getDescription());
            statement.setString(3, event.getLieu());
            statement.setTimestamp(4, Timestamp.valueOf(event.getDateDeb()));
            statement.setTimestamp(5, Timestamp.valueOf(event.getDateFin()));
            statement.setInt(6, event.getCapaciteMax());
            statement.setInt(7, event.getPointGain());
            statement.setString(8, event.getStatut());
            statement.setInt(9, event.getCreateurId());
            if (event.getCoverMediaId() != null) {
                statement.setInt(10, event.getCoverMediaId());
            } else {
                statement.setNull(10, java.sql.Types.INTEGER);
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mets à jour un événement
     */
    public boolean updateEvent(Event event) {
        if (eventTable == null) {
            return false;
        }
        String query = "UPDATE " + eventTable + " SET titre = ?, description = ?, lieu = ?, " +
                      "date_deb = ?, date_fin = ?, capacite_max = ?, point_gain = ?, statut = ?, cover_media_id = ? " +
                      "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, event.getTitre());
            statement.setString(2, event.getDescription());
            statement.setString(3, event.getLieu());
            statement.setTimestamp(4, Timestamp.valueOf(event.getDateDeb()));
            statement.setTimestamp(5, Timestamp.valueOf(event.getDateFin()));
            statement.setInt(6, event.getCapaciteMax());
            statement.setInt(7, event.getPointGain());
            statement.setString(8, event.getStatut());
            if (event.getCoverMediaId() != null) {
                statement.setInt(9, event.getCoverMediaId());
            } else {
                statement.setNull(9, java.sql.Types.INTEGER);
            }
            statement.setInt(10, event.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime un événement
     */
    public boolean deleteEvent(int id) {
        lastDeleteError = null;
        if (eventTable == null) {
            lastDeleteError = "Table d'evenements introuvable.";
            return false;
        }
        String deleteEventSql = "DELETE FROM " + eventTable + " WHERE id = ?";

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // Certaines bases refusent la suppression si des participations existent.
            if (participationTable != null && participationEventFkColumn != null) {
                String deleteParticipationsSql = "DELETE FROM " + participationTable + " WHERE " + participationEventFkColumn + " = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteParticipationsSql)) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
            }

            // Si event.cover_media_id reference media, on nettoie d'abord la reference.
            if (eventCoverMediaColumn != null) {
                String clearCoverSql = "UPDATE " + eventTable + " SET " + eventCoverMediaColumn + " = NULL WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(clearCoverSql)) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
            }

            // Certains schemas lient media -> event (pas toujours en cascade).
            if (mediaTable != null && mediaEventFkColumn != null) {
                String deleteMediaSql = "DELETE FROM " + mediaTable + " WHERE " + mediaEventFkColumn + " = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteMediaSql)) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
            }

            int deletedRows;
            try (PreparedStatement statement = connection.prepareStatement(deleteEventSql)) {
                statement.setInt(1, id);
                deletedRows = statement.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(oldAutoCommit);
            return deletedRows > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            lastDeleteError = buildDeleteErrorMessage(e);
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Convertit un ResultSet en objet Event
     */
    private Event mapResultSetToEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getInt("id"));
        event.setTitre(resultSet.getString("titre"));
        event.setDescription(resultSet.getString("description"));
        event.setLieu(resultSet.getString("lieu"));
        
        Timestamp dateDebTS = resultSet.getTimestamp("date_deb");
        if (dateDebTS != null) {
            event.setDateDeb(dateDebTS.toLocalDateTime());
        }
        
        Timestamp dateFinTS = resultSet.getTimestamp("date_fin");
        if (dateFinTS != null) {
            event.setDateFin(dateFinTS.toLocalDateTime());
        }
        
        event.setCapaciteMax(resultSet.getInt("capacite_max"));
        event.setPointGain(resultSet.getInt("point_gain"));
        event.setStatut(resultSet.getString("statut"));
        event.setCreateurId(resultSet.getInt("createur_id"));
        
        int coverMediaId = resultSet.getInt("cover_media_id");
        if (!resultSet.wasNull()) {
            event.setCoverMediaId(coverMediaId);
        }
        
        try {
            event.setParticipationCount(resultSet.getInt("participation_count"));
        } catch (SQLException ignored) {
            event.setParticipationCount(0);
        }
        
        return event;
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
                e.printStackTrace();
            }
        }
        return null;
    }

    private String participationCountSelectSql() {
        if (participationTable == null || participationEventFkColumn == null) {
            return ", 0 AS participation_count";
        }
        return ", (SELECT COUNT(*) FROM " + participationTable + " p WHERE p." + participationEventFkColumn + " = e.id) AS participation_count";
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
                e.printStackTrace();
            }
        }
        return null;
    }

    private String buildDeleteErrorMessage(SQLException e) {
        String sqlState = e.getSQLState();
        if ("23000".equals(sqlState)) {
            return "Suppression bloquee par des relations en base (cle etrangere).";
        }
        return e.getMessage() == null ? "Erreur SQL inconnue pendant la suppression." : e.getMessage();
    }
}

