/*
 * MainApplication.java
 *
 * Created on 2022-02-09
 * Updated on 2022-02-12
 *
 * Description: Contains the main application class.
 */

package site.overwrite.auditranscribe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Start");

//        // Load the FXML file into the scene
//        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/main-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load());
//
//        // Set stage title and scene
//        stage.setTitle("Main View");
//        stage.setScene(scene);
//
//        // More stage config
//        // Todo: make it not resizable?
//        stage.setMaximized(true);
////        stage.setFullScreen(true);

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/spectrogram-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Spectrogram");
        stage.setScene(scene);
        stage.setMaximized(true);

        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
