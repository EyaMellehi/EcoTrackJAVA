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

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Unable to generate QR code.");
        }
    }

    @FXML
    private void verifyAndEnable() {
        try {
            String code = tfOtpCode.getText().trim();

            if (!code.matches("\\d{6}")) {
                lblError.setText("Enter a valid 6-digit code.");
                return;
            }

            TwoFactorService twoFactorService = new TwoFactorService();
            boolean valid = twoFactorService.verifyCode(secret, code);

            if (!valid) {
                lblError.setText("Invalid code.");
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
            stage.setTitle("Profile");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Activation failed.");
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
            stage.setTitle("Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Unable to go back.");
        }
    }
}