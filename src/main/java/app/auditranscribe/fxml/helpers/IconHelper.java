/*
 * IconHelper.java
 * Description: Class that helps set icons on specific nodes.
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

package app.auditranscribe.fxml.helpers;

import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Class that helps set icons on specific nodes.
 */
@ExcludeFromGeneratedCoverageReport
public final class IconHelper {
    private IconHelper() {
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
        button.setText("");  // Clear text on button as well

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
        return IconsData.ICONS_DATA.svgPaths.get(iconName);
    }

    /**
     * Helper method that retrieves the icon's colour based on the theme name.
     *
     * @param themeName Theme to obtain the colour for.
     * @return A <code>Paint</code> object representing the colour of the icon.
     */
    private static Paint getIconColour(String themeName) {
        return Paint.valueOf(IconsData.ICONS_DATA.themeColours.get(themeName));
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

    // Helper classes

    /**
     * Class that contains the icons' data.
     */
    static final class IconsData extends LoggableClass {
        // Constants
        final String ICON_DATA_FILE_PATH = IOMethods.joinPaths("images", "icons", "icons.json");
        public static final IconsData ICONS_DATA = new IconsData();

        // Attributes
        public Map<String, String> themeColours;
        public Map<String, String> svgPaths;

        /**
         * Initialization method for a new <code>IconsData</code> object.
         */
        private IconsData() {
            try {
                // Create the GSON loader object
                Gson gson = new Gson();

                // Attempt to get the input stream
                InputStream inputStream = IOMethods.getInputStream(ICON_DATA_FILE_PATH);

                // Check if the input stream is null or not
                if (inputStream == null) {
                    throw new IOException("Cannot find the icons data file.");
                }

                try (Reader reader = new InputStreamReader(inputStream)) {
                    // Try loading the filter data
                    IconsDataEncapsulator data = gson.fromJson(reader, IconsDataEncapsulator.class);

                    // Set attributes
                    themeColours = data.themeColours;
                    svgPaths = data.svgPaths;
                } catch (JsonSyntaxException e) {
                    throw new IOException(e);
                }
            } catch (IOException e) {
                // Note that an exception has occurred
                logException(e);
            }
        }
    }

    /**
     * Class that encapsulates the <code>icons.json</code> data file's data.
     */
    static class IconsDataEncapsulator {
        public Map<String, String> themeColours;
        public Map<String, String> svgPaths;
    }
}
