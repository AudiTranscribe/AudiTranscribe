/*
 * MusicKey.java
 * Description: Enum that contains all supported music keys.
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

package app.auditranscribe.music;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.utils.MusicUtils;

import java.util.*;

/**
 * Enum that contains all supported music keys.
 */
public enum MusicKey {
    // Enum values
    C_MAJOR("C Major", 0, false, new Integer[]{0, 2, 4, 5, 7, 9, 11}, 0, false, 1),
    C_SHARP_MAJOR("C♯ Major", 1, false, new Integer[]{1, 3, 5, 6, 8, 10, 0}, 7, false, 2),
    D_FLAT_MAJOR("D♭ Major", 1, false, new Integer[]{1, 3, 5, 6, 8, 10, 0}, -5, true, 3),
    D_MAJOR("D Major", 2, false, new Integer[]{2, 4, 6, 7, 9, 11, 1}, 2, false, 4),
    E_FLAT_MAJOR("E♭ Major", 3, false, new Integer[]{3, 5, 7, 8, 10, 0, 2}, -3, true, 5),
    E_MAJOR("E Major", 4, false, new Integer[]{4, 6, 8, 9, 11, 1, 3}, 4, false, 6),
    F_MAJOR("F Major", 5, false, new Integer[]{5, 7, 9, 10, 0, 2, 4}, -1, true, 7),
    F_SHARP_MAJOR("F♯ Major", 6, false, new Integer[]{6, 8, 10, 11, 1, 3, 5}, 6, false, 8),
    G_FLAT_MAJOR("G♭ Major", 6, false, new Integer[]{6, 8, 10, 11, 1, 3, 5}, -6, true, 9),
    G_MAJOR("G Major", 7, false, new Integer[]{7, 9, 11, 0, 2, 4, 6}, 1, false, 10),
    A_FLAT_MAJOR("A♭ Major", 8, false, new Integer[]{8, 10, 0, 1, 3, 5, 7}, -4, true, 11),
    A_MAJOR("A Major", 9, false, new Integer[]{9, 11, 1, 2, 4, 6, 8}, 3, false, 12),
    B_FLAT_MAJOR("B♭ Major", 10, false, new Integer[]{10, 0, 2, 3, 5, 7, 9}, -2, true, 13),
    B_MAJOR("B Major", 11, false, new Integer[]{11, 1, 3, 4, 6, 8, 10}, 5, false, 14),
    C_FLAT_MAJOR("C♭ Major", 11, false, new Integer[]{11, 1, 3, 4, 6, 8, 10}, -7, true, 15),

    C_MINOR("C Minor", 0, true, new Integer[]{0, 2, 3, 5, 7, 8, 10}, -3, true, 16),
    C_SHARP_MINOR("C♯ Minor", 1, true, new Integer[]{1, 3, 4, 6, 8, 9, 11}, 4, false, 17),
    D_MINOR("D Minor", 2, true, new Integer[]{2, 4, 5, 7, 9, 10, 0}, -1, true, 18),
    D_SHARP_MINOR("D♯ Minor", 3, true, new Integer[]{3, 5, 6, 8, 10, 11, 1}, 6, false, 19),
    E_FLAT_MINOR("E♭ Minor", 3, true, new Integer[]{3, 5, 6, 8, 10, 11, 1}, -6, true, 20),
    E_MINOR("E Minor", 4, true, new Integer[]{4, 6, 7, 9, 11, 0, 2}, 1, false, 21),
    F_MINOR("F Minor", 5, true, new Integer[]{5, 7, 8, 10, 0, 1, 3}, -4, true, 22),
    F_SHARP_MINOR("F♯ Minor", 6, true, new Integer[]{6, 8, 9, 11, 1, 2, 4}, 3, false, 23),
    G_MINOR("G Minor", 7, true, new Integer[]{7, 9, 10, 0, 2, 3, 5}, -2, true, 24),
    G_SHARP_MINOR("G♯ Minor", 8, true, new Integer[]{8, 10, 11, 1, 3, 4, 6}, 5, false, 25),
    A_FLAT_MINOR("A♭ Minor", 8, true, new Integer[]{8, 10, 11, 1, 3, 4, 6}, -7, true, 26),
    A_MINOR("A Minor", 9, true, new Integer[]{9, 11, 0, 2, 4, 5, 7}, 0, false, 27),
    A_SHARP_MINOR("A♯ Minor", 10, true, new Integer[]{10, 0, 1, 3, 5, 6, 8}, 7, false, 28),
    B_FLAT_MINOR("B♭ Minor", 10, true, new Integer[]{10, 0, 1, 3, 5, 6, 8}, -5, true, 29),
    B_MINOR("B Minor", 11, true, new Integer[]{11, 1, 2, 4, 6, 7, 9}, 2, false, 30);

    // Attributes
    public final String name;                  // Name of the key
    public final int offset;                   // Number of semitones away from C
    public final boolean isMinor;              // Whether the key is a major or minor key
    public final HashSet<Integer> notesInKey;  // The integer offsets from C, representing notes within the key
    public final int numericValue;             // The numeric value of the key
    public final boolean usesFlats;
    public final short uuid;

    // Enum constructor
    MusicKey(
            String name, int offset, boolean isMinor, Integer[] notesInKey, int numericValue, boolean usesFlats,
            int uuid
    ) {
        this.name = name;
        this.offset = offset;
        this.isMinor = isMinor;
        this.notesInKey = new HashSet<>(Arrays.asList(notesInKey));
        this.numericValue = numericValue;
        this.usesFlats = usesFlats;
        this.uuid = (short) uuid;
    }

    // Public methods

    /**
     * Gets the music key with the specified name.
     *
     * @param key Name of the music key.
     * @return A <code>MusicKey</code> object that has the specified name, or <code>null</code> if
     * not found.
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
     * Gets the music key with the specific numeric value and key.
     *
     * @param uuid The UUID of the music key.
     * @return A <code>MusicKey</code> object, or <code>null</code> if not found.
     */
    public static MusicKey getMusicKey(short uuid) {
        for (MusicKey musicKey : MusicKey.values()) {
            if (musicKey.uuid == uuid) {
                return musicKey;
            }
        }
        return null;
    }

    /**
     * Obtains a list of possible keys given the offset (number of semitones from C) and whether the
     * key is minor.
     *
     * @param offset  Number of semitones from C.
     * @param isMinor Whether the key is a minor key.
     * @return A list of possible key matches.
     */
    public static List<MusicKey> getPossibleMatches(int offset, boolean isMinor) {
        List<MusicKey> possibleKeys = new ArrayList<>();
        for (MusicKey key : MusicKey.values()) {
            if (key.offset == offset && key.isMinor == isMinor) {
                possibleKeys.add(key);
            }
        }
        return possibleKeys;
    }

    @Override
    @ExcludeFromGeneratedCoverageReport
    public String toString() {
        return name;
    }
}
