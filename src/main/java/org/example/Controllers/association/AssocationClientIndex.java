package org.example.Controllers.association;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Entities.Association;
import org.example.Services.AssociationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssocationClientIndex {

    @FXML private FlowPane cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> regionFilter;
    @FXML private Label pageLabel;

    private final AssociationService service = new AssociationService();

    private List<Association> allData = new ArrayList<>();
    private List<Association> filteredData = new ArrayList<>();

    private int currentPage = 0;
    private final int pageSize = 6;

    @FXML
    public void initialize() {

        if (regionFilter != null) {
            regionFilter.setItems(FXCollections.observableArrayList(
                    "All",
                    "Tunis",
                    "Ariana",
                    "Ben Arous",
                    "Manouba",
                    "Nabeul",
                    "Zaghouan",
                    "Bizerte",
                    "Béja",
                    "Jendouba",
                    "Kef",
                    "Siliana",
                    "Sousse",
                    "Monastir",
                    "Mahdia",
                    "Sfax",
                    "Kairouan",
                    "Kasserine",
                    "Sidi Bouzid",
                    "Gabès",
                    "Medenine",
                    "Tataouine",
                    "Gafsa",
                    "Tozeur",
                    "Kebili"
            ));

            regionFilter.setValue("All");

            regionFilter.setOnAction(e -> onFilter());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> onSearch());
        }

        loadData();
    }

    private void loadData() {
        try {
            allData = service.getAll();

            if (allData == null) {
                allData = new ArrayList<>();
            }

        } catch (Exception e) {
            allData = new ArrayList<>();
            e.printStackTrace();
        }

        applyFilters();
    }

    private void applyFilters() {

        String keyword = "";
        String region = "All";

        if (searchField != null && searchField.getText() != null) {
            keyword = searchField.getText().trim().toLowerCase();
        }

        if (regionFilter != null && regionFilter.getValue() != null) {
            region = regionFilter.getValue();
        }

        final String searchValue = keyword;
        final String selectedRegion = region;

        filteredData = allData.stream()

                .filter(a -> {

                    if (searchValue.isEmpty()) return true;

                    String nom = a.getNom() == null ? "" : a.getNom().toLowerCase();
                    String desc = a.getDescription() == null ? "" : a.getDescription().toLowerCase();
                    String reg = a.getRegion() == null ? "" : a.getRegion().toLowerCase();

                    return nom.contains(searchValue)
                            || desc.contains(searchValue)
                            || reg.contains(searchValue);
                })

                .filter(a -> {
                    if (selectedRegion.equals("All")) return true;

                    return a.getRegion() != null &&
                            a.getRegion().equalsIgnoreCase(selectedRegion);
                })

                .collect(Collectors.toList());

        currentPage = 0;
        refreshPage();
    }

    private void refreshPage() {

        cardContainer.getChildren().clear();

        if (filteredData.isEmpty()) {
            Label empty = new Label("😕 No association found");
            empty.setStyle("-fx-font-size:18px; -fx-text-fill:#64748b;");
            cardContainer.getChildren().add(empty);
            pageLabel.setText("0 / 0");
            return;
        }

        int totalPages = (int) Math.ceil((double) filteredData.size() / pageSize);

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, filteredData.size());

        List<Association> pageItems = filteredData.subList(start, end);

        for (Association association : pageItems) {
            cardContainer.getChildren().add(createCard(association));
        }

        pageLabel.setText((currentPage + 1) + " / " + totalPages);
    }

    private VBox createCard(Association a) {

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(250);

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");

        ImageView logo = new ImageView();
        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        try {
            if (a.getLogo() != null && !a.getLogo().isEmpty()) {
                logo.setImage(new Image("file:" + a.getLogo(), true));
            } else {
                logo.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
            }
        } catch (Exception e) {
            logo.setImage(new Image(getClass().getResourceAsStream("/images/default.png")));
        }

        imageContainer.getChildren().add(logo);

        Label name = new Label(
                a.getNom() == null ? "No Name" : a.getNom()
        );
        name.getStyleClass().add("card-title");

        Label region = new Label(
                "📍 " + (a.getRegion() == null ? "Unknown" : a.getRegion())
        );

        Label phone = new Label(
                "📞 " + (a.getTel() == 0 ? "--" : a.getTel())
        );

        Label status = new Label(
                a.isActive() ? "ACTIVE" : "INACTIVE"
        );

        status.getStyleClass().add(
                a.isActive() ? "badge-active" : "badge-inactive"
        );

        Button viewBtn = new Button("👁 View");
        viewBtn.getStyleClass().add("icon-btn");
        viewBtn.setOnAction(e -> openShowPage(a));

        HBox footer = new HBox(10, status, viewBtn);
        footer.setStyle("-fx-alignment:center-space-between;");

        card.getChildren().addAll(
                imageContainer,
                name,
                region,
                phone,
                footer
        );

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-scale-x:1.03;-fx-scale-y:1.03;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("")
        );

        return card;
    }

    private void openShowPage(Association a) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client_association/show.fxml")
            );

            Parent root = loader.load();

            AssociationClientShow controller = loader.getController();
            controller.setAssociation(a);

            Stage stage = new Stage();
            stage.setTitle("Association Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        applyFilters();
    }

    @FXML
    private void onFilter() {
        applyFilters();
    }

    @FXML
    private void nextPage() {

        int totalPages = (int) Math.ceil((double) filteredData.size() / pageSize);

        if (currentPage < totalPages - 1) {
            currentPage++;
            refreshPage();
        }
    }

    @FXML
    private void prevPage() {

        if (currentPage > 0) {
            currentPage--;
            refreshPage();
        }
    }
}