/*
 * IOMethodsTest.java
 * Description: Test `IOMethods.java`.
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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IOMethodsTest {
    // Define the testing file path for testing the creation and deletion path
    static final String TESTING_FILES_PATH = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "testing-files"
    );
    static final String FILE_FOR_TESTING_CREATION_AND_DELETION_PATH = IOMethods.joinPaths(
            TESTING_FILES_PATH, "text", "FileForTestingCreationAndDeletion.txt"
    );
    static final String FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED = IOMethods.joinPaths(
            TESTING_FILES_PATH, "nonexistent-directory", "FakeFile.txt"
    );

    // File path handling
    @Test
    void getFileURLString() {
        String urlString = IOMethods.getFileURLAsString("testing-files/text/README.txt");
        assertTrue(urlString.contains("file:/"));
        assertTrue(urlString.contains("testing-files/text/README.txt"));
    }

    // IO handling
    @Test
    @Order(1)
    void createFile() {
        // The test file should create successfully
        assertEquals(0, IOMethods.createFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to create the same file again should return 1
        assertEquals(1, IOMethods.createFile(FILE_FOR_TESTING_CREATION_AND_DELETION_PATH));

        // Attempt to make a file in a folder that does not exist should return -1
        assertEquals(-1, IOMethods.createFile(new File(FILE_THAT_SHOULD_NOT_BE_CREATED_OR_DELETED)));
    }

    @Test
    @Order(2)
    void deleteFile() {
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
        // No exception is thrown on macOS and Linux apparently
    void deleteFileWhileInUseShouldCauseException() throws IOException {
        // Attempt to delete file while it is being used should return false, and then delete file
        // on exit
        String testFilePath = IOMethods.joinPaths(
                TESTING_FILES_PATH, "text", "lock-file.txt"
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
        String testDirectory = IOMethods.joinPaths(TESTING_FILES_PATH, "new-directory-1");

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
                TESTING_FILES_PATH, "new-directory-2", "new-sub-directory", "new-sub-sub-directory"
        );

        // The test folder should create successfully
        assertTrue(IOMethods.createFolder(testDirectory));

        // Second time round, the folder should not be created
        assertFalse(IOMethods.createFolder(testDirectory));

        // Now delete the folders
        assertTrue(IOMethods.delete(testDirectory));
        assertTrue(IOMethods.delete(new File(testDirectory).getParentFile()));  // Test delete on file object
        assertTrue(IOMethods.delete(new File(testDirectory).getParentFile().getParent()));  // Test delete on path

        // Attempting to delete again should return false
        assertFalse(IOMethods.delete(testDirectory));
        assertFalse(IOMethods.delete(new File(testDirectory).getParent()));  // Delete on file object
        assertFalse(IOMethods.delete(new File(testDirectory).getParentFile().getParent()));  // Delete on path
    }

    @Test
    void copyFile() throws IOException {
        try {
            // Test 1: Should be successful
            boolean success1 = IOMethods.copyFile(
                    IOMethods.joinPaths(TESTING_FILES_PATH, "text", "MyFile.txt"),
                    IOMethods.joinPaths(TESTING_FILES_PATH, "text", "MyCopyFile.txt")
            );
            assertTrue(success1);
            assertEquals(
                    Files.readAllLines(Path.of(IOMethods.joinPaths(TESTING_FILES_PATH, "text", "MyFile.txt"))),
                    Files.readAllLines(Path.of(IOMethods.joinPaths(TESTING_FILES_PATH, "text", "MyCopyFile.txt")))
            );

            // Test 2: Should not be successful
            boolean success2 = IOMethods.copyFile(
                    "this-file-does-not-exist", "this-path-should-not-work"
            );
            assertFalse(success2);
        } finally {
            IOMethods.delete(IOMethods.joinPaths(TESTING_FILES_PATH, "text", "MyCopyFile.txt"));
        }

    }

    // File location handling
    @Test
    void isFileAt() {
        assertFalse(IOMethods.isSomethingAt(null));
        assertTrue(IOMethods.isSomethingAt(IOMethods.getAbsoluteFilePath("testing-files/text/README.txt")));
        assertTrue(IOMethods.isSomethingAt(IOMethods.getAbsoluteFilePath("conf/logging.properties")));
        assertTrue(IOMethods.isSomethingAt(TESTING_FILES_PATH));
        assertFalse(IOMethods.isSomethingAt("this-is-a-totally-fake-file.fakefile.fake"));
    }

    @Test
    void moveFile() throws IOException {
        // Define paths
        String originalFilePath = IOMethods.joinPaths(TESTING_FILES_PATH, "text", "MyFile.txt");
        String newFilePath = IOMethods.joinPaths(TESTING_FILES_PATH, "MyNewFile.txt");

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
                IOMethods.getInputStream("not-a-file-that-exists"), "UTF-8"
        ));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void numFilesInDir() {
        // Make sure the directories has the required testing files
        if (!IOMethods.isSomethingAt(IOMethods.joinPaths(TESTING_FILES_PATH, ".DS_Store"))) {
            IOMethods.createFile(IOMethods.joinPaths(TESTING_FILES_PATH, ".DS_Store"));
        }

        if (IOMethods.isSomethingAt(IOMethods.joinPaths(TESTING_FILES_PATH, "text", ".DS_Store"))) {
            IOMethods.delete(IOMethods.joinPaths(TESTING_FILES_PATH, "text", ".DS_Store"));
        }

        // Run tests
        assertEquals(6, IOMethods.numThingsInDir(TESTING_FILES_PATH));
        assertEquals(3, IOMethods.numThingsInDir(IOMethods.joinPaths(TESTING_FILES_PATH, "text")));
        assertEquals(-1, IOMethods.numThingsInDir("not-a-dir"));
    }
}