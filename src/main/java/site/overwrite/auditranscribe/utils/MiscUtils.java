/*
 * MiscUtils.java
 * Description: Miscellaneous utility methods.
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

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Miscellaneous utility methods.
 */
public final class MiscUtils {
    private MiscUtils() {
        // Private constructor to signal this is a utility class
    }

    // Time utils

    /**
     * Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.<br>
     * The epoch second count is a simple incrementing count of seconds where second 0 is
     * 1970-01-01T00:00:00Z (i.e. Unix Epoch).
     *
     * @param clock Clock to use for generating the Unix timestamp.
     * @return Double representing the number of seconds. Decimal part is the fractional second
     * part.
     * @implNote The fractional part cannot be fully trusted; precision may only be accurate up to
     * 10 milliseconds (i.e. up to 0.01 s).
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
     * The epoch second count is a simple incrementing count of seconds where second 0 is
     * 1970-01-01T00:00:00Z (i.e. Unix Epoch).
     *
     * @return Double representing the number of seconds. Decimal part is the fractional second
     * part.
     * @implNote The fractional part cannot be fully trusted; precision may only be accurate up to
     * 10 milliseconds (i.e. up to 0.01 s).
     */
    public static double getUnixTimestamp() {
        return getUnixTimestamp(Clock.systemUTC());  // Query current system UTC clock
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

    // Bit manipulation utils

    /**
     * Method that gets the number of bits set in the given integer.<br>
     * (i.e. the number of significant bits in the binary representation of the integer).
     *
     * @param value The integer to get the number of bits set in.
     * @return The number of bits set in the given integer.
     * @implNote This method is based on the algorithm found at
     * <a href="https://stackoverflow.com/a/2891946">this StackOverflow answer</a>.
     */
    public static int getNumSetBits(int value) {
        // Get the number which has all ones in the binary representation
        int allOnes = value;  // Initially set as the given value
        allOnes |= (allOnes >> 1);
        allOnes |= (allOnes >> 2);
        allOnes |= (allOnes >> 4);
        allOnes |= (allOnes >> 8);
        allOnes |= (allOnes >> 16);

        // Get number of digits in the binary representation of the `allOnes` integer
        return (int) MathUtils.round(MathUtils.log2(allOnes + 1), 10);  // To account for weird double rounding
    }

    // Name utils

    /**
     * Method that creates a shortened name based on the provided name.<br>
     * The shortened name will follow the following rules.
     * <ol>
     *     <li>
     *         Strip all whitespace before and after the name.
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
     *         If there are <b>no characters</b> in the name, the shortened name will be
     *         <code>?</code>.
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
        // Strip whitespace
        name = name.strip();

        // Handle the easiest case of `name` being an empty string
        if (Objects.equals(name, "")) return "?";

        // Get the uppercase letters and alphabetical characters of the name
        List<String> uppercaseLetters = new ArrayList<>();
        List<String> alphabeticalCharacters = new ArrayList<>();

        int length = name.length();
        for (int i = 0; i < length; i++) {
            // Get current character
            char ch = name.charAt(i);
            String charAsString = String.valueOf(ch);

            // Check if the `chr` is an alphabetical character
            if (Character.isLetter(ch)) {
                alphabeticalCharacters.add(charAsString);

                // Check if the alphabetical character is uppercase
                if (Character.isUpperCase(ch)) {
                    uppercaseLetters.add(charAsString);
                }
            }
        }

        // Create a variable to store the final shortened name
        StringBuilder shortNameBuff = new StringBuilder();

        // Check if there is at least one uppercase letter
        int numUppercaseLetters = uppercaseLetters.size();
        if (numUppercaseLetters >= 1) {
            // The shortened name is just the first 2 letters (or only letter) in that list
            int numChars = Math.min(numUppercaseLetters, 2);
            for (int i = 0; i < numChars; i++) {
                shortNameBuff.append(uppercaseLetters.get(i));
            }
        } else {  // No uppercase letters
            // Check if there is at least one alphabetical character
            int numAlphabeticalChars = alphabeticalCharacters.size();
            if (numAlphabeticalChars >= 1) {
                shortNameBuff.append(alphabeticalCharacters.get(0).toUpperCase());  // Take first alphabetical character
            } else {  // No alphabetical characters
                shortNameBuff.append(String.valueOf(name.charAt(0)).toUpperCase());  // Take first character
            }
        }

        // Return the shortened name
        return shortNameBuff.toString();
    }
}
