package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Utils.ModernNotification;
import org.example.Utils.PasswordResetStore;

public class VerifyCodeController {

    @FXML
    private Label lblEmailInfo;

    @FXML
    private TextField tfCode;

    private String email;

    public void setEmail(String email) {
        this.email = email;
        lblEmailInfo.setText("Code sent to: " + email);
    }

    @FXML
    void verifyCode() {
        try {
            String code = tfCode.getText().trim();

            if (code.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Error", "Please enter the code.");
                return;
            }

            if (!PasswordResetStore.verifyCode(email, code)) {
                ModernNotification.showError(getCurrentStage(), "Error", "Invalid or expired code.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/reset_password.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Reset Password");
            stage.show();

            ModernNotification.showSuccess(stage, "Success", "Code verified successfully.");

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
        return tfCode != null && tfCode.getScene() != null
                ? (Stage) tfCode.getScene().getWindow()
                : null;
    }
}