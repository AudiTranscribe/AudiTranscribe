/*
 * UnitConversionUtils.java
 * Description: Unit conversion utilities.
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

import app.auditranscribe.generic.exceptions.FormatException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit conversion utilities.
 */
public final class UnitConversionUtils {
    private UnitConversionUtils() {
        // Private constructor to signal this is a utility class
    }

    // Music unit conversion

    /**
     * Convert one or more note names to frequency.
     *
     * @param note Note string. Notes may be spelled out with optional accidentals or octave
     *             numbers. The leading note name is case-insensitive. Sharps are indicated with
     *             <code>#</code> or <code>♯</code>, flats may be indicated with <code>!</code>,
     *             <code>b</code>, or <code>♭</code>.
     * @return Frequency of that note.
     */
    public static double noteToFreq(String note) {
        return noteNumberToFreq(noteToNoteNumber(note));
    }

    /**
     * Converts a spelled note to the note number.<br>
     * Note that the note number for C0 is 0 and subsequent note numbers are given by their offset
     * from C0. For example, A4 has note number 57 as it is 57 notes away from C0.
     *
     * @param note Note string. Notes may be spelled out with optional accidentals or octave
     *             numbers. The leading note name is case-insensitive. Sharps are indicated with
     *             <code>#</code> or <code>♯</code>, flats may be indicated with <code>!</code>,
     *             <code>b</code>, or <code>♭</code>.
     * @return Note number for the given note.
     * @throws FormatException the note format is incorrect.
     * @implNote Double sharp and double flat are not currently supported.
     */
    public static int noteToNoteNumber(String note) {
        // Define constants
        final Map<String, Integer> PITCH_MAP = Map.of(
                "C", 0,
                "D", 2,
                "E", 4,
                "F", 5,
                "G", 7,
                "A", 9,
                "B", 11
        );

        final Map<String, Integer> ACC_MAP = Map.of(
                "#", 1,
                "♯", 1,
                "", 0,
                "b", -1,
                "!", -1,
                "♭", -1
        );

        final Pattern NOTE_PATTERN = Pattern.compile(
                "^(?<tonic>[A-Ga-g])(?<accidental>[#♯b!♭]*)(?<octave>[+-]?\\d+)?$"
        );

        // Attempt to match the `note` string to the pattern
        Matcher matcher = NOTE_PATTERN.matcher(note);
        if (!matcher.find()) {
            throw new FormatException("Improper note format '" + note + "'");
        }

        // Get the matched groups from the note
        String pitchChar = matcher.group("tonic").toUpperCase();
        String offsetChars = matcher.group("accidental");
        String octaveChar = matcher.group("octave");

        // Now get the actual required numbers to form the note number
        int offset = 0;
        for (int i = 0; i < offsetChars.length(); i++) {
            offset += ACC_MAP.get(Character.toString(offsetChars.charAt(i)));
        }

        int octave;
        if (octaveChar == null) {
            octave = 0;
        } else {
            octave = Integer.parseInt(octaveChar);
        }

        // Calculate note number and return
        return 12 * octave + PITCH_MAP.get(pitchChar) + offset;
    }

    /**
     * Converts the note number to a frequency.
     *
     * @param noteNumber The note number. Note that a note number of 0 means the key C0.
     * @return Frequency of the note with that note number. The returned frequency assumes that the
     * notes have been tuned to A440.
     */
    public static double noteNumberToFreq(int noteNumber) {
        return 440 * Math.pow(2, (noteNumber - 57.) / 12);
    }

    // Audio unit conversion

    /**
     * Method that converts a frequency in Hertz (Hz) into (fractional) octave numbers.<br>
     * This method assumes that there is no tuning deviation from A440 (i.e.
     * <code>tuning = 0</code>).
     *
     * @param hz Frequency in Hertz.
     * @return Octave number for the specified frequency.
     */
    public static double hzToOctaves(double hz) {
        return MathUtils.log2(hz) - 4.781359713524660;  // log2(hz) + log2(16/440), to 16 sf
    }
}
