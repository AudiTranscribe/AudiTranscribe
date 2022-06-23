/*
 * PropertyFile.java
 *
 * Created on 2022-05-02
 * Updated on 2022-06-23
 *
 * Description: Property file object to handle the loading and retrieval of data from a property
 *              file.
 */

package site.overwrite.auditranscribe.io;

import site.overwrite.auditranscribe.MainApplication;
import site.overwrite.auditranscribe.exceptions.io.NoSuchPropertyException;

import java.io.IOException;
import java.util.Properties;

/**
 * Property file object to handle the loading and retrieval of data from a property file.
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
        properties.load(MainApplication.class.getClassLoader().getResourceAsStream(propertyFileName));
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
