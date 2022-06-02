/*
 * RhythmTest.java
 *
 * Created on 2022-06-02
 * Updated on 2022-06-02
 *
 * Description: Test `Rhythm.java`.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.io.IOMethods;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RhythmTest {
    
    @Test
    void tempogram() throws UnsupportedAudioFileException, IOException {
        // Get the audio files
        Audio audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-audio-files/100bpm.wav")), false
        );
        Audio audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("testing-audio-files/175bpm.wav")), false
        );

        // Extract samples and sample rate
        double[] samples1 = audio1.getMonoSamples();
        double sampleRate1 = audio1.getSampleRate();

        double[] samples2 = audio2.getMonoSamples();
        double sampleRate2 = audio2.getSampleRate();

        // Check sample rate and the number of samples
        assertEquals(44100, sampleRate1, "Audio 1 sample rate is not 44100.");
        assertEquals(441000, samples1.length, "Audio 1 do not have correct sample length.");

        assertEquals(44100, sampleRate2, "Audio 2 sample rate is not 44100.");
        assertEquals(418950, samples2.length, "Audio 2 do not have correct sample length.");

        // Generate tempograms
        double[][] tempogram1 = Rhythm.tempogram(samples1, sampleRate1, 512, 384);
        double[][] tempogram2 = Rhythm.tempogram(samples2, sampleRate2, 512, 384);

        // Check shapes
        assertEquals(384, tempogram1.length, "Tempogram 1 is not the correct shape.");
        assertEquals(862, tempogram1[0].length, "Tempogram 1 is not the correct shape.");

        assertEquals(384, tempogram2.length, "Tempogram 2 is not the correct shape.");
        assertEquals(819, tempogram2[0].length, "Tempogram 2 is not the correct shape.");

        // Check specific values
        assertEquals(1, tempogram1[0][0], 1e-5);
        assertEquals(0.14636221663949453, tempogram1[10][10], 1e-5);
        assertEquals(0.08303415986079692, tempogram1[100][100], 1e-5);
        assertEquals(0.01108652795528559, tempogram1[200][100], 1e-5);
        assertEquals(0.09774993214257335, tempogram1[100][200], 1e-5);
        assertEquals(0.019966318364714193, tempogram1[200][700], 1e-5);
        assertEquals(0, tempogram1[383][861], 1e-5);

        assertEquals(1, tempogram2[0][0], 1e-5);
        assertEquals(0.0990677439302926, tempogram2[10][10], 1e-5);
        assertEquals(0.06631539915306119, tempogram2[100][100], 1e-5);
        assertEquals(0.009447871329599832, tempogram2[200][100], 1e-5);
        assertEquals(0.08091095220304986, tempogram2[100][200], 1e-5);
        assertEquals(0.014109313669571865, tempogram2[200][700], 1e-5);
        assertEquals(0, tempogram2[383][818], 1e-5);
    }
}