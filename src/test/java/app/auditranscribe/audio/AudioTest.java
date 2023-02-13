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
    void checkMonoSamples() {
        double[] samples = audio.getMonoSamples();
        assertEquals(0.07012939453125, samples[1234], 1e-5);
        assertEquals(0.0779266357421875, samples[2345], 1e-5);
        assertEquals(0.0315704345703125, samples[3456], 1e-5);
        assertEquals(-0.0475616455078125, samples[45678], 1e-5);
        assertEquals(-0.0015411376953125, samples[212345], 1e-5);
    }

    final boolean TEST_PLAYBACK = false;

    @Test
    @DisabledIf("isPlaybackTestDisabled")
    void playback() throws InterruptedException {
        System.out.println("VOLUME 25%");
        audio.setVolume(0.25);
        audio.play();

//        audio.getAudioPlaybackThread().join();

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1500);
                System.out.println("VOLUME 100%");
                audio.setVolume(1);
                Thread.sleep(1000);
                System.out.println("SEEK 2.75");
                audio.seekToTime(2.75);
                Thread.sleep(1000);
                System.out.println("CHECK TIME: " + audio.getCurrentTime());
                Thread.sleep(1000);
                System.out.println("SEEK 0.25");
                audio.seekToTime(0.25);
                Thread.sleep(1000);
                System.out.println("CHECK TIME: " + audio.getCurrentTime());
                Thread.sleep(1000);
                System.out.println("VOLUME 50%");
                audio.setVolume(0.5);
                Thread.sleep(1000);
                System.out.println("SEEK 1");
                audio.seekToTime(1);
                Thread.sleep(1000);
                audio.pause();
                System.out.println("PAUSED 1.5");
                Thread.sleep(1500);
                audio.play();
                System.out.println("RESUMED");
                Thread.sleep(1000);
                System.out.println("SEEK 0");
                audio.seekToTime(0);
                Thread.sleep(1000);
                System.out.println("VOLUME 150%");
                audio.setVolume(1.5);
                Thread.sleep(3000);
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