/*
 * FFT.java
 *
 * Created on 2022-02-12
 * Updated on 2022-03-12
 *
 * Description: Class that implements the Fast Fourier Transform (FFT) algorithm.
 *
 * Note:
 *  - Adapted from https://github.com/stefanGT44/AudioVisualizer-RealTime-Spectrogram/blob/master/src/app/FFT.java
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.utils.Complex;

import java.util.Arrays;

/**
 * Class that implements static FFT methods.
 * Reference: https://en.wikipedia.org/wiki/Fast_Fourier_transform
 */
public class FFT {
    // Public methods

    /**
     * Computes the FFT of the input array <code>x</code>.<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @throws RuntimeException If the length of <code>x</code> is not a power of 2.
     * @implNote The array returned has length <code>N / 2 + 1</code>, so that only the non-negative
     * frequencies are returned.
     */
    public static Complex[] fft(Complex[] x) {
        // Compute radix-2 Cooley-Turkey FFT
        Complex[] tempMatrix = fft_helper(x);

        // Keep only the first `n / 2 + 1` elements
        int finalLength = x.length / 2 + 1;
        return Arrays.copyOfRange(tempMatrix, 0, finalLength);
    }

    // Private methods

    /**
     * Helper method that computes the FFT of the input array <code>x</code>.
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param x The complex array <code>x</code> representing the data source.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @throws RuntimeException If the length of <code>x</code> is not a power of 2.
     * @implNote Unlike the public method, the array returned has length <code>N</code>.
     * @see <a href="https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm">Radix-2
     * Cooley-Tukey Algorithm</a>, which was the algorithm used to generate the FFT.
     */
    private static Complex[] fft_helper(Complex[] x) {
        // Get length of input array
        int length = x.length;

        // Base case: length is 1
        if (length == 1) return new Complex[]{x[0]};

        // Assert that the length is AT LEAST a multiple of 2
        // (We'll be able to catch non-powers of two in subsequent recursive calls)
        if (length % 2 != 0) {
            throw new RuntimeException("The length of the array is not a power of 2");
        }

        // Compute FFT of even terms
        Complex[] terms = new Complex[length / 2];  // Will be used for both even and odd terms
        for (int k = 0; k < length / 2; k++) {
            terms[k] = x[2 * k];
        }
        Complex[] p = fft_helper(terms);

        // Compute FFT of odd terms
        for (int k = 0; k < length / 2; k++) {
            terms[k] = x[2 * k + 1];  // We reuse the above array
        }
        Complex[] q = fft_helper(terms);

        // Combine even and odd terms together
        Complex[] y = new Complex[length];
        for (int k = 0; k < length / 2; k++) {
            // Compute w = exp(−2πi/N k)
            double kth = -2 * k * Math.PI / length;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));

            // Set values for `y[k]` and `y[k+length/2]`
            y[k] = p[k].plus(wk.times(q[k]));
            y[k + length / 2] = p[k].minus(wk.times(q[k]));
        }

        // Return FFT array
        return y;
    }
}
