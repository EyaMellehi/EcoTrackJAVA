package org.example.Controllers.signalement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.SignalementService;

import java.io.IOException;
import java.sql.SQLException;

public class EditSignalementController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextField tfDelegation;


    private final SignalementService signalementService = new SignalementService();
    private User loggedUser;
    private Signalement signalement;

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;

    }


    public void setSignalement(Signalement signalement) {
        this.signalement = signalement;
        fillFields();
    }

    @FXML
    public void initialize() {
        cbType.getItems().addAll(
                "Pollution",
                "Illegal Dumping",
                "Waste",
                "Water Leak",
                "Noise",
                "Other"
        );

        cbStatut.getItems().addAll(
                "EN_ATTENTE",
                "EN_COURS",
                "TRAITE"
        );

        addNumericValidation(tfLatitude);
        addNumericValidation(tfLongitude);
    }

    private void fillFields() {
        if (signalement == null) {
            return;
        }

        tfTitre.setText(signalement.getTitre());
        taDescription.setText(signalement.getDescription());
        cbType.setValue(signalement.getType());
        cbStatut.setValue(signalement.getStatut());
        tfAddresse.setText(signalement.getAddresse());
        tfLatitude.setText(String.valueOf(signalement.getLatitude()));
        tfLongitude.setText(String.valueOf(signalement.getLongitude()));
        tfDelegation.setText(signalement.getDelegation());
    }

    @FXML
    public void updateSignalement() {
        if (signalement == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No report selected.");
            return;
        }

        String titre = tfTitre.getText().trim();
        String description = taDescription.getText().trim();
        String type = cbType.getValue();
        String statut = cbStatut.getValue();
        String addresse = tfAddresse.getText().trim();
        String delegation = tfDelegation.getText().trim();
        String latitudeText = tfLatitude.getText().trim();
        String longitudeText = tfLongitude.getText().trim();

        if (titre.isEmpty() || type == null || statut == null || addresse.isEmpty()
                || latitudeText.isEmpty() || longitudeText.isEmpty()) {
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
                showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a latitude within Tunisia.");
                return;
            }

            if (longitude < 7 || longitude > 12) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a longitude within Tunisia.");
                return;
            }

            signalement.setTitre(titre);
            signalement.setDescription(description);
            signalement.setType(type);
            signalement.setStatut(statut);
            signalement.setAddresse(addresse);
            signalement.setLatitude(latitude);
            signalement.setLongitude(longitude);
            signalement.setDelegation(delegation.isEmpty() ? null : delegation);

            signalementService.update(signalement);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Report updated successfully.");
            goToList();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Latitude and longitude must be valid numbers.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
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