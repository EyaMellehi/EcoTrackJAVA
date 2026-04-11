package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;

import java.io.IOException;
import java.sql.SQLException;

public class ShowPointRecyclageController {

    @FXML private NavbarCitoyenController navbarController;

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
        loadData();
    }

    private void loadData() {
        if (currentPoint == null) return;

        lblTitle.setText("Point #" + currentPoint.getId());
        lblAddressTop.setText(currentPoint.getAddress());
        lblStatut.setText(currentPoint.getStatut());
        lblCategorie.setText(currentPoint.getCategorie() != null ? currentPoint.getCategorie().getNom() : "-");
        lblQuantite.setText(currentPoint.getQuantite() + " kg");
        lblDate.setText(currentPoint.getDateDec() != null ? currentPoint.getDateDec().toString() : "-");
        lblDescription.setText(currentPoint.getDescription() != null ? currentPoint.getDescription() : "-");
        lblCoords.setText("Coordonnées : " + currentPoint.getLatitude() + ", " + currentPoint.getLongitude());

        loadMap();
    }

    private void loadMap() {
        String html =
                "<html><head>" +
                        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                        "</head><body style='margin:0;'>" +
                        "<div id='map' style='width:100%;height:100%;'></div>" +
                        "<script>" +
                        "var map=L.map('map').setView([" + currentPoint.getLatitude() + "," + currentPoint.getLongitude() + "],15);" +
                        "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'&copy; OpenStreetMap contributors'}).addTo(map);" +
                        "L.marker([" + currentPoint.getLatitude() + "," + currentPoint.getLongitude() + "]).addTo(map);" +
                        "</script></body></html>";

        WebEngine engine = mapView.getEngine();
        engine.loadContent(html);
    }

    @FXML
    void backToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/points_connected.fxml"));
            Parent root = loader.load();

            PointsConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/edit_point_recyclage.fxml"));
            Parent root = loader.load();

            EditPointRecyclageController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setPoint(currentPoint);

            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}