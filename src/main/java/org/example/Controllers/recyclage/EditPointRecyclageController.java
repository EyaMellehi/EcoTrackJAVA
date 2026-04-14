package org.example.Controllers.recyclage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.List;
import java.util.Locale;

public class EditPointRecyclageController {

    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfAddress;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextArea taDescription;
    @FXML private Label lblPickInfo;
    @FXML private Label lblTitle;
    @FXML private WebView mapView;
    @FXML private NavbarCitoyenController navbarController;

    private final CategorieService categorieService = new CategorieService();
    private final PointRecyclageService pointService = new PointRecyclageService();

    private User loggedUser;
    private PointRecyclage currentPoint;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }
    }

    public void setPoint(PointRecyclage point) {
        this.currentPoint = point;

        if (point != null) {
            lblTitle.setText("Modifier point #" + point.getId());
            tfQuantite.setText(String.valueOf(point.getQuantite()));
            tfAddress.setText(point.getAddress());
            tfLatitude.setText(String.format(Locale.US, "%.6f", point.getLatitude()));
            tfLongitude.setText(String.format(Locale.US, "%.6f", point.getLongitude()));
            taDescription.setText(point.getDescription() != null ? point.getDescription() : "");
            lblPickInfo.setText("Point : "
                    + String.format(Locale.US, "%.6f", point.getLatitude())
                    + ", "
                    + String.format(Locale.US, "%.6f", point.getLongitude()));

            if (cbCategorie.getItems() != null) {
                cbCategorie.getSelectionModel().select(point.getCategorie());
            }
        }
    }

    @FXML
    public void initialize() {
        loadCategories();
        initMap();
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

                if (currentPoint != null) {
                    String script = "map.setView([" + currentPoint.getLatitude() + "," + currentPoint.getLongitude() + "], 15);"
                            + "marker.setLatLng([" + currentPoint.getLatitude() + "," + currentPoint.getLongitude() + "]);";
                    engine.executeScript(script);
                }
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
    void updatePoint() {
        if (loggedUser == null || currentPoint == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté ou point introuvable.");
            return;
        }

        try {
            currentPoint.setCategorie(cbCategorie.getValue());
            currentPoint.setQuantite(Double.parseDouble(tfQuantite.getText().trim()));
            currentPoint.setAddress(tfAddress.getText().trim());
            currentPoint.setLatitude(Double.parseDouble(tfLatitude.getText().trim().replace(",", ".")));
            currentPoint.setLongitude(Double.parseDouble(tfLongitude.getText().trim().replace(",", ".")));
            currentPoint.setDescription(taDescription.getText().trim());

            pointService.updatePoint(currentPoint);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Point mis à jour avec succès.");
            backToList();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le point.");
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