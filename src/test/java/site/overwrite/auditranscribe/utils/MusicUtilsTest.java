/*
 * MusicUtilsTest.java
 *
 * Created on 2022-06-11
 * Updated on 2022-06-11
 *
 * Description: Test `MusicUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicUtilsTest {

    @Test
    void doesKeyUseFlats() {
        // Keys that use flats
        assertTrue(MusicUtils.doesKeyUseFlats("F Major"));   // 1 flat
        assertTrue(MusicUtils.doesKeyUseFlats("Bb Major"));  // 2 flats
        assertTrue(MusicUtils.doesKeyUseFlats("Eb Major"));  // 3 flats
        assertTrue(MusicUtils.doesKeyUseFlats("Ab Major"));  // 4 flats
        assertTrue(MusicUtils.doesKeyUseFlats("Db Major"));  // 5 flats
        assertTrue(MusicUtils.doesKeyUseFlats("Gb Major"));  // 6 flats
        assertTrue(MusicUtils.doesKeyUseFlats("Cb Major"));  // 7 flats

        assertTrue(MusicUtils.doesKeyUseFlats("D Minor"));   // 1 flat
        assertTrue(MusicUtils.doesKeyUseFlats("G Minor"));   // 2 flats
        assertTrue(MusicUtils.doesKeyUseFlats("C Minor"));   // 3 flats
        assertTrue(MusicUtils.doesKeyUseFlats("F Minor"));   // 4 flats
        assertTrue(MusicUtils.doesKeyUseFlats("B♭ Minor"));  // 5 flats
        assertTrue(MusicUtils.doesKeyUseFlats("E♭ Minor"));  // 6 flats
        assertTrue(MusicUtils.doesKeyUseFlats("A♭ Minor"));  // 7 flats

        // Keys that don't use flats
        assertFalse(MusicUtils.doesKeyUseFlats("C Major"));   // No accidentals
        assertFalse(MusicUtils.doesKeyUseFlats("G Major"));   // 1 sharp
        assertFalse(MusicUtils.doesKeyUseFlats("D Major"));   // 2 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("A Major"));   // 3 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("E Major"));   // 4 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("B Major"));   // 5 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("F# Major"));  // 6 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("C# Major"));  // 7 sharps

        assertFalse(MusicUtils.doesKeyUseFlats("A Minor"));   // No accidentals
        assertFalse(MusicUtils.doesKeyUseFlats("E Minor"));   // 1 sharp
        assertFalse(MusicUtils.doesKeyUseFlats("B Minor"));   // 2 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("F♯ Minor"));  // 3 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("C♯ Minor"));  // 4 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("G♯ Minor"));  // 5 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("D♯ Minor"));  // 6 sharps
        assertFalse(MusicUtils.doesKeyUseFlats("A♯ Minor"));  // 7 sharps

    }

    @Test
    void fancifyMusicString() {
        assertEquals("C Major", MusicUtils.fancifyMusicString("C Major"));
        assertEquals("C♯ Minor", MusicUtils.fancifyMusicString("C# Minor"));
        assertEquals("C♭ Major", MusicUtils.fancifyMusicString("Cb Major"));
        assertEquals("C♯ Minor", MusicUtils.fancifyMusicString("C♯ Minor"));
        assertEquals("C♭ Major", MusicUtils.fancifyMusicString("C♭ Major"));
    }
}