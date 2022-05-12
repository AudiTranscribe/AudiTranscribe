/*
 * MiscUtilsTest.java
 *
 * Created on 2022-05-11
 * Updated on 2022-05-11
 *
 * Description: Test `MiscUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiscUtilsTest {
    @Test
    void getShortenedName() {
        assertEquals("A", MiscUtils.getShortenedName("Abracadabra"));
        assertEquals("AB", MiscUtils.getShortenedName("Abracadabra Bowing"));
        assertEquals("AB", MiscUtils.getShortenedName("Abracadabra Bowling Coach"));
        assertEquals("A", MiscUtils.getShortenedName("abracadabra"));
        assertEquals("3", MiscUtils.getShortenedName(" 314159    "));
        assertEquals("M", MiscUtils.getShortenedName("314159m"));
        assertEquals("?", MiscUtils.getShortenedName("?????"));
        assertEquals("?", MiscUtils.getShortenedName(""));
        assertEquals("?", MiscUtils.getShortenedName("    "));
    }
}