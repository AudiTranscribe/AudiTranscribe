/*
 * UnitConversionUtils.java
 *
 * Created on 2022-03-12
 * Updated on 2022-06-14
 *
 * Description: Unit conversion methods.
 */

package site.overwrite.auditranscribe.utils;

import site.overwrite.auditranscribe.exceptions.FormatException;
import site.overwrite.auditranscribe.exceptions.ValueException;

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
     * @param noteNumber       Note number for the given note.
     * @param musicKey         Music key, with both the key and the mode.
     * @param fancyAccidentals Whether <em>fancier accidentals</em> (i.e. ♯ instead of # and ♭
     *                         instead of b) should be used.
     * @return Note string.
     */
    public static String noteNumberToNote(int noteNumber, String musicKey, boolean fancyAccidentals) {
        // Fancify music key
        musicKey = MusicUtils.fancifyMusicString(musicKey);

        // Determine which set of note strings to use
        String[] noteStrings;
        if (MusicUtils.doesKeyUseFlats(musicKey)) {
            noteStrings = new String[]{"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
        } else {
            noteStrings = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        }

        // Replace notes if the key demands it
        if (musicKey.equals("G♭ Major") ||
                musicKey.equals("C♭ Major") ||
                musicKey.equals("E♭ Minor") ||
                musicKey.equals("A♭ Minor")) {
            noteStrings[11] = "Cb";  // Cb instead of B
        }

        if (musicKey.equals("C♭ Major") || musicKey.equals("A♭ Minor")) {
            noteStrings[4] = "Fb";  // Fb instead of E
        }

        if (musicKey.equals("F♯ Major") ||
                musicKey.equals("C♯ Major") ||
                musicKey.equals("D♯ Minor") ||
                musicKey.equals("A♯ Minor")) {
            noteStrings[5] = "E#";  // E# instead of F
        }

        if (musicKey.equals("C♯ Major") || musicKey.equals("A♯ Minor")) {
            noteStrings[0] = "B#";  // B# instead of C
        }

        // Check if we want to use fancy accidentals
        if (fancyAccidentals) {
            for (int i = 0; i < noteStrings.length; i++) {
                noteStrings[i] = MusicUtils.fancifyMusicString(noteStrings[i]);
            }
        }

        // Compute the octave and the key value
        int octave = Math.floorDiv(noteNumber, 12);  // Note that C0 has note number 0, C1 is 12, C2 is 24 etc.
        int key = noteNumber % 12;  // 0 = C, 1 = C#/Db, 2 = D, 3 = D#/Eb etc.

        // Get the pitch/offset string
        String noteString = noteStrings[key];

        // Return the full string
        return noteString + octave;  // Example: C0, D#3, Eb5 etc.
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
     * Converts a MIDI number to its corresponding note number (as defined by AudiTranscribe).
     *
     * @param midiNumber MIDI number.
     * @return Corresponding note number. Note that this will return <code>-1</code> is there is no
     * corresponding note number (say below C0).
     */
    public static int midiNumberToNoteNumber(int midiNumber) {
        // If the note number is less than 12 (i.e. below C0) there is no note number equivalent
        if (midiNumber < 12) return -1;

        // Otherwise, subtract 12 from the MIDI number to get the note number
        return midiNumber - 12;
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

    /**
     * Converts a MIDI number to its corresponding note string.
     *
     * @param midiNumber       MIDI number.
     * @param fancyAccidentals Whether <em>fancier accidentals</em> (i.e. ♯ instead of # and ♭
     *                         instead of b) should be used.
     * @return Corresponding note string.
     */
    public static String midiNumberToNote(int midiNumber, boolean fancyAccidentals) {
        return noteNumberToNote(midiNumberToNoteNumber(midiNumber), "C Major", fancyAccidentals);
    }

    // Audio unit conversion

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
     * Convert a power value (amplitude squared) to decibel (dB) units.
     *
     * @param power Input power.
     * @return Decibel value for the given power.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#power_to_db">
     * Librosa's Implementation</a> of this function.
     */
    public static double powerToDecibel(double power) {
        return powerToDecibel(power, 1.0);
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
        if (topDB < 0) throw new ValueException("The value of `topDB` must be non-negative.");

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

    /**
     * Method that converts a frequency in Hertz (Hz) into Mel frequency (Mels).
     *
     * @param freq Frequency in Hertz.
     * @return Frequency in Mel.
     */
    public static double hzToMel(double freq) {
        // Fill in the linear part
        double fMin = 0;
        double fSp = 200. / 3.;

        double mel = (freq - fMin) / fSp;

        // Fill in the log-scale part
        double minLogHz = 1000.0;  // Beginning of log region (Hz)
        double minLogMel = (minLogHz - fMin) / fSp;  // Same (Mels)
        double logstep = Math.log(6.4) / 27.0;  // Step size for log region

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
        double fMin = 0.0;
        double fSp = 200.0 / 3.;
        double freq = fMin + fSp * mel;

        // And now the nonlinear scale
        double min_log_hz = 1000.0;  // Beginning of log region (Hz)
        double min_log_mel = (min_log_hz - fMin) / fSp;  // Same (Mels)
        double logstep = Math.log(6.4) / 27.0;  // Step size for log region

        if (mel >= min_log_mel) {
            freq = min_log_hz * Math.exp(logstep * (mel - min_log_mel));
        }

        return freq;
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
