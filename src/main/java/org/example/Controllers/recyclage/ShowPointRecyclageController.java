package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.Entities.PointRecyclage;
import org.example.Services.PointRecyclageService;

import java.io.IOException;
import java.sql.SQLException;

public class ShowPointRecyclageController {

    @FXML private Label lblTitle;
    @FXML private Label lblAddressTop;
    @FXML private Label lblStatut;
    @FXML private Label lblCategorie;
    @FXML private Label lblQuantite;
    @FXML private Label lblDate;
    @FXML private Label lblDescription;
    @FXML private Label lblCoords;
    @FXML private WebView mapView;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private PointRecyclage point;

    public void setPointId(int id) {
        try {
            point = pointService.getPointById(id);
            if (point != null) {
                loadData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        lblTitle.setText("Point #" + point.getId());
        lblAddressTop.setText(point.getAddress());
        lblStatut.setText(point.getStatut());
        lblCategorie.setText(point.getCategorie() != null ? point.getCategorie().getNom() : "-");
        lblQuantite.setText(point.getQuantite() + " kg");
        lblDate.setText(point.getDateDec() != null ? point.getDateDec().toString() : "-");
        lblDescription.setText(point.getDescription() == null || point.getDescription().isEmpty() ? "-" : point.getDescription());
        lblCoords.setText("Coordonnées : " + point.getLatitude() + ", " + point.getLongitude());

        initMap(point.getLatitude(), point.getLongitude());
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
                    var map = L.map('map').setView([%LAT%, %LNG%], 15);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                      maxZoom: 19,
                      attribution: '&copy; OpenStreetMap contributors'
                    }).addTo(map);
                    L.marker([%LAT%, %LNG%]).addTo(map);
                  </script>
                </body>
                </html>
                """
                .replace("%LAT%", String.valueOf(lat))
                .replace("%LNG%", String.valueOf(lng));

        engine.loadContent(html);
    }

    @FXML
    void backToList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/recyclage/points_connected.fxml"));
            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToEdit() {
        if (point == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/edit_point_connected.fxml"));
            Parent root = loader.load();

            EditPointRecyclageController controller = loader.getController();
            controller.setPointId(point.getId());

            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}