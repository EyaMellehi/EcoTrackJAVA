package org.example.Controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Entities.User;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
public class ProfileController {

    @FXML
    private ImageView imgAvatar;
    @FXML private Label lblAvatar;
    @FXML private Label lblNameCard;
    @FXML private Label lblEmailCard;
    @FXML private Label lblRegionChip;
    @FXML private Label lblPoints;

    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblRegion;
    @FXML private Label lblEcoPoints;

    private User user;


    public void setUser(User user) {
        this.user = user;

        if (user != null) {
            lblNameCard.setText(user.getName() != null ? user.getName() : "");
            lblEmailCard.setText(user.getEmail() != null ? user.getEmail() : "");
            lblRegionChip.setText(user.getRegion() != null ? user.getRegion() : "");

            lblFullName.setText(user.getName() != null ? user.getName() : "");
            lblEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            lblPhone.setText(user.getPhone() != null ? user.getPhone() : "");
            lblRegion.setText(user.getRegion() != null ? user.getRegion() : "");

            lblPoints.setText(String.valueOf(user.getPoints()));
            lblEcoPoints.setText(String.valueOf(user.getPoints()));

            if (user.getImage() != null && !user.getImage().isEmpty()) {
                File file = new File(user.getImage());

                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imgAvatar.setImage(image);
                    imgAvatar.setVisible(true);
                    lblAvatar.setVisible(false);
                } else {
                    imgAvatar.setVisible(false);
                    lblAvatar.setVisible(true);

                    if (user.getName() != null && !user.getName().isEmpty()) {
                        lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getName().charAt(0))));
                    } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                        lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getEmail().charAt(0))));
                    } else {
                        lblAvatar.setText("U");
                    }
                }
            } else {
                imgAvatar.setVisible(false);
                lblAvatar.setVisible(true);

                if (user.getName() != null && !user.getName().isEmpty()) {
                    lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getName().charAt(0))));
                } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    lblAvatar.setText(String.valueOf(Character.toUpperCase(user.getEmail().charAt(0))));
                } else {
                    lblAvatar.setText("U");
                }
            }
        }
    }

    @FXML
    void goToProfile() {
        // already on profile
    }

    @FXML
    void goToDonations() {
        System.out.println("Open donations page");
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/edit_profile.fxml"));
            Parent root = loader.load();

            EditProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) lblFullName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void changePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/change_password.fxml"));
            Parent root = loader.load();

            ChangePasswordController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) lblFullName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Change Password");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}