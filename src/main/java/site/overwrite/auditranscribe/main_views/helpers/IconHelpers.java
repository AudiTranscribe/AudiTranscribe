/*
 * IconHelpers.java
 * Description: Helper methods for setting icons on SVG path objects.
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

package site.overwrite.auditranscribe.main_views.helpers;

import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import site.overwrite.auditranscribe.io.data_files.DataFiles;

/**
 * Helper methods for setting icons on SVG path objects.
 */
public final class IconHelpers {
    private IconHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that sets up the <code>SVGPath</code> FXML element.
     *
     * @param svgPath   Object to set up.
     * @param length    Length of the image.<br>
     *                  Note that the width and the height will be exactly the same.
     * @param iconName  Name of the icon to set.
     * @param themeName Theme name, which determines the colour of the icon.
     */
    public static void setSVGPath(SVGPath svgPath, double length, String iconName, String themeName) {
        svgPath.setContent(getIconSVGPath(iconName));
        svgPath.setFill(getIconColour(themeName));
        resize(svgPath, length);
    }

    /**
     * Method that sets up the <code>SVGPath</code> FXML element.
     *
     * @param svgPath   Object to set up.
     * @param width     Width of the image.
     * @param height    Height of the image.
     * @param iconName  Name of the icon to set.
     * @param themeName Theme name, which determines the colour of the icon.
     */
    public static void setSVGPath(
            SVGPath svgPath, double width, double height, String iconName, String themeName
    ) {
        svgPath.setContent(getIconSVGPath(iconName));
        svgPath.setFill(getIconColour(themeName));
        resize(svgPath, width, height);
    }

    /**
     * Method that helps set an SVG on a button.
     *
     * @param button       Button to set the SVG on.
     * @param svgLength    Length of the SVG to be placed on the button.<br>
     *                     Note that both the width and the height will be the same.
     * @param buttonLength The final length of the button.<br>
     *                     Note that both the width and the height will be the same.
     * @param iconName     Name of the icon to set.
     * @param themeName    Theme name, which determines the colour of the icon.
     */
    public static void setSVGOnButton(
            Button button, double svgLength, double buttonLength, String iconName, String themeName
    ) {
        setSVGOnButton(button, svgLength, svgLength, buttonLength, buttonLength, iconName, themeName);
    }

    /**
     * Method that helps set an SVG on a button.
     *
     * @param button       Button to set the SVG on.
     * @param svgWidth     Width of the SVG to be placed on the button.
     * @param svgHeight    Height of the SVG to be placed on the button.
     * @param buttonWidth  The final width of the button.
     * @param buttonHeight The final height of the button.
     * @param iconName     Name of the icon to set.
     * @param themeName    Theme name, which determines the colour of the icon.
     */
    public static void setSVGOnButton(
            Button button, double svgWidth, double svgHeight, double buttonWidth, double buttonHeight,
            String iconName, String themeName
    ) {
        // Create the SVG path object first
        SVGPath svgPath = new SVGPath();
        setSVGPath(svgPath, svgWidth, svgHeight, iconName, themeName);

        // Set the SVG path on the button
        button.setGraphic(svgPath);

        // Update its width and height
        button.setPrefSize(buttonWidth, buttonHeight);
        button.setMinSize(buttonWidth, buttonHeight);
        button.setMaxSize(buttonWidth, buttonHeight);
    }

    // Private methods

    /**
     * Helper method that retrieves the icon's SVG path from the icon data file.
     *
     * @param iconName Name of the icon.
     * @return SVG path of the icon.
     */
    private static String getIconSVGPath(String iconName) {
        return DataFiles.ICONS_DATA_FILE.data.svgPaths.get(iconName);
    }

    /**
     * Helper method that retrieves the icon's colour based on the theme name.
     *
     * @param themeName Theme to obtain the colour for.
     * @return A <code>Paint</code> object representing the colour of the icon.
     */
    private static Paint getIconColour(String themeName) {
        return Paint.valueOf(DataFiles.ICONS_DATA_FILE.data.themeColours.get(themeName));
    }

    /**
     * Helper method that helps resize the SVG object correctly.
     *
     * @param svgPath   Object to resize.
     * @param newLength New length to resize.<br>
     *                  Note that the width and the height will be exactly the same.
     */
    private static void resize(SVGPath svgPath, double newLength) {
        resize(svgPath, newLength, newLength);
    }

    /**
     * Helper method that helps resize the SVG object correctly.
     *
     * @param svgPath   Object to resize.
     * @param newWidth  New width to resize to.
     * @param newHeight New height to resize to.
     */
    private static void resize(SVGPath svgPath, double newWidth, double newHeight) {
        double originalWidth = svgPath.prefWidth(-1);
        double originalHeight = svgPath.prefHeight(-1);

        double scaleX = newWidth / originalWidth;
        double scaleY = newHeight / originalHeight;

        svgPath.setScaleX(scaleX);
        svgPath.setScaleY(scaleY);
    }
}
