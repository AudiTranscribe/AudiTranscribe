/*
 * BPMEstimatorTest.java
 *
 * Created on 2022-06-02
 * Updated on 2022-06-21
 *
 * Description: Test `BPMEstimator.java`.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.io.IOMethods;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BPMEstimatorTest {
    @Disabled
    @Test
    void estimate() throws UnsupportedAudioFileException, IOException, AudioTooLongException {
        // Get the audio files
        Audio audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-audio-files/175bpm.wav")),
                "175bpm.wav",
                AudioProcessingMode.SAMPLES_ONLY
        );
        Audio audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-audio-files/137bpmNoisy.wav")),
                "137bpmNoisy.wav",
                AudioProcessingMode.SAMPLES_ONLY
        );

        // Extract samples and sample rate
        double[] samples1 = audio1.getMonoSamples();
        double sampleRate1 = audio1.getSampleRate();

        double[] samples2 = audio2.getMonoSamples();
        double sampleRate2 = audio2.getSampleRate();

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
        assertEquals(87.59269068, bpms1.get(0), 1e-5);  // Yes, the BPM estimated is not 175 bpm, but that's what librosa says
        assertEquals(135.99917763, bpms2.get(0), 1e-5);
    }
}