package org.example.Controllers.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Controllers.HomeConnectedController;
import org.example.Entities.GoogleUserInfo;
import org.example.Entities.User;
import org.example.Services.UserService;

public class CompleteGoogleRegisterController {

    @FXML private Label lblEmail;
    @FXML private TextField tfName;
    @FXML private ComboBox<String> cbRegion;
    @FXML private TextField tfPhone;
    @FXML private Button btnComplete;

    private GoogleUserInfo googleUserInfo;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        cbRegion.setItems(FXCollections.observableArrayList(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", "Jendouba",
                "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia", "La Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana",
                "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));

        tfPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tfPhone.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (tfPhone.getText().length() > 8) {
                tfPhone.setText(tfPhone.getText().substring(0, 8));
            }
        });
    }

    public void setGoogleUserInfo(GoogleUserInfo googleUserInfo) {
        this.googleUserInfo = googleUserInfo;

        if (googleUserInfo != null) {
            lblEmail.setText("Email: " + googleUserInfo.getEmail());
            tfName.setText(googleUserInfo.getName() != null ? googleUserInfo.getName() : "");
        }
    }

    @FXML
    void completeRegistration() {
        try {
            if (googleUserInfo == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Google user data is missing.");
                return;
            }

            String name = tfName.getText().trim();
            String region = cbRegion.getValue();
            String phone = tfPhone.getText().trim();

            if (name.isEmpty() || region == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields.");
                return;
            }

            if (!phone.isEmpty() && !phone.matches("\\d{8}")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Phone must contain exactly 8 digits.");
                return;
            }

            if (userService.findByEmail(googleUserInfo.getEmail()) != null) {
                showAlert(Alert.AlertType.ERROR, "Error", "An account with this email already exists.");
                return;
            }

            User user = new User();
            user.setEmail(googleUserInfo.getEmail());
            user.setName(name);
            user.setRoles("[\"ROLE_CITOYEN\"]");
            user.setPassword(java.util.UUID.randomUUID().toString());
            user.setPhone(phone.isEmpty() ? null : phone);
            user.setRegion(region);
            user.setPoints(0);
            user.setActive(true);
            user.setImage(googleUserInfo.getPicture());
            user.setDelegation(null);
            user.setFaceioId(null);

            userService.addGoogleUser(user);

            User loggedUser = userService.findByEmail(googleUserInfo.getEmail());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) btnComplete.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("EcoTrack - Home");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/User/login.fxml"));
            Stage stage = (Stage) btnComplete.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
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