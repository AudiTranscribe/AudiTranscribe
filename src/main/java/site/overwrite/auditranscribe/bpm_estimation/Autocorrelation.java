/*
 * Autocorrelation.java
 *
 * Created on 2022-06-02
 * Updated on 2022-06-23
 *
 * Description: Class that handles audio autocorrelation methods.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.FFT;
import site.overwrite.auditranscribe.utils.ArrayUtils;

/**
 * Class that handles audio autocorrelation methods.
 */
public class Autocorrelation {
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
        Complex[][] yTransposed = ArrayUtils.transpose(yPadded);

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
        return ArrayUtils.transpose(autocorrelationTransposed);
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
