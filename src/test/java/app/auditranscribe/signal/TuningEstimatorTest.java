package app.auditranscribe.signal;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.utils.TypeConversionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TuningEstimatorTest {
    // Load samples and sample rate for some tests
    static Audio audio;

    static double[] samples;
    static double sampleRate;

    @BeforeAll
    static void beforeAll() throws UnsupportedAudioFileException, Audio.TooLongException, IOException {
        audio = new Audio(
                new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Trumpet.wav")),
                Audio.ProcessingMode.WITH_SAMPLES
        );
        samples = audio.getMonoSamples();
        sampleRate = audio.getSampleRate();
    }

    @Test
    void estimateTuning() {
        assertEquals(-0.09, TuningEstimator.estimateTuning(samples, sampleRate), 1e-5);
    }

    @Test
    void estimateTuning_weirdThreshold() {
        assertEquals(-0.5, TuningEstimator.estimateTuning(samples, sampleRate, 1e6), 1e-5);
    }

    @Test
    void parabolicInterp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Make this private method available to the test
        Method parabolicInterp = TuningEstimator.class.getDeclaredMethod("parabolicInterp", double[][].class);
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