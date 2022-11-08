/*
 * CannotFindFFmpegViewController.java
 * Description: View controller of the view that reports the FFmpeg could not be found.
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

package site.overwrite.auditranscribe.views.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller of the view that reports the FFmpeg could not be found.
 */
public class CannotFindFFmpegViewController extends ClassWithLogging implements Initializable {
    // Attributes
    private boolean isSpecifyManually;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button specifyManuallyButton, readInstructionsAgainButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        specifyManuallyButton.setOnMouseClicked(event -> {
            isSpecifyManually = true;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        readInstructionsAgainButton.setOnMouseClicked(event -> {
            isSpecifyManually = false;
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        log(Level.INFO, "Can't find FFmpeg");
    }

    // Getter methods

    /**
     * Method that returns the value of <code>isSpecifyManually</code>.
     *
     * @return Value of <code>isSpecifyManually</code>.
     */
    public boolean getIsSpecifyManually() {
        return isSpecifyManually;
    }

    // Public methods

    /**
     * Method that sets the scene's theme.
     *
     * @param theme Theme to set.
     */
    public void setThemeOnScene(Theme theme) {
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }
}
