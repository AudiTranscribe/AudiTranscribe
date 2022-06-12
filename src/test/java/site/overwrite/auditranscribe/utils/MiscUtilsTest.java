/*
 * MiscUtilsTest.java
 *
 * Created on 2022-05-11
 * Updated on 2022-06-01
 *
 * Description: Test `MiscUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiscUtilsTest {
    @Test
    void setBits() {
        assertEquals(0, MiscUtils.numOfSetBits(0));
        assertEquals(1, MiscUtils.numOfSetBits(1));
        assertEquals(2, MiscUtils.numOfSetBits(2));
        assertEquals(2, MiscUtils.numOfSetBits(3));
        assertEquals(4, MiscUtils.numOfSetBits(13));
        assertEquals(7, MiscUtils.numOfSetBits(127));
        assertEquals(21, MiscUtils.numOfSetBits(1924282));
    }
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