/*
 * JSONFileTest.java
 *
 * Created on 2022-07-03
 * Updated on 2022-07-03
 *
 * Description: Test `JSONFile.java`.
 */


package site.overwrite.auditranscribe.io.json_files;

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
class JSONFileTest {
    // Attributes
    static String FILE_PATH = IOMethods.joinPaths(IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH, "testing-files", "my-json.json");
    static JSONFile<TestingJSONData> jsonFile;

    // Tests
    @Test
    @Order(1)
    void initJSONFileObject() {
        jsonFile = new TestingJSONFile();
    }

    @Test
    @Order(2)
    void checkFileContentsOne() {
        assertEquals(1, jsonFile.data.attr1);
        assertEquals(2.345, jsonFile.data.attr2);
        assertFalse(jsonFile.data.attr3);
        assertEquals("Yes", jsonFile.data.attr4);
        assertEquals("This should not be changed", jsonFile.data.attr5);
    }

    @Test
    @Order(3)
    void saveFile() {
        // Modify some data
        jsonFile.data.attr1 = -1;
        jsonFile.data.attr2 = -2.345;
        jsonFile.data.attr3 = true;
        jsonFile.data.attr4 = "No";

        // Save the file
        jsonFile.saveFile();
    }

    @Test
    @Order(4)
    void checkFileContentsTwo() {
        // Re-initialize the `jsonFile` object
        jsonFile = new TestingJSONFile();

        // Check attribute values
        assertEquals(-1, jsonFile.data.attr1);
        assertEquals(-2.345, jsonFile.data.attr2);
        assertTrue(jsonFile.data.attr3);
        assertEquals("No", jsonFile.data.attr4);
        assertEquals("This should not be changed", jsonFile.data.attr5);
    }

    @Test
    @Order(5)
    void checkFileSaveFailure() {
        // Delete original file
        IOMethods.deleteFile(FILE_PATH);

        // Make it a folder
        IOMethods.createFolder(FILE_PATH);

        // Now try and save
        assertThrowsExactly(RuntimeException.class, () -> jsonFile.saveFile());
    }

    @Test
    @Order(6)
    void spoofJSONData() throws IOException {
        // Delete folder
        IOMethods.deleteFile(FILE_PATH);

        // Write spoofed file
        PrintWriter writer = new PrintWriter(FILE_PATH, StandardCharsets.UTF_8);
        writer.println("Not JSON data");
        writer.close();
    }

    @Test
    @Order(7)
    void checkInitializationFails() {
        assertThrowsExactly(JsonIOException.class, TestingJSONFile::new);
    }

    @Test
    @Order(8)
    void deleteJSONFile() {
        IOMethods.deleteFile(FILE_PATH);
    }

    @Test
    @Order(9)
    void incorrectInitializerTest() {
        assertThrowsExactly(FailedToMakeJSONFileException.class, WrongTestingJSONFile::new);
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

    static class TestingJSONFile extends JSONFile<TestingJSONData> {
        public TestingJSONFile() {
            super(JSONFileTest.FILE_PATH, TestingJSONData.class);
        }
    }

    static class WrongTestingJSONFile extends JSONFile<WrongTestingJSONData> {
        public WrongTestingJSONFile() {
            super(JSONFileTest.FILE_PATH, WrongTestingJSONData.class);
        }
    }
}