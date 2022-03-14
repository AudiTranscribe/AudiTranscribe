/*
 * UnitConversionTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-14
 *
 * Description: Test `UnitConversion.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionTest {
    // Notes conversion
    @Test
    void noteToFreq() {
        assertEquals(440, UnitConversion.noteToFreq("A4"), 0.001);
        assertEquals(16.352, UnitConversion.noteToFreq("C0"), 0.001);
        assertEquals(15804.266, UnitConversion.noteToFreq("B9"), 0.001);
    }

    @Test
    void noteToNoteNumber() {
        // Basic tests
        assertEquals(0, UnitConversion.noteToNoteNumber("c0"));
        assertEquals(57, UnitConversion.noteToNoteNumber("a4"));
        assertEquals(60, UnitConversion.noteToNoteNumber("c5"));
        assertEquals(108, UnitConversion.noteToNoteNumber("c9"));

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
        assertEquals(440, UnitConversion.noteNumberToFreq(57), 0.001);  // A4
        assertEquals(16.352, UnitConversion.noteNumberToFreq(0), 0.001);  // C0
        assertEquals(15804.266, UnitConversion.noteNumberToFreq(119), 0.001);  // B9
    }

    // Magnitude Scaling - Unit Conversion
    @Test
    void powerToDecibel() {
        // With `refVal` equals to 1
        assertEquals(3.010, UnitConversion.powerToDecibel(2, 1), 0.001);
        assertEquals(10, UnitConversion.powerToDecibel(10, 1), 0.001);
        assertEquals(10.915, UnitConversion.powerToDecibel(12.345, 1), 0.001);

        // With variable `refVal`
        assertEquals(-7.782, UnitConversion.powerToDecibel(2, 12), 0.001);
        assertEquals(3.010, UnitConversion.powerToDecibel(10, 5), 0.001);
        assertEquals(2.597, UnitConversion.powerToDecibel(12.345, 6.789), 0.001);
    }

    @Test
    void amplitudeToDecibel() {
        // With `refVal` equals to 1
        assertEquals(6.021, UnitConversion.amplitudeToDecibel(2, 1), 0.001);
        assertEquals(20, UnitConversion.amplitudeToDecibel(10, 1), 0.001);
        assertEquals(21.830, UnitConversion.amplitudeToDecibel(12.345, 1), 0.001);

        // With variable `refVal`
        assertEquals(-15.563, UnitConversion.amplitudeToDecibel(2, 12), 0.001);
        assertEquals(6.021, UnitConversion.amplitudeToDecibel(10, 5), 0.001);
        assertEquals(5.194, UnitConversion.amplitudeToDecibel(12.345, 6.789), 0.001);
    }
}