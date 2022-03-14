/*
 * VQTTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-13
 *
 * Description: Testing code for the VQT.
 * Todo: depreciate
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.UnitConversion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class VQTTest {
    public static void main(String[] args) {
        // Get the audio file
        Audio audio = new Audio("audioFiles/Choice.wav");

        // Get the MONO samples
        double[] samples = audio.getMonoSamples();

        // Perform VQT
        Complex[][] vqtMatrix = VQT.vqt(
                samples, audio.getSampleRate(), 512, UnitConversion.noteToFreq("C1"),
                168, 24, false, 0, Window.HANN_WINDOW
        );
        System.out.println("VQT Shape: (" + vqtMatrix.length + ", " + vqtMatrix[0].length + ")");

        // Write the VQT matrix to a file
        try {
            FileWriter myWriter = new FileWriter("VQT_data.txt");
            myWriter.write("[");
            for (Complex[] subarray : vqtMatrix) {
                myWriter.write(Arrays.toString(subarray) + ",");
            }
            myWriter.write("]");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
