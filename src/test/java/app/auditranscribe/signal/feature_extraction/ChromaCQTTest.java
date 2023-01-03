package app.auditranscribe.signal.feature_extraction;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.AudioProcessingMode;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.utils.UnitConversionUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ChromaCQTTest {
    // Get audio file, audio samples, and sample rate for the tests
    Audio audio = new Audio(
            new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Trumpet.wav")),
            AudioProcessingMode.WITH_SAMPLES
    );

    double[] samples = audio.getMonoSamples();
    double sampleRate = audio.getSampleRate();

    ChromaCQTTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
    }

    @Test
    @Order(1)
    void chromaCQT() {
        double[][] chroma = ChromaCQT.chromaCQT(
                samples, sampleRate, 512, UnitConversionUtils.noteToFreq("C1"), 12, 7,
                36, null
        );

        assertEquals(0.21, chroma[0][0], 1e-2);
        assertEquals(0.05, chroma[1][1], 1e-2);
        assertEquals(0.30, chroma[11][23], 1e-2);
        assertEquals(0.61, chroma[5][214], 1e-2);
        assertEquals(0.13, chroma[9][128], 1e-2);
    }

    @Test
    @Order(2)
    void chromaCQT_withDifferentThreshold() {
        double[][] chroma = ChromaCQT.chromaCQT(
                samples, sampleRate, 512, UnitConversionUtils.noteToFreq("C1"), 12, 7,
                36, 0.45, null
        );

        assertEquals(0.23, chroma[0][0], 1e-2);
        assertEquals(0.05, chroma[1][1], 1e-2);
        assertEquals(0.30, chroma[11][23], 1e-2);
        assertEquals(1.00, chroma[5][214], 1e-2);
        assertEquals(0.15, chroma[9][128], 1e-2);
    }

    @Test
    @Order(3)
    void chromaCQT_causeException() {
        // An exception should be thrown if the number of bins is not a multiple of `numChroma`
        assertThrowsExactly(ValueException.class, () ->
                ChromaCQT.chromaCQT(
                        samples, sampleRate, 512, UnitConversionUtils.noteToFreq("C1"), 12,
                        7, 13, null)
        );
    }
}