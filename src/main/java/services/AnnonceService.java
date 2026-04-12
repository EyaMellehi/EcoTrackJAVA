package services;

import entities.Annonce;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnonceService implements IService<Annonce> {
    
    private Connection connection;
    
    public AnnonceService() {
        this.connection = MyConnection.getInstance().getConnection();
    }
    
    @Override
    public void create(Annonce annonce) throws SQLException {
        String sql = "INSERT INTO annonce (titre, region, contenu, categorie, media_path) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, annonce.getTitre());
        ps.setString(2, annonce.getRegion());
        ps.setString(3, annonce.getContenu());
        ps.setString(4, annonce.getCategorie());
        ps.setString(5, annonce.getMediaPath());
        ps.executeUpdate();
        ps.close();
    }
    
    @Override
    public void update(Annonce annonce) throws SQLException {
        String sql = "UPDATE annonce SET titre = ?, region = ?, contenu = ?, categorie = ?, media_path = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, annonce.getTitre());
        ps.setString(2, annonce.getRegion());
        ps.setString(3, annonce.getContenu());
        ps.setString(4, annonce.getCategorie());
        ps.setString(5, annonce.getMediaPath());
        ps.setInt(6, annonce.getId());
        ps.executeUpdate();
        ps.close();
    }
    
    @Override
    public void delete(Annonce annonce) throws SQLException {
        String sql = "DELETE FROM annonce WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, annonce.getId());
        ps.executeUpdate();
        ps.close();
    }
    
    @Override
    public List<Annonce> readAll() throws SQLException {
        List<Annonce> annonces = new ArrayList<>();
        String sql = "SELECT id, titre, date_pub, region, contenu, categorie, media_path FROM annonce";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Annonce annonce = new Annonce(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getTimestamp("date_pub"),
                    rs.getString("region"),
                    rs.getString("contenu"),
                    rs.getString("categorie"),
                    rs.getString("media_path")
            );
            annonces.add(annonce);
        }
        rs.close();
        stmt.close();
        return annonces;
    }
}
