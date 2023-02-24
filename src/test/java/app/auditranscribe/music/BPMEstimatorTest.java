package app.auditranscribe.music;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BPMEstimatorTest {
    // Get the audio files
    static Audio audio1, audio2;

    // Extract samples and sample rate
    static double[] samples1;
    static double sampleRate1;

    static double[] samples2;
    static double sampleRate2;

    @BeforeAll
    static void beforeAll() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("test-files/music/BPMEstimatorTest/175bpm.wav")),
                Audio.ProcessingMode.WITH_SAMPLES
        );
        audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("test-files/music/BPMEstimatorTest/137bpmNoisy.wav")),
                Audio.ProcessingMode.WITH_SAMPLES
        );

        samples1 = audio1.getMonoSamples();
        sampleRate1 = audio1.getSampleRate();

        samples2 = audio2.getMonoSamples();
        sampleRate2 = audio2.getSampleRate();
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    void estimate() {
        // Check sample rate and the number of samples
        assertEquals(44100, sampleRate1, "Audio 1 sample rate is not 44100.");
        assertEquals(418950, samples1.length, "Audio 1 does not have correct sample length.");

        assertEquals(44100, sampleRate2, "Audio 2 sample rate is not 44100.");
        assertEquals(441000, samples2.length, "Audio 2 does not have correct sample length.");

        // Generate the estimated BPMs
        List<Double> bpms1 = BPMEstimator.estimate(samples1, sampleRate1);
        List<Double> bpms2 = BPMEstimator.estimate(samples2, sampleRate2);

        // Check the number of elements in the lists
        assertEquals(1, bpms1.size());
        assertEquals(1, bpms2.size());

        // Check the values of the elements
        assertEquals(87.59269068, bpms1.get(0), 1e-5);  // Surprisingly we did not estimate 175 bpm
        assertEquals(135.99917763, bpms2.get(0), 1e-5);

        // Will throw the " The `winLength` must be a positive integer" exception
        assertThrowsExactly(ValueException.class, () -> BPMEstimator.estimate(samples1, 1));
    }
}