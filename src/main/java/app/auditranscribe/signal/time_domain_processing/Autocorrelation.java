/*
 * Autocorrelation.java
 * Description: Autocorrelation methods.
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

package app.auditranscribe.signal.time_domain_processing;

import app.auditranscribe.misc.Complex;
import app.auditranscribe.signal.representations.FFT;
import app.auditranscribe.utils.MatrixUtils;

/**
 * Autocorrelation methods.
 */
public final class Autocorrelation {
    private Autocorrelation() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Bounded-lag autocorrelation of a given matrix.<br>
     * This computes the autocorrelation <b>column-by-column</b> of a given matrix.
     *
     * @param y The matrix to autocorrelate.
     * @return The autocorrelation of the given matrix.
     */
    public static Complex[][] autocorrelation(Complex[][] y) {
        // Compute the maximum size
        int maxSize = y.length;

        // Pad out the signal to support full-length auto-correlation
        // (Note that this padding is *right* padding)
        int fullLength = 2 * y.length + 1;
        Complex[][] yPadded = new Complex[fullLength][y[0].length];  // We also need to convert to complex
        for (int i = 0; i < y.length; i++) {
            System.arraycopy(y[i], 0, yPadded[i], 0, y[0].length);
        }
        for (int i = y.length; i < fullLength; i++) {
            for (int j = 0; j < y[0].length; j++) {
                yPadded[i][j] = Complex.ZERO;
            }
        }

        // Transpose the padded matrix
        Complex[][] yTransposed = MatrixUtils.transpose(yPadded);

        // Compute the transposed autocorrelation matrix
        Complex[][] rawAutocorrelationTransposed = new Complex[y[0].length][fullLength];

        for (int i = 0; i < y[0].length; i++) {
            // Compute raw spectrogram
            Complex[] rawSpec = FFT.fft(yTransposed[i]);

            // Now convert to power
            Complex[] powerSpec = new Complex[fullLength];

            for (int j = 0; j < fullLength; j++) {
                // Get magnitude only
                double magnitude = rawSpec[j].abs();

                // Convert to power
                powerSpec[j] = new Complex(magnitude * magnitude);
            }

            // Convert back to time domain and add to autocorrelation matrix
            rawAutocorrelationTransposed[i] = FFT.ifft(powerSpec);
        }

        // Slice down to `maxSize`
        Complex[][] autocorrelationTransposed = new Complex[y[0].length][maxSize];
        for (int i = 0; i < y[0].length; i++) {
            System.arraycopy(
                    rawAutocorrelationTransposed[i],
                    0,
                    autocorrelationTransposed[i],
                    0,
                    maxSize
            );
        }

        // Now transpose back and return
        return MatrixUtils.transpose(autocorrelationTransposed);
    }

    /**
     * Bounded-lag autocorrelation of a given matrix.<br>
     * This computes the autocorrelation <b>column-by-column</b> of a given matrix.
     *
     * @param y The matrix to autocorrelate.
     * @return The autocorrelation of the given matrix.
     */
    public static double[][] autocorrelation(double[][] y) {
        // Convert to complex
        Complex[][] yComplex = new Complex[y.length][y[0].length];
        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[0].length; j++) {
                yComplex[i][j] = new Complex(y[i][j]);
            }
        }

        // Compute autocorrelation
        Complex[][] autocorrelated = autocorrelation(yComplex);

        // Convert each of the complex values to doubles
        double[][] autocorrelatedDoubles = new double[autocorrelated.length][autocorrelated[0].length];
        for (int i = 0; i < autocorrelated.length; i++) {
            for (int j = 0; j < autocorrelated[0].length; j++) {
                autocorrelatedDoubles[i][j] = autocorrelated[i][j].abs();
            }
        }

        return autocorrelatedDoubles;
    }
}
