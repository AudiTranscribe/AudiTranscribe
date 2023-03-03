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

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

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

    /**
     * Method that formats the given date according to the format.
     *
     * @param date   A <code>Date</code> object describing the date to format.
     * @param format The format for the date.
     * @return String representing the formatted date.
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
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

    // Other methods

    /**
     * Method that creates a shortened name based on the provided name.<br>
     * The shortened name will follow the following rules.
     * <ol>
     *     <li>
     *         Strip all whitespace before and after the name.
     *     </li>
     *     <li>
     *         If there are <b>no characters</b> in the stripped name, the shortened name will be
     *         <code>?</code>.
     *     </li>
     *     <li>
     *         Get all the <b>uppercase</b> letters in <code>name</code>.
     *     </li>
     *     <li>
     *         If there are no uppercase letters, take the <b>first alphabetical character</b> of
     *         the <code>name</code> and use it as the shortened name.
     *     </li>
     *     <li>
     *         If there are no alphabetical characters in the name, take the <b>first character</b>
     *         of the <code>name</code> and use it as the shortened name.
     *     </li>
     *     <li>
     *         If there are more than 2 uppercase letters, take the <b>first two uppercase
     *         characters</b> and use it as the shortened name.
     *     </li>
     *     <li>
     *         Otherwise, take all the <b>uppercase letters</b> and use it as the shortened name.
     *     </li>
     * </ol>
     * Note that the shortened name will <b>always be capitalised</b>.
     *
     * @param name The long name to shorten.
     * @return A string, representing the shortened name.
     */
    public static String getShortenedName(String name) {
        name = name.strip();
        if (Objects.equals(name, "")) return "?";

        // Get the uppercase letters and alphabetical characters of the name
        List<String> uppercaseLetters = new ArrayList<>();
        List<String> alphabeticalCharacters = new ArrayList<>();

        int length = name.length();
        for (int i = 0; i < length; i++) {
            char ch = name.charAt(i);
            String charAsString = String.valueOf(ch);

            if (Character.isLetter(ch)) {
                alphabeticalCharacters.add(charAsString);

                if (Character.isUpperCase(ch)) {
                    uppercaseLetters.add(charAsString);
                }
            }
        }

        // Create a variable to store the final shortened name
        StringBuilder shortNameBuffer = new StringBuilder();

        // Check if there is at least one uppercase letter
        int numUppercaseLetters = uppercaseLetters.size();
        if (numUppercaseLetters >= 1) {
            int numChars = Math.min(numUppercaseLetters, 2);
            for (int i = 0; i < numChars; i++) shortNameBuffer.append(uppercaseLetters.get(i));
        } else {
            // No uppercase letters; check if there is at least one alphabetical character
            if (alphabeticalCharacters.size() >= 1) {
                shortNameBuffer.append(alphabeticalCharacters.get(0).toUpperCase());
            } else {
                // No alphabetical characters; take first character
                shortNameBuffer.append(String.valueOf(name.charAt(0)).toUpperCase());
            }
        }

        return shortNameBuffer.toString();
    }

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

    // Randomisation utils

    /**
     * Method that generates a UUID string based on a given seed.
     *
     * @param seed Seed to generate the UUID on.
     * @return Generated UUID string.
     */
    public static String generateUUID(long seed) {
        // Declare random generator
        Random random = new Random(seed);

        // Generate UUID seed
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt());
        }

        // Generate UUID from bytes
        return UUID.nameUUIDFromBytes(sb.toString().getBytes()).toString();
    }
}
