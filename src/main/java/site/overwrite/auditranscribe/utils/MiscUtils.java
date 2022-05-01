/*
 * MiscUtils.java
 *
 * Created on 2022-04-30
 * Updated on 2022-04-30
 *
 * Description: Miscellaneous utility methods.
 */

package site.overwrite.auditranscribe.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

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
}
