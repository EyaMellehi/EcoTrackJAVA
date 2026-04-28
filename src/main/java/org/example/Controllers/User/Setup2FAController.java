package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Services.TwoFactorService;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;

public class Setup2FAController {

    @FXML
    private ImageView imgQrCode;

    @FXML
    private Label lblSecret;

    @FXML
    private TextField tfOtpCode;

    @FXML
    private Label lblError;

    private User user;
    private String secret;

    public void setUser(User user) {
        this.user = user;

        try {
            TwoFactorService twoFactorService = new TwoFactorService();

            secret = twoFactorService.generateSecret();
            String otpAuthUrl = twoFactorService.buildOtpAuthUrl(user.getEmail(), secret);

            Image qrImage = new Image(twoFactorService.generateQrCodeImage(otpAuthUrl));
            imgQrCode.setImage(qrImage);

            lblSecret.setText(secret);
            lblError.setText("");

            ModernNotification.showInfo(getCurrentStage(), "2FA", "QR code generated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Unable to generate QR code.");
            ModernNotification.showError(getCurrentStage(), "2FA", "Unable to generate QR code.");
        }
    }

    @FXML
    private void verifyAndEnable() {
        try {
            String code = tfOtpCode.getText().trim();

            if (!code.matches("\\d{6}")) {
                lblError.setText("Enter a valid 6-digit code.");
                ModernNotification.showWarning(getCurrentStage(), "2FA", "Enter a valid 6-digit code.");
                return;
            }

            TwoFactorService twoFactorService = new TwoFactorService();
            boolean valid = twoFactorService.verifyCode(secret, code);

            if (!valid) {
                lblError.setText("Invalid code.");
                ModernNotification.showError(getCurrentStage(), "2FA", "Invalid verification code.");
                return;
            }

            UserService userService = new UserService();
            userService.enableTwoFactor(user.getId(), secret);

            user.setTwoFactorEnabled(true);
            user.setTwoFactorSecret(secret);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) tfOtpCode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Profile");
            stage.show();

            ModernNotification.showSuccess(stage, "2FA", "Two-factor authentication activated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Activation failed.");
            ModernNotification.showError(getCurrentStage(), "2FA", "Activation failed.");
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) tfOtpCode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Profile");
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