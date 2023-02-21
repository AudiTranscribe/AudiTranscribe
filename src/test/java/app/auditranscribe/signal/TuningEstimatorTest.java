package app.auditranscribe.signal;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TuningEstimatorTest {
    // Load samples and sample rate for some tests
    Audio audio = new Audio(
            new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Trumpet.wav")),
            Audio.ProcessingMode.WITH_SAMPLES
    );

    double[] samples = audio.getMonoSamples();
    double sampleRate = audio.getSampleRate();

    TuningEstimatorTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
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