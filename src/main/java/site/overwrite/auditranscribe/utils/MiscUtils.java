/*
 * MiscUtils.java
 *
 * Created on 2022-04-30
 * Updated on 2022-05-12
 *
 * Description: Miscellaneous utility methods.
 */

package site.overwrite.auditranscribe.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Miscellaneous utility methods.
 */
public class MiscUtils {
    // Music utils

    /**
     * Gets the notes in the music key <code>key</code>.
     *
     * @param key Music key.
     * @return A <code>HashSet</code> object containing integer <b>offsets</b> from C (i.e. number
     * of notes above the C key). Each integer in the returned  is the modulo 12 of a note number
     * that is in the key.
     */
    public static HashSet<Integer> getNotesInKey(String key) {
        // Convert the key into a note number for easier processing
        int keyNum = UnitConversion.noteToNoteNumber(key + "0");

        // Get the note offsets and place them into an array (for now)
        Integer[] noteOffsets = switch (keyNum % 12) {  // See the keys inside each musical scale
            default -> new Integer[]{0, 2, 4, 5, 7, 9, 11};  // Default is C, i.e. offset 0
            case 1 -> new Integer[]{1, 3, 5, 6, 8, 10, 0};
            case 2 -> new Integer[]{2, 4, 6, 7, 9, 11, 1};
            case 3 -> new Integer[]{3, 5, 7, 8, 10, 0, 2};
            case 4 -> new Integer[]{4, 6, 8, 9, 11, 1, 3};
            case 5 -> new Integer[]{5, 7, 9, 10, 0, 2, 4};
            case 6 -> new Integer[]{6, 8, 10, 11, 1, 3, 5};
            case 7 -> new Integer[]{7, 9, 11, 0, 2, 4, 6};
            case 8 -> new Integer[]{8, 10, 0, 1, 3, 5, 7};
            case 9 -> new Integer[]{9, 11, 1, 2, 4, 6, 8};
            case 10 -> new Integer[]{10, 0, 2, 3, 5, 7, 9};
            case 11 -> new Integer[]{11, 1, 3, 4, 6, 8, 10};
        };

        // Convert to a hash set and return
        return new HashSet<>(Arrays.asList(noteOffsets));
    }

    // Time utils

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
        // Get both the current epoch second and the nanosecond part
        Instant now = Instant.now();  // Get current time

        long epochSecond = now.getEpochSecond();
        int epochNanoseconds = now.getNano();

        // Combine into a single double object and return
        return Double.parseDouble(epochSecond + "." + epochNanoseconds);
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
