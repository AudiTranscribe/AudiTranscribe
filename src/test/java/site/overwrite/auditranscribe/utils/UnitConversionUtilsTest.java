/*
 * UnitConversionUtilsTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-05-28
 *
 * Description: Test `UnitConversionUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionUtilsTest {
    // Notes conversion
    @Test
    void noteToFreq() {
        assertEquals(440, UnitConversionUtils.noteToFreq("A4"), 0.001);
        assertEquals(16.352, UnitConversionUtils.noteToFreq("C0"), 0.001);
        assertEquals(15804.266, UnitConversionUtils.noteToFreq("B9"), 0.001);
    }

    @Test
    void noteToNoteNumber() {
        // Basic tests
        assertEquals(0, UnitConversionUtils.noteToNoteNumber("c0"));
        assertEquals(57, UnitConversionUtils.noteToNoteNumber("a4"));
        assertEquals(60, UnitConversionUtils.noteToNoteNumber("c5"));
        assertEquals(108, UnitConversionUtils.noteToNoteNumber("c9"));

        // Full key tests
        assertEquals(48, UnitConversionUtils.noteToNoteNumber("C4"));
        assertEquals(49, UnitConversionUtils.noteToNoteNumber("C#4"));
        assertEquals(50, UnitConversionUtils.noteToNoteNumber("D4"));
        assertEquals(51, UnitConversionUtils.noteToNoteNumber("D#4"));
        assertEquals(52, UnitConversionUtils.noteToNoteNumber("E4"));
        assertEquals(53, UnitConversionUtils.noteToNoteNumber("F4"));
        assertEquals(54, UnitConversionUtils.noteToNoteNumber("F#4"));
        assertEquals(55, UnitConversionUtils.noteToNoteNumber("G4"));
        assertEquals(56, UnitConversionUtils.noteToNoteNumber("G#4"));
        assertEquals(57, UnitConversionUtils.noteToNoteNumber("A4"));
        assertEquals(58, UnitConversionUtils.noteToNoteNumber("A#4"));
        assertEquals(59, UnitConversionUtils.noteToNoteNumber("B4"));
        assertEquals(60, UnitConversionUtils.noteToNoteNumber("C5"));

        // Accidental tests
        assertEquals(60, UnitConversionUtils.noteToNoteNumber("C5"));
        assertEquals(60, UnitConversionUtils.noteToNoteNumber("C♮5"));
        assertEquals(61, UnitConversionUtils.noteToNoteNumber("C#5"));
        assertEquals(61, UnitConversionUtils.noteToNoteNumber("C♯5"));
        assertEquals(62, UnitConversionUtils.noteToNoteNumber("C##5"));
        assertEquals(62, UnitConversionUtils.noteToNoteNumber("C♯♯5"));
        assertEquals(59, UnitConversionUtils.noteToNoteNumber("Cb5"));
        assertEquals(59, UnitConversionUtils.noteToNoteNumber("C!5"));
        assertEquals(59, UnitConversionUtils.noteToNoteNumber("C♭5"));
        assertEquals(58, UnitConversionUtils.noteToNoteNumber("Cbb5"));
        assertEquals(58, UnitConversionUtils.noteToNoteNumber("C!!5"));
        assertEquals(58, UnitConversionUtils.noteToNoteNumber("C♭♭5"));
    }

    @Test
    void noteNumberToFreq() {
        assertEquals(440, UnitConversionUtils.noteNumberToFreq(57), 0.001);  // A4
        assertEquals(16.352, UnitConversionUtils.noteNumberToFreq(0), 0.001);  // C0
        assertEquals(15804.266, UnitConversionUtils.noteNumberToFreq(119), 0.001);  // B9
    }

    @Test
    void freqToNoteNumber() {
        assertEquals(57, UnitConversionUtils.freqToNoteNumber(440), 0.001);  // A4
        assertEquals(0, UnitConversionUtils.freqToNoteNumber(16.352), 0.001);  // C0
        assertEquals(119, UnitConversionUtils.freqToNoteNumber(15804.266), 0.001);  // B9
    }

    @Test
    void noteNumberToNote() {
        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, false));
        assertEquals("D#3", UnitConversionUtils.noteNumberToNote(39, false));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, false));
        assertEquals("A#7", UnitConversionUtils.noteNumberToNote(94, false));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, false));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, true));
        assertEquals("D♯3", UnitConversionUtils.noteNumberToNote(39, true));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, true));
        assertEquals("A♯7", UnitConversionUtils.noteNumberToNote(94, true));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, true));
    }

    @Test
    void noteNumberToMIDINumber() {
        assertEquals(12, UnitConversionUtils.noteNumberToMIDINumber(0));     // C0
        assertEquals(127, UnitConversionUtils.noteNumberToMIDINumber(115));  // G9
        assertEquals(60, UnitConversionUtils.noteNumberToMIDINumber(48));    // C4
        assertEquals(113, UnitConversionUtils.noteNumberToMIDINumber(101));  // F8
        assertEquals(68, UnitConversionUtils.noteNumberToMIDINumber(56));    // G#4
        assertEquals(-1, UnitConversionUtils.noteNumberToMIDINumber(119));   // B9
    }

    @Test
    void noteToMIDINumber() {
        assertEquals(12, UnitConversionUtils.noteToMIDINumber("C0"));
        assertEquals(127, UnitConversionUtils.noteToMIDINumber("G9"));
        assertEquals(60, UnitConversionUtils.noteToMIDINumber("C4"));
        assertEquals(113, UnitConversionUtils.noteToMIDINumber("F8"));
        assertEquals(68, UnitConversionUtils.noteToMIDINumber("G#4"));
        assertEquals(-1, UnitConversionUtils.noteToMIDINumber("B9"));
    }

    // Magnitude Scaling - Unit Conversion
    @Test
    void powerToDecibel() {
        // With `refVal` equals to 1
        assertEquals(3.010, UnitConversionUtils.powerToDecibel(2, 1), 0.001);
        assertEquals(10, UnitConversionUtils.powerToDecibel(10, 1), 0.001);
        assertEquals(10.915, UnitConversionUtils.powerToDecibel(12.345, 1), 0.001);

        // With variable `refVal`
        assertEquals(-7.782, UnitConversionUtils.powerToDecibel(2, 12), 0.001);
        assertEquals(3.010, UnitConversionUtils.powerToDecibel(10, 5), 0.001);
        assertEquals(2.597, UnitConversionUtils.powerToDecibel(12.345, 6.789), 0.001);
    }

    @Test
    void amplitudeToDecibel() {
        // With `refVal` equals to 1
        assertEquals(6.021, UnitConversionUtils.amplitudeToDecibel(2, 1), 0.001);
        assertEquals(20, UnitConversionUtils.amplitudeToDecibel(10, 1), 0.001);
        assertEquals(21.830, UnitConversionUtils.amplitudeToDecibel(12.345, 1), 0.001);

        // With variable `refVal`
        assertEquals(-15.563, UnitConversionUtils.amplitudeToDecibel(2, 12), 0.001);
        assertEquals(6.021, UnitConversionUtils.amplitudeToDecibel(10, 5), 0.001);
        assertEquals(5.194, UnitConversionUtils.amplitudeToDecibel(12.345, 6.789), 0.001);
    }

    // Graphics Units Conversion
    @Test
    void pxToPt() {
        assertEquals(15, UnitConversionUtils.pxToPt(20), 0.001);
        assertEquals(51.25, UnitConversionUtils.pxToPt(68.3333333333), 0.001);
        assertEquals(0, UnitConversionUtils.pxToPt(0), 0.001);
        assertEquals(164.25, UnitConversionUtils.pxToPt(219), 0.001);
    }

    @Test
    void ptToPx() {
        assertEquals(20, UnitConversionUtils.ptToPx(15), 0.001);
        assertEquals(68.333, UnitConversionUtils.ptToPx(51.25), 0.001);
        assertEquals(0, UnitConversionUtils.ptToPx(0), 0.001);
        assertEquals(219, UnitConversionUtils.ptToPx(164.25), 0.001);
    }
}