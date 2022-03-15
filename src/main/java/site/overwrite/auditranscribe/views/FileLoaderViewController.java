/*
 * FileLoaderViewController.java
 *
 * Created on 2022-03-15
 * Updated on 2022-03-15
 *
 * Description: Testing view to test file loading dialog.
 */

package site.overwrite.auditranscribe.views;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileLoaderViewController implements Initializable {
    @FXML
    private Button openFileButton;

    final FileChooser fileChooser = new FileChooser();

    private final Desktop desktop = Desktop.getDesktop();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set the title of the file chooser
        fileChooser.setTitle("Open Resource File");

        // Define the action to take when the open file button is clicked
        openFileButton.setOnAction(event -> {
            // Get current window
            Window window = ((Node) event.getSource()).getScene().getWindow();

            // Ask user to choose a file
            File file = fileChooser.showOpenDialog(window);

            // Verify that the user choose a file
            if (file != null) {
                openFile(file);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "No file selected");
            }
        });
    }

    private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger("File dialog log").log(Level.SEVERE, null, ex);
        }
    }
}
