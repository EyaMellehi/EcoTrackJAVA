package services;

import entities.Commentaire;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {
    
    private Connection connection;
    
    public CommentaireService() {
        this.connection = MyConnection.getInstance().getConnection();
    }
    
    @Override
    public void create(Commentaire commentaire) throws SQLException {
        String sql = "INSERT INTO commentaire (annonce_id, texte, date_creation) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, commentaire.getAnnonceId());
        ps.setString(2, commentaire.getTexte());
        ps.setTimestamp(3, commentaire.getDateCreation());
        ps.executeUpdate();
        ps.close();
    }
    
    @Override
    public void update(Commentaire commentaire) throws SQLException {
        String sql = "UPDATE commentaire SET texte = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, commentaire.getTexte());
        ps.setInt(2, commentaire.getId());
        ps.executeUpdate();
        ps.close();
    }
    
    @Override
    public void delete(Commentaire commentaire) throws SQLException {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, commentaire.getId());
        ps.executeUpdate();
        ps.close();
    }
    
    @Override
    public List<Commentaire> readAll() throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Commentaire commentaire = new Commentaire(
                    rs.getInt("id"),
                    rs.getInt("annonce_id"),
                    rs.getString("texte"),
                    rs.getTimestamp("date_creation")
            );
            commentaires.add(commentaire);
        }
        rs.close();
        stmt.close();
        return commentaires;
    }
    
    public List<Commentaire> readByAnnonceId(int annonceId) throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE annonce_id = ? ORDER BY date_creation DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, annonceId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Commentaire commentaire = new Commentaire(
                    rs.getInt("id"),
                    rs.getInt("annonce_id"),
                    rs.getString("texte"),
                    rs.getTimestamp("date_creation")
            );
            commentaires.add(commentaire);
        }
        rs.close();
        ps.close();
        return commentaires;
    }
}

