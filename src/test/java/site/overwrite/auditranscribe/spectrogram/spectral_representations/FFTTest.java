/*
 * FFTTest.java
 *
 * Created on 2022-04-10
 * Updated on 2022-06-28
 *
 * Description: Test `FFT.java`.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.generic.LengthException;
import site.overwrite.auditranscribe.misc.Complex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FFTTest {

    @Test
    void fftAndRFFT() {
        // Define the complex number arrays
        double[] realNumberArray1 = {1, 2, -3, -4, 5, 6, -7};
        double[] realNumberArray2 = {
                25.442832341330558, 67.35970565394922, 35.09989960920626, 61.77920972475006,
                62.4223934582534, 34.63818528787935, 10.867667057159514, 19.66065078483943,
                68.66050464419456, 98.64623871074612, 20.397346011908567, 33.125277868376244,
                13.969015082960123, 58.838543552236466, 27.565901473219746, 81.41318148048603
        };

        Complex[] complexNumberArray1 = {
                new Complex(1), new Complex(0, 2), new Complex(-3), new Complex(0, -4),
                new Complex(5), new Complex(0, 6), new Complex(-7), new Complex(0, -8)
        };
        Complex[] complexNumberArray2 = {
                new Complex(1), new Complex(0, 2), new Complex(-3), new Complex(0, -4),
                new Complex(5), new Complex(0, 6)
        };
        Complex[] complexNumberArray3 = {};  // Empty complex number array

        // Generate the RFFT outputs
        Complex[] fftRealNumberArray1 = FFT.rfft(realNumberArray1);
        Complex[] fftRealNumberArray2 = FFT.rfft(realNumberArray2);

        // Check the RFFT outputs
        assertEquals(fftRealNumberArray1.length, 4);
        assertEquals(fftRealNumberArray2.length, 9);

        assertEquals(Complex.ZERO.roundNicely(5), fftRealNumberArray1[0].roundNicely(5));
        assertEquals(new Complex(-3.68598068, 5.64282152).roundNicely(5), fftRealNumberArray1[1].roundNicely(5));
        assertEquals(new Complex(0.03318787, -19.7157882).roundNicely(5), fftRealNumberArray1[2].roundNicely(5));
        assertEquals(new Complex(7.15279281, -2.16708578).roundNicely(5), fftRealNumberArray1[3].roundNicely(5));

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
        Complex[] fftComplexNumberArray1 = FFT.fft(complexNumberArray1);
        Complex[] fftComplexNumberArray2 = FFT.fft(complexNumberArray2);
        Complex[] fftComplexNumberArray3 = FFT.fft(complexNumberArray3);

        // Check the FFT output
        assertEquals(fftComplexNumberArray1.length, 8);
        assertEquals(fftComplexNumberArray2.length, 6);
        assertEquals(fftComplexNumberArray3.length, 0);

        assertEquals(new Complex(-4, -4), fftComplexNumberArray1[0].roundNicely(3));
        assertEquals(new Complex(-4, -9.657), fftComplexNumberArray1[1].roundNicely(3));
        assertEquals(new Complex(36), fftComplexNumberArray1[2].roundNicely(3));
        assertEquals(new Complex(-4, 9.657), fftComplexNumberArray1[3].roundNicely(3));
        assertEquals(new Complex(-4, 4), fftComplexNumberArray1[4].roundNicely(3));
        assertEquals(new Complex(-4, 1.657), fftComplexNumberArray1[5].roundNicely(3));
        assertEquals(new Complex(-4), fftComplexNumberArray1[6].roundNicely(3));
        assertEquals(new Complex(-4, -1.657), fftComplexNumberArray1[7].roundNicely(3));

        assertEquals(new Complex(3, 4).roundNicely(5), fftComplexNumberArray2[0].roundNicely(5));
        assertEquals(new Complex(-3.46410162, 14.92820323).roundNicely(5), fftComplexNumberArray2[1].roundNicely(5));
        assertEquals(new Complex(-3.46410162, -14.92820323).roundNicely(5), fftComplexNumberArray2[2].roundNicely(5));
        assertEquals(new Complex(3, -4).roundNicely(5), fftComplexNumberArray2[3].roundNicely(5));
        assertEquals(new Complex(3.46410162, -1.07179677).roundNicely(5), fftComplexNumberArray2[4].roundNicely(5));
        assertEquals(new Complex(3.46410162, 1.07179677).roundNicely(5), fftComplexNumberArray2[5].roundNicely(5));
    }

    @Test
    void ifftAndIRFFT() {
        // Define the FFT and RFFT outputs
        Complex[] fftRealNumberArray1 = {
                Complex.ZERO, new Complex(-3.68598068, 5.64282152),
                new Complex(0.03318787, -19.7157882), new Complex(7.15279281, -2.16708578)
        };
        Complex[] fftRealNumberArray2 = {
                new Complex(719.8865527414956), new Complex(27.359410650674633, -15.552276162639878),
                new Complex(73.36021402907151, -63.987191769117224), new Complex(-102.59354674280576, 137.52573336870296),
                new Complex(76.56393137524456, -63.50435334635938), new Complex(-28.249218140615575, 37.79665773805458),
                new Complex(-37.936357140448315, -29.859837587646098), new Complex(-69.38733497870936, 78.53216170788484),
                new Complex(-191.03543338503016)
        };

        Complex[] fftComplexNumberArray1 = {
                new Complex(-4, -4), new Complex(-4, -9.65685424949238), new Complex(36), new Complex(-4, 9.65685424949238),
                new Complex(-4, 4), new Complex(-4.0, 1.6568542494923797), new Complex(-4), new Complex(-4, -1.6568542494923797)
        };
        Complex[] fftComplexNumberArray2 = {
                new Complex(3, 4), new Complex(-3.46410162, 14.92820323),
                new Complex(-3.46410162, -14.92820323), new Complex(3, -4),
                new Complex(3.46410162, -1.07179677), new Complex(3.46410162, 1.07179677)
        };

        // Generate the IRFFT outputs
        Complex[] realNumberArray1 = FFT.irfft(fftRealNumberArray1, 7);
        Complex[] realNumberArray2 = FFT.irfft(fftRealNumberArray2, 16);

        // Check the IRFFT outputs
        assertEquals(realNumberArray1.length, 7);
        assertEquals(realNumberArray2.length, 16);

        assertEquals(Complex.ONE.roundNicely(5), realNumberArray1[0].roundNicely(5));
        assertEquals(new Complex(2).roundNicely(5), realNumberArray1[1].roundNicely(5));
        assertEquals(new Complex(-3).roundNicely(5), realNumberArray1[2].roundNicely(5));
        assertEquals(new Complex(-4).roundNicely(5), realNumberArray1[3].roundNicely(5));
        assertEquals(new Complex(5).roundNicely(5), realNumberArray1[4].roundNicely(5));
        assertEquals(new Complex(6).roundNicely(5), realNumberArray1[5].roundNicely(5));
        assertEquals(new Complex(-7).roundNicely(5), realNumberArray1[6].roundNicely(5));

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
        Complex[] complexNumberArray1 = FFT.ifft(fftComplexNumberArray1);
        Complex[] complexNumberArray2 = FFT.ifft(fftComplexNumberArray2);

        // Check the IFFT output
        assertEquals(complexNumberArray1.length, 8);
        assertEquals(complexNumberArray2.length, 6);

        assertEquals(new Complex(1).roundNicely(5), complexNumberArray1[0].roundNicely(5));
        assertEquals(new Complex(0, 2).roundNicely(5), complexNumberArray1[1].roundNicely(5));
        assertEquals(new Complex(-3).roundNicely(5), complexNumberArray1[2].roundNicely(5));
        assertEquals(new Complex(0, -4).roundNicely(5), complexNumberArray1[3].roundNicely(5));
        assertEquals(new Complex(5).roundNicely(5), complexNumberArray1[4].roundNicely(5));
        assertEquals(new Complex(0, 6).roundNicely(5), complexNumberArray1[5].roundNicely(5));
        assertEquals(new Complex(-7).roundNicely(5), complexNumberArray1[6].roundNicely(5));
        assertEquals(new Complex(0, -8).roundNicely(5), complexNumberArray1[7].roundNicely(5));

        assertEquals(Complex.ONE.roundNicely(5), complexNumberArray2[0].roundNicely(5));
        assertEquals(new Complex(0, 2).roundNicely(5), complexNumberArray2[1].roundNicely(5));
        assertEquals(new Complex(-3).roundNicely(5), complexNumberArray2[2].roundNicely(5));
        assertEquals(new Complex(0, -4).roundNicely(5), complexNumberArray2[3].roundNicely(5));
        assertEquals(new Complex(5), complexNumberArray2[4].roundNicely(5));
        assertEquals(new Complex(0, 6).roundNicely(5), complexNumberArray2[5].roundNicely(5));
    }

    @Test
    void circularConvolution() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Make the method accessible to this test
        Method circularConvMtd = FFT.class.getDeclaredMethod("circularConvolution", Complex[].class, Complex[].class);
        circularConvMtd.setAccessible(true);

        // Define vectors to test the circular convolution method
        Complex[] vectorA = {Complex.ZERO, Complex.ONE, Complex.IMAG_UNIT, Complex.ONE, Complex.ZERO};
        Complex[] vectorB = {new Complex(10), new Complex(11), new Complex(0, 11), new Complex(11), new Complex(10)};
        Complex[] vectorC = {new Complex(-1, -2), new Complex(-3, -4)};
        Complex[] vectorD = {new Complex(10), new Complex(0, 13)};

        // Define correct output vectors
        Complex[] correctConvAB = {new Complex(10, 22), new Complex(21, 10), new Complex(21, 10), new Complex(10, 22), new Complex(11)};
        Complex[] correctConvCD = {new Complex(42, -59), new Complex(-4, -53)};

        // Generate the circular convolution outputs
        Complex[] convAB = (Complex[]) circularConvMtd.invoke(null, vectorA, vectorB);
        Complex[] convCD = (Complex[]) circularConvMtd.invoke(null, vectorC, vectorD);

        // Check the circulat convolution outputs
        assertEquals(correctConvAB.length, convAB.length);
        assertEquals(correctConvCD.length, convCD.length);

        for (int i = 0; i < correctConvAB.length; i++) {
            assertEquals(correctConvAB[i].roundNicely(5), convAB[i].roundNicely(5));
        }
        for (int i = 0; i < correctConvCD.length; i++) {
            assertEquals(correctConvCD[i].roundNicely(5), convCD[i].roundNicely(5));
        }

        assertThrowsExactly(LengthException.class, () -> {
            try {
                circularConvMtd.invoke(null, vectorA, vectorC);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        });
        assertThrowsExactly(LengthException.class, () -> {
            try {
                circularConvMtd.invoke(null, vectorD, vectorB);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        });
    }
}