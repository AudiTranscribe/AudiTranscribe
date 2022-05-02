/*
 * ApplicationScenes.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-02
 *
 * Description: Enum that contains the possible scenes that the application can toggle to.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import site.overwrite.auditranscribe.utils.FileUtils;

import java.io.IOException;

/**
 * Enum that contains the possible scenes that the application can toggle to.
 */
public enum ApplicationScenes {
    // Enum values
    MAIN_SCENE("main-view.fxml"),
    SPECTROGRAM_SCENE("spectrogram-view.fxml");

    // Attributes
    public final Scene scene;

    // Enum constructor
    ApplicationScenes(String sceneFile) {
        // Get the scene
        Scene tempScene;

        try {
            // Get the root scene
            Parent root = FXMLLoader.load(FileUtils.getFileURL("views/fxml/" + sceneFile));

            // Create the scene
            tempScene = new Scene(root);

        } catch (IOException e) {
            tempScene = null;
            e.printStackTrace();
        }

        // Actually assign to the attribute
        scene = tempScene;
    }
}
