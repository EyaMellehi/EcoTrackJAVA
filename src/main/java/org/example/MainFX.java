package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Services.DailyPriorityScheduler;

public class MainFX extends Application {

    private final DailyPriorityScheduler dailyPriorityScheduler = new DailyPriorityScheduler();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        stage.setTitle("EcoTrack - Home");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();

        // lance la mise à jour quotidienne des priorités
        dailyPriorityScheduler.start();
    }

    @Override
    public void stop() throws Exception {
        dailyPriorityScheduler.stop();
        super.stop();
    }
}