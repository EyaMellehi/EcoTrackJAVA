package org.example.Controllers.User;

import org.example.Entities.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.UserService;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfPhone;

    @FXML
    private TextField tfEmail;

    @FXML
    private ComboBox<String> cbRegion;

    @FXML
    private PasswordField pfPassword;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        cbRegion.setItems(FXCollections.observableArrayList(
                "Ariana",
                "Béja",
                "Ben Arous",
                "Bizerte",
                "Gabès",
                "Gafsa",
                "Jendouba",
                "Kairouan",
                "Kasserine",
                "Kébili",
                "Le Kef",
                "Mahdia",
                "La Manouba",
                "Médenine",
                "Monastir",
                "Nabeul",
                "Sfax",
                "Sidi Bouzid",
                "Siliana",
                "Sousse",
                "Tataouine",
                "Tozeur",
                "Tunis",
                "Zaghouan"
        ));
        tfPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tfPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }

            if (tfPhone.getText().length() > 8) {
                tfPhone.setText(tfPhone.getText().substring(0, 8));
            }
        });

        tfName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[A-Za-zÀ-ÿ\\s]*")) {
                tfName.setText(newValue.replaceAll("[^A-Za-zÀ-ÿ\\s]", ""));
            }
        });
    }

    @FXML
    void register() {
        try {
            String name = tfName.getText().trim();
            String phone = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();
            String region = cbRegion.getValue();
            String password = pfPassword.getText().trim();

            // Name
            if (name.isEmpty()) {
                showAlert("Erreur", "Le nom est obligatoire");
                return;
            }

            if (name.length() < 3) {
                showAlert("Erreur", "Le nom doit contenir au moins 3 caractères");
                return;
            }

            if (!name.matches("[A-Za-zÀ-ÿ\\s]+")) {
                showAlert("Erreur", "Le nom doit contenir uniquement des lettres et des espaces");
                return;
            }

            // Phone
            if (phone.isEmpty()) {
                showAlert("Erreur", "Le téléphone est obligatoire");
                return;
            }

            if (!phone.matches("\\d{8}")) {
                showAlert("Erreur", "Le téléphone doit contenir exactement 8 chiffres");
                return;
            }

            // Email
            if (email.isEmpty()) {
                showAlert("Erreur", "L'email est obligatoire");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showAlert("Erreur", "Format d'email invalide");
                return;
            }

            // Region
            if (region == null || region.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner une région");
                return;
            }

            // Password
            if (password.isEmpty()) {
                showAlert("Erreur", "Le mot de passe est obligatoire");
                return;
            }

            if (password.length() < 6) {
                showAlert("Erreur", "Le mot de passe doit contenir au moins 6 caractères");
                return;
            }

            // Email exists
            if (userService.emailExists(email)) {
                showAlert("Erreur", "Email déjà existant");
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setName(name);
            user.setPhone(phone);
            user.setRegion(region);

            userService.registerCitoyen(user);

            showAlert("Succès", "Compte créé avec succès");

            Stage stage = (Stage) tfName.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/user/login.fxml")));
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void goToLogin() {
        try {
            Stage stage = (Stage) tfName.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/user/login.fxml")));
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}