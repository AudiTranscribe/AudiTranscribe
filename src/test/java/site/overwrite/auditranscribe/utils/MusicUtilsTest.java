/*
 * MusicUtilsTest.java
 *
 * Created on 2022-06-11
 * Updated on 2022-06-28
 *
 * Description: Test `MusicUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicUtilsTest {

    @Test
    void getNotesInKey() {
        assertEquals(new HashSet<>(List.of(new Integer[]{0, 2, 4, 5, 7, 9, 11})), MusicUtils.getNotesInKey("C Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{7, 9, 11, 0, 2, 4, 6})), MusicUtils.getNotesInKey("G Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{2, 4, 6, 7, 9, 11, 1})), MusicUtils.getNotesInKey("D Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{9, 11, 1, 2, 4, 6, 8})), MusicUtils.getNotesInKey("A Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{4, 6, 8, 9, 11, 1, 3})), MusicUtils.getNotesInKey("E Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{11, 1, 3, 4, 6, 8, 10})), MusicUtils.getNotesInKey("B Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{11, 1, 3, 4, 6, 8, 10})), MusicUtils.getNotesInKey("Cb Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{6, 8, 10, 11, 1, 3, 5})), MusicUtils.getNotesInKey("F# Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{6, 8, 10, 11, 1, 3, 5})), MusicUtils.getNotesInKey("Gb Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{1, 3, 5, 6, 8, 10, 0})), MusicUtils.getNotesInKey("C# Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{1, 3, 5, 6, 8, 10, 0})), MusicUtils.getNotesInKey("Db Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{5, 7, 9, 10, 0, 2, 4})), MusicUtils.getNotesInKey("F Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{10, 0, 2, 3, 5, 7, 9})), MusicUtils.getNotesInKey("Bb Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{3, 5, 7, 8, 10, 0, 2})), MusicUtils.getNotesInKey("Eb Major"));
        assertEquals(new HashSet<>(List.of(new Integer[]{8, 10, 0, 1, 3, 5, 7})), MusicUtils.getNotesInKey("Ab Major"));

        assertEquals(new HashSet<>(List.of(new Integer[]{9, 11, 0, 2, 4, 5, 7})), MusicUtils.getNotesInKey("A Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{4, 6, 7, 9, 11, 0, 2})), MusicUtils.getNotesInKey("E Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{11, 1, 2, 4, 6, 7, 9})), MusicUtils.getNotesInKey("B Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{6, 8, 9, 11, 1, 2, 4})), MusicUtils.getNotesInKey("F# Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{1, 3, 4, 6, 8, 9, 11})), MusicUtils.getNotesInKey("C# Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{8, 10, 11, 1, 3, 4, 6})), MusicUtils.getNotesInKey("G# Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{3, 5, 6, 8, 10, 11, 1})), MusicUtils.getNotesInKey("D# Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{10, 0, 1, 3, 5, 6, 8})), MusicUtils.getNotesInKey("A# Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{2, 4, 5, 7, 9, 10, 0})), MusicUtils.getNotesInKey("D Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{7, 9, 10, 0, 2, 3, 5})), MusicUtils.getNotesInKey("G Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{0, 2, 3, 5, 7, 8, 10})), MusicUtils.getNotesInKey("C Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{5, 7, 8, 10, 0, 1, 3})), MusicUtils.getNotesInKey("F Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{10, 0, 1, 3, 5, 6, 8})), MusicUtils.getNotesInKey("Bb Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{3, 5, 6, 8, 10, 11, 1})), MusicUtils.getNotesInKey("Eb Minor"));
        assertEquals(new HashSet<>(List.of(new Integer[]{8, 10, 11, 1, 3, 4, 6})), MusicUtils.getNotesInKey("Ab Minor"));
    }

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