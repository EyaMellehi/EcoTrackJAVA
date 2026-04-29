package org.example.Controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Services.EmailService;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFieldAgentController {

    @FXML private MenuButton menuAdmin;

    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfPhone;
    @FXML private ComboBox<String> cbRegion;
    @FXML private ComboBox<String> cbDelegation;
    @FXML private CheckBox chkActive;

    private final EmailService emailService = new EmailService();
    private final UserService userService = new UserService();
    private User loggedUser;

    private final Map<String, List<String>> delegationsByRegion = new HashMap<>();

    @FXML
    public void initialize() {
        loadRegions();
        loadDelegationsMap();

        chkActive.setSelected(true);
        cbDelegation.setDisable(true);

        tfPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tfPhone.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (tfPhone.getText().length() > 8) {
                tfPhone.setText(tfPhone.getText().substring(0, 8));
            }
        });
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        if (user != null && user.getName() != null) {
            menuAdmin.setText(user.getName());
        }
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
    void saveFieldAgent() {
        try {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String password = pfPassword.getText().trim();
            String phone = tfPhone.getText().trim();
            String region = cbRegion.getValue();
            String delegation = cbDelegation.getValue();
            boolean active = chkActive.isSelected();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || region == null || delegation == null) {
                ModernNotification.showError(getCurrentStage(), "Error", "Please fill all required fields.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ModernNotification.showError(getCurrentStage(), "Error", "Invalid email format.");
                return;
            }

            if (password.length() < 6) {
                ModernNotification.showError(getCurrentStage(), "Error", "Password must contain at least 6 characters.");
                return;
            }

            if (!phone.isEmpty() && !phone.matches("\\d{8}")) {
                ModernNotification.showError(getCurrentStage(), "Error", "Phone must contain exactly 8 digits.");
                return;
            }

            if (userService.emailExists(email)) {
                ModernNotification.showError(getCurrentStage(), "Error", "Email already exists.");
                return;
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setPhone(phone);
            user.setRegion(region);
            user.setDelegation(delegation);
            user.setActive(active);
            user.setImage(null);

            userService.addFieldAgent(user);

            try {
                emailService.sendFieldAgentCredentials(email, name, password);
                ModernNotification.showSuccess(getCurrentStage(), "Success", "Field agent added and email sent successfully.");
            } catch (Exception mailEx) {
                mailEx.printStackTrace();
                ModernNotification.showWarning(getCurrentStage(), "Warning", "Field agent added, but email was not sent.");
            }

            backToFieldAgents();

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Error", e.getMessage());
        }
    }

    @FXML
    void backToFieldAgents() {
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