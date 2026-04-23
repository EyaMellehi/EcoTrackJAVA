package org.example.Controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Services.UserService;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
public class AdminDashboardController {

    @FXML
    private MenuButton menuAdmin;
    @FXML private TableColumn<User, Integer> colPoints;
    @FXML private javafx.scene.control.Pagination paginationTopCitoyens;
    @FXML private javafx.scene.chart.BarChart<String, Number> barChartSignalementsDelegation;
    @FXML private javafx.scene.chart.PieChart pieChartRecyclageStatut;
    @FXML private javafx.scene.chart.BarChart<String, Number> barChartAssociationsRegion;
    @FXML private javafx.scene.chart.PieChart pieChartAnnoncesCategorie;
    @FXML private javafx.scene.chart.BarChart<String, Number> barChartEventsStatut;

    @FXML private javafx.scene.chart.BarChart<String, Number> barChartStats;
    @FXML private javafx.scene.chart.PieChart pieChartUsers;
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
    @FXML private Label lblSubscribersCount;
    @FXML private Label lblMunicipalCount;
    @FXML private Label lblFieldCount;
    @FXML private Label lblEventsCount;

    private User loggedUser;
    private final UserService userService = new UserService();
    private ObservableList<User> topCitoyensData = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 5;


    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isActive() ? "Active" : "Inactive"
                )
        );

        loadStats();
        loadTopCitoyens();
        loadCharts();
    }
    private void loadCharts() {
        try {
            int subscribers = userService.countSubscribers();
            int municipal = userService.countMunicipalAgents();
            int field = userService.countFieldAgents();
            int events = userService.countPublishedEvents();

            // Bar chart
            barChartStats.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Subscribers", subscribers));
            series.getData().add(new XYChart.Data<>("Municipal", municipal));
            series.getData().add(new XYChart.Data<>("Field", field));
            series.getData().add(new XYChart.Data<>("Events", events));
            barChartStats.getData().add(series);

            // Pie chart
            pieChartUsers.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Citizens", subscribers),
                    new PieChart.Data("Municipal Agents", municipal),
                    new PieChart.Data("Field Agents", field)
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // Signalements par delegation
            barChartSignalementsDelegation.getData().clear();
            XYChart.Series<String, Number> signalementSeries = new XYChart.Series<>();
            for (var entry : userService.countSignalementsByDelegation().entrySet()) {
                signalementSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barChartSignalementsDelegation.getData().add(signalementSeries);

            // Recyclage par statut
            pieChartRecyclageStatut.setData(FXCollections.observableArrayList());
            for (var entry : userService.countRecyclageByStatut().entrySet()) {
                pieChartRecyclageStatut.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            // Associations par region
            barChartAssociationsRegion.getData().clear();
            XYChart.Series<String, Number> associationSeries = new XYChart.Series<>();
            for (var entry : userService.countAssociationsByRegion().entrySet()) {
                associationSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barChartAssociationsRegion.getData().add(associationSeries);

            // Annonces par categorie
            pieChartAnnoncesCategorie.setData(FXCollections.observableArrayList());
            for (var entry : userService.countAnnoncesByCategorie().entrySet()) {
                pieChartAnnoncesCategorie.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            // Events par statut
            barChartEventsStatut.getData().clear();
            XYChart.Series<String, Number> eventsSeries = new XYChart.Series<>();
            for (var entry : userService.countEventsByStatut().entrySet()) {
                eventsSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barChartEventsStatut.getData().add(eventsSeries);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user != null && menuAdmin != null) {
            menuAdmin.setText(user.getName() != null ? user.getName() : "Admin");
        }

        loadStats();
        loadTopCitoyens();
        loadCharts();
    }

    private void loadTopCitoyens() {
        try {
            topCitoyensData = FXCollections.observableArrayList(
                    userService.getTopCitoyensByPoints(100)
            );

            int pageCount = (int) Math.ceil((double) topCitoyensData.size() / ROWS_PER_PAGE);
            paginationTopCitoyens.setPageCount(Math.max(pageCount, 1));
            paginationTopCitoyens.setCurrentPageIndex(0);

            paginationTopCitoyens.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                updateTopCitoyensPage(newIndex.intValue());
            });

            updateTopCitoyensPage(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateTopCitoyensPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, topCitoyensData.size());

        if (fromIndex > toIndex) {
            tableSubscribers.setItems(FXCollections.observableArrayList());
            return;
        }

        tableSubscribers.setItems(FXCollections.observableArrayList(
                topCitoyensData.subList(fromIndex, toIndex)
        ));
    }

    private void loadStats() {
        try {
            lblSubscribersCount.setText(String.valueOf(userService.countSubscribers()));
            lblMunicipalCount.setText(String.valueOf(userService.countMunicipalAgents()));
            lblFieldCount.setText(String.valueOf(userService.countFieldAgents()));
            lblEventsCount.setText(String.valueOf(userService.countPublishedEvents()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Field Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToCategories() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin/categories.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Catégories");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToAssociation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_association/association.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("associaitons");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToDonation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/donation/donationIndex.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("donations");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    void goToAddMunicipalAgent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/add_municipal_agent.fxml"));
            Parent root = loader.load();

            AddMunicipalAgentController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) lblSubscribersCount.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Add Municipal Agent");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToAddFieldAgent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/add_field_agent.fxml"));
            Parent root = loader.load();

            AddFieldAgentController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) lblSubscribersCount.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Add Field Agent");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}