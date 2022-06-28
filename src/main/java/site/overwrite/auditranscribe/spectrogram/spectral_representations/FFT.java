/*
 * FFT.java
 *
 * Created on 2022-02-12
 * Updated on 2022-06-28
 *
 * Description: Class that implements Fast Fourier Transform (FFT) algorithms.
 *
 * Partially adapted from
 * https://github.com/stefanGT44/AudioVisualizer-RealTime-Spectrogram/blob/master/src/app/FFT.java
 * and partially adapted from https://www.nayuki.io/page/free-small-fft-in-multiple-languages.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.utils.MiscUtils;

import java.util.Arrays;

/**
 * Class that implements Fast Fourier Transform (FFT) algorithms.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fast_Fourier_transform">This Wikipedia Article</a>
 * about the FFT.
 */
public final class FFT {
    private FFT() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Computes the Fast Fourier Transform (FFT) of the input array <code>x</code>.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     */
    public static Complex[] fft(Complex[] x) {
        // Get length fo input array
        int length = x.length;

        // Handle different cases of the length
        if (length == 0) {
            return new Complex[0];
        } else if ((length & (length - 1)) == 0) {  // Is power of 2
            return fftRadix2(x, length);
        } else {  // Is not power of 2
            return fftBluestein(x, length);
        }
    }

    /**
     * Computes the Real-Valued FFT (RFFT) of the input array <code>x</code>.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the RFFT of the data source.
     * @implNote The array returned is so that only the non-negative frequencies are returned. This
     * means that the first <code>(N / 2) + 1</code> elements are returned if the length of the
     * array, <code>N</code>, is even and <code>(N + 1) / 2</code> if <code>N</code> is odd.
     */
    public static Complex[] rfft(Complex[] x) {
        // Get length of the array
        int length = x.length;

        // Compute FFT of the matrix
        Complex[] tempMatrix = fft(x);

        // Determine number of elements to keep
        int numElemToKeep = length % 2 == 0 ? length / 2 + 1 : (length + 1) / 2;

        // Keep only that number of elements
        return Arrays.copyOfRange(tempMatrix, 0, numElemToKeep);
    }

    /**
     * Computes the Real-Valued FFT (RFFT) of the input array <code>x</code>.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the RFFT of the data source.
     * @implNote The array returned is so that only the non-negative frequencies are returned. This
     * means that the first <code>(N / 2) + 1</code> elements are returned if the length of the
     * array, <code>N</code>, is even and <code>(N + 1) / 2</code> if <code>N</code> is odd.
     */
    public static Complex[] rfft(double[] x) {
        // Get the length of the array `x`
        int n = x.length;

        // Convert `x` into a complex number array
        Complex[] z = new Complex[n];

        for (int i = 0; i < n; i++) {
            z[i] = new Complex(x[i]);
        }

        // Now run the FFT on this complex number array
        return rfft(z);
    }

    /**
     * Helper method that computes the Inverse FFT (IFFT) of the input array <code>x</code>.<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the IFFT of the data source.
     * @throws ValueException If the length of <code>x</code> is not a power of 2.
     */
    public static Complex[] ifft(Complex[] x) {
        // Get length of input array
        int length = x.length;

        // Compute conjugate of each element of `x`
        Complex[] y = new Complex[length];  // We will reuse the array later
        for (int i = 0; i < length; i++) {
            y[i] = x[i].conjugate();
        }

        // Compute forward FFT
        y = fft(y);

        // Take conjugate again
        for (int i = 0; i < length; i++) {
            y[i] = y[i].conjugate();
        }

        // Divide by the length of `x`
        for (int i = 0; i < length; i++) {
            y[i] = y[i].divides(length);
        }

        // Return the inverse FFT array, `y`
        return y;
    }

    /**
     * Computes the Inverse Real-Valued FFT (IRFFT) of the input array <code>x</code>.<br>
     * This assumes that the length of the input array, say <code>N</code>, is of the form
     * <code>K / 2 + 1</code> where <code>K</code> is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @param n Length of the transformed axis of the output.
     * @return An array of <code>Complex</code> objects representing the IRFFT of the data source.
     * @throws ValueException If the length of <code>x</code> is not of the form
     *                        <code>K / 2 + 1</code>.
     */
    public static Complex[] irfft(Complex[] x, int n) {
        // Extend the input array to `n`
        Complex[] extended = new Complex[n];
        System.arraycopy(x, 0, extended, 0, x.length);

        for (int i = x.length; i < n; i++) {
            extended[i] = x[n - i].conjugate();
        }

        // Compute IFFT and return
        return ifft(extended);
    }

    // Private methods

