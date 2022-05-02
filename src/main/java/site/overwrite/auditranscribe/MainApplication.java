/*
 * MainApplication.java
 *
 * Created on 2022-02-09
 * Updated on 2022-05-02
 *
 * Description: Contains the main application class.
 */

package site.overwrite.auditranscribe;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.views.ApplicationScenes;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        // Load the FXML file into the scene
//        FXMLLoader fxmlLoader = new FXMLLoader(FileUtils.getFile("views/fxml/main-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load());
//
//        // Set stage title and scene
//        stage.setTitle("Main View");
//        stage.setScene(scene);
//
//        // More stage config
//        // Todo: make it not resizable?
//        stage.setMaximized(true);
//        // stage.setFullScreen(true);

//        FXMLLoader fxmlLoader = new FXMLLoader(FileUtils.getFileURL("views/fxml/main-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load());
//        stage.setTitle("Welcome to AudiTranscribe");
//        stage.setScene(scene);
//        stage.setResizable(false);

        Scene scene = ApplicationScenes.SPECTROGRAM_SCENE.scene;
        stage.setTitle("Spectrogram");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);

//        FXMLLoader fxmlLoader = new FXMLLoader(FileUtils.getFileURL("views/fxml/file-loader-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load());
//        stage.setTitle("File Loader");
//        stage.setScene(scene);

        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
