/*
 * PropertyFileTest.java
 * Description: Test `PropertyFile.java`.
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

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.exceptions.NoSuchPropertyException;

import java.io.IOException;

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
