package org.example.Services;

import org.example.Entities.User;
import org.example.Utils.MyConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UserService {
    private Connection cnx;

    public UserService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM user WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public int countReportsForUser(User user) throws SQLException {
        if (user == null || user.getRoles() == null) {
            return 0;
        }

        String sql;

        if (user.getRoles().contains("ROLE_AGENT_TERRAIN")) {
            sql = "SELECT COUNT(*) FROM signalement WHERE agent_assigne_id = ?";
        } else if (user.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
            if (user.getRegion() == null || user.getRegion().isBlank()) {
                return 0;
            }
            sql = "SELECT COUNT(*) FROM signalement WHERE delegation IS NOT NULL AND delegation LIKE ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + user.getRegion() + "%");
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } else {
            sql = "SELECT COUNT(*) FROM signalement WHERE citoyen_id = ?";
        }

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, user.getId());
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int countRecyclingForUser(User user) throws SQLException {
        if (user == null || user.getRoles() == null) {
            return 0;
        }

        String sql;

        if (user.getRoles().contains("ROLE_AGENT_TERRAIN")) {
            sql = "SELECT COUNT(*) FROM point_recyclage WHERE agent_terrain_id = ?";
        } else if (user.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
            if (user.getRegion() == null || user.getRegion().isBlank()) {
                return 0;
            }
            sql = "SELECT COUNT(*) FROM point_recyclage WHERE address LIKE ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + user.getRegion() + "%");
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } else {
            sql = "SELECT COUNT(*) FROM point_recyclage WHERE citoyen_id = ?";
        }

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, user.getId());
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int countAssociations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM association";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int countPublishedEvents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM event WHERE statut = 'publie'";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public void registerCitoyen(User user) throws SQLException {
        String sql = "INSERT INTO user (email, roles, password, name, phone, region, points, is_active, image, delegation, faceio_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getEmail());
        ps.setString(2, "[\"ROLE_CITOYEN\"]"); // same format as Symfony JSON roles
        ps.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        ps.setString(4, user.getName());
        ps.setString(5, user.getPhone());
        ps.setString(6, user.getRegion());
        ps.setInt(7, 0);
        ps.setBoolean(8, true);
        ps.setString(9, null);
        ps.setString(10, null);
        ps.setString(11, null);

        ps.executeUpdate();
    }

    public User login(String email, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String hashedPassword = rs.getString("password");
            if (hashedPassword == null || hashedPassword.isEmpty()) {
                return null;
            }

            // Symfony/PHP bcrypt compatibility
            if (hashedPassword.startsWith("$2y$")) {
                hashedPassword = "$2a$" + hashedPassword.substring(4);
            }

            if (BCrypt.checkpw(plainPassword, hashedPassword)) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setPhone(rs.getString("phone"));
                user.setRegion(rs.getString("region"));
                user.setPoints(rs.getInt("points"));
                user.setActive(rs.getBoolean("is_active"));
                user.setImage(rs.getString("image"));
                user.setDelegation(rs.getString("delegation"));
                user.setFaceioId(rs.getString("faceio_id"));
                return user;
            }
        }

        return null;
    }
    public void updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE `user` SET password = ? WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt()));
        ps.setString(2, email);
        ps.executeUpdate();
    }
    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE `user` SET name = ?, email = ?, phone = ?, region = ?, image = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getRegion());
        ps.setString(5, user.getImage());
        ps.setInt(6, user.getId());
        ps.executeUpdate();
    }
    public boolean emailExistsForAnotherUser(String email, int userId) throws SQLException {
        String sql = "SELECT id FROM `user` WHERE email = ? AND id != ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public List<User> getCitoyens() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE roles LIKE '%ROLE_CITOYEN%' ORDER BY id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));

            users.add(user);
        }

        return users;
    }
    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM `user` WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
    public void updateSubscriberByAdmin(User user) throws SQLException {
        String sql = "UPDATE `user` SET name = ?, email = ?, phone = ?, region = ?, is_active = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getRegion());
        ps.setBoolean(5, user.isActive());
        ps.setInt(6, user.getId());
        ps.executeUpdate();
    }
    public List<User> getMunicipalAgents() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE roles LIKE '%ROLE_AGENT_MUNICIPAL%' ORDER BY id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setDelegation(rs.getString("delegation"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));

            users.add(user);
        }

        return users;
    }
    public List<User> getFieldAgents() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE roles LIKE '%ROLE_AGENT_TERRAIN%' ORDER BY id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setDelegation(rs.getString("delegation"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));

            users.add(user);
        }

        return users;
    }
    public void addMunicipalAgent(User user) throws SQLException {
        String sql = "INSERT INTO `user` (email, roles, password, name, phone, region, points, is_active, image, delegation, faceio_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getEmail());
        ps.setString(2, "[\"ROLE_AGENT_MUNICIPAL\"]");
        ps.setString(3, org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt()));
        ps.setString(4, user.getName());
        ps.setString(5, user.getPhone());
        ps.setString(6, user.getRegion());
        ps.setInt(7, 0);
        ps.setBoolean(8, user.isActive());
        ps.setString(9, user.getImage());
        ps.setString(10, user.getDelegation());
        ps.setString(11, null);

        ps.executeUpdate();
    }
    public boolean municipalDelegationExists(String region, String delegation) throws SQLException {
        String sql = "SELECT id FROM `user` WHERE roles LIKE '%ROLE_AGENT_MUNICIPAL%' AND region = ? AND delegation = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, region);
        ps.setString(2, delegation);

        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public void updateMunicipalAgent(User user) throws SQLException {
        String sql = "UPDATE `user` SET name = ?, email = ?, phone = ?, region = ?, delegation = ?, is_active = ? WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getRegion());
        ps.setString(5, user.getDelegation());
        ps.setBoolean(6, user.isActive());
        ps.setInt(7, user.getId());

        ps.executeUpdate();
    }
    public boolean municipalDelegationExistsForAnotherUser(String region, String delegation, int userId) throws SQLException {
        String sql = "SELECT id FROM `user` WHERE roles LIKE '%ROLE_AGENT_MUNICIPAL%' AND region = ? AND delegation = ? AND id != ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, region);
        ps.setString(2, delegation);
        ps.setInt(3, userId);

        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    public void addFieldAgent(User user) throws SQLException {
        String sql = "INSERT INTO `user` (email, roles, password, name, phone, region, points, is_active, image, delegation, faceio_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getEmail());
        ps.setString(2, "[\"ROLE_AGENT_TERRAIN\"]");
        ps.setString(3, org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt()));
        ps.setString(4, user.getName());
        ps.setString(5, user.getPhone());
        ps.setString(6, user.getRegion());
        ps.setInt(7, 0);
        ps.setBoolean(8, user.isActive());
        ps.setString(9, user.getImage());
        ps.setString(10, user.getDelegation());
        ps.setString(11, null);

        ps.executeUpdate();
    }
    public void updateFieldAgent(User user) throws SQLException {
        String sql = "UPDATE `user` SET name = ?, email = ?, phone = ?, region = ?, delegation = ?, is_active = ? WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getRegion());
        ps.setString(5, user.getDelegation());
        ps.setBoolean(6, user.isActive());
        ps.setInt(7, user.getId());

        ps.executeUpdate();
    }


    public List<User> getFieldAgentsByDelegation(String delegation) throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE roles LIKE '%ROLE_AGENT_TERRAIN%' AND delegation = ? ORDER BY id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, delegation);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setDelegation(rs.getString("delegation"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));

            users.add(user);
        }

        return users;
    }


    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));
            user.setDelegation(rs.getString("delegation"));
            user.setFaceioId(rs.getString("faceio_id"));
            return user;
        }

        return null;
    }

    public User findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM user WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));
            user.setDelegation(rs.getString("delegation"));
            user.setFaceioId(rs.getString("faceio_id"));
            return user;
        }
        return null;
    }
    public void addGoogleUser(User user) throws Exception {
        String sql = "INSERT INTO user (email, roles, password, name, phone, region, points, is_active, image, delegation, faceio_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getEmail());
        ps.setString(2, user.getRoles());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getName());
        ps.setString(5, user.getPhone());
        ps.setString(6, user.getRegion());
        ps.setInt(7, user.getPoints());
        ps.setBoolean(8, user.isActive());
        ps.setString(9, user.getImage());
        ps.setString(10, user.getDelegation());
        ps.setString(11, user.getFaceioId());
        ps.executeUpdate();
    }
    public int countSubscribers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE roles LIKE '%ROLE_CITOYEN%'";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int countMunicipalAgents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE roles LIKE '%ROLE_AGENT_MUNICIPAL%'";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int countFieldAgents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE roles LIKE '%ROLE_AGENT_TERRAIN%'";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }


    public List<User> getTopCitoyensByPoints(int limit) throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE roles LIKE '%ROLE_CITOYEN%' ORDER BY points DESC, id ASC LIMIT ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setRoles(rs.getString("roles"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPhone(rs.getString("phone"));
            user.setRegion(rs.getString("region"));
            user.setPoints(rs.getInt("points"));
            user.setActive(rs.getBoolean("is_active"));
            user.setImage(rs.getString("image"));

            users.add(user);
        }

        return users;
    }
    public java.util.Map<String, Integer> countSignalementsByDelegation() throws SQLException {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();
        String sql = "SELECT delegation, COUNT(*) as total FROM signalement WHERE delegation IS NOT NULL AND delegation <> '' GROUP BY delegation ORDER BY total DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            data.put(rs.getString("delegation"), rs.getInt("total"));
        }
        return data;
    }
    public java.util.Map<String, Integer> countRecyclageByStatut() throws SQLException {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();
        String sql = "SELECT statut, COUNT(*) as total FROM point_recyclage GROUP BY statut ORDER BY total DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            data.put(rs.getString("statut"), rs.getInt("total"));
        }
        return data;
    }
    public java.util.Map<String, Integer> countAssociationsByRegion() throws SQLException {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();
        String sql = "SELECT region, COUNT(*) as total FROM association WHERE region IS NOT NULL AND region <> '' GROUP BY region ORDER BY total DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            data.put(rs.getString("region"), rs.getInt("total"));
        }
        return data;
    }
    public java.util.Map<String, Integer> countAnnoncesByCategorie() throws SQLException {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();
        String sql = "SELECT categorie, COUNT(*) as total FROM annonce WHERE categorie IS NOT NULL AND categorie <> '' GROUP BY categorie ORDER BY total DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            data.put(rs.getString("categorie"), rs.getInt("total"));
        }
        return data;
    }
    public java.util.Map<String, Integer> countEventsByStatut() throws SQLException {
        java.util.Map<String, Integer> data = new java.util.LinkedHashMap<>();
        String sql = "SELECT statut, COUNT(*) as total FROM event GROUP BY statut ORDER BY total DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            data.put(rs.getString("statut"), rs.getInt("total"));
        }
        return data;
    }
    public boolean verifyRecaptcha(String token, String secretKey) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify";
            String params = "secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8)
                    + "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }

            return response.toString().contains("\"success\": true")
                    || response.toString().contains("\"success\":true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