    /**
     * Helper method that computes the FFT of the input array <code>x</code> using the Radix-2
     * Cooley-Tukey Algorithm.<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x      The complex array <code>x</code> representing the data source.
     * @param length Length of the input array.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @see <a href="https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm">Radix-2
     * Cooley-Tukey Algorithm</a>, which was the algorithm used to generate the FFT.
     */
    private static Complex[] fftRadix2(Complex[] x, int length) {
        // Base case: length is 1
        if (length == 1) return new Complex[]{x[0]};

        // Compute length of each of the two sub-arrays
        int halfLength = length / 2;

        // Compute FFT of even terms
        Complex[] terms = new Complex[halfLength];  // Will be used for both even and odd terms
        for (int k = 0; k < halfLength; k++) {
            terms[k] = x[2 * k];
        }
        Complex[] p = fftRadix2(terms, halfLength);

        // Compute FFT of odd terms
        for (int k = 0; k < halfLength; k++) {
            terms[k] = x[2 * k + 1];  // We reuse the above array
        }
        Complex[] q = fftRadix2(terms, halfLength);

        // Combine even and odd terms together
        Complex[] y = new Complex[length];
        for (int k = 0; k < halfLength; k++) {
            // Compute w = exp(−2kπi/N)
            Complex wk = Complex.exp(new Complex(0, -2 * k * Math.PI / length));

            // Set values for `y[k]` and `y[k + length/2]`
            y[k] = p[k].plus(wk.times(q[k]));
            y[k + length / 2] = p[k].minus(wk.times(q[k]));
        }

        // Return FFT array
        return y;
    }

    /**
     * Helper method that computes the FFT of the input array <code>x</code> using Bluestein's
     * algorithm (aka Chirp-Z Transform, or CZT for short).<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @see <a href="https://en.wikipedia.org/wiki/Chirp_Z-transform#Bluestein's_algorithm">
     * Bluestein's CZT Algorithm</a>, which was the algorithm used to generate the FFT.
     */
    private static Complex[] fftBluestein(Complex[] x, int length) {
        // Find a power of 2 convolution length such that it is at least `length * 2 + 1`
        int convolutionLength = (int) Math.pow(2, MiscUtils.numOfSetBits(length * 2));

        // Generate trigonometric table
        double exponentCoefficient = -Math.PI / length;

        Complex[] exponentialTable = new Complex[length];
        for (int i = 0; i < length; i++) {
            exponentialTable[i] = Complex.exp(
                    Complex.IMAG_UNIT.times(
                            new Complex((i * i) % (length * 2)).scale(exponentCoefficient)
                    )
            );
        }

        // Generate the first temporary vector
        Complex[] aVector = new Complex[convolutionLength];
        for (int i = 0; i < length; i++) {
            aVector[i] = x[i].times(exponentialTable[i]);
        }
        for (int i = length; i < convolutionLength; i++) {
            aVector[i] = Complex.ZERO;
        }

        // Generate the second temporary vector
        Complex[] bVector = new Complex[convolutionLength];
        System.arraycopy(exponentialTable, 0, bVector, 0, length);
        for (int i = length; i < convolutionLength - length + 1; i++) {
            bVector[i] = Complex.ZERO;
        }
        for (int i = 0; i < length - 1; i++) {
            bVector[convolutionLength - length + 1 + i] = exponentialTable[length - i - 1];
        }
        for (int i = 0; i < convolutionLength; i++) {  // Now we need to take the conjugate of it
            bVector[i] = bVector[i].conjugate();
        }

        // Generate the convolution vector
        Complex[] convolutionVector = circularConvolution(aVector, bVector);

        // Keep only the first `length` elements
        Complex[] y = new Complex[length];
        System.arraycopy(convolutionVector, 0, y, 0, length);

        // Postprocessing step
        Complex[] finalVector = new Complex[length];
        for (int i = 0; i < length; i++) {
            finalVector[i] = y[i].times(exponentialTable[i]);
        }

        return finalVector;
    }

    /**
     * Helper method that computes the circular convolution of the two input vectors <code>aVector</code> and <code>bVector</code>.
     *
     * @param aVector The first vector.
     * @param bVector The second vector.
     * @return The circular convolution of the two input vectors.
     * @throws ValueException If the lengths of <code>aVector</code> and <code>bVector</code> are not equal.
     */
    private static Complex[] circularConvolution(Complex[] aVector, Complex[] bVector) {
        // Check that the lengths of the vectors are equal
        if (aVector.length != bVector.length)
            throw new ValueException("The lengths of the a and b vectors are not equal.");

        // Get the number of elements in the vectors, `length`
        int length = aVector.length;

        // Apply convolution theorem to obtain resultant convoluted vector
        // (See https://en.wikipedia.org/wiki/Convolution_theorem)
        Complex[] AVector = fft(aVector);  // Convention: capital letter => fourier transformed vector
        Complex[] BVector = fft(bVector);
        Complex[] XVector = new Complex[length];
        for (int i = 0; i < length; i++) {
            XVector[i] = AVector[i].times(BVector[i]);
        }

        return ifft(XVector);
    }
}
