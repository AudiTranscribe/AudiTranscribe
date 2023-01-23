/*
 * MusicUtils.java
 * Description: Music utilities.
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

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.music.MusicKey;

import java.util.HashSet;

/**
 * Music utilities.
 */
public final class MusicUtils {
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

}
