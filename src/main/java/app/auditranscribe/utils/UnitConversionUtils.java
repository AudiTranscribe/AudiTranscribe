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
    // Constants
    static final double POWER_TO_DB_MIN_AMPLITUDE = 1e-10;

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

    /**
     * Converts the frequency to an estimated note number.
     *
     * @param freq Frequency of the note with that note number.<br>
     *             The returned frequency assumes that the notes have been tuned to A440.
     * @return The estimated note number as a decimal number.
     */
    public static double freqToNoteNumber(double freq) {
        return 12 * MathUtils.log2(freq / 440) + 57;
    }

    // Audio unit conversion

    /**
     * Convert a power value (amplitude squared) to decibel (dB) units.
     *
     * @param power  Input power.
     * @param refVal Value such that the amplitude <code>abs(power)</code> is scaled relative to
     *               <code>refVal</code> using the formula <code>10 * log10(power / refVal)</code>.
     * @return Decibel value for the given power.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#power_to_db">
     * Librosa's Implementation</a> of this function.
     */
    public static double powerToDecibel(double power, double refVal) {
        // Treat the reference value
        refVal = Math.abs(refVal);

        // Calculate decibel
        double logSpec = 10 * Math.log10(Math.max(POWER_TO_DB_MIN_AMPLITUDE, power));
        logSpec -= 10 * Math.log10(Math.max(POWER_TO_DB_MIN_AMPLITUDE, refVal));

        // Return it
        return logSpec;
    }

    /**
     * Convert an amplitude value to a dB-scaled value.<br>
     * This is equivalent to <code>powerToDecibel(Math.pow(amplitude, 2))</code>, but is provided
     * for convenience.
     *
     * @param amplitude Input amplitude.
     * @param refVal    Value such that the amplitude <code>abs(power)</code> is scaled relative to
     *                  <code>refVal</code> using the formula <code>20 * log10(power /
     *                  refVal)</code>.
     * @return Decibel value for the given amplitude.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#amplitude_to_db">
     * Librosa's Implementation</a> of this function.
     */
    public static double amplitudeToDecibel(double amplitude, double refVal) {
        return powerToDecibel(amplitude * amplitude, refVal * refVal);
    }

    /**
     * Method that converts a frequency in Hertz (Hz) into mel frequency (mels).
     *
     * @param freq Frequency in Hertz.
     * @return Frequency in mel.
     */
    public static double hzToMel(double freq) {
        // Fill in the linear scale
        double fMin = 0;
        double fSp = 200. / 3;
        double mel = (freq - fMin) / fSp;

        // And now the log-scale
        double minLogHz = 1000.;                     // Beginning of log region (Hz)
        double minLogMel = (minLogHz - fMin) / fSp;
        double logstep = 0.06875177742094912;        // Step size for log region, equals `Math.log(6.4) / 27` to 16 sf

        if (freq >= minLogHz) {
            mel = minLogMel + Math.log(freq / minLogHz) / logstep;
        }

        // Return the final mel value
        return mel;
    }

    /**
     * Method that converts a frequency in Mels into frequency in Hertz (Hz).
     *
     * @param mel Frequency in Mel.
     * @return Frequency in Hertz.
     */
    public static double melToHz(double mel) {
        // Fill in the linear scale
        double fMin = 0;
        double fSp = 200. / 3;
        double freq = fMin + fSp * mel;

        // And now the log-scale
        double minLogHz = 1000.;                     // Beginning of log region (Hz)
        double minLogMel = (minLogHz - fMin) / fSp;
        double logstep = 0.06875177742094912;        // Step size for log region, equals `Math.log(6.4) / 27` to 16 sf

        if (mel >= minLogMel) {
            freq = minLogHz * Math.exp(logstep * (mel - minLogMel));
        }

        return freq;
    }

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
