package org.example.Controllers.signalement;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.Entities.Media;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.MediaService;
import org.example.Services.SignalementService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddSignalementController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfAddresse;
    @FXML private TextField tfLatitude;
    @FXML private TextField tfLongitude;
    @FXML private TextField tfDelegation;
    @FXML private WebView mapView;
    @FXML private Label lblSelectedPhotos;
    @FXML private Label lblPickInfo;

    private final SignalementService signalementService = new SignalementService();
    private final MediaService mediaService = new MediaService();

    private User loggedUser;
    private final List<File> selectedPhotos = new ArrayList<>();

    private volatile long geocodeRequestId = 0;
    private volatile String lastResolvedAddress = "";

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    @FXML
    public void initialize() {
        cbType.getItems().addAll(
                "Waste",
                "Air Pollution",
                "Water Pollution",
                "Fire",
                "Other"
        );

        addNumericValidation(tfLatitude);
        addNumericValidation(tfLongitude);
        initMap();

        tfLatitude.setText(String.format(Locale.US, "%.6f", 36.8065));
        tfLongitude.setText(String.format(Locale.US, "%.6f", 10.1815));
        lblPickInfo.setText("Point: "
                + String.format(Locale.US, "%.6f", 36.8065)
                + ", "
                + String.format(Locale.US, "%.6f", 10.1815));

        loadAddressFromCoordinates(36.8065, 10.1815);
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
                    window.javaConnector.setLocationFromMap(lat, lng);
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

                window.setMarkerAndCenter = function(lat, lng) {
                  marker.setLatLng([lat, lng]);
                  map.setView([lat, lng], 15);
                }

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
        public void setLocationFromMap(double lat, double lng) {
            Platform.runLater(() -> {
                tfLatitude.setText(String.format(Locale.US, "%.6f", lat));
                tfLongitude.setText(String.format(Locale.US, "%.6f", lng));
                lblPickInfo.setText("Point: "
                        + String.format(Locale.US, "%.6f", lat)
                        + ", "
                        + String.format(Locale.US, "%.6f", lng));
            });

            loadAddressFromCoordinates(lat, lng);
        }
    }

    private void loadAddressFromCoordinates(double lat, double lng) {
        final long requestId = ++geocodeRequestId;

        Platform.runLater(() -> tfAddresse.setText("Loading address..."));

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
                            tfAddresse.setText(lastResolvedAddress);
                            String delegation = extractDelegationFromAddress(lastResolvedAddress);
                            if (!delegation.isBlank()) {
                                tfDelegation.setText(delegation);
                            }
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
                            tfAddresse.setText(lastResolvedAddress);
                            String delegation = extractDelegationFromAddress(lastResolvedAddress);
                            if (!delegation.isBlank()) {
                                tfDelegation.setText(delegation);
                            }
                        });
                    }
                    return;
                }

                if (requestId == geocodeRequestId) {
                    lastResolvedAddress = address;
                    Platform.runLater(() -> {
                        tfAddresse.setText(address);

                        String delegation = extractDelegationFromAddress(address);
                        if (!delegation.isBlank()) {
                            tfDelegation.setText(delegation);
                        }
                    });
                }

            } catch (Exception e) {
                if (requestId == geocodeRequestId) {
                    Platform.runLater(() -> {
                        tfAddresse.setText(lastResolvedAddress);
                        String delegation = extractDelegationFromAddress(lastResolvedAddress);
                        if (!delegation.isBlank()) {
                            tfDelegation.setText(delegation);
                        }
                    });
                }
            } finally {
                try {
                    if (in != null) in.close();
                } catch (Exception ignored) {
                }
                if (con != null) {
                    con.disconnect();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private String extractDelegationFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }

        String normalized = address.trim();
        String lower = normalized.toLowerCase();

        String marker1 = "délégation ";
        String marker2 = "delegation ";

        int idx = lower.indexOf(marker1);
        if (idx != -1) {
            String value = normalized.substring(idx + marker1.length()).trim();
            int comma = value.indexOf(",");
            return (comma != -1 ? value.substring(0, comma) : value).trim();
        }

        idx = lower.indexOf(marker2);
        if (idx != -1) {
            String value = normalized.substring(idx + marker2.length()).trim();
            int comma = value.indexOf(",");
            return (comma != -1 ? value.substring(0, comma) : value).trim();
        }

        String[] parts = normalized.split(",");
        for (String part : parts) {
            String p = part.trim();
            String pl = p.toLowerCase();

            if (!p.isEmpty()
                    && !pl.contains("tunisie")
                    && !pl.contains("tunisie")
                    && !pl.contains("governorate")
                    && !pl.matches(".*\\d.*")) {
                return p;
            }
        }

        return "";
    }

    private String extractDisplayName(String json) {
        String marker = "\"display_name\":\"";
        int start = json.indexOf(marker);

        if (start == -1) {
            return "";
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
    public void choosePhotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Photos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(tfTitre.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            selectedPhotos.clear();
            selectedPhotos.addAll(files);
            lblSelectedPhotos.setText(selectedPhotos.size() + " photo(s) selected");
        } else {
            lblSelectedPhotos.setText("No photos selected");
        }
    }

    @FXML
    public void addSignalement() {
        if (loggedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No logged user found.");
            return;
        }

        String titre = tfTitre.getText().trim();
        String description = taDescription.getText().trim();
        String type = cbType.getValue();
        String addresse = tfAddresse.getText().trim();
        String delegation = tfDelegation.getText().trim();
        String latitudeText = tfLatitude.getText().trim();
        String longitudeText = tfLongitude.getText().trim();

        if (titre.isEmpty() || type == null || addresse.isEmpty() || latitudeText.isEmpty() || longitudeText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please fill all required fields.");
            return;
        }

        if (titre.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Title must contain at least 3 characters.");
            return;
        }

        if (!description.isEmpty() && description.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Description must not exceed 255 characters.");
            return;
        }

        if (addresse.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Address must contain at least 5 characters.");
            return;
        }

        try {
            double latitude = Double.parseDouble(latitudeText);
            double longitude = Double.parseDouble(longitudeText);

            if (latitude < 30 || latitude > 38) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a latitude within Tunisia.");
                return;
            }

            if (longitude < 7 || longitude > 12) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a longitude within Tunisia.");
                return;
            }

            Signalement s = new Signalement();
            s.setTitre(titre);
            s.setDescription(description);
            s.setType(type);
            s.setStatut("EN_ATTENTE");
            s.setAddresse(addresse);
            s.setLatitude(latitude);
            s.setLongitude(longitude);
            s.setDateCreation(LocalDateTime.now());
            s.setCitoyenId(loggedUser.getId());
            s.setAgentAssigneId(null);
            s.setDelegation(delegation.isEmpty() ? null : delegation);
            s.setAssignedAt(null);

            int signalementId = signalementService.addAndReturnId(s);
            saveSelectedPhotos(signalementId);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Report added successfully.");
            goToList();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Latitude and longitude must be valid numbers.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSelectedPhotos(int signalementId) throws IOException, SQLException {
        if (selectedPhotos.isEmpty()) {
            return;
        }

        Path uploadDir = Path.of("uploads", "signalements");
        Files.createDirectories(uploadDir);

        for (File file : selectedPhotos) {
            String originalName = file.getName();
            String extension = "";

            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }

            String uniqueFileName = UUID.randomUUID() + extension;
            Path destination = uploadDir.resolve(uniqueFileName);

            Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            Media media = new Media();
            media.setFilename(originalName);
            media.setType("image");
            media.setUrl(destination.toString().replace("\\", "/"));
            media.setCreatedAt(LocalDateTime.now());
            media.setUserId(loggedUser.getId());
            media.setSignalementId(signalementId);
            media.setRapportSignalementId(null);
            media.setAnnonceId(null);
            media.setEventId(null);

            mediaService.add(media);
        }
    }

    @FXML
    public void goBack() {
        goToList();
    }

    private void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
            Parent root = loader.load();

            ListSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Reports");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}