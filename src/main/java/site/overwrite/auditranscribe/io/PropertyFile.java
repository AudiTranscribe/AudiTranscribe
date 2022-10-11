/*
 * PropertyFile.java
 * Description: Class that handles the retrieval of data from a property file.
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

package site.overwrite.auditranscribe.io;

import site.overwrite.auditranscribe.MainApplication;
import site.overwrite.auditranscribe.io.exceptions.NoSuchPropertyException;

import java.io.IOException;
import java.util.Properties;

/**
 * Class that handles the retrieval of data from a property file.
 */
public class PropertyFile {
    // Attributes
    private final Properties properties = new Properties();

    /**
     * Initialization method of the <code>PropertyFile</code> class.
     *
     * @param propertyFileName Name of the property file, <b>including</b> the
     *                         <code>.properties</code> extension.<br>
     *                         Note that the property file <b>must be in the root resources
     *                         directory</b> (i.e. the one named <code>resources</code>).
     * @throws IOException If the file does not exist, or if the input stream is null.
     */
    public PropertyFile(String propertyFileName) throws IOException {
        // Load the properties from the property file into the `Properties` object
        try {
            properties.load(MainApplication.class.getClassLoader().getResourceAsStream(propertyFileName));
        } catch (NullPointerException e) {
            throw new IOException("Property file with name '" + propertyFileName + "' not found.");
        }

    }

    // Public methods

    /**
     * Method that gets a property with the specified name from the properties file.
     *
     * @param name         Name of the property.
     * @param defaultValue Default value, if the property with the desired name cannot be found.
     * @return String, representing the value of the property <b>or</b> the
     * <code>defaultValue</code> if the property is not found.
     */
    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    /**
     * Method that gets a property with the specified name from the properties file.
     *
     * @param name Name of the property.
     * @return String, representing the value of the property.
     * @throws NoSuchPropertyException If the property does not exist within the properties file.
     */
    public String getProperty(String name) throws NoSuchPropertyException {
        String value = properties.getProperty(name, null);

        if (value == null) {  // If `null` then the property does not exist
            throw new NoSuchPropertyException("The properties file does not have a property with name " + name);
        }

        return value;
    }
}
