/*
 * UnitConversionUtilsTest.java
 * Description: Test `UnitConversionUtils.java`.
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

import org.junit.jupiter.api.Test;
import app.auditranscribe.generic.exceptions.FormatException;
import app.auditranscribe.generic.exceptions.ValueException;

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

        // Weird 'no octave provided' tests
        assertEquals(-2, UnitConversionUtils.noteToNoteNumber("C♭♭"));
        assertEquals(2, UnitConversionUtils.noteToNoteNumber("C##"));

        // Improper format tests
        assertThrowsExactly(FormatException.class, () -> UnitConversionUtils.noteToNoteNumber("haha1"));
        assertThrowsExactly(FormatException.class, () -> UnitConversionUtils.noteToNoteNumber("1C#"));
        assertThrowsExactly(FormatException.class, () -> UnitConversionUtils.noteToNoteNumber("#C1"));
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
        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "C Major", false));
        assertEquals("C4", UnitConversionUtils.noteNumberToNote(48, "C Major", false));
        assertEquals("F5", UnitConversionUtils.noteNumberToNote(65, "C Major", false));
        assertEquals("G#6", UnitConversionUtils.noteNumberToNote(80, "C Major", false));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, "C Major", false));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "Gb Major", false));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, "Gb Major", false));
        assertEquals("F7", UnitConversionUtils.noteNumberToNote(89, "Gb Major", false));
        assertEquals("Cb9", UnitConversionUtils.noteNumberToNote(107, "Gb Major", false));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "Eb Minor", false));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, "Eb Minor", false));
        assertEquals("F7", UnitConversionUtils.noteNumberToNote(89, "Eb Minor", false));
        assertEquals("Cb9", UnitConversionUtils.noteNumberToNote(107, "Eb Minor", false));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "Cb Major", false));
        assertEquals("Fb5", UnitConversionUtils.noteNumberToNote(64, "Cb Major", false));
        assertEquals("F7", UnitConversionUtils.noteNumberToNote(89, "Cb Major", false));
        assertEquals("Cb9", UnitConversionUtils.noteNumberToNote(107, "Cb Major", false));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "A♭ Minor", true));
        assertEquals("F♭5", UnitConversionUtils.noteNumberToNote(64, "A♭ Minor", true));
        assertEquals("F7", UnitConversionUtils.noteNumberToNote(89, "A♭ Minor", true));
        assertEquals("C♭9", UnitConversionUtils.noteNumberToNote(107, "A♭ Minor", true));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "F♯ Major", true));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, "F♯ Major", true));
        assertEquals("E♯7", UnitConversionUtils.noteNumberToNote(89, "F♯ Major", true));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, "F♯ Major", true));

        assertEquals("C0", UnitConversionUtils.noteNumberToNote(0, "D♯ Minor", true));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, "D♯ Minor", true));
        assertEquals("E♯7", UnitConversionUtils.noteNumberToNote(89, "D♯ Minor", true));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, "D♯ Minor", true));

        assertEquals("B♯0", UnitConversionUtils.noteNumberToNote(12, "C♯ Major", true));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, "C♯ Major", true));
        assertEquals("E♯7", UnitConversionUtils.noteNumberToNote(89, "C♯ Major", true));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, "C♯ Major", true));

        assertEquals("B#0", UnitConversionUtils.noteNumberToNote(12, "A# Minor", false));
        assertEquals("E5", UnitConversionUtils.noteNumberToNote(64, "A♯ Minor", true));
        assertEquals("E♯7", UnitConversionUtils.noteNumberToNote(89, "A♯ Minor", true));
        assertEquals("B9", UnitConversionUtils.noteNumberToNote(119, "A# Minor", false));
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
    void midiNumberToNoteNumber() {
        assertEquals(0, UnitConversionUtils.midiNumberToNoteNumber(12));     // C0
        assertEquals(115, UnitConversionUtils.midiNumberToNoteNumber(127));  // G9
        assertEquals(48, UnitConversionUtils.midiNumberToNoteNumber(60));    // C4
        assertEquals(101, UnitConversionUtils.midiNumberToNoteNumber(113));  // F8
        assertEquals(56, UnitConversionUtils.midiNumberToNoteNumber(68));    // G#4
        assertEquals(-1, UnitConversionUtils.midiNumberToNoteNumber(-12));   // C-1
    }

    // Audio unit conversion
    @Test
    void powerToDecibel() {
        // With double
        assertEquals(3.010, UnitConversionUtils.powerToDecibel(2, 1), 0.001);
        assertEquals(10, UnitConversionUtils.powerToDecibel(10, 1), 0.001);
        assertEquals(10.915, UnitConversionUtils.powerToDecibel(12.345, 1), 0.001);
        assertEquals(-7.782, UnitConversionUtils.powerToDecibel(2, 12), 0.001);
        assertEquals(3.010, UnitConversionUtils.powerToDecibel(10, 5), 0.001);
        assertEquals(2.597, UnitConversionUtils.powerToDecibel(12.345, 6.789), 0.001);

        // With matrix
        assertArrayEquals(
                new double[][]{{-60, 0}, {10, 20}, {20, 10}},
                UnitConversionUtils.powerToDecibel(new double[][]{{0, 1}, {10, 100}, {100, 10}}, 1, 80)
        );
        assertThrowsExactly(ValueException.class, () -> UnitConversionUtils.powerToDecibel(new double[][]{{0, 1}, {10, 100}, {100, 10}}, 1, -1));
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

    @Test
    void hzToMel() {
        assertEquals(0.9, UnitConversionUtils.hzToMel(60), 1e-5);
        assertEquals(1.65, UnitConversionUtils.hzToMel(110), 1e-5);
        assertEquals(3.3, UnitConversionUtils.hzToMel(220), 1e-5);
        assertEquals(6.6, UnitConversionUtils.hzToMel(440), 1e-5);
    }

    @Test
    void melToHz() {
        assertEquals(60, UnitConversionUtils.melToHz(0.9), 1e-5);
        assertEquals(110, UnitConversionUtils.melToHz(1.65), 1e-5);
        assertEquals(220, UnitConversionUtils.melToHz(3.3), 1e-5);
        assertEquals(440, UnitConversionUtils.melToHz(6.6), 1e-5);
    }

    @Test
    void hzToOctaves() {
        assertEquals(4, UnitConversionUtils.hzToOctaves(440), 1e-5);
        assertEquals(6.21864, UnitConversionUtils.hzToOctaves(2048), 1e-5);
        assertEquals(8.81028, UnitConversionUtils.hzToOctaves(12345), 1e-5);
    }

    // Time unit conversion
    @Test
    void timeToSamples() {
        // Define times arrays
        double[] times1 = {0., 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        double[] times2 = {1., 1.25, 1.5, 1.75, 2., 2.25, 2.5, 2.75, 3., 3.25, 3.5, 3.75};

        // Define correct output arrays
        int[] correctOutput1 = {0, 2205, 4410, 6615, 8820, 11025, 13230, 15435, 17640, 19845};
        int[] correctOutput2 = {44100, 55125, 66150, 77175, 88200, 99225, 110250, 121275, 132300, 143325, 154350, 165375};

        // Run tests
        assertArrayEquals(correctOutput1, UnitConversionUtils.timeToSamples(times1, 22050));
        assertArrayEquals(correctOutput2, UnitConversionUtils.timeToSamples(times2, 44100));
    }

    @Test
    void samplesToFrames() {
        // Define samples arrays
        int[] samples1 = {
                0, 256, 512, 768, 1024, 1280, 1536, 1792, 2048,
                2304, 2560, 2816, 3072, 3328, 3584, 3840, 4096, 4352,
                4608, 4864, 5120, 5376, 5632, 5888, 6144, 6400, 6656,
                6912, 7168, 7424, 7680, 7936, 8192, 8448, 8704, 8960,
                9216, 9472, 9728, 9984, 10240, 10496, 10752, 11008, 11264,
                11520, 11776, 12032, 12288, 12544, 12800, 13056, 13312, 13568,
                13824, 14080, 14336, 14592, 14848, 15104, 15360, 15616, 15872,
                16128, 16384, 16640, 16896, 17152, 17408, 17664, 17920, 18176,
                18432, 18688, 18944, 19200, 19456, 19712, 19968, 20224, 20480,
                20736, 20992, 21248, 21504, 21760, 22016
        };
        int[] samples2 = {
                22050, 23074, 24098, 25122, 26146, 27170, 28194, 29218, 30242,
                31266, 32290, 33314, 34338, 35362, 36386, 37410, 38434, 39458,
                40482, 41506, 42530, 43554
        };

        // Define correct output arrays
        int[] correctOutput1 = {
                0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
                7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13,
                14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20,
                21, 21, 22, 22, 23, 23, 24, 24, 25, 25, 26, 26, 27, 27,
                28, 28, 29, 29, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34,
                35, 35, 36, 36, 37, 37, 38, 38, 39, 39, 40, 40, 41, 41,
                42, 42, 43
        };
        int[] correctOutput2 = {
                84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 124, 128, 132,
                136, 140, 144, 148, 152, 156, 160, 164, 168
        };

        // Run tests
        assertArrayEquals(correctOutput1, UnitConversionUtils.samplesToFrames(samples1, 512));
        assertArrayEquals(correctOutput2, UnitConversionUtils.samplesToFrames(samples2, 256, 1024));
    }

    @Test
    void timeToFrames() {
        // Define times arrays
        double[] times1 = {0., 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        double[] times2 = {1., 1.25, 1.5, 1.75, 2., 2.25, 2.5, 2.75, 3., 3.25, 3.5, 3.75};

        // Define correct output arrays
        int[] correctOutput1 = {0, 4, 8, 12, 17, 21, 25, 30, 34, 38};
        int[] correctOutput2 = {170, 213, 256, 299, 342, 385, 428, 471, 514, 557, 600, 643};

        // Run tests
        assertArrayEquals(correctOutput1, UnitConversionUtils.timeToFrames(times1, 22050, 512));
        assertArrayEquals(correctOutput2, UnitConversionUtils.timeToFrames(times2, 44100, 256, 1024));
    }

    // Other unit conversions
    @Test
    void secondsToTimeString() {
        assertEquals("00:00", UnitConversionUtils.secondsToTimeString(0));
        assertEquals("00:01", UnitConversionUtils.secondsToTimeString(1));
        assertEquals("01:00", UnitConversionUtils.secondsToTimeString(60));
        assertEquals("01:02", UnitConversionUtils.secondsToTimeString(62));
        assertEquals("00:12", UnitConversionUtils.secondsToTimeString(12));
        assertEquals("12:00", UnitConversionUtils.secondsToTimeString(720));
        assertEquals("12:34", UnitConversionUtils.secondsToTimeString(754));

        assertEquals("12345:40", UnitConversionUtils.secondsToTimeString(740740));  // Weird
    }
}