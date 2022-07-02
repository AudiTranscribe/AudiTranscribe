/*
 * IOMethodsTest.java
 *
 * Created on 2022-05-10
 * Updated on 2022-07-02
 *
 * Description: Test `IOMethods.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IOMethodsTest {
    // Define the testing file path for testing the creation and deletion path
    static final String FILE_FOR_TESTING_CREATION_AND_DELETION_PATH = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "testing-files", "text", "FileForTestingCreationAndDeletion.txt"
    );
    static final String FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "testing-files", "nonexistent-directory", "FakeFile.txt"
    );

    // File path handling
    @Test
    void getFileURLString() {
        String urlString = IOMethods.getFileURLAsString("testing-files/text/README.txt");
        assertTrue(urlString.contains("file:/"));
        assertTrue(urlString.contains("testing-files/text/README.txt"));
    }

    // IO Handling
    @Test
    @Order(1)
    void createFile() {
        // The test file should create successfully
        assertEquals(0, IOMethods.createFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to create the same file again should return 1
        assertEquals(1, IOMethods.createFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to make a file in a folder that does not exist should return -1
        assertEquals(-1, IOMethods.createFile(FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED));
    }

    @Test
    @Order(2)
    void deleteFile() {
        // First time round, the file should be deleted successfully
        assertTrue(IOMethods.deleteFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Second time round, the file could not be deleted and thus returns `false`
        assertFalse(IOMethods.deleteFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to delete a file in a folder that does not exist should return `false`
        assertFalse(IOMethods.deleteFile(FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED));
    }

    @Test
    @Order(2)
    @EnabledOnOs({OS.WINDOWS})
    void deleteFileWhileInUseShouldCauseException() throws IOException {
        // Attempt to delete file while it is being used should return false, and then delete file
        // on exit
        String testFilePath = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "text", "lock-file.txt"
        );
        IOMethods.createFile(testFilePath);

        try (
                RandomAccessFile reader = new RandomAccessFile(testFilePath, "rw");
                FileLock ignored = reader.getChannel().lock()
        ) {
            assertFalse(IOMethods.deleteFile(testFilePath));
        }
    }

    @Test
    @Order(3)
    void createDirectory() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "new-directory"
        );

        // The test folder should create successfully
        assertTrue(IOMethods.createFolder(testDirectory));

        // Second time round, the folder should not be created
        assertFalse(IOMethods.createFolder(testDirectory));

        // Now delete the folder
        assertTrue(IOMethods.deleteFile(testDirectory));
    }

    // File location handling
    @Test
    void isFileAt() {
        assertTrue(IOMethods.isFileAt(IOMethods.getAbsoluteFilePath("testing-files/text/README.txt")));
        assertTrue(IOMethods.isFileAt(IOMethods.getAbsoluteFilePath("conf/logging.properties")));
        assertFalse(IOMethods.isFileAt("this-is-a-totally-fake-file.fakefile.fake"));
    }

    @Test
    void moveFile() throws IOException {
        // Define paths
        String originalFilePath = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "text", "MyFile.txt"
        );
        String newFilePath = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "MyNewFile.txt"
        );

        // Check if the file that we want to move exists
        assertTrue(IOMethods.isFileAt(originalFilePath));
        assertFalse(IOMethods.isFileAt(newFilePath));

        // Now move the file
        IOMethods.moveFile(originalFilePath, newFilePath);

        // Check if the file was moved
        assertFalse(IOMethods.isFileAt(originalFilePath));
        assertTrue(IOMethods.isFileAt(newFilePath));

        // Now move back
        IOMethods.moveFile(newFilePath, originalFilePath);

        // Check if the file was moved back
        assertTrue(IOMethods.isFileAt(originalFilePath));
        assertFalse(IOMethods.isFileAt(newFilePath));

        // Test if the move file operation will fail if the file does not exist
        assertThrows(IOException.class, () -> IOMethods.moveFile(newFilePath, originalFilePath));

        // Test if the move file operation will fail if the destination does not exist
        assertThrows(IOException.class, () -> IOMethods.moveFile(
                originalFilePath, "qwerty/not-a-folder/text.txt"
        ));
    }

    // Path handling
    @Test
    void buildPath() {
        assertEquals(
                "a" + IOConstants.SEPARATOR + "bc" + IOConstants.SEPARATOR + "def",
                IOMethods.buildPath("a", "bc", "def")
        );
        assertEquals(
                "a" + IOConstants.SEPARATOR + "bc" + IOConstants.SEPARATOR + "def",
                IOMethods.buildPath(
                        "a" + IOConstants.SEPARATOR, "bc" + IOConstants.SEPARATOR, IOConstants.SEPARATOR + "def"
                )
        );
        assertEquals(
                "a" + IOConstants.SEPARATOR + "bc" + IOConstants.SEPARATOR + "def",
                IOMethods.buildPath(
                        "a", null, "bc", null, null, null, IOConstants.SEPARATOR + "def"
                )
        );
    }

    @Test
    void joinPaths() {
        assertEquals("a/bc/def", IOMethods.joinPaths("a", "bc", "def"));
        assertEquals("a//bc///def", IOMethods.joinPaths("a/", "bc/", "/def"));
        assertEquals("a/bc//def", IOMethods.joinPaths("a", null, "bc", null, null, null, "/def"));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void treatPathUnix() {
        assertEquals("testing/file/1/hello.txt", IOMethods.treatPath("testing/file/1/hello.txt"));
        assertEquals("testing\\file\\2\\hello.txt", IOMethods.treatPath("testing\\file\\2\\hello.txt"));
        assertEquals("nothing/unusual/", IOMethods.treatPath("nothing/unusual/"));
        assertEquals(
                "there%20are%20now%20spaces/test.txt/",
                IOMethods.treatPath("there%20are%20now%20spaces/test.txt/")
        );
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void treatPathWindows() {
        assertEquals("C:/testing/file/1/hello.txt", IOMethods.treatPath("/C:/testing/file/1/hello.txt"));
        assertEquals("D:\\testing\\file\\2\\hello.txt", IOMethods.treatPath("\\D:\\testing\\file\\2\\hello.txt"));
        assertEquals("/nothing/unusual/", IOMethods.treatPath("/nothing/unusual/"));
        assertEquals(
                "E:/there are now spaces/test.txt/",
                IOMethods.treatPath("/E:/there%20are%20now%20spaces/test.txt/")
        );
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void splitPathsUnix() {
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a/bc/def/ghij"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a/bc/def/ghij/"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a/bc/def/ghij////"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg/"));
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void splitPathsWindows() {
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a\\bc\\def\\ghij"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a\\bc\\def\\ghij\\"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a\\bc\\def\\ghij\\\\\\\\"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg\\"));
    }

    // Environmental variable management
    @Test
    void getOrDefault() {
        try {
            assertNotEquals("12345", IOMethods.getOrDefault("PATH", "12345"));
        } catch (AssertionFailedError e) {
            assertNotEquals("12345", IOMethods.getOrDefault("Path", "12345"));
        }
        assertEquals("67890", IOMethods.getOrDefault("not-a-real-environment-variable", "67890"));
    }
}