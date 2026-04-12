package gui;

import entities.Annonce;
import entities.Commentaire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import services.AnnonceService;
import services.CommentaireService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.ValidationUtil;
import utils.ValidationUtil.ValidationResult;

public class AfficherDetailsAnnonce {
    
    @FXML private Label titreLabel;
    @FXML private Label dateLabel;
    @FXML private Label regionLabel;
    @FXML private Label categorieLabel;
    @FXML private TextArea contenuArea;
    @FXML private ListView<HBox> commentairesListView;
    @FXML private TextArea texteCommentaireField;
    @FXML private ImageView mediaImageView;
    @FXML private Label mediaInfoLabel;
    @FXML private Label commentCountLabel;
    
    private Annonce annonce;
    private CommentaireService commentaireService;
    private AnnonceService annonceService;
    private Map<Integer, TextArea> commentairesEnEdition = new HashMap<>();
    
    @FXML
    public void initialize() {
        System.out.println("✅ AfficherDetailsAnnonce initialisé");
        commentaireService = new CommentaireService();
        annonceService = new AnnonceService();
    }
    
    public void setAnnonce(Annonce annonce) {
        System.out.println("📄 Annonce reçue: " + annonce.getTitre());
        this.annonce = annonce;
        afficherDetails();
        afficherMedia();
        chargerCommentaires();
    }
    
    private void afficherDetails() {
        titreLabel.setText(annonce.getTitre());
        dateLabel.setText(annonce.getDatePub() != null ? annonce.getDatePub().toString().substring(0, 10) : "N/A");
        regionLabel.setText(annonce.getRegion());
        categorieLabel.setText(annonce.getCategorie() != null ? annonce.getCategorie() : "N/A");
        contenuArea.setText(annonce.getContenu());
        contenuArea.setWrapText(true);
        contenuArea.setEditable(false);
    }
    
    private void afficherMedia() {
        if (annonce.getMediaPath() != null && !annonce.getMediaPath().isEmpty() && !annonce.getMediaPath().equals("null")) {
            try {
                File file = new File(annonce.getMediaPath());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    mediaImageView.setImage(image);
                    mediaInfoLabel.setText("✓ " + file.getName());
                    mediaInfoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    System.out.println("✅ Image affichée: " + file.getName());
                } else {
                    mediaInfoLabel.setText("Fichier non trouvé");
                    mediaInfoLabel.setStyle("-fx-text-fill: #f44336;");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement image: " + e.getMessage());
                mediaInfoLabel.setText("Erreur chargement");
                mediaInfoLabel.setStyle("-fx-text-fill: #f44336;");
            }
        } else {
            mediaInfoLabel.setText("Aucune image");
            mediaInfoLabel.setStyle("-fx-text-fill: #999;");
        }
    }
    
    private void chargerCommentaires() {
        try {
            List<Commentaire> commentaires = commentaireService.readByAnnonceId(annonce.getId());
            ObservableList<HBox> items = FXCollections.observableArrayList();
            
            if (commentaires != null && !commentaires.isEmpty()) {
                for (Commentaire c : commentaires) {
                    HBox commentaireBox = creerCommentaireBox(c);
                    items.add(commentaireBox);
                }
                commentCountLabel.setText("(" + commentaires.size() + ")");
            } else {
                Label label = new Label("Aucun commentaire");
                HBox box = new HBox(label);
                items.add(box);
                commentCountLabel.setText("(0)");
            }
            commentairesListView.setItems(items);
            System.out.println("✅ Commentaires chargés");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
        }
    }
    
    private HBox creerCommentaireBox(Commentaire c) {
        VBox contenuBox = new VBox(8);
        contenuBox.setStyle("-fx-padding: 0;");
        
        // Header avec date
        Label dateLabel = new Label(c.getDateCreation() + "");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1976D2;");
        
        // TextArea éditable
        TextArea texteArea = new TextArea(c.getTexte());
        texteArea.setWrapText(true);
        texteArea.setPrefHeight(70);
        texteArea.setStyle("-fx-control-inner-background: white; -fx-font-size: 12px; -fx-padding: 10; -fx-border-radius: 3;");
        texteArea.setEditable(false);
        
        // Boutons
        HBox boutonsBox = new HBox(10);
        boutonsBox.setStyle("-fx-padding: 0;");
        
        Button btnEditer = new Button("✏️ Éditer");
        btnEditer.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-border-radius: 3; -fx-cursor: hand;");
        btnEditer.setOnAction(e -> enableEditMode(c, texteArea, btnEditer, boutonsBox));
        
        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-border-radius: 3; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerCommentaire(c));
        
