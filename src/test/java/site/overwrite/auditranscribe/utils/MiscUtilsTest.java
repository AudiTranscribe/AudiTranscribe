/*
 * MiscUtilsTest.java
 *
 * Created on 2022-05-11
 * Updated on 2022-06-25
 *
 * Description: Test `MiscUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class MiscUtilsTest {
    @Test
    void getUnixTimestamp() {
        // Define three clocks for testing
        Clock constantClock1 = Clock.fixed(Instant.parse("2018-01-01T10:00:00Z"), ZoneOffset.UTC);
        Clock constantClock2 = Clock.fixed(Instant.parse("1970-01-01T00:00:00Z"), ZoneOffset.UTC);
        Clock constantClock3 = Clock.fixed(Instant.parse("2012-03-04T05:06:07Z"), ZoneOffset.UTC);

        // Define offsets for testing
        Duration duration1 = Duration.ofSeconds(12);  // 10 seconds in the future
        Duration duration2 = Duration.ofMinutes(34);  // 34 minutes in the future
        Duration duration3 = Duration.ofDays(5);      // 5 days in the future

        // Assertions
        assertEquals(1514800812, MiscUtils.getUnixTimestamp(Clock.offset(constantClock1, duration1)), 1e-5);
        assertEquals(1514802840, MiscUtils.getUnixTimestamp(Clock.offset(constantClock1, duration2)), 1e-5);
        assertEquals(1515232800, MiscUtils.getUnixTimestamp(Clock.offset(constantClock1, duration3)), 1e-5);

        assertEquals(12, MiscUtils.getUnixTimestamp(Clock.offset(constantClock2, duration1)), 1e-5);
        assertEquals(2040, MiscUtils.getUnixTimestamp(Clock.offset(constantClock2, duration2)), 1e-5);
        assertEquals( 432000, MiscUtils.getUnixTimestamp(Clock.offset(constantClock2, duration3)), 1e-5);

        assertEquals(1330837579, MiscUtils.getUnixTimestamp(Clock.offset(constantClock3, duration1)), 1e-5);
        assertEquals(1330839607, MiscUtils.getUnixTimestamp(Clock.offset(constantClock3, duration2)), 1e-5);
        assertEquals(1331269567, MiscUtils.getUnixTimestamp(Clock.offset(constantClock3, duration3)), 1e-5);
    }

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