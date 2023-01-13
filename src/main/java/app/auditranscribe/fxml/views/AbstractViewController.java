/*
 * AbstractViewController.java
 * Description: An abstract view controller that defines some useful methods for use in other view
 *              controllers.
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

package app.auditranscribe.fxml.views;

import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Theme;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

/**
 * An abstract view controller that defines some useful methods for use in other view controllers.
 */
public abstract class AbstractViewController extends LoggableClass implements Initializable {
    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public abstract void setThemeOnScene();

    // Protected methods

    /**
     * Helps update the theme CSS loaded by resetting all theme CSS files before adding the new one.
     */
    protected void updateThemeCSS(AnchorPane rootPane) {
        // Get the new theme
        Theme theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Clear old themes
        for (String url : Theme.getThemeCSSURLs()) {
            rootPane.getStylesheets().remove(url);
        }

        // Set new theme
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("fxml/css/theme/" + theme.cssFile));
    }
}
