package org.example.Controllers.association;

 import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
 import org.example.Entities.Association;

public class AssociationClientShow {

    @FXML private Label name;
    @FXML private Label type;
    @FXML private Label description;
    @FXML private Label region;
    @FXML private Label tel;
    @FXML private Label email;
    @FXML private Label adresse;
    @FXML private Label status;
    @FXML private ImageView logo;

    public void setAssociation(Association a) {
        this.currentAssociation = a;

        // 🔥 SAFE DATA (avoid crash)
        String nom = (a.getNom() != null) ? a.getNom() : "N/A";
        String typ = (a.getType() != null) ? a.getType() : "N/A";
        String desc = (a.getDescription() != null) ? a.getDescription() : "N/A";
        String reg = (a.getRegion() != null) ? a.getRegion() : "N/A";
        String mail = (a.getEmail() != null) ? a.getEmail() : "N/A";
        String adr = (a.getAddresse() != null) ? a.getAddresse() : "N/A";

        name.setText(nom);
        type.setText(typ);
        description.setText(desc);
        region.setText("📍 " + reg);
        tel.setText("📞 " + a.getTel());
        email.setText("📧 " + mail);
        adresse.setText("🏠 " + adr);

        // 🔥 STATUS
        if (a.isActive()) {
            status.setText("ACTIVE");
            status.getStyleClass().setAll("badge-active");
        } else {
            status.setText("INACTIVE");
            status.getStyleClass().setAll("badge-inactive");
        }

        // 🔥 IMAGE
        try {
            if (a.getLogo() != null && !a.getLogo().isEmpty()) {
                logo.setImage(new Image("file:" + a.getLogo(), true));
            } else {
                logo.setImage(new Image(
                        getClass().getResourceAsStream("/images/default.png")
                ));
            }
        } catch (Exception e) {
            logo.setImage(new Image("https://via.placeholder.com/150"));
        }
    }

    @FXML
    void close() {
        Stage stage = (Stage) name.getScene().getWindow();
        stage.close();
    }

    private Association currentAssociation;

    @FXML
    void openDonationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/donation/add.fxml")
            );

            Parent root = loader.load();

            DonationController controller = loader.getController();
            controller.setAssociation(this.currentAssociation); // 🔥 important

            Stage stage = new Stage();
            stage.setTitle("Faire un don");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}