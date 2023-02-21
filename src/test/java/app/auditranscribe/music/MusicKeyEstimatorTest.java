package app.auditranscribe.music;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicKeyEstimatorTest {
    @Test
    void musicKeyEstimatorTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        // Test 1
        Audio audio1 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Choice.wav")),
                Audio.ProcessingMode.WITH_SAMPLES
        );

        double[] samples1 = audio1.getMonoSamples();
        double sampleRate1 = audio1.getSampleRate();

        List<MusicKey> mostLikelyKeys1 = getMostLikelyKeys(samples1, sampleRate1, 3);
        assertEquals(List.of(MusicKey.G_MAJOR, MusicKey.D_MINOR, MusicKey.G_MINOR), mostLikelyKeys1);

        // Test 2
        Audio audio2 = new Audio(
                new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Trumpet.wav")),
                Audio.ProcessingMode.WITH_SAMPLES
        );

        double[] samples2 = audio2.getMonoSamples();
        double sampleRate2 = audio2.getSampleRate();

        List<MusicKey> mostLikelyKeys2 = getMostLikelyKeys(samples2, sampleRate2, 4);

        assertEquals(
                List.of(MusicKey.C_SHARP_MINOR, MusicKey.F_MAJOR,MusicKey.B_FLAT_MAJOR,  MusicKey.F_MINOR),
                mostLikelyKeys2
        );

        // Test 3: Invalid key values
        assertThrowsExactly(ValueException.class, () -> getMostLikelyKeys(samples1, sampleRate1, 0));
        assertThrowsExactly(ValueException.class, () -> getMostLikelyKeys(samples2, sampleRate2, -1));
        assertThrowsExactly(ValueException.class, () -> getMostLikelyKeys(samples1, sampleRate1, 31));
        assertThrowsExactly(ValueException.class, () -> getMostLikelyKeys(samples2, sampleRate2, 1337));
    }

    // Helper functions
    static List<MusicKey> getMostLikelyKeys(double[] x, double sampleRate, int numKeys) {
        // Get the keys with correlation
        List<Pair<MusicKey, Double>> keysWithCorr = MusicKeyEstimator.getMostLikelyKeysWithCorrelation(
                x, sampleRate, numKeys, null
        );

        // Get only the keys
        List<MusicKey> keys = new ArrayList<>(keysWithCorr.size());
        for (Pair<MusicKey, Double> pair : keysWithCorr) {
            keys.add(pair.value0());
        }
        return keys;
    }
}