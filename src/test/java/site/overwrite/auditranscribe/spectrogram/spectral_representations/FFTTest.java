/*
 * FFTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-05-28
 *
 * Description: Test `FFT.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.misc.Complex;

import static org.junit.jupiter.api.Assertions.*;

class FFTTest {

    @Test
    void fft() {
        // Define the complex number arrays
        double[] realNumberArray1 = {1, 2, -3, -4, 5, 6, -7, -8};
        double[] realNumberArray2 = {
                25.442832341330558, 67.35970565394922, 35.09989960920626, 61.77920972475006,
                62.4223934582534, 34.63818528787935, 10.867667057159514, 19.66065078483943,
                68.66050464419456, 98.64623871074612, 20.397346011908567, 33.125277868376244,
                13.969015082960123, 58.838543552236466, 27.565901473219746, 81.41318148048603
        };
        Complex[] complexNumberArray = {
                new Complex(1), new Complex(0, 2), new Complex(-3), new Complex(0, -4),
                new Complex(5), new Complex(0, 6), new Complex(-7), new Complex(0, -8)
        };

        // Generate the FFT outputs
        Complex[] fftRealNumberArray1 = FFT.fft(realNumberArray1);
        Complex[] fftRealNumberArray2 = FFT.fft(realNumberArray2);
        Complex[] fftComplexNumberArray = FFT.fft(complexNumberArray);  // Keeps only non-negative frequencies

        // Check the FFT output
        assertEquals(fftRealNumberArray1.length, 5);
        assertEquals(fftRealNumberArray2.length, 9);
        assertEquals(fftComplexNumberArray.length, 5);

        assertEquals(new Complex(-8), fftRealNumberArray1[0].roundNicely(3));
        assertEquals(new Complex(-9.657, -4), fftRealNumberArray1[1].roundNicely(3));
        assertEquals(new Complex(16, -20), fftRealNumberArray1[2].roundNicely(3));
        assertEquals(new Complex(1.657, 4), fftRealNumberArray1[3].roundNicely(3));
        assertEquals(new Complex(0), fftRealNumberArray1[4].roundNicely(3));

        assertEquals(new Complex(719.8865527), fftRealNumberArray2[0].roundNicely(7));
        assertEquals(new Complex(27.3594107, -15.5522762), fftRealNumberArray2[1].roundNicely(7));
        assertEquals(new Complex(73.3602140, -63.9871918), fftRealNumberArray2[2].roundNicely(7));
        assertEquals(new Complex(-102.5935467, 137.5257334), fftRealNumberArray2[3].roundNicely(7));
        assertEquals(new Complex(76.5639314, -63.5043533), fftRealNumberArray2[4].roundNicely(7));
        assertEquals(new Complex(-28.2492181, 37.7966577), fftRealNumberArray2[5].roundNicely(7));
        assertEquals(new Complex(-37.9363571, -29.8598376), fftRealNumberArray2[6].roundNicely(7));
        assertEquals(new Complex(-69.3873350, 78.5321617), fftRealNumberArray2[7].roundNicely(7));
        assertEquals(new Complex(-191.0354334), fftRealNumberArray2[8].roundNicely(7));

        assertEquals(new Complex(-4, -4), fftComplexNumberArray[0].roundNicely(3));
        assertEquals(new Complex(-4, -9.657), fftComplexNumberArray[1].roundNicely(3));
        assertEquals(new Complex(36), fftComplexNumberArray[2].roundNicely(3));
        assertEquals(new Complex(-4, 9.657), fftComplexNumberArray[3].roundNicely(3));
        assertEquals(new Complex(-4, 4), fftComplexNumberArray[4].roundNicely(3));
    }
}