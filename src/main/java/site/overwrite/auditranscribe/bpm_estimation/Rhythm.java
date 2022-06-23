/*
 * Rhythm.java
 *
 * Created on 2022-06-02
 * Updated on 2022-06-23
 *
 * Description: Rhythmic feature extraction methods.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.MathUtils;

/**
 * Rhythmic feature extraction methods.
 */
public class Rhythm {
    // Public methods

    /**
     * Compute the tempogram: local autocorrelation of the onset strength envelope.
     *
     * @param x          Audio time series.
     * @param sampleRate Sample rate of the audio signal.
     * @param hopLength  Number of audio samples between successive onset measurements.
     * @param winLength  Length of the onset autocorrelation window (in frames/onset measurements).
     * @return Localized autocorrelation of the onset strength envelope.
     * @throws ValueException If the window length is not a positive integer.
     * @see <a href="https://librosa.org/doc/0.9.1/_modules/librosa/feature/rhythm.html#tempogram">
     * Librosa's Implementation</a> of the tempogram.
     */
    public static double[][] tempogram(double[] x, double sampleRate, int hopLength, int winLength) {
        // Ascertain that the `winLength` is a positive integer
        if (winLength < 1) {
            throw new ValueException("The `winLength` must be a positive integer.");
        }

        // Generate onset envelope
        double[] rawOnsetEnvelope = Onset.onsetStrength(x, sampleRate, 2048, hopLength);

        // Center the autocorrelation windows
        int n = rawOnsetEnvelope.length;
        int padAmount = (int) Math.floor(winLength / 2.);
        double[] onsetEnvelope = new double[2 * padAmount + n];

        for (int i = 0; i < padAmount; i++) {
            onsetEnvelope[i] = MathUtils.normalize(
                    (double) (i + 1) / padAmount,
                    0,
                    1,
                    0,
                    rawOnsetEnvelope[0]
            );
        }
        System.arraycopy(rawOnsetEnvelope, 0, onsetEnvelope, padAmount, n);
        for (int i = padAmount + n; i < onsetEnvelope.length; i++) {
            onsetEnvelope[i] = MathUtils.normalize(
                    (double) (i - padAmount - n + 1) / padAmount,
                    0,
                    1,
                    rawOnsetEnvelope[rawOnsetEnvelope.length - 1],
                    0
            );
        }

        // Carve onset envelope into frames
        double[][] rawODFFrame = ArrayUtils.frame(onsetEnvelope, winLength, 1, true);

        // Truncate to the length of the original signal
        double[][] odfFrame = new double[winLength][n];

        for (int i = 0; i < winLength; i++) {
            System.arraycopy(rawODFFrame[i], 0, odfFrame[i], 0, n);
        }

        // Generate the window array
        double[] windowArr = WindowFunction.HANN_WINDOW.window.generateWindow(winLength, false);

        // Compute the windowed onset envelope
        double[][] odfWindowed = new double[winLength][n];
        for (int i = 0; i < odfFrame.length; i++) {
            for (int j = 0; j < n; j++) {
                odfWindowed[i][j] = odfFrame[i][j] * windowArr[i];
            }
        }

        // Autocorrelate the windowed onset envelope
        double[][] autocorrelated = Autocorrelation.autocorrelation(odfWindowed);

        // Transpose the autocorrelation
        double[][] autocorrelatedTransposed = ArrayUtils.transpose(autocorrelated);

        // Normalize the transposed autocorrelation
        double[][] normalizedTransposed = new double[n][winLength];
        for (int i = 0; i < n; i++) {
            normalizedTransposed[i] = ArrayUtils.lpNormalize(autocorrelatedTransposed[i], Double.POSITIVE_INFINITY);
        }

        // Transpose back and return
        return ArrayUtils.transpose(normalizedTransposed);
    }
}
