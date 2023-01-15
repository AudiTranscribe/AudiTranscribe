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

import app.auditranscribe.fxml.Theme;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

/**
 * An abstract view controller that defines some useful methods for use in other view controllers.
 */
public abstract class AbstractViewController extends LoggableClass implements Initializable {
    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public abstract void setThemeOnScene();

    /**
     * Method that finishes the setting up of the view controller.<br>
     * This method may be called after showing the stage.
     */
    public void finishSetup() {
        // We assume that nothing needs to be done; can update in child classes
    }

    // Protected methods

    /**
     * Helps update the theme CSS loaded by resetting all theme CSS files before adding the new one.
     *
     * @return A <code>Theme</code> value, representing the theme that the scene was updated to.
     */
    protected Theme updateThemeCSS(Pane rootPane) {
        // Get the new theme
        Theme theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Clear old themes
        for (String url : Theme.getThemeCSSURLs()) {
            rootPane.getStylesheets().remove(url);
        }

        // Set new theme
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("fxml/css/theme/" + theme.cssFile));

        // Return the retrieved theme
        return theme;
    }
}
