/*
 * PropertyFileTest.java
 *
 * Created on 2022-07-02
 * Updated on 2022-07-02
 *
 * Description: Test `PropertyFile.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.io.NoSuchPropertyException;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PropertyFileTest {
    // Define the file path
    static final String FILE_PATH = IOMethods.joinPaths(
            IOConstants.RESOURCES_FOLDER_PATH, "testing-files", "misc", "testing-properties-file.properties"
    );

    // Define attributes
    PropertyFile propertyFile;

    // Initialization method
    public PropertyFileTest() throws IOException {
        propertyFile = new PropertyFile(FILE_PATH);
    }

    @Test
    void nonExistentPropertiesFile() {
        assertThrowsExactly(IOException.class, () -> new PropertyFile("Nonexistent.properties"));
    }

    @Test
    void getProperty() {
        assertEquals("hello", propertyFile.getProperty("prop1"));
        assertEquals("world", propertyFile.getProperty("prop2"));
        assertEquals("123.456", propertyFile.getProperty("prop3"));
        assertThrowsExactly(NoSuchPropertyException.class, () -> propertyFile.getProperty("prop4"));

        assertEquals("hello", propertyFile.getProperty("prop1", "none"));
        assertEquals("world", propertyFile.getProperty("prop2", "none"));
        assertEquals("123.456", propertyFile.getProperty("prop3", "none"));
        assertEquals("none", propertyFile.getProperty("prop4", "none"));
    }
}
