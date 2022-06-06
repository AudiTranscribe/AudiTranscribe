/*
 * VQTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-06-06
 *
 * Description: Test `VQT.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VQTTest {

    @Test
    void vqt() {
        // Constants
        final boolean writeToFile = false;

        // Run the test
        try {
            // Get the audio file
            Audio audio = Audio.initAudio(
                    new File(IOMethods.getAbsoluteFilePath("testing-audio-files/Choice.wav")),
                    "Choice.wav",
                    AudioProcessingMode.SAMPLES_ONLY
            );

            // Extract samples
            double[] samples = audio.getMonoSamples();

            // Process VQT on those samples
            Complex[][] vqtMatrix = VQT.vqt(
                    samples, audio.getSampleRate(), 512, UnitConversionUtils.noteToFreq("C1"), 168,
                    24, false, 0, WindowFunction.HANN_WINDOW, null
            );

            // Check specific values in this resultant VQT matrix
            assertEquals(-0.002, MathUtils.round(vqtMatrix[109][32].re(), 3), 0.005);
            assertEquals(-0.001, MathUtils.round(vqtMatrix[109][32].im(), 3), 0.005);
            assertEquals(0.000, MathUtils.round(vqtMatrix[16][422].re(), 3), 0.005);
            assertEquals(-0.000, MathUtils.round(vqtMatrix[16][422].im(), 3), 0.005);
            assertEquals(-0.001, MathUtils.round(vqtMatrix[8][25].re(), 3), 0.005);
            assertEquals(-0.026, MathUtils.round(vqtMatrix[8][25].im(), 3), 0.005);
            assertEquals(-0.000, MathUtils.round(vqtMatrix[167][425].re(), 3), 0.005);
            assertEquals(0.001, MathUtils.round(vqtMatrix[167][425].im(), 3), 0.005);
            assertEquals(0.513, MathUtils.round(vqtMatrix[11][68].re(), 3), 0.005);
            assertEquals(-0.305, MathUtils.round(vqtMatrix[11][68].im(), 3), 0.005);
            assertEquals(0.001, MathUtils.round(vqtMatrix[118][341].re(), 3), 0.005);
            assertEquals(0.000, MathUtils.round(vqtMatrix[118][341].im(), 3), 0.005);
            assertEquals(-0.006, MathUtils.round(vqtMatrix[27][87].re(), 3), 0.005);
            assertEquals(-0.072, MathUtils.round(vqtMatrix[27][87].im(), 3), 0.005);
            assertEquals(-0.367, MathUtils.round(vqtMatrix[22][178].re(), 3), 0.005);
            assertEquals(-0.252, MathUtils.round(vqtMatrix[22][178].im(), 3), 0.005);
            assertEquals(-0.000, MathUtils.round(vqtMatrix[104][46].re(), 3), 0.005);
            assertEquals(0.001, MathUtils.round(vqtMatrix[104][46].im(), 3), 0.005);
            assertEquals(0.001, MathUtils.round(vqtMatrix[147][398].re(), 3), 0.005);
            assertEquals(0.001, MathUtils.round(vqtMatrix[147][398].im(), 3), 0.005);

            // Write the VQT matrix to a file
            if (writeToFile) {
                FileWriter myWriter = new FileWriter("test-VQT-data.txt");
                myWriter.write("[");
                for (Complex[] subarray : vqtMatrix) {
                    myWriter.write(Arrays.toString(subarray) + ",");
                }
                myWriter.write("]");
                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}