/*
 * IconsData.java
 * Description: Class that contains the icons' data.
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

package site.overwrite.auditranscribe.main_views.icon;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Class that contains the icons' data.
 */
public class IconsData extends ClassWithLogging {
    // Constants
    final String ICON_DATA_FILE_PATH = IOMethods.joinPaths("images", "icons", "icons.json");
    public static final IconsData ICONS_DATA = new IconsData();

    // Attributes
    public Map<String, String> themeColours;
    public Map<String, String> svgPaths;

    /**
     * Initialization method for a new <code>IconsData</code> object.
     */
    public IconsData() {
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

    // Helper class(es)
    static class IconsDataEncapsulator {
        public Map<String, String> themeColours;
        public Map<String, String> svgPaths;
    }
}
