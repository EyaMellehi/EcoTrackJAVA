package org.example.Controllers.User;

import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.admin.AdminDashboardController;
import org.example.Entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import org.example.Entities.GoogleUserInfo;
import org.example.Services.GoogleAuthService;

public class LoginController {

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Hyperlink linkRegister;

    private final UserService userService = new UserService();

    private void openHomeAccordingToRole(User user) throws IOException {
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
            return;
        }

        if (!user.isActive()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Your account is disabled.");
            return;
        }

        if (user.getRoles().contains("ROLE_ADMIN")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();

        } else if (user.getRoles().contains("ROLE_CITOYEN")
                || user.getRoles().contains("ROLE_AGENT_TERRAIN")
                || user.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();

        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Unknown role.");
        }
    }
    @FXML
    void login(ActionEvent event) {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        try {
            User user = userService.login(email, password);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid email or password.");
                return;
            }

            if (!user.isActive()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Your account is disabled.");
                return;
            }

            if (user.getRoles().contains("ROLE_ADMIN")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
                Parent root = loader.load();

                AdminDashboardController controller = loader.getController();
                controller.setLoggedUser(user);

                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Admin Dashboard");
                stage.show();

            } else if (user.getRoles().contains("ROLE_CITOYEN")
                    || user.getRoles().contains("ROLE_AGENT_TERRAIN")
                    || user.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
                Parent root = loader.load();

                HomeConnectedController controller = loader.getController();
                controller.setLoggedUser(user);

                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("EcoTrack - Home");
                stage.show();

            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Unknown role.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/Register.fxml"));
            Stage stage = (Stage) linkRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
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
    @FXML
    void goToForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/forgot_password.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Forgot Password");
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    void loginWithGoogle() {
        try {
            GoogleAuthService googleAuthService = new GoogleAuthService();
            GoogleUserInfo googleUser = googleAuthService.authenticate();

            User existingUser = userService.findByEmail(googleUser.getEmail());

            if (existingUser != null) {
                openHomeAccordingToRole(existingUser);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/complete_google_register.fxml"));
            Parent root = loader.load();

            CompleteGoogleRegisterController controller = loader.getController();
            controller.setGoogleUserInfo(googleUser);

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Complete Registration");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Google Login Error", e.getMessage());
        }
    }
}
