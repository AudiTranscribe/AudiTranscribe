/*
 * IOMethodsTest.java
 *
 * Created on 2022-05-10
 * Updated on 2022-06-25
 *
 * Description: Test `IOMethods.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IOMethodsTest {
    // Define the file path
    static final String FILE_PATH = IOMethods.joinPaths(
            IOConstants.ROOT_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "io-testing-directory", "files", "testFile.txt"
    );

    @Test
    @Order(1)
    void createFile() {
        // The test file should create successfully
        assertEquals(0, IOMethods.createFile(FILE_PATH));

        // Attempt to create the same file again should return 1
        assertEquals(1, IOMethods.createFile(FILE_PATH));
    }

    @Test
    @Order(2)
    void deleteFile() throws IOException {
        // First time round, the file should be deleted successfully
        assertTrue(IOMethods.deleteFile(FILE_PATH));

        // Second time round, the file could not be deleted and thus returns `false`
        assertFalse(IOMethods.deleteFile(FILE_PATH));
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void treatPath() {
        assertEquals("C:/testing/file/1/hello.txt", IOMethods.treatPath("/C:/testing/file/1/hello.txt"));
        assertEquals("D:\\testing\\file\\2\\hello.txt", IOMethods.treatPath("\\D:\\testing\\file\\2\\hello.txt"));
        assertEquals("/nothing/unusual/", IOMethods.treatPath("/nothing/unusual/"));
        assertEquals("E:/there are now spaces/test.txt/", IOMethods.treatPath("/E:/there%20are%20now%20spaces/test.txt/"));
    }
}