        boutonsBox.getChildren().addAll(btnEditer, btnSupprimer);
        contenuBox.getChildren().addAll(dateLabel, texteArea, boutonsBox);
        
        HBox box = new HBox(15, contenuBox);
        box.setStyle("-fx-border-color: #e8e8e8; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: #fafafa;");
        box.setPadding(new Insets(10));
        HBox.setHgrow(contenuBox, Priority.ALWAYS);
        
        return box;
    }
    
    private void enableEditMode(Commentaire c, TextArea texteArea, Button btnEditer, HBox boutonsBox) {
        texteArea.setEditable(true);
        texteArea.setStyle("-fx-control-inner-background: #fffacd; -fx-font-size: 12px; -fx-padding: 10; -fx-border-color: #FF9800; -fx-border-width: 2; -fx-border-radius: 3;");
        texteArea.requestFocus();
        
        // Vider et ajouter boutons d'édition
        boutonsBox.getChildren().clear();
        
        Button btnSauvegarder = new Button("💾 Sauvegarder");
        btnSauvegarder.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-border-radius: 3; -fx-cursor: hand;");
        btnSauvegarder.setOnAction(e -> sauvegarderCommentaire(c, texteArea));
        
        Button btnAnnuler = new Button("❌ Annuler");
        btnAnnuler.setStyle("-fx-background-color: #999; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-border-radius: 3; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> disableEditMode(c, texteArea, btnEditer, boutonsBox));
        
        boutonsBox.getChildren().addAll(btnSauvegarder, btnAnnuler);
    }
    
    private void disableEditMode(Commentaire c, TextArea texteArea, Button btnEditer, HBox boutonsBox) {
        texteArea.setEditable(false);
        texteArea.setText(c.getTexte());
        texteArea.setStyle("-fx-control-inner-background: white; -fx-font-size: 12px; -fx-padding: 10; -fx-border-radius: 3;");
        
        boutonsBox.getChildren().clear();
        
        Button btnEditer2 = new Button("✏️ Éditer");
        btnEditer2.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-border-radius: 3; -fx-cursor: hand;");
        btnEditer2.setOnAction(e -> enableEditMode(c, texteArea, btnEditer2, boutonsBox));
        
        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-border-radius: 3; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerCommentaire(c));
        
        boutonsBox.getChildren().addAll(btnEditer2, btnSupprimer);
    }
    
    private void sauvegarderCommentaire(Commentaire c, TextArea texteArea) {
        String nouveau = texteArea.getText().trim();
        
        // VALIDATION COMMENTAIRE
        ValidationResult validation = ValidationUtil.validerCommentaire(nouveau);
        if (!validation.isValide()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur Validation");
            alert.setContentText(validation.getMessage());
            alert.show();
            return;
        }
        
        try {
            c.setTexte(nouveau);
            commentaireService.update(c);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Commentaire modifié!");
            alert.show();
            chargerCommentaires();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
        }
    }
    
    private void supprimerCommentaire(Commentaire c) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Supprimer ce commentaire?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                commentaireService.delete(c);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Commentaire supprimé!");
                alert.show();
                chargerCommentaires();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur: " + e.getMessage());
                alert.show();
            }
        }
    }
    
    @FXML
    public void ajouterCommentaire(ActionEvent event) {
        try {
            String texte = texteCommentaireField.getText().trim();
            
            // VALIDATION COMMENTAIRE
            ValidationResult validation = ValidationUtil.validerCommentaire(texte);
            if (!validation.isValide()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Erreur Validation");
                alert.setContentText(validation.getMessage());
                alert.show();
                return;
            }
            
            Commentaire commentaire = new Commentaire(annonce.getId(), texte);
            commentaireService.create(commentaire);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Commentaire ajouté!");
            alert.show();
            
            texteCommentaireField.clear();
            chargerCommentaires();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
        }
    }
    
    @FXML
    public void editerAnnonce(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditerAnnonce.fxml"));
            Parent root = loader.load();
            EditerAnnonce controller = loader.getController();
            controller.setAnnonce(annonce);
            controller.setDetailsController(this);
            
            texteCommentaireField.getScene().setRoot(root);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur chargement formulaire édition");
            alert.show();
        }
    }
    
    @FXML
    public void supprimerAnnonce(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Supprimer cette annonce définitivement?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                annonceService.delete(annonce);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Annonce supprimée!");
                alert.show();
                retournerAuListe(null);
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur: " + e.getMessage());
                alert.show();
            }
        }
    }
    
    public void rafraichir() {
        chargerCommentaires();
    }
    
    @FXML
    public void retournerAuListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherAnnonces.fxml"));
            texteCommentaireField.getScene().setRoot(root);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur retour");
            alert.show();
        }
    }
}

