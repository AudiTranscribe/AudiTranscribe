/*
 * UnitConversionTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-12
 *
 * Description: Test `UnitConversion.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionTest {
    @Test
    void noteToFreq() {
        assertEquals(440, OtherMath.round(UnitConversion.noteToFreq("A4"), 3));
        assertEquals(16.352, OtherMath.round(UnitConversion.noteToFreq("C0"), 3));
        assertEquals(15804.266, OtherMath.round(UnitConversion.noteToFreq("B9"), 3));
    }

    @Test
    void noteToNoteNumber() {
        // Basic tests
        assertEquals(0, UnitConversion.noteToNoteNumber("C0"));
        assertEquals(57, UnitConversion.noteToNoteNumber("A4"));
        assertEquals(60, UnitConversion.noteToNoteNumber("C5"));
        assertEquals(108, UnitConversion.noteToNoteNumber("C9"));

        // Full key tests
        assertEquals(48, UnitConversion.noteToNoteNumber("C4"));
        assertEquals(49, UnitConversion.noteToNoteNumber("C#4"));
        assertEquals(50, UnitConversion.noteToNoteNumber("D4"));
        assertEquals(51, UnitConversion.noteToNoteNumber("D#4"));
        assertEquals(52, UnitConversion.noteToNoteNumber("E4"));
        assertEquals(53, UnitConversion.noteToNoteNumber("F4"));
        assertEquals(54, UnitConversion.noteToNoteNumber("F#4"));
        assertEquals(55, UnitConversion.noteToNoteNumber("G4"));
        assertEquals(56, UnitConversion.noteToNoteNumber("G#4"));
        assertEquals(57, UnitConversion.noteToNoteNumber("A4"));
        assertEquals(58, UnitConversion.noteToNoteNumber("A#4"));
        assertEquals(59, UnitConversion.noteToNoteNumber("B4"));
        assertEquals(60, UnitConversion.noteToNoteNumber("C5"));

        // Accidental tests
        assertEquals(60, UnitConversion.noteToNoteNumber("C5"));
        assertEquals(60, UnitConversion.noteToNoteNumber("C♮5"));
        assertEquals(61, UnitConversion.noteToNoteNumber("C#5"));
        assertEquals(61, UnitConversion.noteToNoteNumber("C♯5"));
        assertEquals(62, UnitConversion.noteToNoteNumber("C##5"));
        assertEquals(62, UnitConversion.noteToNoteNumber("C♯♯5"));
        assertEquals(59, UnitConversion.noteToNoteNumber("Cb5"));
        assertEquals(59, UnitConversion.noteToNoteNumber("C!5"));
        assertEquals(59, UnitConversion.noteToNoteNumber("C♭5"));
        assertEquals(58, UnitConversion.noteToNoteNumber("Cbb5"));
        assertEquals(58, UnitConversion.noteToNoteNumber("C!!5"));
        assertEquals(58, UnitConversion.noteToNoteNumber("C♭♭5"));
    }

    @Test
    void noteNumberToFreq() {
        assertEquals(440, OtherMath.round(UnitConversion.noteNumberToFreq(57), 3));  // A4
        assertEquals(16.352, OtherMath.round(UnitConversion.noteNumberToFreq(0), 3));  // C0
        assertEquals(15804.266, OtherMath.round(UnitConversion.noteNumberToFreq(119), 3));  // B9
    }
}