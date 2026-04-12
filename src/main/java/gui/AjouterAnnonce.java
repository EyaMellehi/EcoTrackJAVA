package gui;

import entities.Annonce;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.AnnonceService;
import utils.ValidationUtil;
import utils.ValidationUtil.ValidationResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AjouterAnnonce {
    
    @FXML
    private TextField titreField;
    
    @FXML
    private ComboBox<String> regionComboBox;
    
    @FXML
    private TextArea contenuField;
    
    @FXML
    private ComboBox<String> categorieComboBox;
    
    @FXML
    private Label mediaLabel;
    
    private String selectedMediaPath = null;
    
    @FXML
    public void initialize() {
        // Populate régions
        regionComboBox.getItems().addAll(
                "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan",
                "Bizerte", "Béja", "Jendouba", "Le Kef", "Siliana", "Sousse",
                "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine",
                "Sidi Bouzid", "Gabès", "Medenine", "Tataouine", "Gafsa",
                "Tozeur", "Kébili"
        );
        
        // Populate catégories
        categorieComboBox.getItems().addAll(
                "Agriculture",
                "Collectes de déchets",
                "Associations et collectifs citoyens",
                "Environnement"
        );
        
        if (mediaLabel != null) {
            mediaLabel.setText("Aucune image sélectionnée");
        }
    }
    
    @FXML
    public void selectMedia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            selectedMediaPath = file.getAbsolutePath();
            if (mediaLabel != null) {
                mediaLabel.setText("✓ " + file.getName());
                mediaLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }
        }
    }
    
    @FXML
    public void ajouterAnnonce(ActionEvent event) {
        try {
            String titre = titreField.getText();
            String region = regionComboBox.getValue();
            String contenu = contenuField.getText();
            String categorie = categorieComboBox.getValue();
            
            // VALIDATION COMPLÈTE
            ValidationResult valTitre = ValidationUtil.validerTitre(titre);
            if (!valTitre.isValide()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur Validation");
                alert.setContentText(valTitre.getMessage());
                alert.show();
                return;
            }
            
            ValidationResult valContenu = ValidationUtil.validerContenu(contenu);
            if (!valContenu.isValide()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur Validation");
                alert.setContentText(valContenu.getMessage());
                alert.show();
                return;
            }
            
            ValidationResult valRegion = ValidationUtil.validerRegion(region);
            if (!valRegion.isValide()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur Validation");
                alert.setContentText(valRegion.getMessage());
                alert.show();
                return;
            }
            
            ValidationResult valCategorie = ValidationUtil.validerCategorie(categorie);
            if (!valCategorie.isValide()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur Validation");
                alert.setContentText(valCategorie.getMessage());
                alert.show();
                return;
            }
            
            // Créer annonce AVEC media
            Annonce annonce = new Annonce(titre, region, contenu, categorie, selectedMediaPath);
            AnnonceService service = new AnnonceService();
            service.create(annonce);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Annonce ajoutée avec succès !");
            alert.show();
            
            // Clear all fields
            titreField.clear();
            regionComboBox.setValue(null);
            contenuField.clear();
            categorieComboBox.setValue(null);
            selectedMediaPath = null;
            if (mediaLabel != null) {
                mediaLabel.setText("Aucune image sélectionnée");
                mediaLabel.setStyle("-fx-text-fill: #666;");
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Base de Données");
            alert.setContentText("Erreur base de données: " + e.getMessage());
            alert.show();
            System.err.println("Erreur SQL: " + e.getMessage());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.show();
        }
    }
    
    @FXML
    public void naviguerVersAfficher(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherAnnonces.fxml"));
            titreField.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
