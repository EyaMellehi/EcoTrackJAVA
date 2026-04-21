package org.example.Controllers.User;

import javafx.scene.web.WebView;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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

            if (recaptchaToken == null || recaptchaToken.isBlank()) {
                showAlert("Erreur", "Veuillez valider le reCAPTCHA.");
                return;
            }

            boolean captchaValid = userService.verifyRecaptcha(recaptchaToken, RECAPTCHA_SECRET_KEY);

            if (!captchaValid) {
                showAlert("Erreur", "Échec de vérification reCAPTCHA.");
                return;
            }
            userService.registerCitoyen(user);

            showAlert("Succès", "Compte créé avec succès");

            Stage stage = (Stage) tfName.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/user/login.fxml")));
            stage.setScene(scene);
            stage.setMaximized(true);
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
            stage.setMaximized(true);
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
    public class CaptchaBridge {
        public void setToken(String token) {
            recaptchaToken = token;
        }
    }
}