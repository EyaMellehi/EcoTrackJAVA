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
import org.example.Entities.Categorie;
import org.example.Entities.PointRecyclage;
import org.example.Services.CategorieService;
import org.example.Services.PointRecyclageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class EditPointRecyclageController {

    @FXML private Label lblTitle;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfAddress;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextArea taDescription;
    @FXML private Label lblPickInfo;
    @FXML private WebView mapView;

    private final CategorieService categorieService = new CategorieService();
    private final PointRecyclageService pointService = new PointRecyclageService();

    private PointRecyclage point;

    @FXML
    public void initialize() {
        loadCategories();
    }

    public void setPointId(int id) {
        try {
            point = pointService.getPointById(id);
            if (point != null) {
                fillForm();
                initMap(point.getLatitude(), point.getLongitude());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.getAllCategories();
            cbCategorie.getItems().setAll(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillForm() {
        lblTitle.setText("Modifier point #" + point.getId());
        cbCategorie.setValue(point.getCategorie());
        tfQuantite.setText(String.valueOf(point.getQuantite()));
        tfAddress.setText(point.getAddress());
        tfLatitude.setText(String.format("%.6f", point.getLatitude()));
        tfLongitude.setText(String.format("%.6f", point.getLongitude()));
        taDescription.setText(point.getDescription());
        lblPickInfo.setText("Position actuelle chargée.");
    }

    private void initMap(double lat, double lng) {
        WebEngine engine = mapView.getEngine();

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                  <style> html, body, #map { height:100%; margin:0; } </style>
                </head>
                <body>
                  <div id="map"></div>
                  <script>
                    var map = L.map('map').setView([%LAT%, %LNG%], 13);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                      maxZoom: 19,
                      attribution: '&copy; OpenStreetMap contributors'
                    }).addTo(map);

                    var marker = L.marker([%LAT%, %LNG%], {draggable:true}).addTo(map);

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

                    notify(%LAT%, %LNG%);
                  </script>
                </body>
                </html>
                """
                .replace("%LAT%", String.valueOf(lat))
                .replace("%LNG%", String.valueOf(lng));

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
                lblPickInfo.setText("Position : " + String.format("%.6f", lat) + ", " + String.format("%.6f", lng));
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
        if (point == null) return;

        try {
            point.setCategorie(cbCategorie.getValue());
            point.setQuantite(Double.parseDouble(tfQuantite.getText().trim()));
            point.setAddress(tfAddress.getText().trim());
            point.setLatitude(Double.parseDouble(tfLatitude.getText().trim()));
            point.setLongitude(Double.parseDouble(tfLongitude.getText().trim()));
            point.setDescription(taDescription.getText().trim().isEmpty() ? null : taDescription.getText().trim());

            pointService.updatePoint(point);

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Succès");
            a.setHeaderText(null);
            a.setContentText("Point mis à jour avec succès.");
            a.showAndWait();

            backToList();

        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur");
            a.setHeaderText(null);
            a.setContentText("Impossible de mettre à jour le point.");
            a.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    void backToList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/recyclage/points_connected.fxml"));
            Stage stage = (Stage) cbCategorie.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}