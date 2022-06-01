/*
 * FFT.java
 *
 * Created on 2022-02-12
 * Updated on 2022-06-01
 *
 * Description: Class that implements Fast Fourier Transform (FFT) algorithms.
 *
 * Adapted from https://github.com/stefanGT44/AudioVisualizer-RealTime-Spectrogram/blob/master/src/app/FFT.java
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.exceptions.ValueException;
import site.overwrite.auditranscribe.misc.Complex;

import java.util.Arrays;

/**
 * Class that implements Fast Fourier Transform (FFT) algorithms.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fast_Fourier_transform">This Wikipedia Article</a>
 * about the FFT.
 */
public class FFT {
    // Public methods

    /**
     * Method that computes the FFT of the input array <code>x</code>.
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @throws ValueException If the length of <code>x</code> is not a power of 2.
     * @implNote Unlike {@link #rfft(Complex[])}, the array returned has length <code>N</code>.
     * @see <a href="https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm">Radix-2
     * Cooley-Tukey Algorithm</a>, which was the algorithm used to generate the FFT.
     */
    public static Complex[] fft(Complex[] x) {
        // Get length of input array
        int length = x.length;

        // Base case: length is 1
        if (length == 1) return new Complex[]{x[0]};

        // Assert that the length is AT LEAST a multiple of 2
        // (We'll be able to catch non-powers of two in subsequent recursive calls)
        if (length % 2 != 0) {
            throw new ValueException("The length of the array is not a power of 2.");
        }

        // Compute FFT of even terms
        Complex[] terms = new Complex[length / 2];  // Will be used for both even and odd terms
        for (int k = 0; k < length / 2; k++) {
            terms[k] = x[2 * k];
        }
        Complex[] p = fft(terms);

        // Compute FFT of odd terms
        for (int k = 0; k < length / 2; k++) {
            terms[k] = x[2 * k + 1];  // We reuse the above array
        }
        Complex[] q = fft(terms);

        // Combine even and odd terms together
        Complex[] y = new Complex[length];
        for (int k = 0; k < length / 2; k++) {
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
     * Computes the Real-Valued FFT (RFFT) of the input array <code>x</code>.<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the RFFT of the data source.
     * @throws ValueException If the length of <code>x</code> is not a power of 2.
     * @implNote The array returned has length <code>N / 2 + 1</code>, so that only the non-negative
     * frequencies are returned.
     */
    public static Complex[] rfft(Complex[] x) {
        // Compute Radix-2 Cooley-Tukey FFT
        Complex[] tempMatrix = fft(x);

        // Keep only the first `n / 2 + 1` elements
        int finalLength = x.length / 2 + 1;
        return Arrays.copyOfRange(tempMatrix, 0, finalLength);
    }

    /**
     * Computes the Real-Valued FFT (RFFT) of the input array <code>x</code>.<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The real-number array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the RFFT of the data source.
     * @throws ValueException If the length of <code>x</code> is not a power of 2.
     * @implNote The array returned has length <code>N / 2 + 1</code>, so that only the non-negative
     * frequencies are returned.
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
     * @implNote Unlike {@link #irfft(Complex[])}, this function expects an array of length
     * <code>N</code> which is a power of 2.
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
            y[i] = y[i].scale(1. / length);
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
     * @return An array of <code>Complex</code> objects representing the IRFFT of the data source.
     * @throws ValueException If the length of <code>x</code> is not of the form
     *                        <code>K / 2 + 1</code>.
     */
    public static Complex[] irfft(Complex[] x) {
        // Extend the input array to a power of 2
        Complex[] extended = new Complex[(x.length - 1) * 2];
        System.arraycopy(x, 0, extended, 0, x.length);

        for (int i = x.length; i < extended.length; i++) {
            extended[i] = x[2 * x.length - i - 2].conjugate();
        }

        // Compute IFFT and return
        return ifft(extended);
    }
}
