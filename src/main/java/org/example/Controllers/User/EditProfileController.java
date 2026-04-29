package org.example.Controllers.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.User;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;

public class EditProfileController {

    @FXML private HBox navbarCitoyen;
    @FXML private HBox navbarMunicipal;

    @FXML private NavbarCitoyenController navbarCitoyenController;
    @FXML private NavbarMunicipalController navbarMunicipalController;

    @FXML private Label lblAvatar;
    @FXML private Label lblNamePreview;
    @FXML private Label lblEmailPreview;
    @FXML private Label lblRegionPreview;
    @FXML private Label lblImageName;

    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private ComboBox<String> cbRegion;

    private final UserService userService = new UserService();
    private User user;
    private File selectedImage;

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

        tfPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tfPhone.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (tfPhone.getText().length() > 8) {
                tfPhone.setText(tfPhone.getText().substring(0, 8));
            }
        });

        tfName.textProperty().addListener((obs, oldVal, newVal) -> {
            lblNamePreview.setText(newVal.isBlank() ? "USER NAME" : newVal);

            if (!newVal.isBlank()) {
                lblAvatar.setText(String.valueOf(Character.toUpperCase(newVal.charAt(0))));
            } else {
                lblAvatar.setText("U");
            }
        });

        tfEmail.textProperty().addListener((obs, oldVal, newVal) ->
                lblEmailPreview.setText(newVal.isBlank() ? "user@email.com" : newVal)
        );
    }

    public void setUser(User user) {
        this.user = user;

        configureNavbar();

        if (user != null) {
            tfName.setText(user.getName());
            tfEmail.setText(user.getEmail());
            tfPhone.setText(user.getPhone());
            cbRegion.setValue(user.getRegion());

            lblNamePreview.setText(user.getName() != null ? user.getName() : "USER NAME");
            lblEmailPreview.setText(user.getEmail() != null ? user.getEmail() : "user@email.com");
            lblRegionPreview.setText(user.getRegion() != null ? user.getRegion() : "Region");

            if (user.getName() != null && !user.getName().isEmpty()) {
                lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getName().charAt(0))));
            } else {
                lblAvatar.setText("U");
            }
        }
    }

    private void configureNavbar() {
        if (user == null || user.getRoles() == null) {
            showCitoyenNavbar();
            return;
        }

        String roles = user.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            showMunicipalNavbar();
            if (navbarMunicipalController != null) {
                navbarMunicipalController.setLoggedUser(user);
            }
        } else {
            showCitoyenNavbar();
            if (navbarCitoyenController != null) {
                navbarCitoyenController.setLoggedUser(user);
            }
        }
    }

    private void showCitoyenNavbar() {
        navbarCitoyen.setVisible(true);
        navbarCitoyen.setManaged(true);

        navbarMunicipal.setVisible(false);
        navbarMunicipal.setManaged(false);
    }

    private void showMunicipalNavbar() {
        navbarMunicipal.setVisible(true);
        navbarMunicipal.setManaged(true);

        navbarCitoyen.setVisible(false);
        navbarCitoyen.setManaged(false);
    }

    @FXML
    void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );

        selectedImage = fileChooser.showOpenDialog(tfName.getScene().getWindow());

        if (selectedImage != null) {
            lblImageName.setText(selectedImage.getName());
            ModernNotification.showInfo(getCurrentStage(), "Image", "Image selected: " + selectedImage.getName());
        }
    }

    @FXML
    void saveChanges() {
        try {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String region = cbRegion.getValue();

            if (name.isEmpty() || email.isEmpty() || region == null) {
                ModernNotification.showError(getCurrentStage(), "Error", "Please fill required fields.");
                return;
            }

            if (!name.matches("[A-Za-zÀ-ÿ\\s]+")) {
                ModernNotification.showError(getCurrentStage(), "Error", "Name must contain only letters and spaces.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ModernNotification.showError(getCurrentStage(), "Error", "Invalid email format.");
                return;
            }

            if (!phone.isEmpty() && !phone.matches("\\d{8}")) {
                ModernNotification.showError(getCurrentStage(), "Error", "Phone must contain exactly 8 digits.");
                return;
            }

            if (userService.emailExistsForAnotherUser(email, user.getId())) {
                ModernNotification.showError(getCurrentStage(), "Error", "This email is already used by another account.");
                return;
            }

            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRegion(region);

            String imagePath = user.getImage();

            if (selectedImage != null) {
                String fileName = System.currentTimeMillis() + "_" + selectedImage.getName();

                Path uploadDir = Paths.get("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path destination = uploadDir.resolve(fileName);
                Files.copy(selectedImage.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                imagePath = destination.toString();
            }

            user.setImage(imagePath);
            userService.updateProfile(user);

            ModernNotification.showSuccess(getCurrentStage(), "Success", "Profile updated successfully.");
            goBackToProfilePage();

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", e.getMessage());
        }
    }

    @FXML
    void backToProfile() {
        goBackToProfilePage();
    }

    private void goBackToProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) tfName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("My Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", "Unable to return to profile page.");
        }
    }

    private Stage getCurrentStage() {
        return tfName != null && tfName.getScene() != null
                ? (Stage) tfName.getScene().getWindow()
                : null;
    }
}