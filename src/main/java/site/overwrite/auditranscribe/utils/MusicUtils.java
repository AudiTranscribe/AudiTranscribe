/*
 * MusicUtils.java
 *
 * Created on 2022-06-11
 * Updated on 2022-07-17
 *
 * Description: Musical utility methods.
 */

package site.overwrite.auditranscribe.utils;

import org.javatuples.Pair;
import site.overwrite.auditranscribe.exceptions.generic.FormatException;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Musical utility methods.
 */
public final class MusicUtils {
    // Constants
    public static final String[] MUSIC_KEYS = {
            // Major Scales
            "C Major", "C♯ Major", "D♭ Major", "D Major", "E♭ Major", "E Major", "F Major", "F♯ Major", "G♭ Major",
            "G Major", "A♭ Major", "A Major", "B♭ Major", "B Major", "C♭ Major",

            // (Natural) Minor Scales
            "C Minor", "C♯ Minor", "D Minor", "D♯ Minor", "E♭ Minor", "E Minor", "F Minor", "F♯ Minor", "G Minor",
            "G♯ Minor", "A♭ Minor", "A Minor", "A♯ Minor", "B♭ Minor", "B Minor"
    };
    public static final Map<String, Integer> TIME_SIGNATURE_TO_BEATS_PER_BAR = Map.ofEntries(
            // Simple time signatures
            entry("4/4", 4),
            entry("2/2", 2),
            entry("2/4", 2),
            entry("3/4", 3),
            entry("3/8", 3),

            // Compound time signatures
            entry("6/8", 6),
            entry("9/8", 9),
            entry("12/8", 12)
    );  // See https://en.wikipedia.org/wiki/Time_signature#Characteristics
    public static final String[] TIME_SIGNATURES = {"4/4", "2/2", "2/4", "3/4", "3/8", "6/8", "9/8", "12/8"};

    private MusicUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Gets the notes in the music key <code>key</code>.
     *
     * @param key Music key, with both the key and the mode.
     * @return A <code>HashSet</code> object containing integer <b>offsets</b> from C (i.e. number
     * of notes above the C key). Each integer in the returned is the modulo 12 of a note number
     * that is in the key.
     * @throws ValueException If the provided key is invalid.
     */
    public static HashSet<Integer> getNotesInKey(String key) {
        // Fancify the key
        String keyFancified = fancifyMusicString(key);

        // Get the note offsets and place them into an array (for now)
        Integer[] noteOffsets = switch (keyFancified) {  // See the keys inside each musical scale
            // Major Scales
            case "C Major" -> new Integer[]{0, 2, 4, 5, 7, 9, 11};  // Default is C Major
            case "G Major" -> new Integer[]{7, 9, 11, 0, 2, 4, 6};
            case "D Major" -> new Integer[]{2, 4, 6, 7, 9, 11, 1};
            case "A Major" -> new Integer[]{9, 11, 1, 2, 4, 6, 8};
            case "E Major" -> new Integer[]{4, 6, 8, 9, 11, 1, 3};
            case "B Major", "C♭ Major" -> new Integer[]{11, 1, 3, 4, 6, 8, 10};
            case "F♯ Major", "G♭ Major" -> new Integer[]{6, 8, 10, 11, 1, 3, 5};
            case "C♯ Major", "D♭ Major" -> new Integer[]{1, 3, 5, 6, 8, 10, 0};
            case "F Major" -> new Integer[]{5, 7, 9, 10, 0, 2, 4};
            case "B♭ Major" -> new Integer[]{10, 0, 2, 3, 5, 7, 9};
            case "E♭ Major" -> new Integer[]{3, 5, 7, 8, 10, 0, 2};
            case "A♭ Major" -> new Integer[]{8, 10, 0, 1, 3, 5, 7};

            // (Natural) Minor Scales
            case "A Minor" -> new Integer[]{9, 11, 0, 2, 4, 5, 7};
            case "E Minor" -> new Integer[]{4, 6, 7, 9, 11, 0, 2};
            case "B Minor" -> new Integer[]{11, 1, 2, 4, 6, 7, 9};
            case "F♯ Minor" -> new Integer[]{6, 8, 9, 11, 1, 2, 4};
            case "C♯ Minor" -> new Integer[]{1, 3, 4, 6, 8, 9, 11};
            case "G♯ Minor" -> new Integer[]{8, 10, 11, 1, 3, 4, 6};
            case "D♯ Minor" -> new Integer[]{3, 5, 6, 8, 10, 11, 1};
            case "A♯ Minor" -> new Integer[]{10, 0, 1, 3, 5, 6, 8};
            case "D Minor" -> new Integer[]{2, 4, 5, 7, 9, 10, 0};
            case "G Minor" -> new Integer[]{7, 9, 10, 0, 2, 3, 5};
            case "C Minor" -> new Integer[]{0, 2, 3, 5, 7, 8, 10};
            case "F Minor" -> new Integer[]{5, 7, 8, 10, 0, 1, 3};
            case "B♭ Minor" -> new Integer[]{10, 0, 1, 3, 5, 6, 8};
            case "E♭ Minor" -> new Integer[]{3, 5, 6, 8, 10, 11, 1};
            case "A♭ Minor" -> new Integer[]{8, 10, 11, 1, 3, 4, 6};

            // Default case: throw value exception
            default -> throw new ValueException("Invalid key '" + keyFancified + "'");
        };

        // Convert to a hash set and return
        return new HashSet<>(Arrays.asList(noteOffsets));
    }

