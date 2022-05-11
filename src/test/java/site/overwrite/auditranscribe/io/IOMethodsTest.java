/*
 * IOMethodsTest.java
 *
 * Created on 2022-05-10
 * Updated on 2022-05-11
 *
 * Description: Test `IOMethods.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IOMethodsTest {
    @BeforeAll
    static void createAppDataFolder() {
        boolean createFolderAttempt = IOMethods.createAppDataFolder();
        if (createFolderAttempt) {
            System.out.println("Created new AudiTranscribe app data folder.");
        } else {
            System.out.println("Using existing AudiTranscribe app data folder.");
        }
    }

    @Test
    @Order(1)
    void createFile() {
        // The test file should create successfully
        assertEquals(0, IOMethods.createFile(IOMethods.APP_DATA_FOLDER_PATH_STRING + "testFile.txt"));

        // Attempt to create the same file again should return 1
        assertEquals(1, IOMethods.createFile(IOMethods.APP_DATA_FOLDER_PATH_STRING + "testFile.txt"));
    }

    @Test
    @Order(2)
    void deleteFile() {
        // First time round, the file should be deleted successfully
        assertTrue(IOMethods.deleteFile(IOMethods.APP_DATA_FOLDER_PATH_STRING + "testFile.txt"));

        // Second time round, the file could not be deleted and thus returns `false`
        assertFalse(IOMethods.deleteFile(IOMethods.APP_DATA_FOLDER_PATH_STRING + "testFile.txt"));
    }
}