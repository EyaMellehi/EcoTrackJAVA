package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;

public class ResetPasswordController {

    @FXML
    private Label lblEmail;

    @FXML
    private PasswordField pfNewPassword;

    @FXML
    private PasswordField pfConfirmPassword;

    private String email;

    private final UserService userService = new UserService();

    public void setEmail(String email) {
        this.email = email;
        lblEmail.setText("Reset password for: " + email);
    }

    @FXML
    void updatePassword() {
        try {
            String newPassword = pfNewPassword.getText().trim();
            String confirmPassword = pfConfirmPassword.getText().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Error", "Please fill all fields.");
                return;
            }

            if (newPassword.length() < 6) {
                ModernNotification.showError(getCurrentStage(), "Error", "Password must contain at least 6 characters.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                ModernNotification.showError(getCurrentStage(), "Error", "Passwords do not match.");
                return;
            }

            userService.updatePassword(email, newPassword);

            Parent root = FXMLLoader.load(getClass().getResource("/user/login.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Login");
            stage.show();

            ModernNotification.showSuccess(stage, "Success", "Password updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", e.getMessage());
        }
    }

    @FXML
    void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/login.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", "Unable to open login page.");
        }
    }

    private Stage getCurrentStage() {
        return pfNewPassword != null && pfNewPassword.getScene() != null
                ? (Stage) pfNewPassword.getScene().getWindow()
                : null;
    }
}