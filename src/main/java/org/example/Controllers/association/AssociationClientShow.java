package org.example.Controllers.association;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.Entities.Association;
import org.example.Entities.User;

import java.awt.image.BufferedImage;

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
    @FXML private ImageView qrImage;

    private Association currentAssociation;

    /* ✅ CONNECTED USER */
    private User currentUser;

    /* ================================================= */
    /* RECEIVE LOGGED USER FROM PREVIOUS PAGE            */
    /* ================================================= */
    public void setLoggedUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /* ================================================= */
    public void setAssociation(Association a) {

        this.currentAssociation = a;

        name.setText(a.getNom() != null ? a.getNom() : "N/A");
        type.setText(a.getType() != null ? a.getType() : "N/A");
        description.setText(a.getDescription() != null ? a.getDescription() : "N/A");
        region.setText("📍 " + (a.getRegion() != null ? a.getRegion() : "N/A"));
        tel.setText("📞 " + a.getTel());
        email.setText("📧 " + (a.getEmail() != null ? a.getEmail() : "N/A"));
        adresse.setText("🏠 " + (a.getAddresse() != null ? a.getAddresse() : "N/A"));


        generateQR(a);


        if (a.isActive()) {
            status.setText("ACTIVE");
            status.getStyleClass().setAll("badge-active");
        } else {
            status.setText("INACTIVE");
            status.getStyleClass().setAll("badge-inactive");
        }

        try {
            if (a.getLogo() != null && !a.getLogo().isBlank()) {
                logo.setImage(new Image("file:" + a.getLogo(), true));
            } else {
                logo.setImage(
                        new Image(getClass()
                                .getResourceAsStream("/images/default.png"))
                );
            }
        } catch (Exception e) {
            logo.setImage(
                    new Image("https://via.placeholder.com/150")
            );
        }
    }

    /* ================================================= */
    @FXML
    void close() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/client_association/index.fxml"));

            Parent root = loader.load();

            AssocationClientIndex controller =
                    loader.getController();

            /* ✅ RETURN SAME CONNECTED USER */
            controller.setLoggedUser(currentUser);

            Stage stage =
                    (Stage) name.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Associations");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================================================= */
    @FXML
    void openDonationForm() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/donation/add.fxml"));

            Parent root = loader.load();

            DonationController controller =
                    loader.getController();

            controller.setAssociation(currentAssociation);

            /* ✅ PASS REAL CONNECTED USER */
            controller.setLoggedUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Faire un don");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }










    private void generateQR(Association a) {

        try {

            String data =
                    "Association: " + a.getNom() + "\n" +
                            "Type: " + a.getType() + "\n" +
                            "Region: " + a.getRegion() + "\n" +
                            "Tel: " + a.getTel() + "\n" +
                            "Email: " + a.getEmail() + "\n" +
                            "Adresse: " + a.getAddresse();

            QRCodeWriter writer =
                    new QRCodeWriter();

            BitMatrix matrix =
                    writer.encode(
                            data,
                            BarcodeFormat.QR_CODE,
                            220,
                            220
                    );

            BufferedImage buffered =
                    new BufferedImage(
                            220,
                            220,
                            BufferedImage.TYPE_INT_RGB
                    );

            for (int x = 0; x < 220; x++) {
                for (int y = 0; y < 220; y++) {

                    int rgb =
                            matrix.get(x, y)
                                    ? 0x000000
                                    : 0xFFFFFF;

                    buffered.setRGB(x, y, rgb);
                }
            }

            qrImage.setImage(
                    SwingFXUtils.toFXImage(
                            buffered,
                            null
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}