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
}
