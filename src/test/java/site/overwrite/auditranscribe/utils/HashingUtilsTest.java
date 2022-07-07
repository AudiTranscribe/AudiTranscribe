/*
 * HashingUtilsTest.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Test `HashingUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class HashingUtilsTest {

    @Test
    void getHash() throws NoSuchAlgorithmException, IOException {
        assertEquals(
                "518e662b79258f0b5c3610e3b4eb6a1f",
                HashingUtils.getHash("Testing String", "MD5")
        );
        assertEquals(
                "06b06f79bec3540f48c257e29f160f52e14ffbb761cd43c9af36394289759d7a",
                HashingUtils.getHash("Testing String 2", "SHA256")
        );
        assertEquals(
                "2ef7bde608ce5404e97d5f042f95f89f1c232871",
                HashingUtils.getHash("Hello World!", "SHA1")
        );

        assertEquals(
                "82216a8639d73c66768c1ee040d85fe9",
                HashingUtils.getHash(new File(IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "testing-files", "text", "MyFile.txt"
                )), "MD5")
        );
        assertThrows(IOException.class, () -> HashingUtils.getHash(new File("not a file"), "MD5"));

        assertThrows(
                NoSuchAlgorithmException.class,
                () -> HashingUtils.getHash("Not an algorithm", "algorithm123")
        );
    }
}