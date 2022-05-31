/*
 * UnitConversionUtils.java
 *
 * Created on 2022-03-12
 * Updated on 2022-05-31
 *
 * Description: Unit conversion methods.
 */

package site.overwrite.auditranscribe.utils;

import site.overwrite.auditranscribe.exceptions.FormatException;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Unit conversion methods.
 */
public class UnitConversionUtils {
    // Constants
    static final double A_MIN = 1e-10;  // Minimum amplitude for `powerToDB`

    // Notes conversion

    /**
     * Convert one or more note names to frequency.
     *
     * @param note Note name.
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
    // Todo: find a way to incorporate double sharp and double flat
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

        final Pattern NOTE_PATTERN = Pattern.compile(
                "^(?<note>[A-Ga-g])(?<accidental>[#♯b!♭♮]*)(?<octave>[+-]?\\d+)?$"
        );

        // Attempt to match the `note` string to the pattern
        Matcher matcher = NOTE_PATTERN.matcher(note);
        if (!matcher.find()) {
            throw new FormatException("Improper note format '" + note + "'");
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
     *
     * @param noteNumber The note number. Note that a note number of 0 means the key C0.
     * @return Frequency of the note with that note number. The returned frequency assumes that the
     * notes have been tuned to A440.
     */
    public static double noteNumberToFreq(int noteNumber) {
        return 440 * Math.pow(2, (noteNumber - 57d) / 12);
    }

    /**
     * Converts the frequency to an estimated note number.
     *
     * @param freq Frequency of the note with that note number. The returned frequency assumes that
     *             the notes have been tuned to A440.
     * @return The estimated note number. This is a <b>double</b> and needs to be rounded. That
     * task is left to another method and not this one.
     */
    public static double freqToNoteNumber(double freq) {
        return 12 * MathUtils.log2(freq / 440) + 57;
    }

    /**
     * Converts a note number to its spelled note.<br>
     * Note that the note number for C0 is 0 and subsequent note numbers are given by their offset
     * from C0. For example, A4 has note number 57 as it is 57 notes away from C0.
     *
     * @param noteNumber  Note number for the given note.
     * @param fancySharps Whether <em>fancier sharps</em> (i.e. ♯ instead of #) should be used.
     * @return Note string. Note that all notes with accidentals will be changed to those with
     * <b>sharps (# or ♯) only</b>.
     */
    // Todo: incorporate correct note conversion with scale/key consideration (i.e. add flats as well)
    public static String noteNumberToNote(int noteNumber, boolean fancySharps) {
        // Constant array of note strings
        String[] noteStrings;
        if (fancySharps) {
            noteStrings = new String[]{"C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"};
        } else {
            noteStrings = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        }

        // Compute the octave and the key value
        int octave = Math.floorDiv(noteNumber, 12);  // Note that C0 has note number 0, C1 is 12, C2 is 24 etc.
        int key = noteNumber % 12;  // 0 = C, 1 = C#, 2 = D, 3 = D# etc.

        // Get the pitch/offset string
        String noteString = noteStrings[key];

        // Return the full string
        return noteString + octave;  // Example: C0, D#3, E5 etc.
    }

    /**
     * Converts a note number (as defined by AudiTranscribe) to its corresponding MIDI number.
     *
     * @param noteNumber Note number.
     * @return Corresponding MIDI number. Note that this will return <code>-1</code> is there is no
     * corresponding MIDI number (say above G9).
     */
    public static int noteNumberToMIDINumber(int noteNumber) {
        // If the note number is above 116 (i.e. above G9) there is no MIDI number equivalent
        if (noteNumber > 116) return -1;

        // Otherwise, add 12 to the note number to get the MIDI number
        return noteNumber + 12;
    }

    /**
     * Converts a note string to its corresponding MIDI number.
     *
     * @param note Note string. Notes may be spelled out with optional accidentals or octave
     *             numbers. The leading note name is case-insensitive. Sharps are indicated with
     *             <code>#</code> or <code>♯</code>, flats may be indicated with <code>!</code>,
     *             <code>b</code>, or <code>♭</code>.
     * @return Corresponding MIDI number. Note that this will return <code>-1</code> is there is no
     * corresponding MIDI number (say above G9).
     */
    public static int noteToMIDINumber(String note) {
        return noteNumberToMIDINumber(noteToNoteNumber(note));
    }

    // Magnitude Scaling - Unit Conversion

    /**
     * Convert a power value (amplitude squared) to decibel (dB) units.
     *
     * @param power  Input power.
     * @param refVal Value such that the amplitude <code>abs(power)</code> is scaled relative to
     *               <code>refVal</code> using the formula
     *               <code>10 * log10(power / refVal)</code>.
     * @return Decibel value for the given power.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#power_to_db">
     * Librosa's Implementation</a> of this function.
     */
    public static double powerToDecibel(double power, double refVal) {
        // Treat the reference value
        refVal = Math.abs(refVal);

        // Calculate decibel
        double logSpec = 10 * Math.log10(Math.max(A_MIN, power));
        logSpec -= 10 * Math.log10(Math.max(A_MIN, refVal));

        // Return it
        return logSpec;
    }

    /**
     * Convert an amplitude spectrogram to dB-scaled spectrogram.<br>
     * This is equivalent to <code>powerToDecibel(Math.pow(amplitude, 2))</code>, but is provided
     * for convenience.
     *
     * @param amplitude Input amplitude.
     * @param refVal    Value such that the amplitude <code>abs(power)</code> is scaled relative to
     *                  <code>refVal</code> using the formula
     *                  <code>20 * log10(power / refVal)</code>.
     * @return Decibel value for the given amplitude.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#amplitude_to_db">
     * Librosa's Implementation</a> of this function.
     */
    public static double amplitudeToDecibel(double amplitude, double refVal) {
        return powerToDecibel(amplitude * amplitude, refVal * refVal);
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
        int offset = (int) Math.floor(numFFT / 2.0);

        // Compute frame indices
        int[] frames = new int[samples.length];
        for (int i = 0; i < samples.length; i++) {
            frames[i] = (int) Math.floor((double) (samples[i] - offset) / hopLength);
        }
        return frames;
    }

    /**
     * Method that converts sample indices into STFT frames.
     *
     * @param samples   Sample indices to convert.
     * @param hopLength Hop length of the STFT.
     * @return STFT frames corresponding to the given sample indices.
     */
    public static int[] samplesToFrames(int[] samples, int hopLength) {
        // Compute frame indices
        int[] frames = new int[samples.length];
        for (int i = 0; i < samples.length; i++) {
            frames[i] = (int) Math.floor((double) samples[i] / hopLength);
        }
        return frames;
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

    /**
     * Method that converts time stamps into STFT frames.
     *
     * @param times      Timestamps to convert.
     * @param sampleRate Sample rate of the audio.
     * @param hopLength  Hop length of the STFT.
     * @return STFT frames corresponding to the given timestamps.
     */
    public static int[] timeToFrames(double[] times, double sampleRate, int hopLength) {
        // Convert time to samples
        int[] samples = timeToSamples(times, sampleRate);

        // Then convert the samples into frames
        return samplesToFrames(samples, hopLength);
    }


    // Graphics Units Conversion

    /**
     * Function that converts pixels to points.
     *
     * @param px Number of pixels.
     * @return Point value.
     */
    public static double pxToPt(double px) {
        return 0.75 * px;
    }

    /**
     * Function that converts points to pixels.
     *
     * @param pt Number of points.
     * @return Pixel value.
     */
    public static double ptToPx(double pt) {
        return (4. / 3) * pt;
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
        int minutes = truncSeconds / 60;  // Integer division
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
