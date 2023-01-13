/*
 * MiscUtils.java
 * Description: Miscellaneous utilities.
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

package app.auditranscribe.utils;

import java.time.Clock;
import java.time.Instant;

/**
 * Miscellaneous utilities.
 */
public final class MiscUtils {
    private MiscUtils() {
        // Private constructor to signal this is a utility class
    }

    // Time methods

    /**
     * Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.<br>
     * The epoch second count is a simple incrementing count of seconds, where 0 represents
     * 1970-01-01T00:00:00Z (i.e. Unix Epoch).
     *
     * @param clock Clock to use for generating the Unix timestamp.
     * @return A double representing the number of seconds. Decimal part is the fractional second
     * part.
     * @implNote The fractional part cannot be fully trusted; precision may only be accurate up to
     * 10 milliseconds (0.01 s).
     */
    public static double getUnixTimestamp(Clock clock) {
        // Get both the current epoch second and the nanosecond part
        Instant now = Instant.now(clock);  // Set the provided clock

        long epochSecond = now.getEpochSecond();
        int epochNanoseconds = now.getNano();

        // Combine into a single double object and return
        return Double.parseDouble(epochSecond + "." + epochNanoseconds);
    }

    /**
     * Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.<br>
     * The epoch second count is a simple incrementing count of seconds, 0 represents
     * 1970-01-01T00:00:00Z (i.e. Unix Epoch).<br>
     * Uses the system clock to calculate the timestamp.
     *
     * @return A double representing the number of seconds. Decimal part is the fractional second
     * part.
     * @implNote The fractional part cannot be fully trusted; precision may only be accurate up to
     * 10 milliseconds (0.01 s).
     */
    public static double getUnixTimestamp() {
        return getUnixTimestamp(Clock.systemUTC());
    }

    // Bit manipulation methods

    /**
     * Method that gets the number of bits set in the given integer.<br>
     * (i.e., the number of significant bits in the binary representation of the integer).
     *
     * @param value The integer to get the number of bits set in.
     * @return The number of bits set in the given integer.
     */
    public static int getNumSetBits(int value) {
        if (value == 0) return 0;
        return MathUtils.binlog(Integer.highestOneBit(value)) + 1;
    }

    // Other manipulation methods

    /**
     * Converts the provided integer into a padded hexadecimal string.
     *
     * @param x Positive integer to convert.
     * @return A 10-character hexadecimal string of the form <code>0xDDDDDDDD</code>.
     */
    public static String intAsPaddedHexStr(int x) {
        // Use the built-in hexadecimal converter first
        String initialHex = Integer.toHexString(x).toUpperCase();

        // Add "0x" and remaining zeroes
        return "0x" +
                "0".repeat(8 - initialHex.length()) +
                initialHex;
    }
}
