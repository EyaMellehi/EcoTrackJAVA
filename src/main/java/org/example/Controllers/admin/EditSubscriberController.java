package org.example.Controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;

public class EditSubscriberController {

    @FXML private MenuButton menuAdmin;

    @FXML private Label lblAvatar;
    @FXML private Label lblNameHeader;
    @FXML private Label lblEmailHeader;

    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private ComboBox<String> cbRegion;
    @FXML private CheckBox chkActive;

    private final UserService userService = new UserService();
    private User user;
    private User loggedUser;

    @FXML
    public void initialize() {
        cbRegion.setItems(FXCollections.observableArrayList(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", "Jendouba",
                "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia", "La Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana",
                "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));

        tfName.textProperty().addListener((obs, oldVal, newVal) -> {
            lblNameHeader.setText(newVal == null || newVal.isBlank() ? "User" : newVal);
            if (newVal != null && !newVal.isBlank()) {
                lblAvatar.setText(String.valueOf(Character.toUpperCase(newVal.charAt(0))));
            } else {
                lblAvatar.setText("U");
            }
        });

        tfEmail.textProperty().addListener((obs, oldVal, newVal) ->
                lblEmailHeader.setText(newVal == null || newVal.isBlank() ? "" : newVal)
        );

        tfPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tfPhone.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (tfPhone.getText().length() > 8) {
                tfPhone.setText(tfPhone.getText().substring(0, 8));
            }
        });
    }

    public void setUser(User user) {
        this.user = user;

        if (user != null) {
            tfName.setText(user.getName());
            tfEmail.setText(user.getEmail());
            tfPhone.setText(user.getPhone() == null ? "" : user.getPhone());
            cbRegion.setValue(user.getRegion());
            chkActive.setSelected(user.isActive());

            lblNameHeader.setText(user.getName());
            lblEmailHeader.setText(user.getEmail());

            if (user.getName() != null && !user.getName().isEmpty()) {
                lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getName().charAt(0))));
            } else {
                lblAvatar.setText("U");
            }
        }
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
        if (loggedUser != null && loggedUser.getName() != null) {
            menuAdmin.setText(loggedUser.getName());
        }
    }

    @FXML
    void updateSubscriber() {
        try {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String region = cbRegion.getValue();
            boolean active = chkActive.isSelected();

            if (name.isEmpty() || email.isEmpty() || region == null) {
                ModernNotification.showError(getCurrentStage(), "Error", "Please fill all required fields.");
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
            user.setActive(active);

            userService.updateSubscriberByAdmin(user);

            ModernNotification.showSuccess(getCurrentStage(), "Success", "Subscriber updated successfully.");
            backToSubscribers();

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", e.getMessage());
        }
    }

    @FXML
    void backToSubscribers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/subscribers.fxml"));
            Parent root = loader.load();

            SubscribersController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Subscribers");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open subscribers page.");
        }
    }

    @FXML
    void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open admin dashboard.");
        }
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/home.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to logout.");
        }
    }

    @FXML
    void goToSubscribers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/subscribers.fxml"));
            Parent root = loader.load();

            SubscribersController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Subscribers");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open subscribers page.");
        }
    }

    @FXML
    void goToMunicipalAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/municipal_agents.fxml"));
            Parent root = loader.load();

            MunicipalAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Municipal Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open municipal agents page.");
        }
    }

    @FXML
    void goToFieldAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/field_agents.fxml"));
            Parent root = loader.load();

            FieldAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Field Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open field agents page.");
        }
    }

    @FXML
    void goToCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/categories.fxml"));
            Parent root = loader.load();

            CategoriesController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Catégories");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open categories page.");
        }
    }

    public void goToAssociation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_association/association.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("associaitons");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open associations page.");
        }
    }

    public void goToDonation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/donation/donationIndex.fxml"));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("donations");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Navigation", "Unable to open donations page.");
        }
    }

    private Stage getCurrentStage() {
        return menuAdmin != null && menuAdmin.getScene() != null
                ? (Stage) menuAdmin.getScene().getWindow()
                : null;
    }
}