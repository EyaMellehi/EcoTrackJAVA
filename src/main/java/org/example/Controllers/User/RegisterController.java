package org.example.Controllers.User;

import org.example.Entities.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Services.UserService;
import org.example.Utils.ModernNotification;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class RegisterController {

    @FXML
    private TextField tfName;

    @FXML
    private WebView captchaWebView;

    @FXML
    private TextField tfPhone;

    @FXML
    private TextField tfEmail;

    private HttpServer localCaptchaServer;
    private static final int CAPTCHA_PORT = 9091;

    @FXML
    private ComboBox<String> cbRegion;

    @FXML
    private PasswordField pfPassword;

    private final UserService userService = new UserService();

    private String recaptchaToken;
    private static final String RECAPTCHA_SITE_KEY = "6Lci0cIsAAAAABi8cuxVVOyIk_H1_2OJHXkYVDxC";
    private static final String RECAPTCHA_SECRET_KEY = "6Lci0cIsAAAAACJBA8GHnCXNmpoDIiUuo4TkOFyM";

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

        loadRecaptcha();
    }

    private void startLocalCaptchaServer() {
        try {
            if (localCaptchaServer != null) {
                return;
            }

            localCaptchaServer = HttpServer.create(new InetSocketAddress("127.0.0.1", CAPTCHA_PORT), 0);

            localCaptchaServer.createContext("/captcha.html", exchange -> {
                try (InputStream is = getClass().getResourceAsStream("/web/captcha.html")) {
                    if (is == null) {
                        String notFound = "captcha.html not found";
                        exchange.sendResponseHeaders(404, notFound.getBytes(StandardCharsets.UTF_8).length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(notFound.getBytes(StandardCharsets.UTF_8));
                        }
                        return;
                    }

                    byte[] html = is.readAllBytes();
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, html.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(html);
                    }
                }
            });

            localCaptchaServer.createContext("/token", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("value=")) {
                    String value = query.substring("value=".length());
                    recaptchaToken = URLDecoder.decode(value, StandardCharsets.UTF_8);
                }

                byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });

            localCaptchaServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Erreur", "Impossible de démarrer le serveur local du captcha.");
        }
    }

    private void loadRecaptcha() {
        if (captchaWebView == null) {
            return;
        }

        startLocalCaptchaServer();

        WebEngine engine = captchaWebView.getEngine();
        engine.load("http://127.0.0.1:" + CAPTCHA_PORT + "/captcha.html");
    }

    @FXML
    void register() {
        try {
            String name = tfName.getText().trim();
            String phone = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();
            String region = cbRegion.getValue();
            String password = pfPassword.getText().trim();

            if (name.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le nom est obligatoire.");
                return;
            }

            if (name.length() < 3) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le nom doit contenir au moins 3 caractères.");
                return;
            }

            if (!name.matches("[A-Za-zÀ-ÿ\\s]+")) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le nom doit contenir uniquement des lettres et des espaces.");
                return;
            }

            if (phone.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le téléphone est obligatoire.");
                return;
            }

            if (!phone.matches("\\d{8}")) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le téléphone doit contenir exactement 8 chiffres.");
                return;
            }

            if (email.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "L'email est obligatoire.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Format d'email invalide.");
                return;
            }

            if (region == null || region.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Veuillez sélectionner une région.");
                return;
            }

            if (password.isEmpty()) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le mot de passe est obligatoire.");
                return;
            }

            if (password.length() < 6) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }

            if (userService.emailExists(email)) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Email déjà existant.");
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setName(name);
            user.setPhone(phone);
            user.setRegion(region);

            if (recaptchaToken == null || recaptchaToken.isBlank()) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Veuillez valider le reCAPTCHA.");
                return;
            }

            boolean captchaValid = userService.verifyRecaptcha(recaptchaToken, RECAPTCHA_SECRET_KEY);

            if (!captchaValid) {
                ModernNotification.showError(getCurrentStage(), "Erreur", "Échec de vérification reCAPTCHA.");
                return;
            }

            userService.registerCitoyen(user);

            Stage stage = getCurrentStage();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/user/login.fxml")));
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Login");
            stage.show();

            ModernNotification.showSuccess(stage, "Succès", "Compte créé avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Erreur", e.getMessage());
        }
    }

    @FXML
    void goToLogin() {
        try {
            Stage stage = getCurrentStage();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/user/login.fxml")));
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Erreur", e.getMessage());
        }
    }

    private Stage getCurrentStage() {
        return tfName != null && tfName.getScene() != null
                ? (Stage) tfName.getScene().getWindow()
                : null;
    }

    public class CaptchaBridge {
        public void setToken(String token) {
            recaptchaToken = token;
        }
    }
}