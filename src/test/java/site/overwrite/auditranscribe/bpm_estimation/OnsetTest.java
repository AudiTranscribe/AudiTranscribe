/*
 * OnsetTest.java
 *
 * Created on 2022-05-31
 * Updated on 2022-06-02
 *
 * Description: Test `Onset.java`.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.io.IOMethods;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OnsetTest {

    @Test
    void onsetStrength() throws UnsupportedAudioFileException, IOException {
        // Get the audio files
        Audio audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-audio-files/Choice.wav")), false
        );
        Audio audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-audio-files/LevelsOfC.wav")), false
        );

        // Extract samples and sample rate
        double[] samples1 = audio1.getMonoSamples();
        double sampleRate1 = audio1.getSampleRate();

        double[] samples2 = audio2.getMonoSamples();
        double sampleRate2 = audio2.getSampleRate();

        // Check sample rate and the number of samples
        assertEquals(44100, sampleRate1, "Audio 1 sample rate is not 44100.");
        assertEquals(220500, samples1.length, "Audio 1 do not have correct sample length.");

        assertEquals(44100, sampleRate2, "Audio 2 sample rate is not 44100.");
        assertEquals(485100, samples2.length, "Audio 2 do not have correct sample length.");

        // Get the onset strength
        double[] onset1 = Onset.onsetStrength(samples1, sampleRate1);
        double[] onset2 = Onset.onsetStrength(samples2, sampleRate2);

        // Check the onset array lengths
        assertEquals(431, onset1.length, "Onset array 1 is not the correct length.");
        assertEquals(948, onset2.length, "Onset array 2 is not the correct length.");

        // Check specific values of the onset arrays
        assertEquals(0, onset1[0], 1e-5);
        assertEquals(0, onset1[1], 1e-5);
        assertEquals(0, onset1[2], 1e-5);
        assertEquals(15.597718, onset1[3], 1e-5);
        assertEquals(4.275174, onset1[5], 1e-5);
        assertEquals(0.6414422, onset1[34], 1e-5);
        assertEquals(1.1619507, onset1[56], 1e-5);
        assertEquals(0.3802176, onset1[128], 1e-5);
        assertEquals(25.021122, onset1[382], 1e-5);
        assertEquals(0.7082443, onset1[430], 1e-5);

        assertEquals(0, onset2[0], 1e-5);
        assertEquals(0, onset2[1], 1e-5);
        assertEquals(0, onset2[2], 1e-5);
        assertEquals(0.050067842, onset2[3], 1e-5);
        assertEquals(0.11666649, onset2[5], 1e-5);
        assertEquals(0.09207238, onset2[34], 1e-5);
        assertEquals(0.006071925, onset2[56], 1e-5);
        assertEquals(0.24788356, onset2[128], 1e-5);
        assertEquals(0.0063540423, onset2[382], 1e-5);
        assertEquals(19.384262, onset2[432], 1e-5);
        assertEquals(0.0029006004, onset2[678], 1e-5);
        assertEquals(0, onset2[912], 1e-5);
        assertEquals(0, onset2[930], 1e-5);
    }
}