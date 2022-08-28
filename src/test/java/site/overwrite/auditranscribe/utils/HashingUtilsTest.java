/*
 * HashingUtilsTest.java
 * Description: Test `HashingUtils.java`.
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
                "0c7f9618ef4b39f56d2ba77cf7cfc079",
                HashingUtils.getHash(new File(IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "testing-files", "text", "README.txt"
                )), "MD5")
        );
        assertThrows(IOException.class, () -> HashingUtils.getHash(new File("not a file"), "MD5"));

        assertThrows(
                NoSuchAlgorithmException.class,
                () -> HashingUtils.getHash("Not an algorithm", "algorithm123")
        );
    }
}