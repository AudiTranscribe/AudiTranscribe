/*
 * MusicUtils.java
 * Description: Musical utility methods.
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
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.music.MusicKey;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Musical utility methods.
 */
public final class MusicUtils {
    // Constants
    public static final String[] MUSIC_KEYS = MusicKey.getMusicKeyNames();

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
        // Get the music key object that represents that key
        MusicKey musicKey = MusicKey.getMusicKey(key);

        // If not null, return the notes within the key
        if (musicKey != null) {
            return musicKey.notesInKey;
        } else {
            throw new ValueException("Invalid key '" + key + "'");
        }
    }

    /**
     * Method that determines the numeric value of the provided key.<br>
     * The calculation of the numeric value follows
     * <a href="https://www.musictheory.net/lessons/25">https://www.musictheory.net/lessons/25</a>.
     *
     * @param key Music key, with both the key and the mode.
     * @return An integer, representing the key's numeric value.
     * @throws ValueException If the provided key is invalid.
     */
    public static int getNumericValueOfKey(String key) {
        // Get the music key object that represents that key
        MusicKey musicKey = MusicKey.getMusicKey(key);

        // If not null, return the numeric value of the key
        if (musicKey != null) {
            return musicKey.numericValue;
        } else {
            throw new ValueException("Invalid key '" + key + "'");
        }
    }

    /**
     * Method that determines whether the notes within the specified key uses flats instead of sharps.
     *
     * @param key The key to check.
     * @return True if the notes within the key use flats, false if the notes within the key uses
     * sharps.
     * @throws ValueException If the provided key is invalid.
     */
    public static boolean doesKeyUseFlats(String key) {
        // Get the music key object that represents that key
        MusicKey musicKey = MusicKey.getMusicKey(key);

        // If not null, check if the key uses flats
        if (musicKey != null) {
            return musicKey.usesFlats;
        } else {
            throw new ValueException("Invalid key '" + key + "'");
        }
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
     * @param key Music key, with both the tonic and the mode.
     * @return A pair. The first value is the tonic of the key (e.g. C, A♯, G♭) and the second value
     * is the mode (i.e. either <code>Major</code> or <code>Minor</code>).
     */
    public static Pair<String, String> parseKeySignature(String key) {
        // Define key pattern
        final Pattern KEY_PATTERN = Pattern.compile("^(?<tonic>[A-Ga-g][#♯b!♭]?) (?<mode>Major|Minor)$");

        // Fancify key first
        key = fancifyMusicString(key);

        // Attempt to match pattern to the provided string
        Matcher matcher = KEY_PATTERN.matcher(key);
        if (!matcher.find()) {
            throw new FormatException("Improper key format '" + key + "'");
        }

        // Get the matched groups
        String keyPart = matcher.group("tonic");
        String modePart = matcher.group("mode");

        // Return as a pair
        return new Pair<>(keyPart, modePart);
    }
}
