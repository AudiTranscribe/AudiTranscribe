/*
 * QTransformTests.java
 *
 * Created on 2022-04-10
 * Updated on 2022-06-29
 *
 * Description: Test the Q-Transform algorithms.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QTransformTests {
    // Get audio file and audio samples for the tests
    Audio audio = new Audio(
            new File(IOMethods.getAbsoluteFilePath("testing-files/audio/Choice.wav")),
            "Choice.wav",
            AudioProcessingMode.SAMPLES_ONLY
    );
    double[] samples = audio.getMonoSamples();

    QTransformTests() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
    }

    @Test
    @Order(1)
    void cqt() {
        // Process CQT on those samples
        Complex[][] cqtMatrix = CQT.cqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, WindowFunction.HANN_WINDOW, null
        );

        // Check specific values in this resultant VQT matrix
        assertEquals(-0.002, MathUtils.round(cqtMatrix[109][32].re(), 3), 0.005);
        assertEquals(-0.001, MathUtils.round(cqtMatrix[109][32].im(), 3), 0.005);
        assertEquals(-0.010, MathUtils.round(cqtMatrix[16][422].re(), 3), 0.005);
        assertEquals(-0.036, MathUtils.round(cqtMatrix[16][422].im(), 3), 0.005);
        assertEquals(-0.018, MathUtils.round(cqtMatrix[8][25].re(), 3), 0.005);
        assertEquals(0.143, MathUtils.round(cqtMatrix[8][25].im(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[167][425].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[167][425].im(), 3), 0.005);
        assertEquals(-0.051, MathUtils.round(cqtMatrix[11][68].re(), 3), 0.005);
        assertEquals(-0.084, MathUtils.round(cqtMatrix[11][68].im(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[118][341].re(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[118][341].im(), 3), 0.005);
        assertEquals(-0.070, MathUtils.round(cqtMatrix[27][87].re(), 3), 0.005);
        assertEquals(0.073, MathUtils.round(cqtMatrix[27][87].im(), 3), 0.005);
        assertEquals(-0.019, MathUtils.round(cqtMatrix[22][178].re(), 3), 0.005);
        assertEquals(-0.034, MathUtils.round(cqtMatrix[22][178].im(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[104][46].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[104][46].im(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[147][398].re(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[147][398].im(), 3), 0.005);
    }

    @Test
    @Order(1)
    void vqtNormal() {
        // Process VQT on those samples
        Complex[][] vqtMatrix = VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, false, 0, WindowFunction.HANN_WINDOW, null
        );

        // Check specific values in this resultant VQT matrix
        assertEquals(-0.002, MathUtils.round(vqtMatrix[109][32].re(), 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[109][32].im(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[16][422].re(), 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[16][422].im(), 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[8][25].re(), 3), 0.005);
        assertEquals(-0.026, MathUtils.round(vqtMatrix[8][25].im(), 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[167][425].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[167][425].im(), 3), 0.005);
        assertEquals(0.513, MathUtils.round(vqtMatrix[11][68].re(), 3), 0.005);
        assertEquals(-0.305, MathUtils.round(vqtMatrix[11][68].im(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[118][341].re(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[118][341].im(), 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[27][87].re(), 3), 0.005);
        assertEquals(-0.072, MathUtils.round(vqtMatrix[27][87].im(), 3), 0.005);
        assertEquals(-0.367, MathUtils.round(vqtMatrix[22][178].re(), 3), 0.005);
        assertEquals(-0.252, MathUtils.round(vqtMatrix[22][178].im(), 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[104][46].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[104][46].im(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[147][398].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[147][398].im(), 3), 0.005);
    }

    @Test
    @Order(2)
    void vqtUseAlternateCall() {
        // Process VQT on those samples
        Complex[][] vqtMatrix = VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, 0, WindowFunction.HANN_WINDOW, null
        );

        // Check specific values in this resultant VQT matrix
        assertEquals(-0.002, MathUtils.round(vqtMatrix[109][32].re(), 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[109][32].im(), 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[167][425].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[167][425].im(), 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[27][87].re(), 3), 0.005);
        assertEquals(-0.072, MathUtils.round(vqtMatrix[27][87].im(), 3), 0.005);
    }

    @Test
    @Order(2)
    void vqtUseProvidedGamma() {
        // Process VQT on those samples
        Complex[][] vqtMatrix = VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, false, 12, WindowFunction.HANN_WINDOW, null
        );

        // Check specific values in this resultant VQT matrix
        assertEquals(0.000, MathUtils.round(vqtMatrix[123][45].re(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[123][45].im(), 3), 0.005);
        assertEquals(0.006, MathUtils.round(vqtMatrix[121][11].re(), 3), 0.005);
        assertEquals(0.006, MathUtils.round(vqtMatrix[121][11].im(), 3), 0.005);
        assertEquals(0.139, MathUtils.round(vqtMatrix[12][345].re(), 3), 0.005);
        assertEquals(-0.096, MathUtils.round(vqtMatrix[12][345].im(), 3), 0.005);
    }

    @Test
    @Order(2)
    void vqtUseKaiserBest() {
        // Process VQT on those samples
        Complex[][] vqtMatrix = VQT.vqt(
                samples, audio.getSampleRate() / 1.75, 512, UnitConversionUtils.noteToFreq("C1"), 9,
                1, false, 12, WindowFunction.HANN_WINDOW, null
        );

        // Check specific values in this resultant VQT matrix
        assertEquals(-0.002, MathUtils.round(vqtMatrix[3][45].re(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[3][45].im(), 3), 0.005);
        assertEquals(0.003, MathUtils.round(vqtMatrix[5][11].re(), 3), 0.005);
        assertEquals(0.033, MathUtils.round(vqtMatrix[5][11].im(), 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[8][345].re(), 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[8][345].im(), 3), 0.005);
    }

    @Test
    @Order(2)
    void vqtNonMultipleOf2HopLength() {
        // Process VQT on those samples
        Complex[][] vqtMatrix = VQT.vqt(
                samples, audio.getSampleRate(), 511, UnitConversionUtils.noteToFreq("C1"), 28,
                4, 0, WindowFunction.HANN_WINDOW, null
        );

        // Check specific values in this resultant VQT matrix
        assertEquals(-0.001, MathUtils.round(vqtMatrix[10][32].re(), 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[10][32].im(), 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[15][425].re(), 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[15][425].im(), 3), 0.005);
        assertEquals(0.003, MathUtils.round(vqtMatrix[27][87].re(), 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[27][87].im(), 3), 0.005);
    }

    @Test
    @Order(3)
    void vqtUseFakeTask() {
        // Start JavaFX toolkit
        new JFXPanel();

        // Process VQT on those samples
        VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, false, 0, WindowFunction.HANN_WINDOW, new CustomTask<Void>() {
                    @Override
                    protected Void call() {
                        return null;
                    }
                }
        );  // Kaiser Fast

        VQT.vqt(
                samples, audio.getSampleRate() / 1.75, 512, UnitConversionUtils.noteToFreq("C1"), 9,
                1, false, 12, WindowFunction.HANN_WINDOW, new CustomTask<Void>() {
                    @Override
                    protected Void call() {
                        return null;
                    }
                }
        );  // Kaiser Best
    }

    @Test
    @Order(3)
    void vqtPassInvalidValues() {
        // Provide invalid values in an attempt to throw errors
        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 0,
                24, false, 12, WindowFunction.HANN_WINDOW, null
        ));  // Number of bins is invalid
        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), -12,
                24, false, 12, WindowFunction.HANN_WINDOW, null
        ));  // Number of bins is invalid

        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                0, false, 12, WindowFunction.HANN_WINDOW, null
        ));  // Bins per octave is invalid
        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                -12, false, 12, WindowFunction.HANN_WINDOW, null
        ));  // Bins per octave is invalid

        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                13, false, 12, WindowFunction.HANN_WINDOW, null
        ));  // Bins per octave not multiple of number of bins

        assertThrowsExactly(InvalidParameterException.class, () -> VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 336,
                24, false, 12, WindowFunction.HANN_WINDOW, null
        ));  // Too many bins, resulting in cutoff frequency being above Nyquist frequency
    }

    @Test
    @Order(3)
    void vqtCauseErrorInEarlyDownsample() {
        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                new double[1], audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 56,
                8, 0, WindowFunction.HANN_WINDOW, null
        ));  // Input signal length of 1 is too short for 7-octave VQT
        assertThrowsExactly(ValueException.class, () -> VQT.vqt(
                new double[0], audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 56,
                8, 0, WindowFunction.HANN_WINDOW, null
        ));  // Input signal length of 0 is too short for 7-octave VQT
    }
}