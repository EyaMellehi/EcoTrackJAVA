package org.example.Controllers.recyclage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.Categorie;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.AiEstimateResult;
import org.example.Services.CategorieService;
import org.example.Services.CohereRecyclageEstimator;
import org.example.Services.PointRecyclageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class AddPointRecyclageController {

    @FXML private HBox navbar;
    @FXML private NavbarCitoyenController navbarController;

    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfAddress;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextArea taDescription;
    @FXML private Label lblPickInfo;
    @FXML private WebView mapView;

    private volatile long geocodeRequestId = 0;
    private volatile String lastResolvedAddress = "";

    private final CategorieService categorieService = new CategorieService();
    private final PointRecyclageService pointService = new PointRecyclageService();
    private final CohereRecyclageEstimator cohereEstimator = new CohereRecyclageEstimator();

    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }
    }

    @FXML
    public void initialize() {
        loadCategories();
        initMap();

        tfLatitude.setText(String.format(Locale.US, "%.6f", 36.8065));
        tfLongitude.setText(String.format(Locale.US, "%.6f", 10.1815));
        lblPickInfo.setText("Point : "
                + String.format(Locale.US, "%.6f", 36.8065)
                + ", "
                + String.format(Locale.US, "%.6f", 10.1815));

        loadAddressFromCoordinates(36.8065, 10.1815);
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.getAllCategories();
            cbCategorie.getItems().setAll(categories);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories.");
        }
    }

    private void initMap() {
        mapView.setContextMenuEnabled(false);

        WebEngine engine = mapView.getEngine();

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">

              <script>
                var L_DISABLE_3D = true;
              </script>

              <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
              <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

              <style>
                html, body {
                  width: 100%;
                  height: 100%;
                  margin: 0;
                  padding: 0;
                  overflow: hidden;
                  background: white;
                }

                #map {
                  width: 100%;
                  height: 100%;
                  margin: 0;
                  padding: 0;
                  background: #e5e7eb;
                }

                .leaflet-container {
                  width: 100% !important;
                  height: 100% !important;
                  background: #e5e7eb;
                }

                .leaflet-tile,
                .leaflet-pane,
                .leaflet-map-pane,
                .leaflet-tile-container,
                .leaflet-zoom-animated {
                  transform: none !important;
                  -webkit-transform: none !important;
                }
              </style>
            </head>
            <body>
              <div id="map"></div>

              <script>
                var map = L.map('map', {
                  preferCanvas: true,
                  zoomAnimation: false,
                  fadeAnimation: false,
                  markerZoomAnimation: false,
                  inertia: false
                }).setView([36.8065, 10.1815], 12);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                  maxZoom: 19,
                  attribution: '&copy; OpenStreetMap contributors',
                  updateWhenZooming: false,
                  updateWhenIdle: true,
                  keepBuffer: 1
                }).addTo(map);

                var marker = L.marker([36.8065, 10.1815], {
                  draggable: true
                }).addTo(map);

                function notify(lat, lng) {
                  if (window.javaConnector) {
                    window.javaConnector.updatePosition(lat, lng);
                  }
                }

                map.on('click', function(e) {
                  marker.setLatLng(e.latlng);
                  notify(e.latlng.lat, e.latlng.lng);
                });

                marker.on('dragend', function() {
                  var p = marker.getLatLng();
                  notify(p.lat, p.lng);
                });

                setTimeout(function () {
                  map.invalidateSize();
                }, 300);

                notify(36.8065, 10.1815);
              </script>
            </body>
            </html>
            """;

        engine.loadContent(html);

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            try {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
                engine.executeScript("setTimeout(function(){ map.invalidateSize(); }, 500);");
            } catch (Exception ignored) {
            }
        });
    }

    public class JavaConnector {
        public void updatePosition(double lat, double lng) {
            Platform.runLater(() -> {
                tfLatitude.setText(String.format(Locale.US, "%.6f", lat));
                tfLongitude.setText(String.format(Locale.US, "%.6f", lng));
                lblPickInfo.setText("Point : "
                        + String.format(Locale.US, "%.6f", lat)
                        + ", "
                        + String.format(Locale.US, "%.6f", lng));
            });

            loadAddressFromCoordinates(lat, lng);
        }
    }

    private void loadAddressFromCoordinates(double lat, double lng) {
        final long requestId = ++geocodeRequestId;

        Platform.runLater(() -> tfAddress.setText("Chargement de l'adresse..."));

        Thread thread = new Thread(() -> {
            HttpURLConnection con = null;
            BufferedReader in = null;

            try {
                String url = "https://nominatim.openstreetmap.org/reverse"
                        + "?format=jsonv2"
                        + "&addressdetails=1"
                        + "&accept-language=fr"
                        + "&lat=" + lat
                        + "&lon=" + lng;

                con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-Agent", "EcoTrackJavaFX/1.0");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int responseCode = con.getResponseCode();
                if (responseCode != 200) {
                    if (requestId == geocodeRequestId) {
                        Platform.runLater(() -> {
                            if (!lastResolvedAddress.isEmpty()) tfAddress.setText(lastResolvedAddress);
                            else tfAddress.setText("");
                        });
                    }
                    return;
                }

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                String json = response.toString();
                String address = extractDisplayName(json);

                if (address == null || address.isBlank()) {
                    if (requestId == geocodeRequestId) {
                        Platform.runLater(() -> {
                            if (!lastResolvedAddress.isEmpty()) tfAddress.setText(lastResolvedAddress);
                            else tfAddress.setText("");
                        });
                    }
                    return;
                }

                if (requestId == geocodeRequestId) {
                    lastResolvedAddress = address;
                    Platform.runLater(() -> tfAddress.setText(address));
                }

            } catch (Exception e) {
                if (requestId == geocodeRequestId) {
                    Platform.runLater(() -> {
                        if (!lastResolvedAddress.isEmpty()) tfAddress.setText(lastResolvedAddress);
                        else tfAddress.setText("");
                    });
                }
            } finally {
                try {
                    if (in != null) in.close();
                } catch (Exception ignored) {
                }
                if (con != null) con.disconnect();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private String extractDisplayName(String json) {
        String marker = "\"display_name\":\"";
        int start = json.indexOf(marker);

        if (start == -1) {
            return null;
        }

        start += marker.length();

        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);

            if (escaped) {
                switch (ch) {
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    case '/' -> result.append('/');
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                result.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                result.append("\\u").append(hex);
                                i += 4;
                            }
                        }
                    }
                    default -> result.append(ch);
                }
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                continue;
            }

            if (ch == '"') {
                break;
            }

            result.append(ch);
        }

        return result.toString().trim();
    }

    @FXML
    private void openChatbotDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/chatbot_dialog.fxml"));
            Parent root = loader.load();

            ChatbotDialogController controller = loader.getController();
            controller.setTargetDescriptionArea(taDescription);

            java.util.Map<String, String> ctx = new java.util.HashMap<>();
            ctx.put("categorie", cbCategorie.getValue() != null ? cbCategorie.getValue().getNom() : "");
            ctx.put("quantite", tfQuantite.getText() != null ? tfQuantite.getText().trim() : "");

            controller.setMode("POINT_DESC");
            controller.setContext(ctx);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Assistant rédaction");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d’ouvrir l’assistant IA.");
        }
    }

    @FXML
    void savePoint() {
        Categorie categorie = cbCategorie.getValue();
        String quantiteText = tfQuantite.getText().trim();
        String address = tfAddress.getText().trim();
        String latitudeText = tfLatitude.getText().trim().replace(",", ".");
        String longitudeText = tfLongitude.getText().trim().replace(",", ".");
        String description = taDescription.getText().trim();

        if (loggedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            return;
        }

        if (categorie == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La catégorie est obligatoire.");
            return;
        }

        double quantite;
        try {
            quantite = Double.parseDouble(quantiteText);
            if (quantite <= 0 || quantite > 100000) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La quantité doit être > 0 et <= 100000.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Quantité invalide.");
            return;
        }

        if (address.isEmpty() || address.length() < 5 || address.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "L'adresse doit contenir entre 5 et 255 caractères.");
            return;
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(latitudeText);
            longitude = Double.parseDouble(longitudeText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Latitude ou longitude invalide.");
            return;
        }

        if (latitude < 30 || latitude > 38) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Latitude invalide pour la Tunisie.");
            return;
        }

        if (longitude < 7 || longitude > 12) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Longitude invalide pour la Tunisie.");
            return;
        }

        if (description.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La description ne doit pas dépasser 255 caractères.");
            return;
        }

        try {
            PointRecyclage point = new PointRecyclage();
            point.setCategorie(categorie);
            point.setQuantite(quantite);
            point.setAddress(address);
            point.setLatitude(latitude);
            point.setLongitude(longitude);
            point.setDescription(description.isEmpty() ? null : description);
            point.setDateDec(LocalDate.now());
            point.setStatut("PENDING");
            point.setCitoyen(loggedUser);
            point.setAgentTerrain(null);

            try {
                AiEstimateResult estimate = cohereEstimator.estimate(point);
                point.setAiScore(estimate.getScore());
                point.setAiPriority(estimate.getPriority());
                point.setAiExplanation(estimate.getExplanation());
                point.setAiEstimatedAt(LocalDateTime.now());
            } catch (Exception aiEx) {
                System.err.println("AI estimation failed: " + aiEx.getMessage());
                point.setAiScore(null);
                point.setAiPriority(null);
                point.setAiExplanation(null);
                point.setAiEstimatedAt(null);
            }

            pointService.addPoint(point);

            String successMessage = "Point ajouté avec succès.";
            if (point.getAiPriority() != null) {
                successMessage += "\nPriorité AI : " + point.getAiPriority()
                        + " (score " + point.getAiScore() + ")";
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", successMessage);
            backToList();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le point.");
        }
    }

    @FXML
    void backToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/points_connected.fxml"));
            Parent root = loader.load();

            PointsConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) cbCategorie.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}