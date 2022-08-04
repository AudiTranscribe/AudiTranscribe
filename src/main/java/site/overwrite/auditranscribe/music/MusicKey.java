/*
 * MusicKey.java
 *
 * Created on 2022-08-04
 * Updated on 2022-08-04
 *
 * Description: Enum that contains all the supported music keys of AudiTranscribe.
 */

package site.overwrite.auditranscribe.music;

import java.util.Objects;

/**
 * Enum that contains all the supported music keys of AudiTranscribe.
 */
public enum MusicKey {
    // Enum values
    C_MAJOR("C Major", new int[]{0, 2, 4, 5, 7, 9, 11}, 0, false),
    C_SHARP_MAJOR("C♯ Major", new int[]{1, 3, 5, 6, 8, 10, 0}, 7, false),
    D_FLAT_MAJOR("D♭ Major", new int[]{1, 3, 5, 6, 8, 10, 0}, -5, true),
    D_MAJOR("D Major", new int[]{2, 4, 6, 7, 9, 11, 1}, 2, false),
    E_FLAT_MAJOR("E♭ Major", new int[]{3, 5, 7, 8, 10, 0, 2}, -3, true),
    E_MAJOR("E Major", new int[]{4, 6, 8, 9, 11, 1, 3}, 4, false),
    F_MAJOR("F Major", new int[]{5, 7, 9, 10, 0, 2, 4}, -1, true),
    F_SHARP_MAJOR("F♯ Major", new int[]{6, 8, 10, 11, 1, 3, 5}, 6, false),
    G_FLAT_MAJOR("G♭ Major", new int[]{6, 8, 10, 11, 1, 3, 5}, -6, true),
    G_MAJOR("G Major", new int[]{7, 9, 11, 0, 2, 4, 6}, 1, false),
    A_FLAT_MAJOR("A♭ Major", new int[]{8, 10, 0, 1, 3, 5, 7}, -4, true),
    A_MAJOR("A Major", new int[]{9, 11, 1, 2, 4, 6, 8}, 3, false),
    B_FLAT_MAJOR("B♭ Major", new int[]{10, 0, 2, 3, 5, 7, 9}, -2, true),
    B_MAJOR("B Major", new int[]{11, 1, 3, 4, 6, 8, 10}, 5, false),
    C_FLAT_MAJOR("C♭ Major", new int[]{11, 1, 3, 4, 6, 8, 10}, -7, true),

    C_MINOR("C Minor", new int[]{0, 2, 3, 5, 7, 8, 10}, -3, true),
    C_SHARP_MINOR("C♯ Minor", new int[]{1, 3, 4, 6, 8, 9, 11}, 4, false),
    D_MINOR("D Minor", new int[]{2, 4, 5, 7, 9, 10, 0}, -1, true),
    D_SHARP_MINOR("D♯ Minor", new int[]{3, 5, 6, 8, 10, 11, 1}, 6, false),
    E_FLAT_MINOR("E♭ Minor", new int[]{3, 5, 6, 8, 10, 11, 1}, -6, true),
    E_MINOR("E Minor", new int[]{4, 6, 7, 9, 11, 0, 2}, 1, false),
    F_MINOR("F Minor", new int[]{5, 7, 8, 10, 0, 1, 3}, -4, true),
    F_SHARP_MINOR("F♯ Minor", new int[]{6, 8, 9, 11, 1, 2, 4}, 3, false),
    G_MINOR("G Minor", new int[]{7, 9, 10, 0, 2, 3, 5}, -2, true),
    G_SHARP_MINOR("G♯ Minor", new int[]{8, 10, 11, 1, 3, 4, 6}, 5, false),
    A_FLAT_MINOR("A♭ Minor", new int[]{8, 10, 11, 1, 3, 4, 6}, -7, true),
    A_MINOR("A Minor", new int[]{9, 11, 0, 2, 4, 5, 7}, 0, false),
    A_SHARP_MINOR("A♯ Minor", new int[]{10, 0, 1, 3, 5, 6, 8}, 7, false),
    B_FLAT_MINOR("B♭ Minor", new int[]{10, 0, 1, 3, 5, 6, 8}, -5, true),
    B_MINOR("B Minor", new int[]{11, 1, 2, 4, 6, 7, 9}, 2, false);

    // Attributes
    public final String name;  // Name of the key
    public final int[] notesInKey;  // The integer offsets from C, representing the notes within the key
    public final int numericValue;  // The numeric value of the key
    public final boolean usesFlats;

    // Enum constructor
    MusicKey(String name, int[] notesInKey, int numericValue, boolean usesFlats) {
        this.name = name;
        this.notesInKey = notesInKey;
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
        for (MusicKey musicKey : MusicKey.values()) {
            if (Objects.equals(musicKey.name, key)) {
                return musicKey;
            }
        }

        return null;
    }
}
