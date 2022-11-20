/*
 * SpecifyFFmpegPathViewController.java
 * Description: View controller of the view that allows the user to manually specify the path to
 *              FFmpeg.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.views.setup_wizard.view_controllers;

import app.auditranscribe.misc.Popups;
import app.auditranscribe.views.main.ProjectIOHandlers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller of the view that allows the user to manually specify the path to FFmpeg.
 */
public class SpecifyFFmpegPathViewController extends AbstractSetupViewController {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField ffmpegBinaryPathTextField;

    @FXML
    private Button selectFFmpegBinaryButton, checkFFmpegPathButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectFFmpegBinaryButton.setOnAction(event -> {
            // Define file extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "FFmpeg binary",
                    "*.exe", "*"
            );

            // Get the file
            File possibleFFmpegBinary = ProjectIOHandlers.openFileDialog(
                    rootPane.getScene().getWindow(), extFilter
            );

            // Check if the FFmpeg binary is valid
            if (possibleFFmpegBinary != null) {
                // Update the value of the FFmpeg path text field
                ffmpegBinaryPathTextField.setText(possibleFFmpegBinary.getAbsolutePath());
                log(Level.INFO, "FFmpeg path set to " + ffmpegBinaryPathTextField.getText());
            } else {
                Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
            }
        });
        checkFFmpegPathButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());

        log(Level.INFO, "Allowing user to select FFmpeg path");
    }

    // Getter methods

    /**
     * Method that returns the value of <code>customFFmpegPath</code>.
     *
     * @return Value of <code>customFFmpegPath</code>.
     */
    public String getCustomFFmpegPath() {
        return ffmpegBinaryPathTextField.getText();
    }
}
