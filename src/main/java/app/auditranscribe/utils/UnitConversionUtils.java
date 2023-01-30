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
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.music.MusicKey;

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
     * Converts a note number to its spelled note.<br>
     * Note that the note number for C0 is 0 and subsequent note numbers are given by their offset
     * from C0. For example, A4 has note number 57 as it is 57 notes away from C0.
     *
     * @param noteNumber       Note number for the given note.
     * @param musicKey         Music key, with both the key and the mode.
     * @param fancyAccidentals Whether <em>fancier accidentals</em> (i.e. ♯ instead of # and ♭
     *                         instead of b) should be used.
     * @return Note string.
     */
    public static String noteNumberToNote(int noteNumber, MusicKey musicKey, boolean fancyAccidentals) {
        // Compute the octave and the key value
        int octave = Math.floorDiv(noteNumber, 12);  // Note that C0 has note number 0, C1 is 12, C2 is 24 etc.
        int key = noteNumber % 12;  // 0 = C, 1 = C#/Db, 2 = D, 3 = D#/Eb etc.

        // Determine which set of note strings to use
        String[] noteStrings;
        if (musicKey.usesFlats) {
            noteStrings = new String[]{"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
        } else {
            noteStrings = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        }

        // Replace notes if the key demands it
        if (musicKey == MusicKey.G_FLAT_MAJOR || musicKey == MusicKey.C_FLAT_MAJOR ||
                musicKey == MusicKey.E_FLAT_MINOR || musicKey == MusicKey.A_FLAT_MINOR) {
            noteStrings[11] = "Cb";  // Cb instead of B

            if (key == 11) octave++;  // Need to increase octave number by 1
        }

        if (musicKey == MusicKey.C_FLAT_MAJOR || musicKey == MusicKey.A_FLAT_MINOR) {
            noteStrings[4] = "Fb";  // Fb instead of E
        }

        if (musicKey == MusicKey.F_SHARP_MAJOR || musicKey == MusicKey.C_SHARP_MAJOR ||
                musicKey == MusicKey.D_SHARP_MINOR || musicKey == MusicKey.A_SHARP_MINOR) {
            noteStrings[5] = "E#";  // E# instead of F
        }

        if (musicKey == MusicKey.C_SHARP_MAJOR || musicKey == MusicKey.A_SHARP_MINOR) {
            noteStrings[0] = "B#";  // B# instead of C
            if (key == 0) octave--;  // Need to reduce octave number by 1
        }

        // Check if we want to use fancy accidentals
        if (fancyAccidentals) {
            for (int i = 0; i < noteStrings.length; i++) {
                noteStrings[i] = MusicUtils.fancifyMusicString(noteStrings[i]);
            }
        }

        // Get the pitch/offset string
        String noteString = noteStrings[key];

        // Return the full string
        return noteString + octave;  // Example: C0, D#3, Eb5 etc.
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
     * Convert a power value (amplitude squared) to decibel (dB) units.
     *
     * @param S      Spectrogram of input powers.
     * @param refVal Value such that the amplitude <code>abs(power)</code> is scaled relative to
     *               <code>refVal</code> using the formula
     *               <code>10 * log10(power / refVal)</code>.
     * @param topDB  Threshold the output at <code>topDB</code> below the peak:<br>
     *               <code>max(10 * log10(S / ref)) - topDB</code>.
     * @return Spectrogram of decibel values for the given powers.
     * @throws ValueException If the value of <code>topDB</code> is negative.
     */
    public static double[][] powerToDecibel(double[][] S, double refVal, double topDB) {
        // Check that `topDB` is non-negative
        if (topDB < 0) throw new ValueException("The threshold decibel (`topDB`) must be non-negative.");

        // Compute decibel values for all powers
        double maxDB = -Double.MAX_VALUE;

        double[][] DB = new double[S.length][S[0].length];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                // Compute the decibel value for the specific power
                double dbVal = powerToDecibel(S[i][j], refVal);

                // Update `maxDB` if necessary
                if (dbVal > maxDB) maxDB = dbVal;

                // Update decibel matrix
                DB[i][j] = dbVal;
            }
        }

        // Threshold the output at `topDB` below the peak
        for (int i = 0; i < DB.length; i++) {
            for (int j = 0; j < DB[0].length; j++) {
                DB[i][j] = Math.max(DB[i][j], maxDB - topDB);
            }
        }

        // Return the decibel matrix
        return DB;
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

    // Time unit conversion

    /**
     * Method that converts timestamps (in seconds) to sample indices.
     *
     * @param times      Timestamps (in seconds) to convert.
     * @param sampleRate Sample rate of the audio.
     * @return Sample indices corresponding to the given timestamps.
     * @see <a href="https://bit.ly/3N4JoUe">Librosa's Implementation</a> of this method.
     */
    public static int[] timeToSamples(double[] times, double sampleRate) {
        int[] samples = new int[times.length];
        for (int i = 0; i < times.length; i++) {
            samples[i] = (int) Math.round(times[i] * sampleRate);
        }
        return samples;
    }

    /**
     * Method that converts sample indices into STFT frames.
     *
     * @param samples   Sample indices to convert.
     * @param hopLength Hop length of the STFT.
     * @param numFFT    Number of FFT bins.
     * @return STFT frames corresponding to the given sample indices.
     */
    public static int[] samplesToFrames(int[] samples, int hopLength, int numFFT) {
        // Compute offset value
        int offset = Math.floorDiv(numFFT, 2);

        // Compute frame indices
        int[] frames = new int[samples.length];
        for (int i = 0; i < samples.length; i++) {
            frames[i] = Math.floorDiv(samples[i] - offset, hopLength);
        }
        return frames;
    }

    /**
     * Method that converts time stamps into STFT frames.
     *
     * @param times      Timestamps to convert.
     * @param sampleRate Sample rate of the audio.
     * @param hopLength  Hop length of the STFT.
     * @return STFT frames corresponding to the given timestamps.
     */
    public static int[] timeToFrames(double[] times, double sampleRate, int hopLength) {
        return timeToFrames(times, sampleRate, hopLength, 0);
    }

    /**
     * Method that converts time stamps into STFT frames.
     *
     * @param times      Timestamps to convert.
     * @param sampleRate Sample rate of the audio.
     * @param hopLength  Hop length of the STFT.
     * @param numFFT     Number of FFT bins.
     * @return STFT frames corresponding to the given timestamps.
     */
    public static int[] timeToFrames(double[] times, double sampleRate, int hopLength, int numFFT) {
        // Convert time to samples
        int[] samples = timeToSamples(times, sampleRate);

        // Then convert the samples into frames
        return samplesToFrames(samples, hopLength, numFFT);
    }

    // Other unit conversions

    /**
     * Converts the number of seconds into a properly formatted time string.
     *
     * @param numSeconds Number of seconds.
     * @return A string of the form "MM:SS".
     */
    public static String secondsToTimeString(double numSeconds) {
        // Truncate any decimal places
        int truncSeconds = (int) Math.floor(numSeconds);

        // Compute the number of minutes and seconds
        int minutes = truncSeconds / 60;
        int seconds = truncSeconds % 60;

        // Pad the minutes and seconds with needed zeros
        String minuteStr;
        if (minutes < 10) {
            minuteStr = "0" + minutes;
        } else {
            minuteStr = "" + minutes;
        }

        String secondsStr;
        if (seconds < 10) {
            secondsStr = "0" + seconds;
        } else {
            secondsStr = "" + seconds;
        }

        // Return the final string
        return minuteStr + ":" + secondsStr;
    }
}
