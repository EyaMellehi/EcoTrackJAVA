package org.example.Services;


import org.example.Entities.Association;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssociationService {

    private Connection cnx;

    public AssociationService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // ✅ CREATE
    public void add(Association a) {
        String sql = "INSERT INTO association (nom, type, description, region, tel, email, logo, is_active, date_creation, addresse) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, a.getNom());
            ps.setString(2, a.getType());
            ps.setString(3, a.getDescription());
            ps.setString(4, a.getRegion());
            ps.setInt(5, a.getTel());
            ps.setString(6, a.getEmail());
            ps.setString(7, a.getLogo());
            ps.setBoolean(8, a.isActive());
            ps.setTimestamp(9, Timestamp.valueOf(a.getDateCreation()));
            ps.setString(10, a.getAddresse());

            ps.executeUpdate();
            System.out.println("✅ Association ajoutée");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ALL
    public List<Association> getAll() {
        List<Association> list = new ArrayList<>();
        String sql = "SELECT * FROM association";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Association a = new Association();

                a.setId(rs.getInt("id"));
                a.setNom(rs.getString("nom"));
                a.setType(rs.getString("type"));
                a.setDescription(rs.getString("description"));
                a.setRegion(rs.getString("region"));
                a.setTel(rs.getInt("tel"));
                a.setEmail(rs.getString("email"));
                a.setLogo(rs.getString("logo"));
                a.setActive(rs.getBoolean("is_active"));
                a.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                a.setAddresse(rs.getString("addresse"));

                list.add(a);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // ✅ READ BY ID
    public Association getById(int id) {
        String sql = "SELECT * FROM association WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Association a = new Association();

                a.setId(rs.getInt("id"));
                a.setNom(rs.getString("nom"));
                a.setType(rs.getString("type"));
                a.setDescription(rs.getString("description"));
                a.setRegion(rs.getString("region"));
                a.setTel(rs.getInt("tel"));
                a.setEmail(rs.getString("email"));
                a.setLogo(rs.getString("logo"));
                a.setActive(rs.getBoolean("is_active"));
                a.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                a.setAddresse(rs.getString("addresse"));

                return a;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    // ✅ UPDATE
    public void update(Association a) {
        String sql = "UPDATE association SET nom=?, type=?, description=?, region=?, tel=?, email=?, logo=?, is_active=?, addresse=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, a.getNom());
            ps.setString(2, a.getType());
            ps.setString(3, a.getDescription());
            ps.setString(4, a.getRegion());
            ps.setInt(5, a.getTel());
            ps.setString(6, a.getEmail());
            ps.setString(7, a.getLogo());
            ps.setBoolean(8, a.isActive());
            ps.setString(9, a.getAddresse());
            ps.setInt(10, a.getId());

            ps.executeUpdate();
            System.out.println("✅ Association modifiée");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    public void delete(int id) {
        String sql = "DELETE FROM association WHERE id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("✅ Association supprimée");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}