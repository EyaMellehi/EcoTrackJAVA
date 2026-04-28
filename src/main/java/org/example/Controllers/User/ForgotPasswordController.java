package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Services.EmailService;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;
import org.example.Utils.PasswordResetStore;

public class ForgotPasswordController {

    @FXML
    private TextField tfEmail;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();

    @FXML
    void sendResetCode() {
        try {
            String email = tfEmail.getText().trim();

            if (email.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Error", "Please enter your email.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ModernNotification.showError(getCurrentStage(), "Error", "Invalid email format.");
                return;
            }

            if (!userService.emailExists(email)) {
                ModernNotification.showError(getCurrentStage(), "Error", "No account found with this email.");
                return;
            }

            String code = PasswordResetStore.generateCode(email);

            emailService.sendResetCode(email, code);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/verify_code.fxml"));
            Parent root = loader.load();

            VerifyCodeController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Verify Reset Code");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

            ModernNotification.showSuccess(stage, "Success", "Reset code sent successfully.");

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
        return tfEmail != null && tfEmail.getScene() != null
                ? (Stage) tfEmail.getScene().getWindow()
                : null;
    }
}