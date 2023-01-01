package app.auditranscribe.io;

import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IOMethodsTest {
    // Test constants
    static final String TESTING_FILES_PATH = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "test-files"
    );
    static final String FILE_FOR_TESTING_CREATION_AND_DELETION_PATH = IOMethods.joinPaths(
            TESTING_FILES_PATH, "IOMethodsTest", "FileForTestingCreationAndDeletion.txt"
    );
    static final String FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED = IOMethods.joinPaths(
            TESTING_FILES_PATH, "nonexistent-directory", "FakeFile.txt"
    );

    // File path methods
    @Test
    @Order(0)
    void getApplicationDataDirectory() {
        OSType os = OSMethods.getOS();
        String appDir = IOMethods.getApplicationDataDirectory();

        // Check if the specific local data directory is present
        if (os == OSType.WINDOWS) {
            assertTrue(appDir.contains("AppData"));
        } else if (os == OSType.MAC) {
            assertTrue(appDir.contains("Application Support"));
        }

        // Check that "AudiTranscribe" is present
        assertTrue(appDir.contains("AudiTranscribe"));
    }

    // Sequenced CRUD operation tests
    @Test
    @Order(1)
    void createFile() {
        // The test file should create successfully
        assertEquals(0, IOMethods.createFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempting to create the same file again should return 1
        assertEquals(1, IOMethods.createFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempting to make a file in a folder that does not exist should return -1
        assertEquals(-1, IOMethods.createFile(new File(FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED)));
    }

    @Test
    @Order(2)
    void delete_forFiles() {
        // First time round, the file should be deleted successfully
        assertTrue(IOMethods.delete(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Second time round, the file could not be deleted and thus returns `false`
        assertFalse(IOMethods.delete(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to delete a file in a folder that does not exist should return `false`
        assertFalse(IOMethods.delete(new File(FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED)));
    }

    @Test
    @Order(2)
    @EnabledOnOs({OS.WINDOWS})
    void delete_whileInUseShouldCauseExceptionOnWindows() throws IOException {
        // Attempt to delete file while it is being used should return false (only on Windows), and
        // then delete file on exit
        String testFilePath = IOMethods.joinPaths(
                TESTING_FILES_PATH, "IOMethodsTest", "lock-file.txt"
        );
        IOMethods.createFile(testFilePath);

        try (
                RandomAccessFile reader = new RandomAccessFile(testFilePath, "rw");
                FileLock ignored = reader.getChannel().lock()
        ) {
            assertFalse(IOMethods.delete(testFilePath));
        }
    }

    @Test
    @Order(3)
    void createFolder_1() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(TESTING_FILES_PATH, "new-directory-1");

        // The test folder should create successfully
        assertTrue(IOMethods.createFolder(testDirectory));

        // Second time round, the folder should not be created
        assertFalse(IOMethods.createFolder(testDirectory));
    }

    @Test
    @Order(4)
    void delete_forFolders1() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(TESTING_FILES_PATH, "new-directory-1");

        // Delete the folder
        assertTrue(IOMethods.delete(testDirectory));

        // Attempting to delete again should return false
        assertFalse(IOMethods.delete(testDirectory));
    }

    @Test
    @Order(5)
    void createFolder_2() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(
                TESTING_FILES_PATH, "new-directory-2", "new-sub-directory", "new-sub-sub-directory"
        );

        // The test folder should create successfully
        assertTrue(IOMethods.createFolder(testDirectory));

        // Second time round, the folder should not be created
        assertFalse(IOMethods.createFolder(testDirectory));
    }

    @Test
    @Order(6)
    void delete_forFolders2() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(
                TESTING_FILES_PATH, "new-directory-2", "new-sub-directory", "new-sub-sub-directory"
        );

        // Now delete the folders
        assertTrue(IOMethods.delete(testDirectory));
        assertTrue(IOMethods.delete(new File(testDirectory).getParentFile()));  // Test delete on file object
        assertTrue(IOMethods.delete(new File(testDirectory).getParentFile().getParent()));  // Test delete on path

        // Attempting to delete again should return false
        assertFalse(IOMethods.delete(testDirectory));
        assertFalse(IOMethods.delete(new File(testDirectory).getParent()));  // Delete on file object
        assertFalse(IOMethods.delete(new File(testDirectory).getParentFile().getParent()));  // Delete on path
    }

    // Other CRUD operation tests
    @Test
    @Order(0)
    void readAsString() throws IOException {
        assertEquals(
                "SOME TEXT Π",
                IOMethods.readAsString(
                        IOMethods.joinPaths("test-files", "IOMethodsTest", "EncodingTestFile.txt"),
                        "UTF-8"
                )
        );
        assertEquals(
                "SOME TEXT ��",
                IOMethods.readAsString(
                        IOMethods.joinPaths("test-files", "IOMethodsTest", "EncodingTestFile.txt"),
                        "ASCII"
                )
        );
        assertThrowsExactly(NullPointerException.class, () -> IOMethods.readAsString(
                "not-a-file-that-exists", "UTF-8"
        ));
    }

    // Path handling
    @Test
    @Order(0)
    void joinPaths() {
        // Using default separator
        assertEquals("a/bc/def", IOMethods.joinPaths("a", "bc", "def"));
        assertEquals("a/bc/def", IOMethods.joinPaths("a/", "bc/", "/def"));
        assertEquals("a/bc/def", IOMethods.joinPaths("a", null, "bc", null, null, null, "/def"));

        // Using native separator
        assertEquals(
                "a" + IOConstants.SEPARATOR + "bc" + IOConstants.SEPARATOR + "def",
                IOMethods.joinPaths(true, "a", "bc", "def")
        );
        assertEquals(
                "a" + IOConstants.SEPARATOR + "bc" + IOConstants.SEPARATOR + "def",
                IOMethods.joinPaths(
                        true,
                        "a" + IOConstants.SEPARATOR, "bc" + IOConstants.SEPARATOR, IOConstants.SEPARATOR + "def"
                )
        );
        assertEquals(
                "a" + IOConstants.SEPARATOR + "bc" + IOConstants.SEPARATOR + "def",
                IOMethods.joinPaths(
                        true,
                        "a", null, "bc", null, null, null, IOConstants.SEPARATOR + "def"
                )
        );
    }
}