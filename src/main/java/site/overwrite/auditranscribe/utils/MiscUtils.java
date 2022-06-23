/*
 * MiscUtils.java
 *
 * Created on 2022-04-30
 * Updated on 2022-06-23
 *
 * Description: Miscellaneous utility methods.
 */

package site.overwrite.auditranscribe.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Miscellaneous utility methods.
 */
public class MiscUtils {
    // Time utils

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
    public static int numOfSetBits(int value) {
        // Get the number which has all ones in the binary representation
        int allOnes = value;  // Initially set as the given value
        allOnes |= (allOnes >> 1);
        allOnes |= (allOnes >> 2);
        allOnes |= (allOnes >> 4);
        allOnes |= (allOnes >> 8);
        allOnes |= (allOnes >> 16);

        // Get number of digits in the binary representation of the `allOnes` integer
        return (int) MathUtils.log2(allOnes + 1);
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

    // Hyperlink utils

    /**
     * Method that opens the desired URL in the browser.
     *
     * @param url URL to open in the browser.
     */
    public static void openURLInBrowser(String url) {
        // Get the desktop instance
        Desktop desktop = Desktop.getDesktop();

        // Try and browse to the URL
        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
