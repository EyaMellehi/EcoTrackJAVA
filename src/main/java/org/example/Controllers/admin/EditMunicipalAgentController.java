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
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMunicipalAgentController {

    @FXML private MenuButton menuAdmin;

    @FXML private Label lblStatusBadge;
    @FXML private Label lblAvatar;
    @FXML private Label lblNameHeader;
    @FXML private Label lblEmailHeader;
    @FXML private Label lblAgentId;
    @FXML private Label lblRegionInfo;
    @FXML private Label lblStatusInfo;

    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private ComboBox<String> cbRegion;
    @FXML private ComboBox<String> cbDelegation;
    @FXML private CheckBox chkActive;
    @FXML private PasswordField pfNewPassword;

    private final UserService userService = new UserService();
    private final Map<String, List<String>> delegationsByRegion = new HashMap<>();

    private User user;
    private User loggedUser;

    @FXML
    public void initialize() {
        loadRegions();
        loadDelegationsMap();

        tfPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tfPhone.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (tfPhone.getText().length() > 8) {
                tfPhone.setText(tfPhone.getText().substring(0, 8));
            }
        });

        tfName.textProperty().addListener((obs, oldVal, newVal) -> {
            lblNameHeader.setText(newVal == null || newVal.isBlank() ? "User" : newVal);
            if (newVal != null && !newVal.isBlank()) {
                lblAvatar.setText(String.valueOf(Character.toUpperCase(newVal.charAt(0))));
            } else {
                lblAvatar.setText("U");
            }
        });

        tfEmail.textProperty().addListener((obs, oldVal, newVal) ->
                lblEmailHeader.setText(newVal == null ? "" : newVal)
        );

        chkActive.selectedProperty().addListener((obs, oldVal, newVal) -> updateStatusDisplay());
        cbRegion.valueProperty().addListener((obs, oldVal, newVal) -> lblRegionInfo.setText(newVal == null ? "" : newVal));
    }

    public void setUser(User user) {
        this.user = user;

        if (user != null) {
            tfName.setText(user.getName());
            tfEmail.setText(user.getEmail());
            tfPhone.setText(user.getPhone() == null ? "" : user.getPhone());
            cbRegion.setValue(user.getRegion());

            onRegionChanged();
            cbDelegation.setValue(user.getDelegation());

            chkActive.setSelected(user.isActive());

            lblNameHeader.setText(user.getName());
            lblEmailHeader.setText(user.getEmail());
            lblAgentId.setText("#" + user.getId());
            lblRegionInfo.setText(user.getRegion());

            if (user.getName() != null && !user.getName().isEmpty()) {
                lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getName().charAt(0))));
            } else {
                lblAvatar.setText("U");
            }

            updateStatusDisplay();
        }
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
        if (loggedUser != null && loggedUser.getName() != null) {
            menuAdmin.setText(loggedUser.getName());
        }
    }

    private void updateStatusDisplay() {
        boolean active = chkActive.isSelected();
        lblStatusInfo.setText(active ? "Active" : "Inactive");
        lblStatusBadge.setText(active ? "Active" : "Inactive");
        lblStatusBadge.setStyle(
                active
                        ? "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 8;"
                        : "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 8;"
        );
    }

    private void loadRegions() {
        cbRegion.setItems(FXCollections.observableArrayList(
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", "Jendouba",
                "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia", "La Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana",
                "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));
    }

    private void loadDelegationsMap() {
        delegationsByRegion.put("Tunis", List.of("Bab Bhar", "Bab Souika", "Cité El Khadra", "Djebel Jelloud", "El Kabaria", "El Menzah", "El Omrane", "El Omrane Supérieur", "Essijoumi", "Ettahrir", "La Goulette", "La Marsa", "Le Bardo", "Le Kram", "Médina", "Sidi El Béchir", "Sidi Hassine"));
        delegationsByRegion.put("Ariana", List.of("Ariana Ville", "Ettadhamen", "Kalaat El Andalous", "La Soukra", "Mnihla", "Raoued", "Sidi Thabet"));
        delegationsByRegion.put("Ben Arous", List.of("Ben Arous", "Bou Mhel", "Ezzahra", "Fouchana", "Hammam Chott", "Hammam Lif", "Mégrine", "Mornag", "Mohamedia", "El Mourouj", "Radès"));
        delegationsByRegion.put("Bizerte", List.of("Bizerte Nord", "Bizerte Sud", "El Alia", "Ghar El Melh", "Ghezala", "Joumine", "Mateur", "Menzel Bourguiba", "Menzel Jemil", "Ras Jebel", "Sejnane", "Tinja", "Utique", "Zarzouna"));
        delegationsByRegion.put("Béja", List.of("Béja Nord", "Béja Sud", "Amdoun", "Goubellat", "Medjez El Bab", "Nefza", "Téboursouk", "Testour", "Thibar"));
        delegationsByRegion.put("Gabès", List.of("Gabès Ville", "Gabès Ouest", "Gabès Sud", "Ghannouch", "El Hamma", "Matmata", "Mareth", "Métouia", "Nouvelle Matmata"));
        delegationsByRegion.put("Gafsa", List.of("Gafsa Nord", "Gafsa Sud", "Belkhir", "El Guettar", "El Ksar", "Mdhilla", "Métlaoui", "Moularès", "Redeyef", "Sened", "Sidi Aïch"));
        delegationsByRegion.put("Jendouba", List.of("Jendouba", "Aïn Draham", "Balta-Bou Aouane", "Bou Salem", "Fernana", "Ghardimaou", "Jendouba Nord", "Tabarka", "Oued Mliz"));
        delegationsByRegion.put("Kairouan", List.of("Kairouan Nord", "Kairouan Sud", "Bou Hajla", "Chebika", "Chrarda", "Haffouz", "Hajeb El Ayoun", "Nasrallah", "Oueslatia", "Sbikha"));
        delegationsByRegion.put("Kasserine", List.of("Kasserine Nord", "Kasserine Sud", "Ezzouhour", "Fériana", "Foussana", "Haïdra", "Hassi El Ferid", "Jedelienne", "Majel Bel Abbès", "Sbeitla", "Sbiba", "Thala"));
        delegationsByRegion.put("Kébili", List.of("Kébili Nord", "Kébili Sud", "Douz Nord", "Douz Sud", "Faouar", "Souk Lahad"));
        delegationsByRegion.put("Le Kef", List.of("Le Kef Est", "Le Kef Ouest", "Dahmani", "Jerissa", "Kalaat Senan", "Kalaat Khasba", "Nebeur", "Sakiet Sidi Youssef", "Tajerouine", "Touiref"));
        delegationsByRegion.put("Mahdia", List.of("Mahdia", "Bou Merdes", "Chebba", "Chorbane", "El Jem", "Hbira", "Ksour Essef", "Melloulèche", "Ouled Chamekh", "Sidi Alouane", "Souassi"));
        delegationsByRegion.put("La Manouba", List.of("La Manouba", "Borj El Amri", "Douar Hicher", "El Battan", "Jedaida", "Mornaguia", "Oued Ellil", "Tebourba"));
        delegationsByRegion.put("Médenine", List.of("Médenine Nord", "Médenine Sud", "Beni Khedache", "Ben Guerdane", "Djerba Ajim", "Djerba Houmt Souk", "Djerba Midoun", "Sidi Makhlouf", "Zarzis"));
        delegationsByRegion.put("Monastir", List.of("Monastir", "Bekalta", "Bembla", "Beni Hassen", "Jemmal", "Ksar Hellal", "Ksibet El Mediouni", "Moknine", "Ouerdanine", "Sahline", "Sayada-Lamta-Bou Hajar", "Téboulba", "Zéramdine"));
        delegationsByRegion.put("Nabeul", List.of("Nabeul", "Béni Khalled", "Béni Khiar", "Bou Argoub", "Dar Chaâbane El Fehri", "El Haouaria", "Grombalia", "Hammam Ghezèze", "Hammamet", "Kélibia", "Korba", "Menzel Bouzelfa", "Menzel Temime", "Soliman", "Takelsa"));
        delegationsByRegion.put("Sfax", List.of("Sfax Ville", "Sfax Ouest", "Sfax Sud", "Sakiet Eddaïer", "Sakiet Ezzit", "Agareb", "Bir Ali Ben Khalifa", "El Amra", "El Hencha", "Jebiniana", "Kerkennah", "Mahres", "Menzel Chaker", "Sidi Aïch", "Skhira", "Thyna"));
        delegationsByRegion.put("Sidi Bouzid", List.of("Sidi Bouzid Ouest", "Sidi Bouzid Est", "Bir El Hafey", "Cebbala Ouled Asker", "Jilma", "Meknassy", "Menzel Bouzaiane", "Mezzouna", "Ouled Haffouz", "Regueb", "Sabalat Ouled Asker", "Souk Jedid"));
        delegationsByRegion.put("Siliana", List.of("Siliana Nord", "Siliana Sud", "Bargou", "Bouarada", "El Aroussa", "Gaâfour", "Kesra", "Makthar", "Rouhia", "Sidi Bou Rouis"));
        delegationsByRegion.put("Sousse", List.of("Sousse Ville", "Akouda", "Bouficha", "Enfidha", "Hammam Sousse", "Hergla", "Kalâa Kebira", "Kalâa Sghira", "Kondar", "Msaken", "Sidi Bou Ali", "Sidi El Hani"));
        delegationsByRegion.put("Tataouine", List.of("Tataouine Nord", "Tataouine Sud", "Bir Lahmar", "Dhehiba", "Ghomrassen", "Remada", "Smar"));
        delegationsByRegion.put("Tozeur", List.of("Tozeur", "Degache", "Hazoua", "Nefta", "Tameghza"));
        delegationsByRegion.put("Zaghouan", List.of("Zaghouan", "Bir Mcherga", "El Fahs", "Nadhour", "Saouaf", "Zriba"));
    }

    @FXML
    void onRegionChanged() {
        String selectedRegion = cbRegion.getValue();

        if (selectedRegion != null && delegationsByRegion.containsKey(selectedRegion)) {
            cbDelegation.setItems(FXCollections.observableArrayList(delegationsByRegion.get(selectedRegion)));
            cbDelegation.setDisable(false);
            cbDelegation.setPromptText("Choose delegation");
        } else {
            cbDelegation.getItems().clear();
            cbDelegation.setDisable(true);
            cbDelegation.setPromptText("Choose a region first");
        }
    }

    @FXML
    void saveChanges() {
        try {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String region = cbRegion.getValue();
            String delegation = cbDelegation.getValue();
            String newPassword = pfNewPassword.getText().trim();
            boolean active = chkActive.isSelected();

            if (name.isEmpty() || email.isEmpty() || region == null || delegation == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
                return;
            }

            if (!phone.isEmpty() && !phone.matches("\\d{8}")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Phone must contain exactly 8 digits.");
                return;
            }

            if (!newPassword.isEmpty() && newPassword.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Error", "New password must contain at least 6 characters.");
                return;
            }

            if (userService.emailExistsForAnotherUser(email, user.getId())) {
                showAlert(Alert.AlertType.ERROR, "Error", "This email is already used by another account.");
                return;
            }

            if (userService.municipalDelegationExistsForAnotherUser(region, delegation, user.getId())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Cette délégation a déjà un agent municipal.");
                return;
            }

            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRegion(region);
            user.setDelegation(delegation);
            user.setActive(active);

            userService.updateMunicipalAgent(user);

            if (!newPassword.isEmpty()) {
                userService.updatePassword(user.getEmail(), newPassword);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Municipal agent updated successfully.");
            backToMunicipalAgents();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    void backToMunicipalAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/municipal_agents.fxml"));
            Parent root = loader.load();

            MunicipalAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Municipal Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToSubscribers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/subscribers.fxml"));
            Parent root = loader.load();

            SubscribersController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Subscribers");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/home.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
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
    @FXML
    void goToMunicipalAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/municipal_agents.fxml"));
            Parent root = loader.load();

            MunicipalAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Municipal Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToFieldAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/field_agents.fxml"));
            Parent root = loader.load();

            FieldAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Field Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/categories.fxml"));
            Parent root = loader.load();

            CategoriesController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Catégories");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void goToAssociation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_association/association.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("associaitons");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToDonation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/donation/donationIndex.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("donations");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
