package org.example.Controllers.association;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.Entities.Donation;
import org.example.Services.DonationService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DonationIndexController {
// ADD THESE METHODS INSIDE DonationIndexController

    @FXML
    void resetFilters() {
        searchField.clear();
        typeFilter.setValue("All");
        currentPage = 0;
        refresh();
    }

    @FXML
    void goToDashboard() {
        navigate("/admin/dashboard.fxml", "Dashboard");
    }

    @FXML
    void goToSubscribers() {
        navigate("/admin/subscribers.fxml", "Subscribers");
    }

    @FXML
    void goToMunicipalAgents() {
        navigate("/admin/municipal_agents.fxml", "Municipal Agents");
    }

    @FXML
    void goToFieldAgents() {
        navigate("/admin/field_agents.fxml", "Field Agents");
    }

    @FXML
    void goToCategories() {
        navigate("/admin/categories.fxml", "Categories");
    }

    @FXML
    void goToAssociation() {
        navigate("/admin_association/association.fxml", "Associations");
    }

    @FXML
    void goToEvents() {
        navigate("/admin/events.fxml", "Events");
    }

    @FXML
    void logout() {
        navigate("/login.fxml", "Login");
    }

    private void navigate(String path, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));

            Stage stage = (Stage) cardContainer.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private FlowPane cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label pageLabel;

    private final DonationService service = new DonationService();

    private List<Donation> allData = new ArrayList<>();

    private int currentPage = 0;
    private final int pageSize = 6;

    @FXML
    public void initialize() {

        typeFilter.getItems().addAll(
                "All",
                "argent",
                "matériel"
        );

        typeFilter.setValue("All");

        loadData();
    }

    private void loadData() {

        try {
            allData = service.getAllWithJoin();

            if (allData == null) {
                allData = new ArrayList<>();
            }

        } catch (Exception e) {
            allData = new ArrayList<>();
            e.printStackTrace();
        }

        refresh();
    }

    private void refresh() {

        cardContainer.getChildren().clear();

        String q = searchField == null ? "" :
                searchField.getText().trim().toLowerCase();

        String type = typeFilter == null || typeFilter.getValue() == null
                ? "All"
                : typeFilter.getValue();

        List<Donation> filtered = allData.stream()

                .filter(d -> {

                    String associationName =
                            d.getAssociation() != null &&
                                    d.getAssociation().getNom() != null
                                    ? d.getAssociation().getNom().toLowerCase()
                                    : "";

                    String donorName =
                            d.getDonateur() != null &&
                                    d.getDonateur().getName() != null
                                    ? d.getDonateur().getName().toLowerCase()
                                    : "";

                    return associationName.contains(q)
                            || donorName.contains(q);
                })

                .filter(d ->
                        type.equals("All")
                                || (d.getType() != null &&
                                d.getType().equalsIgnoreCase(type))
                )

                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            pageLabel.setText("0 Result");
            return;
        }

        int totalPages =
                (int) Math.ceil((double) filtered.size() / pageSize);

        if (currentPage >= totalPages)
            currentPage = totalPages - 1;

        if (currentPage < 0)
            currentPage = 0;

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<Donation> page = filtered.subList(start, end);

        for (Donation d : page) {
            cardContainer.getChildren().add(createCard(d));
        }

        pageLabel.setText(
                "Page " + (currentPage + 1) + " / " + totalPages
        );
    }

    private VBox createCard(Donation d) {

        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(260);
        card.getStyleClass().add("card");

        Label title = new Label("💚 Donation #" + d.getId());
        title.getStyleClass().add("card-title");

        String donorText = "Anonymous";

        if (d.getDonateur() != null) {

            String name =
                    d.getDonateur().getName() != null
                            ? d.getDonateur().getName()
                            : "Unknown";

            String phone =
                    d.getDonateur().getPhone() != null
                            ? d.getDonateur().getPhone()
                            : "--";

            donorText = name + " " + phone;
        }

        String associationText =
                d.getAssociation() != null &&
                        d.getAssociation().getNom() != null
                        ? d.getAssociation().getNom()
                        : "No Association";

        Label donor = new Label("👤 " + donorText);

        Label association = new Label("🌿 " + associationText);

        Label type = new Label(
                d.getType() != null &&
                        d.getType().equalsIgnoreCase("argent")
                        ? "💰 Argent"
                        : "📦 Matériel"
        );

        Label amount = new Label(
                d.getType() != null &&
                        d.getType().equalsIgnoreCase("argent")
                        ? "💵 " + d.getMontant() + " DT"
                        : "📦 " +
                        (
                                d.getDescriptionMateriel() != null
                                        ? d.getDescriptionMateriel()
                                        : "--"
                        )
        );

        Label date = new Label(
                "📅 " +
                        (
                                d.getDateDon() != null
                                        ? d.getDateDon().format(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                )
                                        : "--"
                        )
        );

        Label status = new Label(
                d.getStatut() != null
                        ? d.getStatut()
                        : "UNKNOWN"
        );

        if ("EN_ATTENTE".equalsIgnoreCase(status.getText())) {
            status.getStyleClass().add("badge-inactive");
        } else {
            status.getStyleClass().add("badge-active");
        }

        donor.getStyleClass().add("card-sub");
        association.getStyleClass().add("card-sub");
        type.getStyleClass().add("card-sub");
        amount.getStyleClass().add("card-sub");
        date.getStyleClass().add("card-sub");

        card.getChildren().addAll(
                title,
                donor,
                association,
                type,
                amount,
                date,
                status
        );

        return card;
    }

    @FXML
    void onSearch() {
        currentPage = 0;
        refresh();
    }

    @FXML
    void onFilter() {
        currentPage = 0;
        refresh();
    }

    @FXML
    void nextPage() {
        currentPage++;
        refresh();
    }

    @FXML
    void prevPage() {
        if (currentPage > 0)
            currentPage--;

        refresh();
    }

    @FXML
    void goToDonation(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/admin_association/association.fxml")
            );

            Parent root = loader.load();

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            Rectangle2D bounds =
                    Screen.getPrimary().getVisualBounds();

            Scene scene =
                    new Scene(root,
                            bounds.getWidth(),
                            bounds.getHeight());

            stage.setScene(scene);
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("❌ Impossible d'ouvrir la page");
            alert.showAndWait();
        }
    }
}