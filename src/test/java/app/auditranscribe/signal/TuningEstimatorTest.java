package app.auditranscribe.signal;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TuningEstimatorTest {
    // Load samples and sample rate for some tests
    static Audio audio;

    static double[] samples;
    static double sampleRate;

    @BeforeAll
    static void beforeAll() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
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
}