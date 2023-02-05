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

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract view controller that defines some useful methods for use in other view controllers.
 */
public abstract class AbstractViewController extends LoggableClass implements Initializable {
    // Constants
    public final static List<AbstractViewController> ACTIVE_VIEW_CONTROLLERS = new ArrayList<>();

    /**
     * Initialization method for a new <code>AbstractViewController</code>.
     */
    public AbstractViewController() {
        ACTIVE_VIEW_CONTROLLERS.add(this);
    }

    // Public methods

    /**
     * Method that sets the current theme on the scene.
     */
    public void setThemeOnScene() {
        setThemeOnScene(getPreferredTheme());
    }

    /**
     * Method that sets the theme for the scene.
     *
     * @param theme Theme to set on the scene.
     * @implNote The general format of this method's implementation should look like this:
     * <pre><code>
     *     updateThemeCSS(rootPane, theme);
     *     setGraphics(theme);
     * </code></pre>
     * This means that the implementation should utilize the {@link #updateThemeCSS(Pane, Theme)}
     * and {@link #setGraphics(Theme)} methods.
     */
    public abstract void setThemeOnScene(Theme theme);

    /**
     * Method that gets the user's preferred theme, as defined in the settings file.
     *
     * @return The theme that the user prefers.
     */
    public Theme getPreferredTheme() {
        return Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];
    }

    /**
     * Method that updates all the active views' themes at once.
     *
     * @param theme Theme to set on all active views.
     */
    public static void updateActiveViewsThemes(Theme theme) {
        for (AbstractViewController controller : ACTIVE_VIEW_CONTROLLERS) controller.setThemeOnScene(theme);
    }

    /**
     * Method that finishes the setting up of the view controller.<br>
     * This method may be called after showing the stage.
     */
    public void finishSetup() {
        // We assume that nothing needs to be done; can update in child classes
    }

    /**
     * Removes this controller from the list of active view controllers.
     */
    public void removeControllerFromActive() {
        ACTIVE_VIEW_CONTROLLERS.remove(this);
    }

    // Protected methods

    /**
     * Helps update the theme CSS loaded by resetting all theme CSS files before adding the new one.
     *
     * @param rootPane Root pane of the scene.
     * @param theme    Theme to apply to the scene.
     */
    protected void updateThemeCSS(Pane rootPane, Theme theme) {
        // Clear old themes
        for (String url : Theme.getThemeCSSURLs()) {
            rootPane.getStylesheets().remove(url);
        }

        // Set new theme
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("fxml/css/theme/" + theme.cssFile));
    }

    /**
     * Helps to set the specific graphics on the scene.
     *
     * @param theme The theme to use when setting the graphics.
     */
    protected abstract void setGraphics(Theme theme);
}
