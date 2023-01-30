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
        File file = new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Choice.wav"));

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
        assertEquals(5.000, audio.getDuration(), 1e-3);  // 5.000 seconds was obtained from Audacity
    }

    @Test
    void getSampleRate() {
        assertEquals(44100, audio.getSampleRate());
    }

    @Test
    void getNumRawSamples() {
        assertEquals(441000, audio.getNumRawSamples());
    }

    @Test
    void getNumMonoSamples() {
        assertEquals(220500, audio.getNumMonoSamples());
    }

    @Test
    @DisabledIf("isPlaybackTestDisabled")
    void playback() throws InterruptedException {
        audio.play();

//        audio.getAudioPlaybackThread().join();

        System.out.println("SLEEPING");
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("SEEK");
                audio.seekToTime(2.75);
                Thread.sleep(1000);
                System.out.println("CHECK TIME: " + audio.getCurrentTime());
                Thread.sleep(1000);
                System.out.println("SEEK");
                audio.seekToTime(0.25);
                Thread.sleep(1000);
                System.out.println("CHECK TIME: " + audio.getCurrentTime());
                Thread.sleep(1000);
                System.out.println("SEEK");
                audio.seekToTime(1);
                Thread.sleep(500);
                audio.pause();
                System.out.println("PAUSED");
                Thread.sleep(2000);
                audio.play();
                System.out.println("RESUMED");
                Thread.sleep(2500);
                System.out.println("DONE");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        t.join();
        audio.stop();
        System.out.println("STOPPED");
    }

    // Helper methods
    boolean isPlaybackTestDisabled() {
        return !TEST_PLAYBACK;
    }
}