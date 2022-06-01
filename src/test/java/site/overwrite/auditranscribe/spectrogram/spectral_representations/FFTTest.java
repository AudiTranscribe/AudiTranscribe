/*
 * FFTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-06-01
 *
 * Description: Test `FFT.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.misc.Complex;

import static org.junit.jupiter.api.Assertions.*;

class FFTTest {

    @Test
    void fft_and_rfft() {
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

        // Generate the RFFT outputs
        Complex[] fftRealNumberArray1 = FFT.rfft(realNumberArray1);
        Complex[] fftRealNumberArray2 = FFT.rfft(realNumberArray2);

        // Check the RFFT outputs
        assertEquals(fftRealNumberArray1.length, 5);
        assertEquals(fftRealNumberArray2.length, 9);

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

        // Generate the FFT outputs
        Complex[] fftComplexNumberArray = FFT.fft(complexNumberArray);

        // Check the FFT output
        assertEquals(fftComplexNumberArray.length, 8);

        assertEquals(new Complex(-4, -4), fftComplexNumberArray[0].roundNicely(3));
        assertEquals(new Complex(-4, -9.657), fftComplexNumberArray[1].roundNicely(3));
        assertEquals(new Complex(36), fftComplexNumberArray[2].roundNicely(3));
        assertEquals(new Complex(-4, 9.657), fftComplexNumberArray[3].roundNicely(3));
        assertEquals(new Complex(-4, 4), fftComplexNumberArray[4].roundNicely(3));
        assertEquals(new Complex(-4, 1.657), fftComplexNumberArray[5].roundNicely(3));
        assertEquals(new Complex(-4), fftComplexNumberArray[6].roundNicely(3));
        assertEquals(new Complex(-4, -1.657), fftComplexNumberArray[7].roundNicely(3));
    }

    @Test
    void ifft_and_irfft() {
        // Define the FFT and RFFT outputs
        Complex[] fftRealNumberArray1 = {
                new Complex(-8), new Complex(-9.65685424949238, -4), new Complex(16.0, -20.0),
                new Complex(1.6568542494923797, 4.0), Complex.ZERO
        };
        Complex[] fftRealNumberArray2 = {
                new Complex(719.8865527414956), new Complex(27.359410650674633, -15.552276162639878),
                new Complex(73.36021402907151, -63.987191769117224), new Complex(-102.59354674280576, 137.52573336870296),
                new Complex(76.56393137524456, -63.50435334635938), new Complex(-28.249218140615575, 37.79665773805458),
                new Complex(-37.936357140448315, -29.859837587646098), new Complex(-69.38733497870936, 78.53216170788484),
                new Complex(-191.03543338503016)
        };
        Complex[] fftComplexNumberArray = {
                new Complex(-4, -4), new Complex(-4, -9.65685424949238), new Complex(36), new Complex(-4, 9.65685424949238),
                new Complex(-4, 4), new Complex(-4.0, 1.6568542494923797), new Complex(-4), new Complex(-4, -1.6568542494923797)
        };

        // Generate the IRFFT outputs
        Complex[] realNumberArray1 = FFT.irfft(fftRealNumberArray1);
        Complex[] realNumberArray2 = FFT.irfft(fftRealNumberArray2);

        // Check the IRFFT outputs
        assertEquals(realNumberArray1.length, 8);
        assertEquals(realNumberArray2.length, 16);

        assertEquals(Complex.ONE.roundNicely(5), realNumberArray1[0].roundNicely(5));
        assertEquals(new Complex(2).roundNicely(5), realNumberArray1[1].roundNicely(5));
        assertEquals(new Complex(-3).roundNicely(5), realNumberArray1[2].roundNicely(5));
        assertEquals(new Complex(-4).roundNicely(5), realNumberArray1[3].roundNicely(5));
        assertEquals(new Complex(5).roundNicely(5), realNumberArray1[4].roundNicely(5));
        assertEquals(new Complex(6).roundNicely(5), realNumberArray1[5].roundNicely(5));
        assertEquals(new Complex(-7).roundNicely(5), realNumberArray1[6].roundNicely(5));
        assertEquals(new Complex(-8).roundNicely(5), realNumberArray1[7].roundNicely(5));

        assertEquals(new Complex(25.442832341330558).roundNicely(5), realNumberArray2[0].roundNicely(5));
        assertEquals(new Complex(67.35970565394922).roundNicely(5), realNumberArray2[1].roundNicely(5));
        assertEquals(new Complex(35.09989960920626).roundNicely(5), realNumberArray2[2].roundNicely(5));
        assertEquals(new Complex(61.77920972475006).roundNicely(5), realNumberArray2[3].roundNicely(5));
        assertEquals(new Complex(62.4223934582534).roundNicely(5), realNumberArray2[4].roundNicely(5));
        assertEquals(new Complex(34.63818528787935).roundNicely(5), realNumberArray2[5].roundNicely(5));
        assertEquals(new Complex(10.867667057159514).roundNicely(5), realNumberArray2[6].roundNicely(5));
        assertEquals(new Complex(19.66065078483943).roundNicely(5), realNumberArray2[7].roundNicely(5));
        assertEquals(new Complex(68.66050464419456).roundNicely(5), realNumberArray2[8].roundNicely(5));
        assertEquals(new Complex(98.64623871074612).roundNicely(5), realNumberArray2[9].roundNicely(5));
        assertEquals(new Complex(20.397346011908567).roundNicely(5), realNumberArray2[10].roundNicely(5));
        assertEquals(new Complex(33.125277868376244).roundNicely(5), realNumberArray2[11].roundNicely(5));
        assertEquals(new Complex(13.969015082960123).roundNicely(5), realNumberArray2[12].roundNicely(5));
        assertEquals(new Complex(58.838543552236466).roundNicely(5), realNumberArray2[13].roundNicely(5));
        assertEquals(new Complex(27.565901473219746).roundNicely(5), realNumberArray2[14].roundNicely(5));
        assertEquals(new Complex(81.41318148048603).roundNicely(5), realNumberArray2[15].roundNicely(5));

        // Generate the IFFT output
        Complex[] complexNumberArray = FFT.ifft(fftComplexNumberArray);

        // Check the IFFT output
        assertEquals(complexNumberArray.length, 8);

        assertEquals(new Complex(1).roundNicely(5), complexNumberArray[0].roundNicely(5));
        assertEquals(new Complex(0, 2).roundNicely(5), complexNumberArray[1].roundNicely(5));
        assertEquals(new Complex(-3).roundNicely(5), complexNumberArray[2].roundNicely(5));
        assertEquals(new Complex(0, -4).roundNicely(5), complexNumberArray[3].roundNicely(5));
        assertEquals(new Complex(5).roundNicely(5), complexNumberArray[4].roundNicely(5));
        assertEquals(new Complex(0, 6).roundNicely(5), complexNumberArray[5].roundNicely(5));
        assertEquals(new Complex(-7).roundNicely(5), complexNumberArray[6].roundNicely(5));
        assertEquals(new Complex(0, -8).roundNicely(5), complexNumberArray[7].roundNicely(5));
    }
}