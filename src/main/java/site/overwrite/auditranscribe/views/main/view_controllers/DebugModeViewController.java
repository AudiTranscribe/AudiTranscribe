/*
 * DebugModeViewController.java
 * Description: View controller that shows the debug view.
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

package site.overwrite.auditranscribe.views.main.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.Theme;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class DebugModeViewController extends ClassWithLogging implements Initializable {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ListView<String> debugList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    // Public methods

    public void setListContent(Map<String, String> listContent) {
        // Todo add
    }

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        // Get the theme
        Theme theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }
}
