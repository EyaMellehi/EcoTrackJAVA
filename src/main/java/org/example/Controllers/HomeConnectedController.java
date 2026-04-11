package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.Controllers.User.ProfileController;
import org.example.Entities.User;

import java.io.IOException;

public class HomeConnectedController {

    @FXML private Button btnHome;
    @FXML private Button btnReport;
    @FXML private Button btnBlogs;
    @FXML private Button btnAssociations;
    @FXML private Button btnRecycling;
    @FXML private Button btnEvents;
    @FXML private MenuItem menuProfile;
    @FXML private MenuItem menuDonations;
    @FXML private MenuItem menuLogout;
    @FXML private Label lblWelcomeUser;

    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user == null || user.getRoles() == null) {
            return;
        }

        if (user.getName() != null && !user.getName().isEmpty()) {
            lblWelcomeUser.setText("Welcome, " + user.getName());
        } else {
            lblWelcomeUser.setText("Welcome");
        }

        String roles = user.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            applyMunicipalNavbar();
        } else if (roles.contains("ROLE_CITOYEN")) {
            applyStandardNavbar();
        } else if (roles.contains("ROLE_AGENT_TERRAIN")) {
            applyTerrainNavbar();
        } else if (roles.contains("ROLE_ADMIN")) {
            applyAdminNavbar();
        } else {
            applyStandardNavbar();
        }

        if (!roles.contains("ROLE_CITOYEN")) {
            menuDonations.setVisible(false);
            menuDonations.setDisable(true);
        }
    }

    private void applyStandardNavbar() {
        btnHome.setText("Home");
        btnReport.setText("Report");
        btnBlogs.setText("Blogs");
        btnAssociations.setText("Associations");
        btnRecycling.setText("Recycling");
        btnEvents.setText("Events");
    }

    private void applyMunicipalNavbar() {
        btnHome.setText("Home");
        btnReport.setText("Report");
        btnBlogs.setText("Announcements Management");
        btnAssociations.setText("Associations");
        btnRecycling.setText("Recycling");
        btnEvents.setText("Events Management");
    }

    private void applyTerrainNavbar() {
        btnHome.setText("Home");
        btnReport.setText("Report");
        btnBlogs.setText("Blogs");
        btnAssociations.setText("Associations");
        btnRecycling.setText("Recycling");
        btnEvents.setText("Events");
    }

    private void applyAdminNavbar() {
        btnHome.setText("Home");
        btnReport.setText("Report");
        btnBlogs.setText("Announcements Management");
        btnAssociations.setText("Associations");
        btnRecycling.setText("Recycling");
        btnEvents.setText("Events Management");
    }

    @FXML
    void goToDonations() {
        System.out.println("Open donations");
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) lblWelcomeUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(loggedUser);

            Stage stage = (Stage) lblWelcomeUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Profile");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToRecycling() {
        if (loggedUser == null || loggedUser.getRoles() == null) {
            System.out.println("No logged user found.");
            return;
        }

        String roles = loggedUser.getRoles();

        try {
            FXMLLoader loader;
            Parent root;

            if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
                loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_points.fxml"));
                root = loader.load();

                org.example.Controllers.recyclage.MunicipalPointsController controller = loader.getController();
                controller.setLoggedUser(loggedUser);

            } else if (roles.contains("ROLE_AGENT_TERRAIN")) {
                loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_points.fxml"));
                root = loader.load();

                org.example.Controllers.recyclage.TerrainPointsController controller = loader.getController();
                controller.setLoggedUser(loggedUser);

            } else {
                loader = new FXMLLoader(getClass().getResource("/recyclage/points_connected.fxml"));
                root = loader.load();

                org.example.Controllers.recyclage.PointsConnectedController controller = loader.getController();
                controller.setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) btnRecycling.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToHome() {
        System.out.println("Already in home");
    }

    @FXML
    void goToReport() {
        System.out.println("Open report page");
    }

    @FXML
    void goToBlogs() {
        if (loggedUser != null && loggedUser.getRoles() != null &&
                loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
            System.out.println("Open announcements management");
        } else {
            System.out.println("Open blogs page");
        }
    }

    @FXML
    void goToAssociations() {
        System.out.println("Open associations page");
    }

    @FXML
    void goToEvents() {
        if (loggedUser != null && loggedUser.getRoles() != null &&
                loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
            System.out.println("Open events management");
        } else {
            System.out.println("Open events page");
        }
    }
}