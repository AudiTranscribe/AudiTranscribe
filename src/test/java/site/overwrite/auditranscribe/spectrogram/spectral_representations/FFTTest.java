/*
 * FFTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-04-10
 *
 * Description: Test `FFT.java`
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.utils.Complex;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FFTTest {

    @Test
    void fft() {
        // Define the complex number arrays
        Complex[] realNumberArray = {
                new Complex(1), new Complex(2), new Complex(-3), new Complex(-4),
                new Complex(5), new Complex(6), new Complex(-7), new Complex(-8),
        };
        Complex[] complexNumberArray = {
                new Complex(1), new Complex(0, 2), new Complex(-3), new Complex(0, -4),
                new Complex(5), new Complex(0, 6), new Complex(-7), new Complex(0, -8)
        };

        // Generate the FFT outputs
        Complex[] fftRealNumberArray = FFT.fft(realNumberArray);
        Complex[] fftComplexNumberArray = FFT.fft(complexNumberArray);  // Keeps only non-negative frequencies

        // Check the FFT output
        assertEquals(fftRealNumberArray.length, 5);
        assertEquals(fftComplexNumberArray.length, 5);

        assertEquals(new Complex(-8), fftRealNumberArray[0].roundNicely(3));
        assertEquals(new Complex(-9.657, -4), fftRealNumberArray[1].roundNicely(3));
        assertEquals(new Complex(16, -20), fftRealNumberArray[2].roundNicely(3));
        assertEquals(new Complex(1.657, 4), fftRealNumberArray[3].roundNicely(3));
        assertEquals(new Complex(0), fftRealNumberArray[4].roundNicely(3));

        assertEquals(new Complex(-4, -4), fftComplexNumberArray[0].roundNicely(3));
        assertEquals(new Complex(-4, -9.657), fftComplexNumberArray[1].roundNicely(3));
        assertEquals(new Complex(36), fftComplexNumberArray[2].roundNicely(3));
        assertEquals(new Complex(-4, 9.657), fftComplexNumberArray[3].roundNicely(3));
        assertEquals(new Complex(-4, 4), fftComplexNumberArray[4].roundNicely(3));
    }
}