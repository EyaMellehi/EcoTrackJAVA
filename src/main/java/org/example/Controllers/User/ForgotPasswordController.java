package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Services.EmailService;
import org.example.Services.UserService;
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
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter your email.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
                return;
            }

            if (!userService.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Error", "No account found with this email.");
                return;
            }

            String code = PasswordResetStore.generateCode(email);

            emailService.sendResetCode(email, code);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/verify_code.fxml"));
            Parent root = loader.load();

            VerifyCodeController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Verify Reset Code");
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
            Stage stage = (Stage) tfEmail.getScene().getWindow();
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