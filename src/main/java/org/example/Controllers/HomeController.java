package org.example.Controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class HomeController {

    @FXML
    private Button btnLogin;

    @FXML
    void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/login.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir la page de connexion", e);
        }
    }

    @FXML
    void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/register.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir la page d'inscription", e);
        }
    }

    @FXML
    void goToEvents() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/events.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Événements");
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir la page Events", e);
        }
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, message + "\n\n" + e.getMessage(), ButtonType.OK);
        alert.setHeaderText("Erreur de navigation");
        alert.showAndWait();
    }

}

