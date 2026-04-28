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
import java.util.UUID;

public class EditAnnonceController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taContenu;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbRegion;
    @FXML private Label lblMediaPath;
    @FXML private Button btnChooseMedia;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private final AnnonceService annonceService = new AnnonceService();
    private User loggedUser;
    private Annonce currentAnnonce;
    private File selectedMediaFile;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public void setAnnonce(Annonce a) {
        this.currentAnnonce = a;
        fillFields();
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
        btnUpdate.setOnAction(e -> updateAnnonce());
        btnCancel.setOnAction(e -> goBack());
    }

    private void fillFields() {
        if (currentAnnonce == null) return;

        tfTitre.setText(currentAnnonce.getTitre());
        taContenu.setText(currentAnnonce.getContenu());
        cbCategorie.setValue(currentAnnonce.getCategorie());
        cbRegion.setValue(currentAnnonce.getRegion());

        if (currentAnnonce.getMediaPath() != null && !currentAnnonce.getMediaPath().isEmpty()) {
            lblMediaPath.setText(currentAnnonce.getMediaPath());
        } else {
            lblMediaPath.setText("Aucun fichier");
        }
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
    private void updateAnnonce() {
        if (currentAnnonce == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune annonce sélectionnée");
            return;
        }

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
            showAlert(Alert.AlertType.WARNING, "Connexion requise", "Vous devez être connecté pour modifier une annonce.");
            return;
        }

        String mediaPath = currentAnnonce.getMediaPath();
        if (selectedMediaFile != null) {
            try {
                mediaPath = copyFileToUploads(selectedMediaFile);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de télécharger le fichier: " + e.getMessage());
                return;
            }
        }

        try {
            currentAnnonce.setTitre(titre);
            currentAnnonce.setContenu(contenu);
            currentAnnonce.setCategorie(categorie);
            currentAnnonce.setRegion(region);
            currentAnnonce.setMediaPath(mediaPath);

            annonceService.update(currentAnnonce);
            showAlert(Alert.AlertType.INFORMATION, "Annonce valide", "Votre saisie est valide et l'annonce a été modifiée avec succès.");
            goBack();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", e.getMessage());
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

