/*
 * InitialViewController.java
 * Description: Controller for the initial setup wizard view.
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

package app.auditranscribe.fxml.views.setup_wizard.controllers;

import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the initial setup wizard view.
 */
@ExcludeFromGeneratedCoverageReport
public class InitialViewController extends AbstractViewController {
    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Label versionLabel;

    @FXML
    private Button continueButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up button methods
        continueButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
        continueButton.setCancelButton(true);
        continueButton.setDefaultButton(true);

        log("Setup wizard initial view ready to be shown");
    }

    // Getter/setter methods
    public void setCurrentVersion(String currentVersion) {
        versionLabel.setText(currentVersion);
    }

    // Public methods
    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
    }

    // Protected methods
    @Override
    protected void setGraphics(Theme theme) {
        // No graphics to set
    }
}
