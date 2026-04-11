package org.example.Controllers.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.Controllers.User.ProfileController;
import org.example.Entities.User;

public class NavbarMunicipalController {

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

            org.example.Controllers.HomeConnectedController controller = loader.getController();
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
        System.out.println("Open report page");
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
        System.out.println("Already in municipal recycling");
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
        return (Stage) menuProfile.getParentPopup().getOwnerWindow();
    }
}