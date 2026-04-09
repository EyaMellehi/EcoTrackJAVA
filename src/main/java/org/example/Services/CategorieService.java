package org.example.Services;

import org.example.Entities.Categorie;
import org.example.Utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private Connection cnx;

    public CategorieService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void addCategorie(Categorie c) throws SQLException {
        String sql = "INSERT INTO categorie (nom, description, coef_points) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setDouble(3, c.getCoefPoints());
        ps.executeUpdate();
    }

    public List<Categorie> getAllCategories() throws SQLException {
        List<Categorie> categories = new ArrayList<>();

        String sql = "SELECT * FROM categorie ORDER BY id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setCoefPoints(rs.getDouble("coef_points"));

            categories.add(c);
        }

        return categories;
    }

    public void updateCategorie(Categorie c) throws SQLException {
        String sql = "UPDATE categorie SET nom = ?, description = ?, coef_points = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setDouble(3, c.getCoefPoints());
        ps.setInt(4, c.getId());
        ps.executeUpdate();
    }

    public void deleteCategorie(int id) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public Categorie getCategorieById(int id) throws SQLException {
        String sql = "SELECT * FROM categorie WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Categorie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getDouble("coef_points")
            );
        }

        return null;
    }
    public List<Categorie> searchCategories(String keyword) throws SQLException {
        List<Categorie> categories = new ArrayList<>();

        String sql = "SELECT * FROM categorie WHERE nom LIKE ? OR description LIKE ? ORDER BY id ASC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setCoefPoints(rs.getDouble("coef_points"));
            categories.add(c);
        }

        return categories;
    }

    public int countPointsByCategory(int categorieId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM point_recyclage WHERE categorie_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, categorieId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
}