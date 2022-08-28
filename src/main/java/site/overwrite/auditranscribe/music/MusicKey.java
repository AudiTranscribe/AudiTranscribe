/*
 * MusicKey.java
 * Description: Enum that contains all the supported music keys of AudiTranscribe.
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

package site.overwrite.auditranscribe.music;

import site.overwrite.auditranscribe.utils.MusicUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

/**
 * Enum that contains all the supported music keys of AudiTranscribe.
 */
public enum MusicKey {
    // Enum values
    C_MAJOR("C Major", new Integer[]{0, 2, 4, 5, 7, 9, 11}, 0, false),
    C_SHARP_MAJOR("C♯ Major", new Integer[]{1, 3, 5, 6, 8, 10, 0}, 7, false),
    D_FLAT_MAJOR("D♭ Major", new Integer[]{1, 3, 5, 6, 8, 10, 0}, -5, true),
    D_MAJOR("D Major", new Integer[]{2, 4, 6, 7, 9, 11, 1}, 2, false),
    E_FLAT_MAJOR("E♭ Major", new Integer[]{3, 5, 7, 8, 10, 0, 2}, -3, true),
    E_MAJOR("E Major", new Integer[]{4, 6, 8, 9, 11, 1, 3}, 4, false),
    F_MAJOR("F Major", new Integer[]{5, 7, 9, 10, 0, 2, 4}, -1, true),
    F_SHARP_MAJOR("F♯ Major", new Integer[]{6, 8, 10, 11, 1, 3, 5}, 6, false),
    G_FLAT_MAJOR("G♭ Major", new Integer[]{6, 8, 10, 11, 1, 3, 5}, -6, true),
    G_MAJOR("G Major", new Integer[]{7, 9, 11, 0, 2, 4, 6}, 1, false),
    A_FLAT_MAJOR("A♭ Major", new Integer[]{8, 10, 0, 1, 3, 5, 7}, -4, true),
    A_MAJOR("A Major", new Integer[]{9, 11, 1, 2, 4, 6, 8}, 3, false),
    B_FLAT_MAJOR("B♭ Major", new Integer[]{10, 0, 2, 3, 5, 7, 9}, -2, true),
    B_MAJOR("B Major", new Integer[]{11, 1, 3, 4, 6, 8, 10}, 5, false),
    C_FLAT_MAJOR("C♭ Major", new Integer[]{11, 1, 3, 4, 6, 8, 10}, -7, true),

    C_MINOR("C Minor", new Integer[]{0, 2, 3, 5, 7, 8, 10}, -3, true),
    C_SHARP_MINOR("C♯ Minor", new Integer[]{1, 3, 4, 6, 8, 9, 11}, 4, false),
    D_MINOR("D Minor", new Integer[]{2, 4, 5, 7, 9, 10, 0}, -1, true),
    D_SHARP_MINOR("D♯ Minor", new Integer[]{3, 5, 6, 8, 10, 11, 1}, 6, false),
    E_FLAT_MINOR("E♭ Minor", new Integer[]{3, 5, 6, 8, 10, 11, 1}, -6, true),
    E_MINOR("E Minor", new Integer[]{4, 6, 7, 9, 11, 0, 2}, 1, false),
    F_MINOR("F Minor", new Integer[]{5, 7, 8, 10, 0, 1, 3}, -4, true),
    F_SHARP_MINOR("F♯ Minor", new Integer[]{6, 8, 9, 11, 1, 2, 4}, 3, false),
    G_MINOR("G Minor", new Integer[]{7, 9, 10, 0, 2, 3, 5}, -2, true),
    G_SHARP_MINOR("G♯ Minor", new Integer[]{8, 10, 11, 1, 3, 4, 6}, 5, false),
    A_FLAT_MINOR("A♭ Minor", new Integer[]{8, 10, 11, 1, 3, 4, 6}, -7, true),
    A_MINOR("A Minor", new Integer[]{9, 11, 0, 2, 4, 5, 7}, 0, false),
    A_SHARP_MINOR("A♯ Minor", new Integer[]{10, 0, 1, 3, 5, 6, 8}, 7, false),
    B_FLAT_MINOR("B♭ Minor", new Integer[]{10, 0, 1, 3, 5, 6, 8}, -5, true),
    B_MINOR("B Minor", new Integer[]{11, 1, 2, 4, 6, 7, 9}, 2, false);

    // Attributes
    public final String name;  // Name of the key
    public final HashSet<Integer> notesInKey;  // The integer offsets from C, representing notes within the key
    public final int numericValue;  // The numeric value of the key
    public final boolean usesFlats;

    // Enum constructor
    MusicKey(String name, Integer[] notesInKey, int numericValue, boolean usesFlats) {
        this.name = name;
        this.notesInKey = new HashSet<>(Arrays.asList(notesInKey));
        this.numericValue = numericValue;
        this.usesFlats = usesFlats;
    }

    // Override methods
    @Override
    public String toString() {
        return name;
    }

    // Public methods

    /**
     * Gets the music key with the specified name.
     *
     * @param key Name of the music key.
     * @return A <code>MusicKey</code> object that has the specified name.
     */
    public static MusicKey getMusicKey(String key) {
        // Fancify key string first
        String keyFancified = MusicUtils.fancifyMusicString(key);

        // Then try and find the fancified key
        for (MusicKey musicKey : MusicKey.values()) {
            if (Objects.equals(musicKey.name, keyFancified)) {
                return musicKey;
            }
        }

        return null;
    }

    /**
     * Gets the names of all the music keys.
     *
     * @return An array of strings, containing the names of the music keys.
     */
    public static String[] getMusicKeyNames() {
        int numKeys = MusicKey.values().length;
        String[] names = new String[numKeys];

        for (int i = 0; i < numKeys; i++) {
            names[i] = MusicKey.values()[i].name;
        }

        return names;
    }
}
