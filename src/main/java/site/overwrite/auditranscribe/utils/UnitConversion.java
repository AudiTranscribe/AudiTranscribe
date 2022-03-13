/*
 * UnitConversion.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-12
 *
 * Description: Unit conversion utilities.
 */

package site.overwrite.auditranscribe.utils;


import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Unit conversion utilities.
 */
public class UnitConversion {
    // Notes conversion

    /**
     * Convert one or more note names to frequency.
     * @param note  Note name.
     * @return  Frequency of that note.
     */
    public static double noteToFreq(String note) {
        return noteNumberToFreq(noteToNoteNumber(note));
    }

    /**
     * Converts a spelled note to the note number.<br>
     * Note that the note number for C0 is 0 and subsequent note numbers are given by their offset
     * from C0. For example, A4 has note number 57 as it is 57 notes away from C0.
     * @param note  Note string. Notes may be spelled out with optional accidentals or octave
     *              numbers. The leading note name is case-insensitive. Sharps are indicated with
     *              <code>#</code> or <code>♯</code>, flats may be indicated with <code>!</code>,
     *              <code>b</code>, or <code>♭</code>.
     * @return  Note number for the given note.
     * @throws InvalidParameterException    If the note format is incorrect.
     * @implNote Todo: find a way to incorporate double sharp and double flat
     */
    public static int noteToNoteNumber(String note) {
        // Define constants
        final Map<String, Integer> PITCH_MAP = Map.ofEntries(
                entry("C", 0),
                entry("D", 2),
                entry("E", 4),
                entry("F", 5),
                entry("G", 7),
                entry("A", 9),
                entry("B", 11)
        );

        final Map<String, Integer> ACC_MAP = Map.ofEntries(
                entry("#", 1),
                entry("", 0),
                entry("b", -1),
                entry("!", -1),
                entry("♯", 1),
                entry("♭", -1),
                entry("♮", 0)
        );

        final Pattern NOTE_PATTERN = Pattern.compile("^(?<note>[A-Ga-g])(?<accidental>[#♯b!♭♮]*)(?<octave>[+-]?\\d+)?$");

        // Attempt to match the `note` string to the pattern
        Matcher matcher = NOTE_PATTERN.matcher(note);
        if (!matcher.find()) {
            throw new InvalidParameterException("Improper note format '" + note + "'");
        }

        // Get the matched groups from the note
        String pitchChar = matcher.group("note").toUpperCase();
        String offsetChars = matcher.group("accidental");
        String octaveChar = matcher.group("octave");

        // Now get the actual required numbers to form the note number
        int offset = 0;
        for (int i = 0; i < offsetChars.length(); i++) {
            offset += ACC_MAP.get(Character.toString(offsetChars.charAt(i)));
        }

        int octave;
        if (Objects.equals(octaveChar, "")) {
            octave = 0;
        } else {
            octave = Integer.parseInt(octaveChar);
        }

        // Calculate note number and return
        return 12 * octave + PITCH_MAP.get(pitchChar) + offset;
    }

    /**
     * Converts the note number to a frequency.
     * @param noteNumber The note number. Note that a note number of 0 means the key C0.
     * @return  Frequency of the note with that note number. The returned frequency assumes that the
     * notes have been tuned to A440.
     */
    public static double noteNumberToFreq(int noteNumber) {
        return 440 * Math.pow(2, (noteNumber - 57.) / 12);
    }
}
