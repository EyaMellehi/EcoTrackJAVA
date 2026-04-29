package org.example.Controllers.User;

import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.admin.AdminDashboardController;
import org.example.Entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Services.UserService;
import org.example.Entities.GoogleUserInfo;
import org.example.Services.GoogleAuthService;
import org.example.Services.FaceAuthService;
import org.example.Utils.ModernNotification;

import java.io.File;
import java.io.IOException;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

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
    void login(ActionEvent event) {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            ModernNotification.showError(getCurrentStage(), "Error", "Please fill all fields.");
            return;
        }

        try {
            User user = userService.login(email, password);

            if (user == null) {
                ModernNotification.showError(getCurrentStage(), "Error", "Invalid email or password.");
                return;
            }

            if (!user.isActive()) {
                ModernNotification.showError(getCurrentStage(), "Error", "Your account is disabled.");
                return;
            }

            if (user.isTwoFactorEnabled()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/verify_2fa.fxml"));
                Parent root = loader.load();

                Verify2FAController controller = loader.getController();
                controller.setUser(user);

                Stage stage = getCurrentStage();
                stage.setScene(new Scene(root));
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.setTitle("Verify 2FA");
                stage.show();
            } else {
                openHomeAccordingToRole(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", e.getMessage());
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/Register.fxml"));
            Stage stage = (Stage) linkRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", "Unable to open register page.");
        }
    }

    @FXML
    void goToForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/forgot_password.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Forgot Password");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", e.getMessage());
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

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Complete Registration");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Google Login Error", e.getMessage());
        }
    }

    @FXML
    void loginWithFace(ActionEvent event) {
        try {
            OpenCV.loadLocally();

            VideoCapture capture = new VideoCapture(0);

            if (!capture.isOpened()) {
                ModernNotification.showError(getCurrentStage(), "Face Login", "Unable to open camera.");
                return;
            }

            Mat frame = new Mat();

            for (int i = 0; i < 10; i++) {
                capture.read(frame);
                Thread.sleep(100);
            }

            capture.release();

            if (frame.empty()) {
                ModernNotification.showError(getCurrentStage(), "Face Login", "No image captured from camera.");
                return;
            }

            File tempFile = new File("temp_login_face.jpg");
            Imgcodecs.imwrite(tempFile.getAbsolutePath(), frame);

            FaceAuthService faceAuthService = new FaceAuthService();
            Integer userId = faceAuthService.identifyUserByFace(tempFile);

            tempFile.delete();

            if (userId == null) {
                ModernNotification.showError(getCurrentStage(), "Face Login", "Face not recognized.");
                return;
            }

            User user = userService.getUserById(userId);

            if (user == null) {
                ModernNotification.showError(getCurrentStage(), "Face Login", "User not found.");
                return;
            }

            if (!user.isActive()) {
                ModernNotification.showError(getCurrentStage(), "Face Login", "Your account is disabled.");
                return;
            }

            openHomeAccordingToRole(user);

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Face Login", "Error during face login.");
        }
    }

    private Stage getCurrentStage() {
        if (btnLogin != null && btnLogin.getScene() != null) {
            return (Stage) btnLogin.getScene().getWindow();
        }
        if (tfEmail != null && tfEmail.getScene() != null) {
            return (Stage) tfEmail.getScene().getWindow();
        }
        return null;
    }
}