package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.example.Services.UserService;

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
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
                return;
            }

            if (newPassword.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Error", "Password must contain at least 6 characters.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
                return;
            }

            userService.updatePassword(email, newPassword);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");

            Parent root = FXMLLoader.load(getClass().getResource("/user/login.fxml"));
            Stage stage = (Stage) pfNewPassword.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/login.fxml"));
            Stage stage = (Stage) pfNewPassword.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}