package org.example.Controllers.annonces;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Entities.Annonce;
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Services.SmsNotificationService;
import org.example.Services.UserService;
import org.example.Utils.InputValidationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AddAnnonceController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taContenu;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbRegion;
    @FXML private Label lblMediaPath;
    @FXML private Button btnChooseMedia;
    @FXML private Button btnSave;
    @FXML private Button btnSaveAndNotify;
    @FXML private Button btnCancel;

    private AnnonceService annonceService = new AnnonceService();
    private UserService userService = new UserService();
    private SmsNotificationService smsNotificationService = new SmsNotificationService();
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
        btnSave.setOnAction(e -> saveAnnonce(false));
        btnSaveAndNotify.setOnAction(e -> saveAnnonce(true));
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

    private void saveAnnonce(boolean notifyCitizens) {
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

        Annonce annonce;
        try {
            annonce = new Annonce(titre, LocalDateTime.now(), region, contenu, categorie, mediaPath, loggedUser.getId());
            annonceService.addAndReturnId(annonce);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", e.getMessage());
            return;
        }

        if (!notifyCitizens) {
            showAlert(Alert.AlertType.INFORMATION, "Annonce créée", "Votre annonce a été publiée avec succès.");
            goBack();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'envoi SMS");
        confirm.setHeaderText("Informer les citoyens de " + region + " ?");
        confirm.setContentText("L'annonce est déjà publiée. Voulez-vous envoyer les SMS maintenant ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            showAlert(Alert.AlertType.INFORMATION, "Annonce créée", "Annonce publiée sans envoi SMS.");
            goBack();
            return;
        }

        setFormDisabled(true);
        btnSaveAndNotify.setText("Envoi SMS...");

        Task<SmsSendResult> task = new Task<>() {
            @Override
            protected SmsSendResult call() throws Exception {
                List<User> recipients = userService.getCitoyensByRegionWithPhone(region);
                if (recipients.isEmpty()) {
                    return SmsSendResult.empty();
                }
                SmsNotificationService.SmsDispatchReport report = smsNotificationService.notifyCitizensForAnnonce(annonce, recipients);
                return SmsSendResult.withReport(recipients.size(), report);
            }
        };

        task.setOnSucceeded(event -> {
            setFormDisabled(false);
            btnSaveAndNotify.setText("Créer + informer citoyens");

            SmsSendResult result = task.getValue();
            if (result.recipients == 0) {
                showAlert(Alert.AlertType.INFORMATION, "Annonce créée", "Annonce publiée. Aucun citoyen avec téléphone dans cette région.");
                goBack();
                return;
            }

            SmsNotificationService.SmsDispatchReport report = result.report;
            String message = "Annonce publiée avec succès.\n\n"
                    + "Cible: " + result.recipients + " citoyen(s)\n"
                    + "SMS envoyés: " + report.sent + "\n"
                    + "Déjà envoyés (ignorés): " + report.skipped + "\n"
                    + "Échecs: " + report.failed;

            if (!report.errors.isEmpty()) {
                message += "\n\nExemple d'erreur: " + report.errors.get(0);
            }

            showAlert(Alert.AlertType.INFORMATION, "Résultat publication + SMS", message);
            goBack();
        });

        task.setOnFailed(event -> {
            setFormDisabled(false);
            btnSaveAndNotify.setText("Créer + informer citoyens");
            Throwable ex = task.getException();
            String details = ex != null ? ex.getMessage() : "Erreur inconnue.";
            showAlert(Alert.AlertType.WARNING, "Annonce créée, SMS échoués",
                    "L'annonce a été publiée, mais l'envoi SMS a échoué.\nDétail: " + details);
            goBack();
        });

        Thread worker = new Thread(task, "add-annonce-sms-task");
        worker.setDaemon(true);
        worker.start();
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

    private void setFormDisabled(boolean disabled) {
        btnSave.setDisable(disabled);
        btnSaveAndNotify.setDisable(disabled);
        btnCancel.setDisable(disabled);
        btnChooseMedia.setDisable(disabled);
    }

    private static class SmsSendResult {
        private final int recipients;
        private final SmsNotificationService.SmsDispatchReport report;

        private SmsSendResult(int recipients, SmsNotificationService.SmsDispatchReport report) {
            this.recipients = recipients;
            this.report = report;
        }

        private static SmsSendResult empty() {
            return new SmsSendResult(0, null);
        }

        private static SmsSendResult withReport(int recipients, SmsNotificationService.SmsDispatchReport report) {
            return new SmsSendResult(recipients, report);
        }
    }
}
