package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class MiscUtilsTest {
    // Time methods
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
        assertEquals(432000, MiscUtils.getUnixTimestamp(Clock.offset(constantClock2, duration3)), 1e-5);

        assertEquals(1330837579, MiscUtils.getUnixTimestamp(Clock.offset(constantClock3, duration1)), 1e-5);
        assertEquals(1330839607, MiscUtils.getUnixTimestamp(Clock.offset(constantClock3, duration2)), 1e-5);
        assertEquals(1331269567, MiscUtils.getUnixTimestamp(Clock.offset(constantClock3, duration3)), 1e-5);
    }

    @Test
    void formatDate() {
        // Set timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Define dates to test
        Date date1 = new Date(1330837567000L);  // 2012-03-04 05:06:07.000
        Date date2 = new Date(11841818460000L);  // 2345-04-03 02:01:00.000
        Date date3 = new Date(2147483647000L);  // 2038-01-19 03:14:07.000, the Epochalypse

        // Assertions
        assertEquals("2012-03-04", MiscUtils.formatDate(date1, "yyyy-MM-dd"));
        assertEquals("2345-04-03 02:01:00", MiscUtils.formatDate(date2, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("38", MiscUtils.formatDate(date3, "yy"));
    }

    // Bit manipulation methods
    @Test
    void getNumSetBits() {
        assertEquals(0, MiscUtils.getNumSetBits(0));
        assertEquals(1, MiscUtils.getNumSetBits(1));
        assertEquals(2, MiscUtils.getNumSetBits(2));
        assertEquals(2, MiscUtils.getNumSetBits(3));
        assertEquals(4, MiscUtils.getNumSetBits(13));
        assertEquals(7, MiscUtils.getNumSetBits(127));
        assertEquals(21, MiscUtils.getNumSetBits(1924282));
    }

    // Other manipulation methods
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

    @Test
    void intAsPaddedHexStr() {
        assertEquals("0x00000005", MiscUtils.intAsPaddedHexStr(5));
        assertEquals("0x0000000D", MiscUtils.intAsPaddedHexStr(13));
        assertEquals("0x0000ABCD", MiscUtils.intAsPaddedHexStr(43981));
        assertEquals("0x1234ABCD", MiscUtils.intAsPaddedHexStr(305441741));
    }
}