package org.example.Controllers.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.User.ProfileController;
import org.example.Controllers.recyclage.MunicipalPointsController;
import org.example.Controllers.signalement.ListMunicipalSignalementController;
import org.example.Entities.User;

public class NavbarMunicipalController {

    @FXML private Button btnHome;
    @FXML private Button btnReport;
    @FXML private Button btnAnnouncements;
    @FXML private Button btnAssociations;
    @FXML private Button btnRecycling;
    @FXML private Button btnEvents;

    @FXML private MenuItem menuProfile;
    @FXML private MenuItem menuLogout;

    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    @FXML
    private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home_connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/list_municipal_signalements.fxml"));
            Parent root = loader.load();

            ListMunicipalSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Municipal Reports");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goAnnouncements() {
        System.out.println("Open announcements management");
    }

    @FXML
    private void goAssociations() {
        System.out.println("Open associations page");
    }

    @FXML
    private void goRecycling() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_points.fxml"));
            Parent root = loader.load();

            MunicipalPointsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = getStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goEvents() {
        System.out.println("Open events management");
    }

    @FXML
    private void goProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(loggedUser);

            Stage stage = getStage();
            stage.setScene(new Scene(root));
            stage.setTitle("My Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = getStage();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Stage getStage() {
        return (Stage) btnHome.getScene().getWindow();
    }
}