package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Services.UserService;
import org.mindrot.jbcrypt.BCrypt;

public class ChangePasswordController {

    @FXML
    private MenuButton menuProfile;

    @FXML
    private PasswordField pfCurrentPassword;

    @FXML
    private PasswordField pfNewPassword;

    @FXML
    private PasswordField pfConfirmPassword;

    private final UserService userService = new UserService();
    private User user;

    public void setUser(User user) {
        this.user = user;

        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            menuProfile.setText(user.getName());
        }
    }

    @FXML
    void updatePassword() {
        try {
            String currentPassword = pfCurrentPassword.getText().trim();
            String newPassword = pfNewPassword.getText().trim();
            String confirmPassword = pfConfirmPassword.getText().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
                return;
            }

            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect.");
                return;
            }

            if (newPassword.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Error", "New password must contain at least 6 characters.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
                return;
            }

            if (BCrypt.checkpw(newPassword, user.getPassword())) {
                showAlert(Alert.AlertType.ERROR, "Error", "New password must be different from current password.");
                return;
            }

            userService.updatePassword(user.getEmail(), newPassword);

            // important: update local user password too
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));

            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");

            backToProfile();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    void backToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) pfCurrentPassword.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) pfCurrentPassword.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
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