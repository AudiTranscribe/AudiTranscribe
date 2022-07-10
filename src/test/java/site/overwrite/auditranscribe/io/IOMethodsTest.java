/*
 * IOMethodsTest.java
 *
 * Created on 2022-05-10
 * Updated on 2022-07-09
 *
 * Description: Test `IOMethods.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
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
        assertTrue(IOMethods.delete(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Second time round, the file could not be deleted and thus returns `false`
        assertFalse(IOMethods.delete(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to delete a file in a folder that does not exist should return `false`
        assertFalse(IOMethods.delete(FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED));
    }

    @Test
    @Order(2)
    @EnabledOnOs({OS.WINDOWS})
        // No exception is thrown on macOS and Linux apparently
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
            assertFalse(IOMethods.delete(testFilePath));
        }
    }

    @Test
    @Order(3)
    void createDirectoryOne() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "new-directory-1"
        );

        // The test folder should create successfully
        assertTrue(IOMethods.createFolder(testDirectory));

        // Second time round, the folder should not be created
        assertFalse(IOMethods.createFolder(testDirectory));

        // Now delete the folder
        assertTrue(IOMethods.delete(testDirectory));

        // Attempting to delete again should return false
        assertFalse(IOMethods.delete(testDirectory));
    }

    @Test
    @Order(3)
    void createDirectoryTwo() {
        // Define the path to the test directory
        String testDirectory = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "new-directory-2", "new-sub-directory", "new-sub-sub-directory"
        );

        // The test folder should create successfully
        assertTrue(IOMethods.createFolder(testDirectory));

        // Second time round, the folder should not be created
        assertFalse(IOMethods.createFolder(testDirectory));

        // Now delete the folders
        assertTrue(IOMethods.delete(testDirectory));
        assertTrue(IOMethods.delete(new File(testDirectory).getParent()));
        assertTrue(IOMethods.delete(new File(new File(testDirectory).getParent()).getParent()));

        // Attempting to delete again should return false
        assertFalse(IOMethods.delete(testDirectory));
        assertFalse(IOMethods.delete(new File(testDirectory).getParent()));
        assertFalse(IOMethods.delete(new File(new File(testDirectory).getParent()).getParent()));
    }

    // File location handling
    @Test
    void isFileAt() {
        assertTrue(IOMethods.isSomethingAt(IOMethods.getAbsoluteFilePath("testing-files/text/README.txt")));
        assertTrue(IOMethods.isSomethingAt(IOMethods.getAbsoluteFilePath("conf/logging.properties")));
        assertTrue(IOMethods.isSomethingAt(IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH, "testing-files"
        )));
        assertFalse(IOMethods.isSomethingAt("this-is-a-totally-fake-file.fakefile.fake"));
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
        assertTrue(IOMethods.isSomethingAt(originalFilePath));
        assertFalse(IOMethods.isSomethingAt(newFilePath));

        // Now move the file
        IOMethods.moveFile(originalFilePath, newFilePath);

        // Check if the file was moved
        assertFalse(IOMethods.isSomethingAt(originalFilePath));
        assertTrue(IOMethods.isSomethingAt(newFilePath));

        // Now move back
        IOMethods.moveFile(newFilePath, originalFilePath);

        // Check if the file was moved back
        assertTrue(IOMethods.isSomethingAt(originalFilePath));
        assertFalse(IOMethods.isSomethingAt(newFilePath));

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
    void treatPath() {
        assertEquals("testing/file/1/hello.txt", IOMethods.treatPath("testing/file/1/hello.txt"));
        assertEquals("testing\\file\\2\\hello.txt", IOMethods.treatPath("testing\\file\\2\\hello.txt"));
        assertEquals("nothing/unusual/", IOMethods.treatPath("nothing/unusual/"));
        assertEquals(
                "there%20are%20n%6fw%20spaces/test.txt/",
                IOMethods.treatPath("there%20are%20n%6fw%20spaces/test.txt/")
        );

        assertEquals("C:/testing/file/1/hello.txt", IOMethods.treatPath("/C:/testing/file/1/hello.txt"));
        assertEquals("D:\\testing\\file\\2\\hello.txt", IOMethods.treatPath("\\D:\\testing\\file\\2\\hello.txt"));
        assertEquals(
                "E:/there are now spaces/test.txt/",
                IOMethods.treatPath("/E:/there%20are%20n%6fw%20spaces/test.txt/")
        );
    }

    @Test
    void splitPaths() {
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a/bc/def/ghij"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a/bc/def/ghij/"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a/bc/def/ghij////"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg/"));

        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a\\bc\\def\\ghij"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a\\bc\\def\\ghij\\"));
        assertArrayEquals(new String[]{"a", "bc", "def", "ghij"}, IOMethods.splitPaths("a\\bc\\def\\ghij\\\\\\\\"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg"));
        assertArrayEquals(new String[]{"abcdefg"}, IOMethods.splitPaths("abcdefg\\"));
    }

    @Test
    void inputStreamToString() throws IOException {
        assertEquals(
                "SOME TEXT Π",
                IOMethods.inputStreamToString(
                        IOMethods.getInputStream(
                                IOMethods.joinPaths("testing-files", "text", "EncodingTestFile.txt")
                        ),
                        "UTF-8"
                )
        );
        assertEquals(
                "SOME TEXT ��",
                IOMethods.inputStreamToString(
                        IOMethods.getInputStream(
                                IOMethods.joinPaths("testing-files", "text", "EncodingTestFile.txt")
                        ),
                        "ASCII")
        );

        assertThrowsExactly(NullPointerException.class, () -> IOMethods.inputStreamToString(
                IOMethods.getInputStream("not-a-file"), "UTF-8"
        ));
    }

    @Test
    void numFilesInDir() {
        // Define base testing directory path
        String testingDirPath = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files"
        );

        // Make sure the directories has the required testing files
        if (!IOMethods.isSomethingAt(IOMethods.joinPaths(testingDirPath, ".DS_Store"))) {
            IOMethods.createFile(IOMethods.joinPaths(testingDirPath, ".DS_Store"));
        }

        if (IOMethods.isSomethingAt(IOMethods.joinPaths(testingDirPath, "text", ".DS_Store"))) {
            IOMethods.delete(IOMethods.joinPaths(testingDirPath, "text", ".DS_Store"));
        }

        // Run tests
        assertEquals(5, IOMethods.numThingsInDir(testingDirPath));
        assertEquals(3, IOMethods.numThingsInDir(IOMethods.joinPaths(testingDirPath, "text")));
        assertEquals(-1, IOMethods.numThingsInDir("not-a-dir"));
    }
}