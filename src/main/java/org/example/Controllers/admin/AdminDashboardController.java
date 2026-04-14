package org.example.Controllers.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.example.Entities.User;

public class AdminDashboardController {

    @FXML
    private MenuButton menuAdmin;

    @FXML
    private TableView<User> tableSubscribers;

    @FXML
    private TableColumn<User, String> colName;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colRegion;

    @FXML
    private TableColumn<User, String> colStatus;

    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user != null && menuAdmin != null) {
            menuAdmin.setText(user.getName() != null ? user.getName() : "Admin");
        }
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/user/home.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToSubscribers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/subscribers.fxml"));
            Parent root = loader.load();

            SubscribersController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Subscribers");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToMunicipalAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/municipal_agents.fxml"));
            Parent root = loader.load();

            MunicipalAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Municipal Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToFieldAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/field_agents.fxml"));
            Parent root = loader.load();

            FieldAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Field Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}