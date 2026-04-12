package gui;

import entities.Annonce;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.AnnonceService;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class AfficherAnnonces {
    
    @FXML
    private TableView<Annonce> annoncesTable;
    
    @FXML
    private TableColumn<Annonce, Integer> idCol;
    
    @FXML
    private TableColumn<Annonce, String> titreCol;
    
    @FXML
    private TableColumn<Annonce, String> datePubCol;
    
    @FXML
    private TableColumn<Annonce, String> regionCol;
    
    @FXML
    private TableColumn<Annonce, String> contenuCol;
    
    @FXML
    private TableColumn<Annonce, String> categorieCol;
    
    @FXML
    public void initialize() {
        System.out.println("=== INITIALISATION AfficherAnnonces ===");
        
        try {
            // Charger les données
            AnnonceService service = new AnnonceService();
            List<Annonce> annonces = service.readAll();
            System.out.println("✅ Nombre d'annonces chargées: " + annonces.size());
            
            for (Annonce a : annonces) {
                System.out.println("  - ID: " + a.getId() + ", Titre: " + a.getTitre());
            }
            
            // Créer observable list
            ObservableList<Annonce> list = FXCollections.observableArrayList(annonces);
            
            // Configurer les colonnes
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
            
            // Date en String
            datePubCol.setCellValueFactory(cellData -> {
                Timestamp ts = cellData.getValue().getDatePub();
                if (ts != null) {
                    return new javafx.beans.property.SimpleStringProperty(ts.toString().substring(0, 10));
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            });
            
            regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
            contenuCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));
            categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
            
            // Colonne Action avec bouton "Voir" - créée dynamiquement
            addActionColumn();
            
            // Bind les données à la table
            annoncesTable.setItems(list);
            System.out.println("✅ Données affichées. Lignes: " + annoncesTable.getItems().size());
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Base de Données");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
            
            // Table vide
            annoncesTable.setItems(FXCollections.observableArrayList());
            
            // Configurer les colonnes
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
            datePubCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(""));
            regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
            contenuCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));
            categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        }
    }
    
    // Ajouter la colonne d'action avec bouton "Voir"
    private void addActionColumn() {
        System.out.println("✅ Création de la colonne Action...");
        
        TableColumn<Annonce, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setMinWidth(80);
        actionCol.setSortable(false);
        
        actionCol.setCellFactory(param -> new TableCell<Annonce, Void>() {
            private final Button btn = new Button("👁️ Voir");
            
            {
                btn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btn.setCursor(javafx.scene.Cursor.HAND);
                
                btn.setOnAction(event -> {
                    try {
                        Annonce annonce = getTableView().getItems().get(getIndex());
                        System.out.println("✅ Clic sur bouton Voir - Annonce: " + annonce.getTitre());
                        afficherDetailsAnnonce(annonce);
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors du clic: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
            
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        annoncesTable.getColumns().add(actionCol);
        System.out.println("✅ Colonne Action créée avec succès");
    }

    
    private void afficherDetailsAnnonce(Annonce annonce) {
        System.out.println("📄 Affichage des détails pour: " + annonce.getTitre());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDetailsAnnonce.fxml"));
            Parent root = loader.load();
            System.out.println("✅ FXML chargé");
            
            AfficherDetailsAnnonce controller = loader.getController();
            System.out.println("✅ Controller obtenu");
            
            controller.setAnnonce(annonce);
            System.out.println("✅ Annonce définie dans le controller");
            
            // Obtenir la scène depuis la table
            javafx.scene.Scene scene = annoncesTable.getScene();
            if (scene != null) {
                System.out.println("✅ Scène obtenue");
                scene.setRoot(root);
                System.out.println("✅ Root changé avec succès");
            } else {
                System.err.println("❌ La scène est null!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur: Scène non disponible");
                alert.show();
            }
        } catch (IOException e) {
            System.err.println("❌ IOException: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de l'affichage des détails:\n" + e.getMessage());
            alert.show();
        } catch (Exception e) {
            System.err.println("❌ Exception générale: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.show();
        }
    }
    
    @FXML
    public void naviguerVersAjouter(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterAnnonce.fxml"));
            annoncesTable.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

