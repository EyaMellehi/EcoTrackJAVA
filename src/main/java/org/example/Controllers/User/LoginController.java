package org.example.Controllers.User;

import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.admin.AdminDashboardController;
import org.example.Entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import org.example.Entities.GoogleUserInfo;
import org.example.Services.GoogleAuthService;

import javafx.stage.FileChooser;
import org.example.Services.FaceAuthService;

import java.io.File;
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
            showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
            return;
        }

        if (!user.isActive()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Your account is disabled.");
            return;
        }

        if (user.getRoles().contains("ROLE_ADMIN")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
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

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("EcoTrack - Home");
            stage.show();

        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Unknown role.");
        }
    }
    @FXML
    void login(ActionEvent event) {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        try {
            User user = userService.login(email, password);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid email or password.");
                return;
            }

            if (!user.isActive()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Your account is disabled.");
                return;
            }

            if (user.isTwoFactorEnabled()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/verify_2fa.fxml"));
                Parent root = loader.load();

                Verify2FAController controller = loader.getController();
                controller.setUser(user);

                Stage stage = (Stage) tfEmail.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.setTitle("Verify 2FA");
                stage.show();
            } else {
                openHomeAccordingToRole(user);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
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
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    void goToForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/forgot_password.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Forgot Password");
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
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

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");

            stage.setTitle("Complete Registration");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Google Login Error", e.getMessage());
        }
    }
    @FXML
    void loginWithFace(ActionEvent event) {
        try {
            OpenCV.loadLocally();

            VideoCapture capture = new VideoCapture(0);

            if (!capture.isOpened()) {
                showAlert(Alert.AlertType.ERROR, "Face Login", "Unable to open camera.");
                return;
            }

            Mat frame = new Mat();

            // on lit quelques frames pour laisser la caméra se stabiliser
            for (int i = 0; i < 10; i++) {
                capture.read(frame);
                Thread.sleep(100);
            }

            capture.release();

            if (frame.empty()) {
                showAlert(Alert.AlertType.ERROR, "Face Login", "No image captured from camera.");
                return;
            }

            File tempFile = new File("temp_login_face.jpg");
            Imgcodecs.imwrite(tempFile.getAbsolutePath(), frame);

            FaceAuthService faceAuthService = new FaceAuthService();
            Integer userId = faceAuthService.identifyUserByFace(tempFile);

            tempFile.delete();

            if (userId == null) {
                showAlert(Alert.AlertType.ERROR, "Face Login", "Face not recognized.");
                return;
            }

            User user = userService.getUserById(userId);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Face Login", "User not found.");
                return;
            }

            if (!user.isActive()) {
                showAlert(Alert.AlertType.ERROR, "Face Login", "Your account is disabled.");
                return;
            }

            openHomeAccordingToRole(user);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Face Login", "Error during face login.");
        }
    }
}
