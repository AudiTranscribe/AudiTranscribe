/*
 * SpectralHelpersTest.java
 * Description: Test `SpectralHelpers.java`.
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

package site.overwrite.auditranscribe.spectrogram;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SpectralHelpersTest {
    // Load samples and sample rate for some tests
    Audio audio = new Audio(
            new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Trumpet.wav")),
            AudioProcessingMode.SAMPLES
    );

    double[] samples = audio.getMonoSamples();
    double sampleRate = audio.getSampleRate();

    // Initialization method
    SpectralHelpersTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
    }

    // Tests
    @Test
    void computeAlpha() {
        assertEquals(0.6, SpectralHelpers.computeAlpha(1));
        assertEquals(MathUtils.round((double) 1 / 3, 6), MathUtils.round(SpectralHelpers.computeAlpha(2), 6));
    }

    @Test
    void estimateTuning() {
        assertEquals(-0.09, SpectralHelpers.estimateTuning(samples, sampleRate), 1e-5);
    }

    @Test
    void estimateTuningWithWeirdThreshold() {
        assertEquals(-0.5, SpectralHelpers.estimateTuning(samples, sampleRate, 1e6), 1e-5);
    }

    @Test
    void parabolicInterp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Make this private method available to the test
        Method parabolicInterp = SpectralHelpers.class.getDeclaredMethod("parabolicInterp", double[][].class);
        parabolicInterp.setAccessible(true);

        // Construct some test spectral matrices
        double[][] S1 = {
                {1, 2, 3, 3, 2, 1},
                {4, 5, 6, 4, 5, 6},
                {9, 8, 7, 7, 8, 9}
        };
        double[][] S2 = {
                {-5, -4, -3},
                {-4, -3, -2},
                {-3, -2, -1},
                {1, 2, 3},
                {2, 3, 4},
                {3, 4, 5},
        };

        double[][] correctOut1a = {
                {0, 0, 0, 0, 0, 0},
                {4, 3, 2, 2, 3, 4},
                {0, 0, 0, 0, 0, 0}
        };
        double[][] correctOut1b = {
                {0, 0, 0, 0, 0, 0},
                {-2, 3, 1, -1, 3, 2},
                {0, 0, 0, 0, 0, 0}
        };
        double[][] correctOut2a = {
                {0, 0, 0},
                {1, 1, 1},
                {5. / 2, 5. / 2, 5. / 2},
                {5. / 2, 5. / 2, 5. / 2},
                {1, 1, 1},
                {0, 0, 0},
        };
        double[][] correctOut2b = {
                {0, 0, 0},
                {1, 1, 1},
                {-5. / 6, -5. / 6, -5. / 6},
                {5. / 6, 5. / 6, 5. / 6},
                {1, 1, 1},
                {0, 0, 0},
        };

        // Compute parabolic interpolation
        @SuppressWarnings({"unchecked"})
        Pair<Double[][], Double[][]> outPair1 = (Pair<Double[][], Double[][]>)
                parabolicInterp.invoke(null, (Object) S1);
        Double[][] out1a = outPair1.value0();
        Double[][] out1b = outPair1.value1();

        @SuppressWarnings({"unchecked"})
        Pair<Double[][], Double[][]> outPair2 = (Pair<Double[][], Double[][]>)
                parabolicInterp.invoke(null, (Object) S2);
        Double[][] out2a = outPair2.value0();
        Double[][] out2b = outPair2.value1();

        // Assertions
        assertEquals(3, out1a.length);
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(correctOut1a[i], TypeConversionUtils.toDoubleArray(out1a[i]), 1e-5);
        }

        assertEquals(3, out1b.length);
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(correctOut1b[i], TypeConversionUtils.toDoubleArray(out1b[i]), 1e-5);
        }

        assertEquals(6, out2a.length);
        for (int i = 0; i < 6; i++) {
            assertArrayEquals(correctOut2a[i], TypeConversionUtils.toDoubleArray(out2a[i]), 1e-5);
        }

        assertEquals(6, out2b.length);
        for (int i = 0; i < 6; i++) {
            assertArrayEquals(correctOut2b[i], TypeConversionUtils.toDoubleArray(out2b[i]), 1e-5);
        }
    }
}