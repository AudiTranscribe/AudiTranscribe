/*
 * FFT.java
 * Description: Class that implements Fast Fourier Transform (FFT) algorithms.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.signal.representations;

import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.MiscUtils;

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
     * Computes the Fast Fourier Transform (FFT) of a complex number array.
     *
     * @param array The complex number array to apply the FFT to.
     * @return An array of <code>Complex</code> objects representing the FFT of <code>array</code>.
     */
    public static Complex[] fft(Complex[] array) {
        // Get length fo input array
        int length = array.length;

        // Handle different cases of the length
        if (length == 0) {
            return new Complex[0];
        } else if (MathUtils.isPowerOf2(length)) {
            return fftRadix2(array, length);
        } else {  // Is not power of 2
            return fftBluestein(array, length);
        }
    }

    /**
     * Computes the Real-Valued FFT (RFFT) of a complex number array.
     *
     * @param array The complex number array to apply the RFFT to.
     * @return An array of <code>Complex</code> objects representing the RFFT of <code>array</code>.
     * @implNote The array returned is so that only the non-negative frequencies are returned. This
     * means that the first <code>(N / 2) + 1</code> elements are returned if the length of the
     * array, <code>N</code>, is even and <code>(N + 1) / 2</code> if <code>N</code> is odd.
     */
    public static Complex[] rfft(Complex[] array) {
        // Get length of the array
        int length = array.length;

        // Compute FFT of the matrix
        Complex[] tempMatrix = fft(array);

        // Determine number of elements to keep
        int numElemToKeep = length / 2 + 1;

        // Keep only that number of elements
        return Arrays.copyOfRange(tempMatrix, 0, numElemToKeep);
    }

    /**
     * Computes the Inverse FFT (IFFT) of a complex number array.<br>
     * This assumes that the length of the input array is a power of 2.
     *
     * @param array The complex number array to apply the IFFT to.
     * @return An array of <code>Complex</code> objects representing the IFFT of <code>array</code>.
     */
    public static Complex[] ifft(Complex[] array) {
        // Get length of input array
        int length = array.length;

        // Compute conjugate of each element of the array
        Complex[] y = new Complex[length];  // Will reuse array later
        for (int i = 0; i < length; i++) {
            y[i] = array[i].conjugate();
        }

        // Compute forward FFT
        y = fft(y);

        // Take conjugate again
        for (int i = 0; i < length; i++) {
            y[i] = y[i].conjugate();
        }

        // Divide by the length of the array
        for (int i = 0; i < length; i++) {
            y[i] = y[i].divides(length);
        }

        // Return the inverse FFT array
        return y;
    }

    // Private methods

    /**
     * Helper method that computes the FFT of the input array using the Radix-2 Cooley-Tukey
     * Algorithm.<br>
     * This assumes that the length of the input array, say <code>N</code>, is a power of 2.
     *
     * @param array  The complex array to apply the algorithm to.
     * @param length Length of the input array.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @see <a href="https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm">Radix-2
     * Cooley-Tukey Algorithm</a>, which was the algorithm used to generate the FFT.
     */
    private static Complex[] fftRadix2(Complex[] array, int length) {
        // Base case: length is 1
        // (Length 0 case is handled by wrapper function)
        if (length == 1) return new Complex[]{array[0]};

        // Compute length of each of the two sub-arrays
        int halfLength = length / 2;

        // Compute FFT of even terms
        Complex[] terms = new Complex[halfLength];  // Will be used for both even and odd terms
        for (int k = 0; k < halfLength; k++) {
            terms[k] = array[2 * k];
        }
        Complex[] p = fftRadix2(terms, halfLength);

        // Compute FFT of odd terms
        for (int k = 0; k < halfLength; k++) {
            terms[k] = array[2 * k + 1];  // We reuse the above array
        }
        Complex[] q = fftRadix2(terms, halfLength);

        // Combine even and odd terms together
        double scale = -2 * Math.PI / length;  // Will be used in loop below
        Complex[] y = new Complex[length];
        for (int k = 0; k < halfLength; k++) {
            // Compute w = exp(−2kπi/N)
            Complex wk = Complex.exp(new Complex(0, k * scale));

            // Set values for `y[k]` and `y[k + length/2]`
            y[k] = p[k].plus(wk.times(q[k]));
            y[k + length / 2] = p[k].minus(wk.times(q[k]));
        }

        // Return FFT array
        return y;
    }

    /**
     * Helper method that computes the FFT of the input array <code>array</code> using Bluestein's
     * algorithm (also known as a Chirp-Z Transform, or CZT for short).
     *
     * @param array The complex array to apply the algorithm to.
     * @return An array of <code>Complex</code> objects representing the FFT of the data source.
     * @see <a href="https://en.wikipedia.org/wiki/Chirp_Z-transform#Bluestein's_algorithm">
     * Bluestein's CZT Algorithm</a>, which was the algorithm used to generate the FFT.
     */
    private static Complex[] fftBluestein(Complex[] array, int length) {
        // Find a power of 2 convolution length such that it is at least `length * 2 + 1`
        // (Note: `numSetBits(length * 2) = numSetBits(length) + 1`)
        int convolutionLength = (int) Math.pow(2, MiscUtils.getNumSetBits(length) + 1);

        // Generate trigonometric table
        double exponentCoefficient = -Math.PI / length;

        Complex[] exponentialTable = new Complex[length];
        for (int i = 0; i < length; i++) {
            exponentialTable[i] = Complex.exp(
                    Complex.IMAG_UNIT.times(
                            new Complex((i * i) % (length * 2)).times(exponentCoefficient)
                    )
            );
        }

        // Generate the first temporary vector
        Complex[] aVector = new Complex[convolutionLength];
        for (int i = 0; i < length; i++) {
            aVector[i] = array[i].times(exponentialTable[i]);
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
     * Helper method that computes the circular convolution of the two input vectors
     * (<code>aVector</code> and <code>bVector</code>).
     *
     * @param aVector The first vector.
     * @param bVector The second vector.
     * @return The circular convolution of the two input vectors.
     * @throws LengthException If the lengths of <code>aVector</code> and <code>bVector</code> are
     *                         not equal.
     */
    private static Complex[] circularConvolution(Complex[] aVector, Complex[] bVector) {
        // Check that the lengths of the vectors are equal
        if (aVector.length != bVector.length)
            throw new LengthException("The lengths of the vectors are not equal.");

        // Get the number of elements in the vectors, `length`
        int length = aVector.length;

        // Apply convolution theorem to obtain resultant convoluted vector
        // (See https://en.wikipedia.org/wiki/Convolution_theorem)
        Complex[] AVector = fft(aVector);  // Convention: capital letter represents a fourier transformed vector
        Complex[] BVector = fft(bVector);
        Complex[] XVector = new Complex[length];
        for (int i = 0; i < length; i++) {
            XVector[i] = AVector[i].times(BVector[i]);
        }

        return ifft(XVector);
    }
}