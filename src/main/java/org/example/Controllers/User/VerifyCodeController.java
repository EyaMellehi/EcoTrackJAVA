package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter the code.");
                return;
            }

            if (!PasswordResetStore.verifyCode(email, code)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid or expired code.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/reset_password.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) tfCode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reset Password");
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
            Stage stage = (Stage) tfCode.getScene().getWindow();
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