/*
 * Tempogram.java
 * Description: Computes the local autocorrelation of the onset strength envelope.
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

package app.auditranscribe.signal.feature_extraction;

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.signal.onset_detection.Onset;
import app.auditranscribe.signal.time_domain_processing.Autocorrelation;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.ArrayUtils;
import app.auditranscribe.utils.MathUtils;

/**
 * Computes the local autocorrelation of the onset strength envelope.
 */
public class Tempogram {
    private Tempogram() {
        // Private constructor to signal this is a utility class
    }

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
        int padAmount = Math.floorDiv(winLength, 2);
        double[] onsetEnvelope = new double[2 * padAmount + n];

        for (int i = 0; i < padAmount; i++) {
            onsetEnvelope[i] = MathUtils.normalize(
                    (double) (i + 1) / padAmount, 0, 1, 0, rawOnsetEnvelope[0]
            );
        }
        System.arraycopy(rawOnsetEnvelope, 0, onsetEnvelope, padAmount, n);
        for (int i = padAmount + n; i < onsetEnvelope.length; i++) {
            onsetEnvelope[i] = MathUtils.normalize(
                    (double) (i - padAmount - n + 1) / padAmount, 0, 1,
                    rawOnsetEnvelope[rawOnsetEnvelope.length - 1], 0
            );
        }

        // Carve onset envelope into frames
        double[][] rawODFFrame = ArrayUtils.frameVertical(onsetEnvelope, winLength, 1);

        // Truncate to the length of the original signal
        double[][] odfFrame = new double[winLength][n];

        for (int i = 0; i < winLength; i++) {
            System.arraycopy(rawODFFrame[i], 0, odfFrame[i], 0, n);
        }

        // Generate the window array
        double[] windowArr = SignalWindow.HANN_WINDOW.window.generateWindow(winLength, false);

        // Compute the windowed onset envelope
        double[][] odfWindowed = new double[winLength][n];
        for (int i = 0; i < odfFrame.length; i++) {
            for (int j = 0; j < n; j++) {
                odfWindowed[i][j] = odfFrame[i][j] * windowArr[i];
            }
        }

        // Autocorrelate the windowed onset envelope
        double[][] autocorrelated = Autocorrelation.autocorrelation(odfWindowed);

        // Normalize autocorrelation
        double[] temp = new double[winLength];
        for (int i = 0; i < n; i++) {
            // Place column into `temp` array
            for (int j = 0; j < winLength; j++) {
                temp[j] = autocorrelated[j][i];
            }

            // Normalize the `temp` array
            temp = ArrayUtils.lpNormalize(temp, Double.POSITIVE_INFINITY);

            // Place back into the autocorrelation matrix
            for (int j = 0; j < winLength; j++) {
                autocorrelated[j][i] = temp[j];
            }
        }

        return autocorrelated;
    }
}
