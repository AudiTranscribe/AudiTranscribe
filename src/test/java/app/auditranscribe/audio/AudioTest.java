package app.auditranscribe.audio;

import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AudioTest {
    final boolean TEST_PLAYBACK = false;

    Audio audio;

    AudioTest() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        File file = new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Trumpet.wav"));

        if (TEST_PLAYBACK) {
            audio = new Audio(
                    file, AudioProcessingMode.WITH_SAMPLES, AudioProcessingMode.WITH_PLAYBACK
            );
        } else {
            audio = new Audio(
                    file, AudioProcessingMode.WITH_SAMPLES
            );
        }
    }

    @Test
    void getDuration() {
        assertEquals(5.333, audio.getDuration(), 1e-3);  // 5.333 seconds was obtained from Audacity
    }

    @Test
    void getNumRawSamples() {
        assertEquals(235202, audio.getNumRawSamples());
    }

    @Test
    void getNumMonoSamples() {
        assertEquals(117601, audio.getNumMonoSamples());
    }

    @Test
    @DisabledIf("isPlaybackTestDisabled")
    void playback() throws InterruptedException {
        audio.play();

//        audio.getAudioPlaybackThread().join();

        System.out.println("SLEEPING");
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        t.join();
        System.out.println("DONE");
        audio.stop();
        System.out.println("STOPPED?");
    }

    // Helper methods
    boolean isPlaybackTestDisabled() {
        return !TEST_PLAYBACK;
    }
}