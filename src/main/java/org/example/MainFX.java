package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize database tables on startup

        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("EcoTrack - Home");
        stage.setScene(scene);
        stage.show();
    }


}
