/*
 * VQTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-04-10
 *
 * Description: Test `VQT.java`
 */


package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.UnitConversion;

import java.io.FileWriter;
import java.io.IOException;
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
            Audio audio = new Audio("testing-audio-files/Choice.wav");

            // Extract samples
            double[] samples = audio.getMonoSamples();

            // Process VQT on those samples
            Complex[][] vqtMatrix = VQT.vqt(
                    samples,
                    audio.getSampleRate(),
                    512,
                    UnitConversion.noteToFreq("C1"),
                    168,
                    24,
                    false,
                    0,
                    Window.HANN_WINDOW
            );

            // Check specific values in this resultant VQT matrix
            assertEquals(new Complex(0.37, 0.46), vqtMatrix[59][368].roundNicely(2));
            assertEquals(new Complex(0.01, 0.01), vqtMatrix[38][162].roundNicely(2));
            assertEquals(new Complex(0.86, 0.04), vqtMatrix[17][56].roundNicely(2));
            assertEquals(new Complex(0.01, 0.01), vqtMatrix[134][142].roundNicely(2));
            assertEquals(new Complex(0.02, 0.03), vqtMatrix[110][3].roundNicely(2));
            assertEquals(new Complex(-0.29, 0.41), vqtMatrix[53][256].roundNicely(2));
            assertEquals(new Complex(0.00, 0.00), vqtMatrix[152][322].roundNicely(2));
            assertEquals(new Complex(0.00, 0.00), vqtMatrix[164][114].roundNicely(2));
            assertEquals(new Complex(0.00, -0.00), vqtMatrix[139][61].roundNicely(2));
            assertEquals(new Complex(0.00, -0.00), vqtMatrix[156][44].roundNicely(2));
            assertEquals(new Complex(-0.31, 1.27), vqtMatrix[36][313].roundNicely(2));
            assertEquals(new Complex(-0.00, -0.00), vqtMatrix[135][226].roundNicely(2));
            assertEquals(new Complex(0.00, 0.00), vqtMatrix[114][102].roundNicely(2));
            assertEquals(new Complex(-0.00, 0.00), vqtMatrix[159][227].roundNicely(2));
            assertEquals(new Complex(0.00, -0.00), vqtMatrix[152][399].roundNicely(2));
            assertEquals(new Complex(0.00, 0.00), vqtMatrix[111][50].roundNicely(2));
            assertEquals(new Complex(-0.00, 0.00), vqtMatrix[24][428].roundNicely(2));
            assertEquals(new Complex(0.00, -0.00), vqtMatrix[138][361].roundNicely(2));
            assertEquals(new Complex(0.03, -0.23), vqtMatrix[46][303].roundNicely(2));
            assertEquals(new Complex(-0.01, 0.04), vqtMatrix[26][100].roundNicely(2));
            assertEquals(new Complex(-0.00, -0.00), vqtMatrix[35][331].roundNicely(2));
            assertEquals(new Complex(0.28, 0.61), vqtMatrix[34][394].roundNicely(2));
            assertEquals(new Complex(-0.20, 0.42), vqtMatrix[34][61].roundNicely(2));
            assertEquals(new Complex(0.01, -0.00), vqtMatrix[72][106].roundNicely(2));
            assertEquals(new Complex(-0.07, -1.10), vqtMatrix[14][294].roundNicely(2));

            // Write the VQT matrix to a file
            if (writeToFile) {
                FileWriter myWriter = new FileWriter("VQT_data.txt");
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