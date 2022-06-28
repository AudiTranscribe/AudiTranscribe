/*
 * QTransformTests.java
 *
 * Created on 2022-04-10
 * Updated on 2022-06-28
 *
 * Description: Test the Q-Transform algorithms.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

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
    void cqtNormal() {
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
}