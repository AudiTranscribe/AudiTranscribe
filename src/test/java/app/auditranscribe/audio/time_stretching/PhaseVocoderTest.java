package app.auditranscribe.audio.time_stretching;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.AudioProcessingMode;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.signal.representations.STFT;
import app.auditranscribe.signal.windowing.SignalWindow;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PhaseVocoderTest {
    @Test
    void phaseVocoder() throws UnsupportedAudioFileException, AudioTooLongException, IOException {
        // Get the audio file
        File file = new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Choice.wav"));
        Audio audio = new Audio(file, AudioProcessingMode.WITH_SAMPLES);
        double[] origSamples = audio.getMonoSamples();

        // Process the phase vocoder on the STFT
        Complex[][] stft = STFT.stft(origSamples, 2048, 512, SignalWindow.HANN_WINDOW);
        Complex[][] modifiedSTFT = PhaseVocoder.phaseVocoder(stft, 512, 0.5);

        assertEquals(
                new Complex(-0.8587852045773049, -0.02124936327651138).round(5),
                modifiedSTFT[123][456].round(5)
        );
        assertEquals(
                new Complex(0.03583205029518891, -0.007203589112932326).round(5),
                modifiedSTFT[456][789].round(5)
        );
        assertEquals(
                new Complex(-0.000618956101298028).round(5),
                modifiedSTFT[1024][861].round(5)
        );
        assertEquals(
                new Complex(0.03217018536084809, 0.02131976270749728).round(5),
                modifiedSTFT[111][222].round(5)
        );
        assertEquals(
                new Complex(0.01123162157701242, -0.0019310488551249204).round(5),
                modifiedSTFT[666][777].round(5)
        );
    }
}