    /**
     * Method that determines the numeric value of the provided key.<br>
     * The calculation of the numeric value follows
     * <a href="https://www.musictheory.net/lessons/25">https://www.musictheory.net/lessons/25</a>.
     *
     * @param key Music key, with both the key and the mode.
     * @return An integer, representing the key's numeric value.
     */
    public static int getNumericValueOfKey(String key) {
        // Fancify the key
        String keyFancified = fancifyMusicString(key);

        // Get the note offsets and place them into an array (for now)
        return switch (keyFancified) {
            case "C♭ Major", "A♭ Minor" -> -7;
            case "G♭ Major", "E♭ Minor" -> -6;
            case "D♭ Major", "B♭ Minor" -> -5;
            case "A♭ Major", "F Minor" -> -4;
            case "E♭ Major", "C Minor" -> -3;
            case "B♭ Major", "G Minor" -> -2;
            case "F Major", "D Minor" -> -1;
            case "C Major", "A Minor" -> 0;
            case "G Major", "E Minor" -> 1;
            case "D Major", "B Minor" -> 2;
            case "A Major", "F♯ Minor" -> 3;
            case "E Major", "C♯ Minor" -> 4;
            case "B Major", "G♯ Minor" -> 5;
            case "F♯ Major", "D♯ Minor" -> 6;
            case "C♯ Major", "A♯ Minor" -> 7;
            default -> throw new ValueException("Invalid key '" + keyFancified + "'");
        };
    }

    /**
     * Method that determines whether the notes within the specified key uses flats instead of sharps.
     *
     * @param key The key to check.
     * @return True if the notes within the key use flats, false if they use sharps.
     */
    public static boolean doesKeyUseFlats(String key) {
        // Fancify the key
        String keyFancified = fancifyMusicString(key);

        // Constant of keys which has flats
        final Set<String> KEYS_WITH_FLATS = Set.of(
                "F Major", "B♭ Major", "E♭ Major", "A♭ Major", "D♭ Major", "G♭ Major", "C♭ Major",
                "D Minor", "G Minor", "C Minor", "F Minor", "B♭ Minor", "E♭ Minor", "A♭ Minor"
        );

        // Check if the key uses flats
        return KEYS_WITH_FLATS.contains(keyFancified);
    }

    /**
     * Method that converts all <code>#</code> to <code>♯</code> and all <code>b</code> to
     * <code>♭</code>.
     *
     * @param string The string to fancify.
     * @return The fancified string.
     */
    public static String fancifyMusicString(String string) {
        return string.replace('#', '♯').replace('b', '♭');
    }

    /**
     * Method that parses a time signature string.
     *
     * @param timeSignature The time signature string.<br>
     *                      The time signature string is assumed to be of the form <code>N/D</code>,
     *                      where <code>N</code> is the numerator of the time signature and
     *                      <code>D</code> is the denominator.
     * @return A pair. First value is the parsed numerator of the time signature, and the second
     * value is the parsed denominator of the time signature.
     * @throws FormatException If the time signature format is incorrect.
     */
    public static Pair<Integer, Integer> parseTimeSignature(String timeSignature) {
        // Define time signature pattern
        final Pattern TIME_SIGNATURE_PATTERN = Pattern.compile("^(?<numerator>\\d+)/(?<denominator>\\d+)$");

        // Attempt to match pattern to the provided string
        Matcher matcher = TIME_SIGNATURE_PATTERN.matcher(timeSignature);
        if (!matcher.find()) {
            throw new FormatException("Improper time signature format '" + timeSignature + "'");
        }

        // Get the matched groups
        String numeratorStr = matcher.group("numerator");
        String denominatorStr = matcher.group("denominator");

        // Attempt to convert to integers
        int numerator = Integer.parseInt(numeratorStr);
        int denominator = Integer.parseInt(denominatorStr);

        // Return as a pair
        return new Pair<>(numerator, denominator);
    }

    /**
     * Method that parses a key string.
     *
     * @param key Music key, with both the key and the mode.
     * @return A pair. The first value is the key/scale of the key (e.g. C, A♯, G♭) and the second
     * value is the mode (i.e. either <code>Major</code> or <code>Minor</code>).
     */
    public static Pair<String, String> parseKeySignature(String key) {
        // Define key pattern
        final Pattern KEY_PATTERN = Pattern.compile("^(?<key>[A-Ga-g][#♯b!♭]?) (?<mode>Major|Minor)$");

        // Fancify key first
        key = fancifyMusicString(key);

        // Attempt to match pattern to the provided string
        Matcher matcher = KEY_PATTERN.matcher(key);
        if (!matcher.find()) {
            throw new FormatException("Improper key format '" + key + "'");
        }

        // Get the matched groups
        String keyPart = matcher.group("key");
        String modePart = matcher.group("mode");

        // Return as a pair
        return new Pair<>(keyPart, modePart);
    }
}
