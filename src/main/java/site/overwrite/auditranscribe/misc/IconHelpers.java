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

package site.overwrite.auditranscribe.misc;

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
     * @param width     Width of the image.
     * @param iconName  Name of the icon to set.
     * @param themeName Theme name, which determines the colour of the icon.
     */
    public static void setSVGPath(SVGPath svgPath, double width, String iconName, String themeName) {
        svgPath.setContent(getIconSVGPath(iconName));
        svgPath.setFill(getIconColour(themeName));
        resize(svgPath, width);
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
     * @param svgPath  Object to resize.
     * @param newWidth New width to resize.<br>
     *                 Note that the width and the height will be exactly the same.
     */
    private static void resize(SVGPath svgPath, double newWidth) {
        double originalWidth = svgPath.prefWidth(-1);
        double originalHeight = svgPath.prefHeight(originalWidth);

        double scaleX = newWidth / originalWidth;
        double scaleY = newWidth / originalHeight;

        svgPath.setScaleX(scaleX);
        svgPath.setScaleY(scaleY);
    }
}
