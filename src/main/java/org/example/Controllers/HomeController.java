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
        navigateTo("/User/Register.fxml", "Register");
    }

    @FXML
    void goToRecycling() {
        navigateTo("/recyclage/points_connected.fxml", "Recycling Points");
    }

    @FXML
    void goToEvents() {
        navigateTo("/events.fxml", "Events");
    }

    @FXML
    void goToBlogs() {
        navigateTo("/annonces/list_annonces_user.fxml", "Blogs");
    }

    @FXML
    void goToAssociation() {
        navigateTo("/client_association/index.fxml", "Associations");
    }

    @FXML
    void goToReport() {
        navigateTo("/signalement/list_signalement.fxml", "Reports");
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
            stage.setMaximized(true);
            stage.setTitle(title);
            stage.setFullScreen(true);
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