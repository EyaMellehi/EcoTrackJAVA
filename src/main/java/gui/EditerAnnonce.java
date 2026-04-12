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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class EditerAnnonce {
    
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
    
    private Annonce annonce;
    private AfficherDetailsAnnonce parentController;
    private AfficherDetailsAnnonce detailsController;
    private AnnonceService annonceService;
    private String selectedMediaPath = null;
    
    @FXML
    public void initialize() {
        annonceService = new AnnonceService();
        
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
    }
    
    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
        this.selectedMediaPath = annonce.getMediaPath();
        remplirFormulaire();
    }
    
    public void setParentController(AfficherDetailsAnnonce parent) {
        this.parentController = parent;
    }
    
    public void setDetailsController(AfficherDetailsAnnonce details) {
        this.detailsController = details;
    }
    
    private void remplirFormulaire() {
        titreField.setText(annonce.getTitre());
        regionComboBox.setValue(annonce.getRegion());
        contenuField.setText(annonce.getContenu());
        categorieComboBox.setValue(annonce.getCategorie());
        if (mediaLabel != null) {
            if (annonce.getMediaPath() != null && !annonce.getMediaPath().isEmpty()) {
                mediaLabel.setText("✓ Image présente");
                mediaLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else {
                mediaLabel.setText("Aucune image");
                mediaLabel.setStyle("-fx-text-fill: #999;");
            }
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
    public void enregistrerModifications(ActionEvent event) {
        try {
            String titre = titreField.getText();
            String region = regionComboBox.getValue();
            String contenu = contenuField.getText();
            String categorie = categorieComboBox.getValue();
            
            // Validation
            if (titre == null || titre.isEmpty() || 
                region == null || 
                contenu == null || contenu.isEmpty() || 
                categorie == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs obligatoires");
                alert.setContentText("Veuillez remplir tous les champs obligatoires");
                alert.show();
                return;
            }
            
            annonce.setTitre(titre);
            annonce.setRegion(region);
            annonce.setContenu(contenu);
            annonce.setCategorie(categorie);
            
            // Si un nouveau média a été sélectionné, mettre à jour
            if (selectedMediaPath != null && !selectedMediaPath.isEmpty()) {
                annonce.setMediaPath(selectedMediaPath);
            }
            
            annonceService.update(annonce);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Annonce modifiée avec succès!");
            alert.show();
            
            if (detailsController != null) {
                detailsController.rafraichir();
            }
            retournerAuxDetails(null);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Base de Données");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.show();
        }
    }
    
    @FXML
    public void annuler(ActionEvent event) {
        retournerAuxDetails(null);
    }
    
    private void retournerAuxDetails(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDetailsAnnonce.fxml"));
            Parent root = loader.load();
            AfficherDetailsAnnonce controller = loader.getController();
            controller.setAnnonce(annonce);
            
            titreField.getScene().setRoot(root);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du retour aux détails");
            alert.show();
        }
    }
}


