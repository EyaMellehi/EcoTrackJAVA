package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.admin.AdminDashboardController;
import org.example.Entities.User;
import org.example.Services.TwoFactorService;
import org.example.Utils.ModernNotification;

public class Verify2FAController {

    @FXML
    private TextField tfOtpCode;

    @FXML
    private Label lblError;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void verifyOtp() {
        try {
            String code = tfOtpCode.getText().trim();

            if (code.isEmpty()) {
                lblError.setText("Please enter the 6-digit code.");
                ModernNotification.showWarning(getCurrentStage(), "2FA", "Please enter the 6-digit code.");
                return;
            }

            if (!code.matches("\\d{6}")) {
                lblError.setText("Code must contain exactly 6 digits.");
                ModernNotification.showWarning(getCurrentStage(), "2FA", "Code must contain exactly 6 digits.");
                return;
            }

            TwoFactorService twoFactorService = new TwoFactorService();
            boolean valid = twoFactorService.verifyCode(user.getTwoFactorSecret(), code);

            if (!valid) {
                lblError.setText("Invalid code.");
                ModernNotification.showError(getCurrentStage(), "2FA", "Invalid code.");
                return;
            }

            lblError.setText("");
            ModernNotification.showSuccess(getCurrentStage(), "2FA", "Verification successful.");
            openHomeAccordingToRole(user);

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Verification failed.");
            ModernNotification.showError(getCurrentStage(), "2FA", "Verification failed.");
        }
    }

    private void openHomeAccordingToRole(User user) throws Exception {
        if (user == null) {
            ModernNotification.showError(getCurrentStage(), "Error", "User not found.");
            return;
        }

        if (!user.isActive()) {
            ModernNotification.showError(getCurrentStage(), "Error", "Your account is disabled.");
            return;
        }

        if (user.getRoles().contains("ROLE_ADMIN")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(user);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Admin Dashboard");
            stage.show();

        } else if (user.getRoles().contains("ROLE_CITOYEN")
                || user.getRoles().contains("ROLE_AGENT_TERRAIN")
                || user.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(user);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("EcoTrack - Home");
            stage.show();

        } else {
            ModernNotification.showError(getCurrentStage(), "Error", "Unknown role.");
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/login.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Unable to go back.");
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to go back.");
        }
    }

    private Stage getCurrentStage() {
        return tfOtpCode != null && tfOtpCode.getScene() != null
                ? (Stage) tfOtpCode.getScene().getWindow()
                : null;
    }
}