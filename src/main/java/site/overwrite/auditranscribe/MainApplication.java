/*
 * MainApplication.java
 *
 * Created on 2022-02-09
 * Updated on 2022-05-10
 *
 * Description: Contains the main application class.
 */

package site.overwrite.auditranscribe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;

public class MainApplication extends Application {
    // Initialization method
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file into the scene
        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set stage properties
        stage.setTitle("Welcome to AudiTranscribe");
        stage.setScene(scene);
        stage.setResizable(false);

        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
