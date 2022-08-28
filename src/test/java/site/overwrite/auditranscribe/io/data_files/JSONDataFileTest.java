/*
 * JSONDataFileTest.java
 * Description: Test JSON file interactions.
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

package site.overwrite.auditranscribe.io.data_files;

import com.google.gson.JsonIOException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import site.overwrite.auditranscribe.exceptions.io.FailedToMakeJSONFileException;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledOnOs({OS.LINUX})
class JSONDataFileTest {
    // Attributes
    static String FILE_PATH = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "testing-files", "my-json.json"
    );
    static JSONDataFile<TestingJSONData> jsonDataFile;

    // Tests
    @Test
    @Order(1)
    void initJSONFileObject() {
        jsonDataFile = new TestingJSONDataFile();
    }

    @Test
    @Order(2)
    void checkFileContentsOne() {
        assertEquals(1, jsonDataFile.data.attr1);
        assertEquals(2.345, jsonDataFile.data.attr2);
        assertFalse(jsonDataFile.data.attr3);
        assertEquals("Yes", jsonDataFile.data.attr4);
        assertEquals("This should not be changed", jsonDataFile.data.attr5);
    }

    @Test
    @Order(3)
    void saveFile() {
        // Modify some data
        jsonDataFile.data.attr1 = -1;
        jsonDataFile.data.attr2 = -2.345;
        jsonDataFile.data.attr3 = true;
        jsonDataFile.data.attr4 = "No";

        // Save the file
        jsonDataFile.saveFile();
    }

    @Test
    @Order(4)
    void checkFileContentsTwo() {
        // Re-initialize the `jsonDataFile` object
        jsonDataFile = new TestingJSONDataFile();

        // Check attribute values
        assertEquals(-1, jsonDataFile.data.attr1);
        assertEquals(-2.345, jsonDataFile.data.attr2);
        assertTrue(jsonDataFile.data.attr3);
        assertEquals("No", jsonDataFile.data.attr4);
        assertEquals("This should not be changed", jsonDataFile.data.attr5);
    }

    @Test
    @Order(5)
    void checkFileSaveFailure() {
        // Delete original file
        IOMethods.delete(FILE_PATH);

        // Make it a folder
        IOMethods.createFolder(FILE_PATH);

        // Now try and save
        assertThrowsExactly(RuntimeException.class, () -> jsonDataFile.saveFile());
    }

    @Test
    @Order(6)
    void spoofJSONData() throws IOException {
        // Delete folder
        IOMethods.delete(FILE_PATH);

        // Write spoofed file
        PrintWriter writer = new PrintWriter(FILE_PATH, StandardCharsets.UTF_8);
        writer.println("Not JSON data");
        writer.close();
    }

    @Test
    @Order(7)
    void checkInitializationFails() {
        assertThrowsExactly(JsonIOException.class, TestingJSONDataFile::new);
    }

    @Test
    @Order(8)
    void deleteJSONFile() {
        IOMethods.delete(FILE_PATH);
    }

    @Test
    @Order(9)
    void incorrectInitializerTest() {
        assertThrowsExactly(FailedToMakeJSONFileException.class, WrongTestingJSONDataFile::new);
    }

    // Helper classes
    public static class TestingJSONData {
        public int attr1 = 1;
        public double attr2 = 2.345;
        public boolean attr3 = false;
        public String attr4 = "Yes";
        public String attr5 = "This should not be changed";
    }

    public static class WrongTestingJSONData {
        public int attr1;
        public double attr2;

        public WrongTestingJSONData(int attr1, double attr2) {
            this.attr1 = attr1;
            this.attr2 = attr2;
        }
    }

    static class TestingJSONDataFile extends JSONDataFile<TestingJSONData> {
        public TestingJSONDataFile() {
            super(JSONDataFileTest.FILE_PATH, TestingJSONData.class);
        }
    }

    static class WrongTestingJSONDataFile extends JSONDataFile<WrongTestingJSONData> {
        public WrongTestingJSONDataFile() {
            super(JSONDataFileTest.FILE_PATH, WrongTestingJSONData.class);
        }
    }
}