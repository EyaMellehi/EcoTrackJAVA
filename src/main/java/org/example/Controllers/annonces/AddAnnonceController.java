package org.example.Controllers.annonces;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Entities.Annonce;
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Utils.InputValidationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class AddAnnonceController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taContenu;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbRegion;
    @FXML private Label lblMediaPath;
    @FXML private Button btnChooseMedia;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private AnnonceService annonceService = new AnnonceService();
    private User loggedUser;
    private File selectedMediaFile;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    @FXML
    public void initialize() {
        cbCategorie.getItems().addAll(
            "Agriculture",
            "Associations et collectifs citoyens",
            "Collectes de déchets",
            "Environnement"
        );

        cbRegion.getItems().addAll(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Sfax", "Sousse", 
            "Kairouan", "Kasserine", "Sidi Bouzid", "Médenine", "Tataouine",
            "Gabès", "Gafsa", "Tozeur", "Kébili", "Jendouba", "Kef", "Siliana",
            "Nabeul", "Bizerte", "Zaghouan", "Monastir", "Mahdia", "Oued Slemane"
        );

        btnChooseMedia.setOnAction(e -> chooseMedia());
        btnSave.setOnAction(e -> saveAnnonce());
        btnCancel.setOnAction(e -> goBack());
        lblMediaPath.setText("Aucun fichier sélectionné");
    }

    @FXML
    private void chooseMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selected = fileChooser.showOpenDialog(btnChooseMedia.getScene().getWindow());
        if (selected != null) {
            selectedMediaFile = selected;
            lblMediaPath.setText(selected.getName());
        }
    }

    @FXML
    private void saveAnnonce() {
        String titre = InputValidationUtil.normalize(tfTitre.getText());
        String contenu = InputValidationUtil.normalize(taContenu.getText());
        String categorie = cbCategorie.getValue();
        String region = cbRegion.getValue();

        String error = InputValidationUtil.validateAnnonceTitle(titre);
        if (error != null) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", error);
            return;
        }

        error = InputValidationUtil.validateAnnonceContent(contenu);
        if (error != null) {
            showAlert(Alert.AlertType.WARNING, "Description invalide", error);
            return;
        }

        error = InputValidationUtil.validateRequiredSelection(categorie, "La catégorie");
        if (error != null) {
            showAlert(Alert.AlertType.WARNING, "Catégorie obligatoire", error);
            return;
        }

        error = InputValidationUtil.validateRequiredSelection(region, "La région");
        if (error != null) {
            showAlert(Alert.AlertType.WARNING, "Région obligatoire", error);
            return;
        }

        if (loggedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Connexion requise", "Vous devez être connecté pour créer une annonce.");
            return;
        }

        String mediaPath = null;
        if (selectedMediaFile != null) {
            try {
                mediaPath = copyFileToUploads(selectedMediaFile);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de télécharger le fichier: " + e.getMessage());
                return;
            }
        }

        try {
            Annonce annonce = new Annonce(titre, LocalDateTime.now(), region, contenu, categorie, mediaPath, loggedUser.getId());
            annonceService.add(annonce);
            showAlert(Alert.AlertType.INFORMATION, "Annonce valide", "Votre saisie est valide et l'annonce a été créée avec succès.");
            goBack();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", e.getMessage());
            e.printStackTrace();
        }
    }

    private String copyFileToUploads(File sourceFile) throws IOException {
        String uploadsDir = "uploads/annonces";
        Files.createDirectories(Path.of(uploadsDir));

        String filename = UUID.randomUUID() + "_" + sourceFile.getName();
        Path targetPath = Path.of(uploadsDir, filename);

        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

