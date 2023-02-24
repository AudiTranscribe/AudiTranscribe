/*
 * ThemeSetupViewController.java
 * Description: Controller that helps set up the theme for AudiTranscribe.
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
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller that helps set up the theme for AudiTranscribe.
 */
@ExcludeFromGeneratedCoverageReport
public class ThemeSetupViewController extends AbstractViewController {
    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ChoiceBox<Theme> themeChoiceBox;

    @FXML
    private Button confirmButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up theme choice box
        for (Theme theme : Theme.values()) themeChoiceBox.getItems().add(theme);
        themeChoiceBox.setValue(Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal]);
        themeChoiceBox.setOnAction(event -> {
            AbstractViewController.updateActiveViewsThemes(themeChoiceBox.getValue());
            log("Changed theme to " + getTheme());
        });

        // Set up other things
        confirmButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());

        log("Theme setup view ready to be shown");
    }

    // Getter/setter methods
    public Theme getTheme() {
        return themeChoiceBox.getValue();
    }

    // Public methods
    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
    }

    // Private methods
    @Override
    protected void setGraphics(Theme theme) {
    }
}
