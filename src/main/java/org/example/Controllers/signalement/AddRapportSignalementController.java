package org.example.Controllers.signalement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Entities.Media;
import org.example.Entities.RapportSignalement;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.MediaService;
import org.example.Services.RapportSignalementService;
import org.example.Services.SignalementService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddRapportSignalementController {

    @FXML private TextField tfSignalementId;
    @FXML private TextArea taCommentaire;
    @FXML private Label lblAddress;
    @FXML private TextArea taSignalementDescription;
    @FXML private Label lblSelectedPhotos;

    private final RapportSignalementService rapportService = new RapportSignalementService();
    private final SignalementService signalementService = new SignalementService();
    private final MediaService mediaService = new MediaService();


    private User loggedUser;
    private Signalement signalement;
    private final List<File> selectedPhotos = new ArrayList<>();

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;

    }

    public void setSignalement(Signalement signalement) {
        this.signalement = signalement;
        if (signalement != null) {
            tfSignalementId.setText(String.valueOf(signalement.getId()));
            lblAddress.setText(signalement.getAddresse() != null ? signalement.getAddresse() : "");
            taSignalementDescription.setText(signalement.getDescription() != null ? signalement.getDescription() : "");
        }
    }




    @FXML
    public void choosePhotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Photos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(tfSignalementId.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            selectedPhotos.clear();
            selectedPhotos.addAll(files);
            lblSelectedPhotos.setText(selectedPhotos.size() + " photo(s) selected");
        } else {
            lblSelectedPhotos.setText("No photos selected");
        }
    }

    @FXML
    public void addRapport() {
        if (loggedUser == null || signalement == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing user or signalement.");
            return;
        }

        String commentaire = taCommentaire.getText().trim();

        if (commentaire.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a commentaire.");
            return;
        }

        if (commentaire.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Commentaire must contain at least 5 characters.");
            return;
        }

        if (commentaire.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Commentaire must not exceed 255 characters.");
            return;
        }

        try {
            RapportSignalement existing = rapportService.getBySignalementId(signalement.getId());
            if (existing != null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "This signalement already has a report.");
                return;
            }

            int rapportId = addRapportAndReturnId(commentaire);

            saveSelectedPhotos(rapportId);

            signalementService.updateStatut(signalement.getId(), "TRAITE");

            showAlert(Alert.AlertType.INFORMATION, "Success", "Report added successfully.");
            redirectByRole();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private int addRapportAndReturnId(String commentaire) throws SQLException {
        Connection cnx = org.example.Utils.MyConnection.getInstance().getConnection();

        String sql = "INSERT INTO rapport_signalement (date_intervention, commentaire, signalement_id, agent_terrain_id) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(2, commentaire);
        ps.setInt(3, signalement.getId());
        ps.setInt(4, loggedUser.getId());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }

        throw new SQLException("Failed to retrieve generated rapport ID.");
    }

    private void saveSelectedPhotos(int rapportId) throws IOException, SQLException {
        if (selectedPhotos.isEmpty()) {
            return;
        }

        Path uploadDir = Path.of("uploads", "rapports");
        Files.createDirectories(uploadDir);

        for (File file : selectedPhotos) {
            String originalName = file.getName();
            String extension = "";

            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }

            String uniqueFileName = UUID.randomUUID() + extension;
            Path destination = uploadDir.resolve(uniqueFileName);

            Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            Media media = new Media();
            media.setFilename(originalName);
            media.setType("image");
            media.setUrl(destination.toString().replace("\\", "/"));
            media.setCreatedAt(LocalDateTime.now());
            media.setUserId(loggedUser.getId());
            media.setSignalementId(null);
            media.setRapportSignalementId(rapportId);
            media.setAnnonceId(null);
            media.setEventId(null);

            mediaService.add(media);
        }
    }

    @FXML
    public void goBack() {
        redirectByRole();
    }

    private void redirectByRole() {
        try {
            FXMLLoader loader;

            if (loggedUser != null && loggedUser.getRoles() != null) {
                if (loggedUser.getRoles().contains("ROLE_AGENT_TERRAIN")) {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_assigned_signalements.fxml"));
                } else if (loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_municipal_signalements.fxml"));
                } else {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
                }
            } else {
                loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
            }

            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ListAssignedSignalementController) {
                ((ListAssignedSignalementController) controller).setLoggedUser(loggedUser);
            } else if (controller instanceof ListMunicipalSignalementController) {
                ((ListMunicipalSignalementController) controller).setLoggedUser(loggedUser);
            } else if (controller instanceof ListSignalementController) {
                ((ListSignalementController) controller).setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) tfSignalementId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}