package org.example.Controllers.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import nu.pattern.OpenCV;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import java.io.File;

public class FaceEnrollController {

    @FXML
    private ImageView cameraView;

    @FXML
    private Label lblStatus;

    private User user;
    private VideoCapture capture;
    private boolean cameraActive = false;
    private Mat currentFrame;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void openCamera() {
        try {
            if (cameraActive) return;

            OpenCV.loadLocally();

            capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                lblStatus.setText("Unable to open camera.");
                return;
            }

            cameraActive = true;
            currentFrame = new Mat();

            Thread cameraThread = new Thread(() -> {
                while (cameraActive) {
                    Mat frame = new Mat();

                    if (capture.read(frame) && !frame.empty()) {
                        currentFrame = frame;
                        Image image = mat2Image(frame);
                        Platform.runLater(() -> cameraView.setImage(image));
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException ignored) {
                    }
                }
            });

            cameraThread.setDaemon(true);
            cameraThread.start();

            lblStatus.setText("Camera opened.");

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Error opening camera.");
        }
    }

    @FXML
    private void capturePhoto() {
        if (currentFrame == null || currentFrame.empty()) {
            lblStatus.setText("No frame captured.");
            return;
        }

        lblStatus.setText("Photo captured. Click Save.");
    }

    @FXML
    private void saveFace() {
        try {
            if (user == null) {
                lblStatus.setText("User not found.");
                return;
            }

            if (currentFrame == null || currentFrame.empty()) {
                lblStatus.setText("No photo to save.");
                return;
            }

            File dir = new File("var/faces");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String path = "var/faces/user_" + user.getId() + ".jpg";
            Imgcodecs.imwrite(path, currentFrame);

            lblStatus.setText("Face login activated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Failed to save photo.");
        }
    }

    @FXML
    private void goBack() {
        try {
            stopCamera();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) cameraView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Profile");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        cameraActive = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }

    private Image mat2Image(Mat frame) {
        try {
            org.opencv.imgcodecs.Imgcodecs.imwrite("temp_frame.jpg", frame);
            return new Image(new File("temp_frame.jpg").toURI().toString());
        } catch (Exception e) {
            return null;
        }
    }
}