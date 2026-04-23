package org.example.Controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class HomeController {

    @FXML
    private Button btnLogin;

    @FXML
    void goToLogin() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToRegister() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToRecycling() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToEvents() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToBlogs() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToAssociation() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToReport() {
        navigateTo("/User/Login.fxml", "Login");
    }

    @FXML
    void goToHome() {
        // Already on home page.
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));

            stage.setTitle(title);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open: " + title);
            alert.showAndWait();
        }
    }

}