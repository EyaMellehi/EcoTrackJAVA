package org.example.Controllers.association;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.example.Entities.Association;
import org.example.Entities.Donation;
import org.example.Entities.User;
import org.example.Services.DonationService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DonationIndexClientController {

    @FXML private FlowPane cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label pageLabel;

    private final DonationService service = new DonationService();

    private List<Donation> allData = new ArrayList<>();

    /* ✅ CONNECTED USER */
    private User currentUser;

    private int currentPage = 0;
    private final int pageSize = 6;

    /* ===================================================== */
    /* RECEIVE CURRENT USER                                  */
    /* ===================================================== */
    public void setUser(User user) {
        this.currentUser = user;

        System.out.println("USER RECEIVED IN DONATION PAGE = " + (user != null ? user.getId() : "NULL"));

        if (user == null) return;

        javafx.application.Platform.runLater(this::loadData);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /* ===================================================== */
    @FXML
    public void initialize() {

        typeFilter.getItems().addAll(
                "All",
                "argent",
                "matériel"
        );

        typeFilter.setValue("All");

    }

    /* ===================================================== */
    private void loadData() {

        try {

            if (currentUser == null) {

                allData = new ArrayList<>();
                pageLabel.setText("Utilisateur non connecté");
                refresh();
                return;
            }

            System.out.println(
                    "LOAD DONATIONS FOR USER ID = "
                            + currentUser.getId()
            );

            allData =
                    service.getMyDonations(
                            currentUser.getId()
                    );

            if (allData == null) {
                allData = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            allData = new ArrayList<>();
        }

        currentPage = 0;
        refresh();
    }

    /* ===================================================== */
    private void refresh() {

        cardContainer.getChildren().clear();

        String q =
                searchField.getText() == null
                        ? ""
                        : searchField.getText()
                        .trim()
                        .toLowerCase();

        String type =
                typeFilter.getValue() == null
                        ? "All"
                        : typeFilter.getValue();

        List<Donation> filtered =
                allData.stream()

                        .filter(d -> {

                            String asso =
                                    d.getAssociation() != null &&
                                            d.getAssociation().getNom() != null
                                            ? d.getAssociation()
                                            .getNom()
                                            .toLowerCase()
                                            : "";

                            return asso.contains(q);
                        })

                        .filter(d ->
                                type.equals("All")
                                        ||
                                        (
                                                d.getType() != null
                                                        &&
                                                        d.getType()
                                                                .equalsIgnoreCase(type)
                                        )
                        )

                        .collect(Collectors.toList());

        if (filtered.isEmpty()) {

            pageLabel.setText("0 donation");
            return;
        }

        int totalPages =
                (int) Math.ceil(
                        (double) filtered.size()
                                / pageSize
                );

        if (currentPage >= totalPages)
            currentPage = totalPages - 1;

        if (currentPage < 0)
            currentPage = 0;

        int start = currentPage * pageSize;

        int end =
                Math.min(
                        start + pageSize,
                        filtered.size()
                );

        List<Donation> page =
                filtered.subList(start, end);

        for (Donation d : page) {
            cardContainer.getChildren()
                    .add(createCard(d));
        }

        pageLabel.setText(
                "Page "
                        + (currentPage + 1)
                        + " / "
                        + totalPages
        );
    }

    /* ===================================================== */
    private VBox createCard(Donation d) {

        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(270);
        card.getStyleClass().add("card");

        Label title =
                new Label(
                        "💚 Donation #"
                                + d.getId()
                );

        title.getStyleClass()
                .add("card-title");

        String associationName =
                d.getAssociation() != null &&
                        d.getAssociation().getNom() != null
                        ? d.getAssociation().getNom()
                        : "No Association";

        Label association =
                new Label("🏢 " + associationName);

        Label type =
                new Label(
                        d.getType() != null &&
                                d.getType()
                                        .equalsIgnoreCase("argent")
                                ? "💰 Argent"
                                : "📦 Matériel"
                );

        Label value =
                new Label(
                        d.getType() != null &&
                                d.getType()
                                        .equalsIgnoreCase("argent")
                                ? "💵 "
                                + d.getMontant()
                                + " DT"
                                : "📦 "
                                + (
                                d.getDescriptionMateriel() != null
                                        ? d.getDescriptionMateriel()
                                        : "--"
                        )
                );

        Label date =
                new Label(
                        "📅 "
                                + (
                                d.getDateDon() != null
                                        ? d.getDateDon().format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy HH:mm"))
                                        : "--"
                        )
                );

        Label status =
                new Label(
                        d.getStatut() != null
                                ? d.getStatut()
                                : "UNKNOWN"
                );



        association.getStyleClass().add("card-sub");
        type.getStyleClass().add("card-sub");
        value.getStyleClass().add("card-sub");
        date.getStyleClass().add("card-sub");

        card.getChildren().addAll(
                title,
                association,
                type,
                value,
                date
        );

        return card;
    }

    /* ===================================================== */
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
    void resetFilters() {

        searchField.clear();
        typeFilter.setValue("All");
        currentPage = 0;
        refresh();
    }

    /* ===================================================== */
    @FXML
    void goToDonation(ActionEvent event) {

        loadPage(
                event,
                "/association/donation_index.fxml"
        );
    }

    /* ===================================================== */
    private void loadPage(
            ActionEvent event,
            String path
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(path));

            Parent root =
                    loader.load();

            DonationIndexClientController controller =
                    loader.getController();

            /* ✅ KEEP USER SESSION */
            controller.setUser(currentUser);

            Stage stage =
                    (Stage) ((Node)
                            event.getSource())
                            .getScene()
                            .getWindow();

            Rectangle2D bounds =
                    Screen.getPrimary()
                            .getVisualBounds();

            Scene scene =
                    new Scene(
                            root,
                            bounds.getWidth(),
                            bounds.getHeight()
                    );

            stage.setScene(scene);
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}