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
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.Categorie;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.CategorieService;
import org.example.Services.PointRecyclageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AddPointRecyclageController {

    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfAddress;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextArea taDescription;
    @FXML private Label lblPickInfo;
    @FXML private WebView mapView;
    @FXML private NavbarCitoyenController navbarController;

    @FXML private HBox navbar;


    private final CategorieService categorieService = new CategorieService();
    private final PointRecyclageService pointService = new PointRecyclageService();

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
        tfLatitude.setText("36.806500");
        tfLongitude.setText("10.181500");
        lblPickInfo.setText("Point : 36.806500, 10.181500");
        loadAddressFromCoordinates(36.8065, 10.1815);

        if (navbarController != null && loggedUser != null) {
            navbarController.setLoggedUser(loggedUser);
        }
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.getAllCategories();
            cbCategorie.getItems().setAll(categories);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories.");
            e.printStackTrace();
        }
    }

    private void initMap() {
        WebEngine engine = mapView.getEngine();

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                  <style>
                    html, body, #map { height: 100%; margin: 0; }
                  </style>
                </head>
                <body>
                  <div id="map"></div>
                  <script>
                    var map = L.map('map').setView([36.8065, 10.1815], 12);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                      maxZoom: 19,
                      attribution: '&copy; OpenStreetMap contributors'
                    }).addTo(map);

                    var marker = L.marker([36.8065, 10.1815], {draggable:true}).addTo(map);

                    function notify(lat, lng){
                      if(window.javaConnector){
                        window.javaConnector.updatePosition(lat, lng);
                      }
                    }

                    map.on('click', function(e){
                      marker.setLatLng(e.latlng);
                      notify(e.latlng.lat, e.latlng.lng);
                    });

                    marker.on('dragend', function(){
                      var p = marker.getLatLng();
                      notify(p.lat, p.lng);
                    });

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
            } catch (Exception ignored) {
            }
        });
    }

    public class JavaConnector {
        public void updatePosition(double lat, double lng) {
            Platform.runLater(() -> {
                tfLatitude.setText(String.format("%.6f", lat));
                tfLongitude.setText(String.format("%.6f", lng));
                lblPickInfo.setText("Point : " + String.format("%.6f", lat) + ", " + String.format("%.6f", lng));
            });
            loadAddressFromCoordinates(lat, lng);
        }
    }

    private void loadAddressFromCoordinates(double lat, double lng) {
        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&addressdetails=1&accept-language=fr"
                        + "&lat=" + lat + "&lon=" + lng;

                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-Agent", "EcoTrackJavaFX/1.0");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String json = response.toString();
                String marker = "\"display_name\":\"";
                int start = json.indexOf(marker);

                if (start != -1) {
                    start += marker.length();
                    int end = json.indexOf("\"", start);
                    if (end != -1) {
                        String address = json.substring(start, end)
                                .replace("\\/", "/")
                                .replace("\\u00e9", "é")
                                .replace("\\u00e8", "è")
                                .replace("\\u00e0", "à")
                                .replace("\\u00f4", "ô")
                                .replace("\\u00e7", "ç");

                        Platform.runLater(() -> tfAddress.setText(address));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void savePoint() {
        Categorie categorie = cbCategorie.getValue();
        String quantiteText = tfQuantite.getText().trim();
        String address = tfAddress.getText().trim();
        String latitudeText = tfLatitude.getText().trim();
        String longitudeText = tfLongitude.getText().trim();
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

            pointService.addPoint(point);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Point ajouté avec succès.");
            backToList();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le point.");
            e.printStackTrace();
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