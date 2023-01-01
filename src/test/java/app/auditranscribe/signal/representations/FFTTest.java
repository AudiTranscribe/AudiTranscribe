package app.auditranscribe.signal.representations;

import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.misc.Complex;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FFTTest {
    @Test
    void fft() {
        // Define the arrays
        Complex[] array1 = {
                new Complex(1), new Complex(0, 2), new Complex(-3), new Complex(0, -4),
                new Complex(5), new Complex(0, 6), new Complex(-7), new Complex(0, -8)
        };
        Complex[] array2 = {
                new Complex(1), new Complex(0, 2), new Complex(-3), new Complex(0, -4),
                new Complex(5), new Complex(0, 6)
        };
        Complex[] array3 = {};  // Empty complex number array

        // Generate the FFT outputs
        Complex[] fftArray1 = FFT.fft(array1);
        Complex[] fftArray2 = FFT.fft(array2);
        Complex[] fftArray3 = FFT.fft(array3);

        // Check the FFT output
        assertEquals(fftArray1.length, 8);
        assertEquals(fftArray2.length, 6);
        assertEquals(fftArray3.length, 0);

        assertEquals(new Complex(-4, -4), fftArray1[0].roundNicely(3));
        assertEquals(new Complex(-4, -9.657), fftArray1[1].roundNicely(3));
        assertEquals(new Complex(36), fftArray1[2].roundNicely(3));
        assertEquals(new Complex(-4, 9.657), fftArray1[3].roundNicely(3));
        assertEquals(new Complex(-4, 4), fftArray1[4].roundNicely(3));
        assertEquals(new Complex(-4, 1.657), fftArray1[5].roundNicely(3));
        assertEquals(new Complex(-4), fftArray1[6].roundNicely(3));
        assertEquals(new Complex(-4, -1.657), fftArray1[7].roundNicely(3));

        assertEquals(
                new Complex(3, 4).roundNicely(5),
                fftArray2[0].roundNicely(5)
        );
        assertEquals(
                new Complex(-3.46410162, 14.92820323).roundNicely(5),
                fftArray2[1].roundNicely(5)
        );
        assertEquals(
                new Complex(-3.46410162, -14.92820323).roundNicely(5),
                fftArray2[2].roundNicely(5)
        );
        assertEquals(
                new Complex(3, -4).roundNicely(5),
                fftArray2[3].roundNicely(5)
        );
        assertEquals(
                new Complex(3.46410162, -1.07179677).roundNicely(5),
                fftArray2[4].roundNicely(5)
        );
        assertEquals(
                new Complex(3.46410162, 1.07179677).roundNicely(5),
                fftArray2[5].roundNicely(5)
        );
    }

    @Test
    void rfft() {
        // Define arrays to run RFFT on
        double[] array1 = {1, 2, -3, -4, 5, 6, -7};
        double[] array2 = {
                25.442832341330558, 67.35970565394922, 35.09989960920626, 61.77920972475006,
                62.4223934582534, 34.63818528787935, 10.867667057159514, 19.66065078483943,
                68.66050464419456, 98.64623871074612, 20.397346011908567, 33.125277868376244,
                13.969015082960123, 58.838543552236466, 27.565901473219746, 81.41318148048603
        };

        // Convert both to complex number arrays
        Complex[] complexArray1 = realArrToComplex(array1);
        Complex[] complexArray2 = realArrToComplex(array2);

        // Generate the RFFT outputs
        Complex[] rfftArray1 = FFT.rfft(complexArray1);
        Complex[] rfftArray2 = FFT.rfft(complexArray2);

        // Check the RFFT outputs
        assertEquals(rfftArray1.length, 4);
        assertEquals(rfftArray2.length, 9);

        assertEquals(Complex.ZERO.roundNicely(5), rfftArray1[0].roundNicely(5));
        assertEquals(new Complex(-3.68598068, 5.64282152).roundNicely(5), rfftArray1[1].roundNicely(5));
        assertEquals(new Complex(0.03318787, -19.7157882).roundNicely(5), rfftArray1[2].roundNicely(5));
        assertEquals(new Complex(7.15279281, -2.16708578).roundNicely(5), rfftArray1[3].roundNicely(5));

        assertEquals(new Complex(719.8865527), rfftArray2[0].roundNicely(7));
        assertEquals(new Complex(27.3594107, -15.5522762), rfftArray2[1].roundNicely(7));
        assertEquals(new Complex(73.3602140, -63.9871918), rfftArray2[2].roundNicely(7));
        assertEquals(new Complex(-102.5935467, 137.5257334), rfftArray2[3].roundNicely(7));
        assertEquals(new Complex(76.5639314, -63.5043533), rfftArray2[4].roundNicely(7));
        assertEquals(new Complex(-28.2492181, 37.7966577), rfftArray2[5].roundNicely(7));
        assertEquals(new Complex(-37.9363571, -29.8598376), rfftArray2[6].roundNicely(7));
        assertEquals(new Complex(-69.3873350, 78.5321617), rfftArray2[7].roundNicely(7));
        assertEquals(new Complex(-191.0354334), rfftArray2[8].roundNicely(7));
    }

    @Test
    void ifft() {
        // Define the FFT outputs
        Complex[] fftComplexNumberArray1 = {
                new Complex(-4, -4), new Complex(-4, -9.65685424949238),
                new Complex(36), new Complex(-4, 9.65685424949238),
                new Complex(-4, 4), new Complex(-4.0, 1.6568542494923797),
                new Complex(-4), new Complex(-4, -1.6568542494923797)
        };
        Complex[] fftComplexNumberArray2 = {
                new Complex(3, 4), new Complex(-3.46410162, 14.92820323),
                new Complex(-3.46410162, -14.92820323), new Complex(3, -4),
                new Complex(3.46410162, -1.07179677), new Complex(3.46410162, 1.07179677)
        };

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
        Method circularConvMtd = FFT.class.getDeclaredMethod(
                "circularConvolution", Complex[].class, Complex[].class
        );
        circularConvMtd.setAccessible(true);

        // Define vectors to test the circular convolution method
        Complex[] vectorA = {Complex.ZERO, Complex.ONE, Complex.IMAG_UNIT, Complex.ONE, Complex.ZERO};
        Complex[] vectorB = {
                new Complex(10), new Complex(11), new Complex(0, 11),new Complex(11),
                new Complex(10)
        };
        Complex[] vectorC = {new Complex(-1, -2), new Complex(-3, -4)};
        Complex[] vectorD = {new Complex(10), new Complex(0, 13)};

        // Define correct output vectors
        Complex[] correctConvAB = {
                new Complex(10, 22), new Complex(21, 10), new Complex(21, 10),
                new Complex(10, 22), new Complex(11)
        };
        Complex[] correctConvCD = {new Complex(42, -59), new Complex(-4, -53)};

        // Generate the circular convolution outputs
        Complex[] convAB = (Complex[]) circularConvMtd.invoke(null, vectorA, vectorB);
        Complex[] convCD = (Complex[]) circularConvMtd.invoke(null, vectorC, vectorD);

        // Check the circular convolution outputs
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

    // Helper methods
    Complex[] realArrToComplex(double[] array) {
        Complex[] z = new Complex[array.length];
        for (int i = 0; i < array.length; i++) {
            z[i] = new Complex(array[i]);
        }
        return z;
    }
}