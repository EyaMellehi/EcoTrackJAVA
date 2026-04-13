package org.example.Controllers.signalement;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.Entities.Media;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.MediaService;
import org.example.Services.SignalementService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddSignalementController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextField tfDelegation;
    @FXML private WebView mapView;
    @FXML private Label lblSelectedPhotos;


    private final SignalementService signalementService = new SignalementService();
    private final MediaService mediaService = new MediaService();

    private User loggedUser;
    private final List<File> selectedPhotos = new ArrayList<>();

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;

    }

    @FXML
    public void initialize() {
        cbType.getItems().addAll(
                "Waste",
                "Air Pollution",
                "Water Pollution",
                "Fire",
                "Other"
        );

        addNumericValidation(tfLatitude);
        addNumericValidation(tfLongitude);
        loadMap();
    }




    private void loadMap() {
        if (mapView == null) return;

        WebEngine webEngine = mapView.getEngine();
        URL mapUrl = getClass().getResource("/maps/signalement_map.html");

        if (mapUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Map HTML file not found.");
            return;
        }

        webEngine.load(mapUrl.toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
            }
        });
    }

    public class JavaConnector {
        public void setLocation(String lat, String lng, String address) {
            Platform.runLater(() -> {
                tfLatitude.setText(lat);
                tfLongitude.setText(lng);
                tfAddresse.setText(address);
            });
        }
    }

    @FXML
    public void choosePhotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Photos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(tfTitre.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            selectedPhotos.clear();
            selectedPhotos.addAll(files);
            lblSelectedPhotos.setText(selectedPhotos.size() + " photo(s) selected");
        } else {
            lblSelectedPhotos.setText("No photos selected");
        }
    }

    @FXML
    public void locateTypedAddress() {
        String address = tfAddresse.getText().trim();

        if (address.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter an address first.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Info",
                "For now, please click directly on the map to select the location.");
    }

    @FXML
    public void addSignalement() {
        if (loggedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No logged user found.");
            return;
        }

        String titre = tfTitre.getText().trim();
        String description = taDescription.getText().trim();
        String type = cbType.getValue();
        String addresse = tfAddresse.getText().trim();
        String delegation = tfDelegation.getText().trim();
        String latitudeText = tfLatitude.getText().trim();
        String longitudeText = tfLongitude.getText().trim();

        if (titre.isEmpty() || type == null || addresse.isEmpty() || latitudeText.isEmpty() || longitudeText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please fill all required fields.");
            return;
        }

        if (titre.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Title must contain at least 3 characters.");
            return;
        }

        if (!description.isEmpty() && description.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Description must not exceed 255 characters.");
            return;
        }

        if (addresse.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Address must contain at least 5 characters.");
            return;
        }

        try {
            double latitude = Double.parseDouble(latitudeText);
            double longitude = Double.parseDouble(longitudeText);

            if (latitude < 30 || latitude > 38) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Invalid location\", \n" +
                        "        \"Please enter a latitude within Tunisia.");
                return;
            }

            if (longitude < 7 || longitude > 12) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Invalid location\", \n" +
                        "        \"Please enter a longitude within Tunisia.");
                return;
            }

            Signalement s = new Signalement();
            s.setTitre(titre);
            s.setDescription(description);
            s.setType(type);
            s.setStatut("EN_ATTENTE");
            s.setAddresse(addresse);
            s.setLatitude(latitude);
            s.setLongitude(longitude);
            s.setDateCreation(LocalDateTime.now());
            s.setCitoyenId(loggedUser.getId());
            s.setAgentAssigneId(null);
            s.setDelegation(delegation.isEmpty() ? null : delegation);
            s.setAssignedAt(null);

            int signalementId = signalementService.addAndReturnId(s);

            saveSelectedPhotos(signalementId);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Report added successfully.");
            goToList();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Latitude and longitude must be valid numbers.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSelectedPhotos(int signalementId) throws IOException, SQLException {
        if (selectedPhotos.isEmpty()) {
            return;
        }

        Path uploadDir = Path.of("uploads", "signalements");
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
            media.setSignalementId(signalementId);
            media.setRapportSignalementId(null);
            media.setAnnonceId(null);
            media.setEventId(null);

            mediaService.add(media);
        }
    }

    @FXML
    public void goBack() {
        goToList();
    }

    private void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
            Parent root = loader.load();

            ListSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Reports");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}