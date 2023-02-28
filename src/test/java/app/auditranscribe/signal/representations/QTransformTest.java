package app.auditranscribe.signal.representations;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.UnitConversionUtils;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QTransformTest {
    static Audio audio;
    static double[] samples;

    static Method vqtHelperMtd;

    @BeforeAll
    static void beforeAll() throws UnsupportedAudioFileException, Audio.TooLongException, IOException,
            NoSuchMethodException {
        // Get audio file and audio samples for the tests
        audio = new Audio(
                new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/Choice.wav")),
                Audio.ProcessingMode.WITH_SAMPLES
        );
        samples = audio.getMonoSamples();

        // Allow access to the alternate Q-Transform call
        vqtHelperMtd = QTransform.class.getDeclaredMethod(
                "vqtHelper", double[].class, double.class, int.class, double.class, int.class, int.class,
                double.class, double.class, SignalWindow.class, CustomTask.class, boolean.class
        );
        vqtHelperMtd.setAccessible(true);
    }

    @Test
    @Order(1)
    void cqt() {
        // Process CQT on those samples
        Complex[][] cqtMatrix = QTransform.cqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, 0, SignalWindow.HANN_WINDOW, null
        );

        // Check specific values in this resultant QTransform matrix
        Assertions.assertEquals(-0.002, MathUtils.round(cqtMatrix[109][32].re, 3), 0.005);
        assertEquals(-0.001, MathUtils.round(cqtMatrix[109][32].im, 3), 0.005);
        assertEquals(-0.010, MathUtils.round(cqtMatrix[16][422].re, 3), 0.005);
        assertEquals(-0.036, MathUtils.round(cqtMatrix[16][422].im, 3), 0.005);
        assertEquals(-0.018, MathUtils.round(cqtMatrix[8][25].re, 3), 0.005);
        assertEquals(0.143, MathUtils.round(cqtMatrix[8][25].im, 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[167][425].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[167][425].im, 3), 0.005);
        assertEquals(-0.051, MathUtils.round(cqtMatrix[11][68].re, 3), 0.005);
        assertEquals(-0.084, MathUtils.round(cqtMatrix[11][68].im, 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[118][341].re, 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[118][341].im, 3), 0.005);
        assertEquals(-0.070, MathUtils.round(cqtMatrix[27][87].re, 3), 0.005);
        assertEquals(0.073, MathUtils.round(cqtMatrix[27][87].im, 3), 0.005);
        assertEquals(-0.019, MathUtils.round(cqtMatrix[22][178].re, 3), 0.005);
        assertEquals(-0.034, MathUtils.round(cqtMatrix[22][178].im, 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[104][46].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[104][46].im, 3), 0.005);
        assertEquals(0.001, MathUtils.round(cqtMatrix[147][398].re, 3), 0.005);
        assertEquals(0.000, MathUtils.round(cqtMatrix[147][398].im, 3), 0.005);
    }

    @Test
    @Order(1)
    void vqt_normal() {
        // Process QTransform on those samples
        Complex[][] vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, 0, 0, SignalWindow.HANN_WINDOW, null
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(-0.002, MathUtils.round(vqtMatrix[109][32].re, 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[109][32].im, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[16][422].re, 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[16][422].im, 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[8][25].re, 3), 0.005);
        assertEquals(-0.026, MathUtils.round(vqtMatrix[8][25].im, 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[167][425].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[167][425].im, 3), 0.005);
        assertEquals(0.513, MathUtils.round(vqtMatrix[11][68].re, 3), 0.005);
        assertEquals(-0.305, MathUtils.round(vqtMatrix[11][68].im, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[118][341].re, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[118][341].im, 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[27][87].re, 3), 0.005);
        assertEquals(-0.072, MathUtils.round(vqtMatrix[27][87].im, 3), 0.005);
        assertEquals(-0.367, MathUtils.round(vqtMatrix[22][178].re, 3), 0.005);
        assertEquals(-0.252, MathUtils.round(vqtMatrix[22][178].im, 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[104][46].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[104][46].im, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[147][398].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[147][398].im, 3), 0.005);
    }

    @Test
    @Order(2)
    void vqt_helperMethodCall() throws InvocationTargetException, IllegalAccessException {
        // Process QTransform on those samples
        Complex[][] vqtMatrix = (Complex[][]) vqtHelperMtd.invoke(null,
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, 0, 0, SignalWindow.HANN_WINDOW, null, false
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(0.000, MathUtils.round(vqtMatrix[16][422].re, 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[16][422].im, 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[167][425].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[167][425].im, 3), 0.005);
        assertEquals(0.513, MathUtils.round(vqtMatrix[11][68].re, 3), 0.005);
        assertEquals(-0.305, MathUtils.round(vqtMatrix[11][68].im, 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[27][87].re, 3), 0.005);
        assertEquals(-0.072, MathUtils.round(vqtMatrix[27][87].im, 3), 0.005);
        assertEquals(-0.000, MathUtils.round(vqtMatrix[104][46].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[104][46].im, 3), 0.005);
    }

    @Test
    @Order(2)
    void vqt_useProvidedGamma() {
        // Process QTransform on those samples
        Complex[][] vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                24, 0, 12, SignalWindow.HANN_WINDOW, null
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(0.000, MathUtils.round(vqtMatrix[123][45].re, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[123][45].im, 3), 0.005);
        assertEquals(0.006, MathUtils.round(vqtMatrix[121][11].re, 3), 0.005);
        assertEquals(0.006, MathUtils.round(vqtMatrix[121][11].im, 3), 0.005);
        assertEquals(0.139, MathUtils.round(vqtMatrix[12][345].re, 3), 0.005);
        assertEquals(-0.096, MathUtils.round(vqtMatrix[12][345].im, 3), 0.005);
    }

    @Test
    @Order(2)
    void vqt_useKaiserBest() {
        // Process QTransform on those samples
        Complex[][] vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate() / 1.75, 512, UnitConversionUtils.noteToFreq("C1"),
                9, 1, 0, 12, SignalWindow.HANN_WINDOW, null
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(-0.002, MathUtils.round(vqtMatrix[3][45].re, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[3][45].im, 3), 0.005);
        assertEquals(0.003, MathUtils.round(vqtMatrix[5][11].re, 3), 0.005);
        assertEquals(0.033, MathUtils.round(vqtMatrix[5][11].im, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[8][345].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[8][345].im, 3), 0.005);
    }

    @Test
    @Order(2)
    void vqt_nonMultipleOf2HopLength() {
        // Process QTransform on those samples
        Complex[][] vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate(), 511, UnitConversionUtils.noteToFreq("C1"), 28,
                4, 0, 0, SignalWindow.HANN_WINDOW, null
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(-0.001, MathUtils.round(vqtMatrix[10][32].re, 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[10][32].im, 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[15][425].re, 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[15][425].im, 3), 0.005);
        assertEquals(0.003, MathUtils.round(vqtMatrix[27][87].re, 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[27][87].im, 3), 0.005);
    }

    @Test
    @Order(2)
    void vqt_useTuning() {
        Complex[][] vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate(), 511, UnitConversionUtils.noteToFreq("C1"), 28,
                4, Double.NaN, 0, SignalWindow.HANN_WINDOW, null
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(-0.001, MathUtils.round(vqtMatrix[10][32].re, 3), 0.005);
        assertEquals(-0.001, MathUtils.round(vqtMatrix[10][32].im, 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[15][425].re, 3), 0.005);
        assertEquals(-0.006, MathUtils.round(vqtMatrix[15][425].im, 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[27][87].re, 3), 0.005);
        assertEquals(0.005, MathUtils.round(vqtMatrix[27][87].im, 3), 0.005);
    }

    @Test
    @Order(2)
    void vqt_useFakeTasks() {
        // Start JavaFX toolkit
        new JFXPanel();

        // Process QTransform using Kaiser Fast on those samples
        Complex[][] vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 28,
                4, 0, 0, SignalWindow.HANN_WINDOW, new CustomTask<Void>() {
                    @Override
                    protected Void call() {
                        return null;
                    }
                }
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(0.000, MathUtils.round(vqtMatrix[10][32].re, 3), 0.005);
        assertEquals(-0.002, MathUtils.round(vqtMatrix[10][32].im, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[15][425].re, 3), 0.005);
        assertEquals(0.002, MathUtils.round(vqtMatrix[15][425].im, 3), 0.005);
        assertEquals(0.007, MathUtils.round(vqtMatrix[27][87].re, 3), 0.005);
        assertEquals(0.002, MathUtils.round(vqtMatrix[27][87].im, 3), 0.005);

        // Do it again, this time using Kaiser Best
        vqtMatrix = QTransform.vqt(
                samples, audio.getSampleRate() / 1.75, 512, UnitConversionUtils.noteToFreq("C1"), 9,
                1, 0, 12, SignalWindow.HANN_WINDOW, new CustomTask<Void>() {
                    @Override
                    protected Void call() {
                        return null;
                    }
                }
        );

        // Check specific values in this resultant QTransform matrix
        assertEquals(-0.002, MathUtils.round(vqtMatrix[3][45].re, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[3][45].im, 3), 0.005);
        assertEquals(0.003, MathUtils.round(vqtMatrix[5][11].re, 3), 0.005);
        assertEquals(0.033, MathUtils.round(vqtMatrix[5][11].im, 3), 0.005);
        assertEquals(0.000, MathUtils.round(vqtMatrix[8][345].re, 3), 0.005);
        assertEquals(0.001, MathUtils.round(vqtMatrix[8][345].im, 3), 0.005);
    }

    @Test
    @Order(3)
    void vqt_passInvalidValues() {
        // Provide invalid values in an attempt to throw errors
        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 0,
                24, 0, 12, SignalWindow.HANN_WINDOW, null
        ));  // Number of bins is invalid
        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), -12,
                24, 0, 12, SignalWindow.HANN_WINDOW, null
        ));  // Number of bins is invalid

        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                0, 0, 12, SignalWindow.HANN_WINDOW, null
        ));  // Bins per octave is invalid
        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                -12, 0, 12, SignalWindow.HANN_WINDOW, null
        ));  // Bins per octave is invalid

        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                13, 0, 12, SignalWindow.HANN_WINDOW, null
        ));  // Bins per octave not multiple of number of bins

        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 336,
                24, 0, 12, SignalWindow.HANN_WINDOW, null
        ));  // Too many bins, resulting in cutoff frequency being above Nyquist frequency
    }

    @Test
    @Order(3)
    void vqt_causeErrorInEarlyDownsample() {
        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                new double[1], audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 56,
                8, 0, 0, SignalWindow.HANN_WINDOW, null
        ));  // Input signal length of 1 is too short for 7-octave QTransform
        assertThrowsExactly(ValueException.class, () -> QTransform.vqt(
                new double[0], audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 56,
                8, 0, 0, SignalWindow.HANN_WINDOW, null
        ));  // Input signal length of 0 is too short for 7-octave QTransform
    }
}