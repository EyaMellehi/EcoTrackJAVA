package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.User;
import org.example.Services.UserService;
import org.mindrot.jbcrypt.BCrypt;

public class ChangePasswordController {

    @FXML private HBox navbarCitoyen;
    @FXML private HBox navbarMunicipal;

    @FXML private NavbarCitoyenController navbarCitoyenController;
    @FXML private NavbarMunicipalController navbarMunicipalController;

    @FXML private PasswordField pfCurrentPassword;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;

    private final UserService userService = new UserService();
    private User user;

    public void setUser(User user) {
        this.user = user;
        configureNavbar();
    }

    private void configureNavbar() {
        if (user == null || user.getRoles() == null) {
            showCitoyenNavbar();
            return;
        }

        String roles = user.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            showMunicipalNavbar();

            if (navbarMunicipalController != null) {
                navbarMunicipalController.setLoggedUser(user);
            }
        } else {
            showCitoyenNavbar();

            if (navbarCitoyenController != null) {
                navbarCitoyenController.setLoggedUser(user);
            }
        }
    }

    private void showCitoyenNavbar() {
        navbarCitoyen.setVisible(true);
        navbarCitoyen.setManaged(true);

        navbarMunicipal.setVisible(false);
        navbarMunicipal.setManaged(false);
    }

    private void showMunicipalNavbar() {
        navbarMunicipal.setVisible(true);
        navbarMunicipal.setManaged(true);

        navbarCitoyen.setVisible(false);
        navbarCitoyen.setManaged(false);
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

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No connected user found.");
                return;
            }

            String storedPassword = user.getPassword();

            if (storedPassword == null || storedPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Stored password is empty.");
                return;
            }

            boolean currentPasswordOk;

            if (isBCryptHash(storedPassword)) {
                try {
                    String normalizedHash = normalizeBcryptHash(storedPassword);
                    currentPasswordOk = BCrypt.checkpw(currentPassword, normalizedHash);
                } catch (IllegalArgumentException e) {
                    currentPasswordOk = currentPassword.equals(storedPassword);
                }
            } else {
                currentPasswordOk = currentPassword.equals(storedPassword);
            }

            if (!currentPasswordOk) {
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

            if (newPassword.equals(currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "New password must be different from current password.");
                return;
            }

            userService.updatePassword(user.getEmail(), newPassword);

            // Mettre à jour l'objet local aussi
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));

            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully.");
            backToProfile();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private boolean isBCryptHash(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$.*");
    }

    private String normalizeBcryptHash(String hash) {
        if (hash == null) {
            return null;
        }

        if (hash.startsWith("$2y$")) {
            return "$2a$" + hash.substring(4);
        }

        return hash;
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
            stage.setMaximized(true);
            stage.setTitle("My Profile");
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