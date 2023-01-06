package app.auditranscribe.io;

import app.auditranscribe.io.exceptions.NoSuchPropertyException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PropertyFileTest {
    // Constants
    static final String FILE_PATH = IOMethods.joinPaths(
            IOConstants.RESOURCES_FOLDER_PATH, "test-files", "PropertyFileTest", "testing-properties-file.properties"
    );

    // Attributes
    PropertyFile propertyFile;

    public PropertyFileTest() throws IOException {
        propertyFile = new PropertyFile(FILE_PATH);
    }

    // Tests

    @Test
    void nonexistentPropertiesFile() {
        assertThrowsExactly(IOException.class, () -> new PropertyFile("Nonexistent.properties"));
    }

    @Test
    void getProperty() throws NoSuchPropertyException {
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