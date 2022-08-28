/*
 * Onset.java
 * Description: Class that handles spectral flux onset detection.
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

package site.overwrite.auditranscribe.bpm_estimation;

import site.overwrite.auditranscribe.spectrogram.spectral_representations.MelSpectrogram;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

/**
 * Class that handles spectral flux onset detection.
 */
public final class Onset {
    private Onset() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Compute a spectral flux onset strength envelope.
     *
     * @param x          <b>Mono</b> audio time-series.
     * @param sampleRate Sample rate of the audio time-series.
     * @param numFFT     FFT window size for the mel spectrogram.
     * @param hopLength  Hop length for use in the mel spectrogram.
     * @return Spectral flux onset strength envelope for the provided data.
     */
    public static double[] onsetStrength(double[] x, double sampleRate, int numFFT, int hopLength) {
        // Compute mel spectrogram
        double[][] S = MelSpectrogram.melSpectrogram(x, sampleRate);

        // Convert to dBs
        double[][] SdB = UnitConversionUtils.powerToDecibel(S, 1, 80);

        // Compute difference to the reference, spaced by lag
        double[][] onsetEnv = new double[S.length][S[0].length - 1];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length - 1; j++) {
                // Compute difference in dBs
                double dbDiff = SdB[i][j + 1] - SdB[i][j];

                // Discard negatives (decreasing amplitude)
                if (dbDiff < 0) dbDiff = 0;

                // Update the onset envelope matrix
                onsetEnv[i][j] = dbDiff;
            }
        }

        // Find the mean within channels
        double[][] onsetEnvNew = new double[1][onsetEnv[0].length];
        for (int j = 0; j < onsetEnv[0].length; j++) {
            // Get the channel's data
            double[] channelData = new double[onsetEnv.length];
            for (int i = 0; i < onsetEnv.length; i++) {
                channelData[i] = onsetEnv[i][j];
            }

            // Now compute the mean
            onsetEnvNew[0][j] = MathUtils.mean(channelData);
        }

        // Counter-act framing effects by shifting the onsets by `numFFT / hopLength`
        int padWidth = 1 + numFFT / (2 * hopLength);
        double[][] onsetEnvPadded = new double[onsetEnvNew.length][onsetEnvNew[0].length + padWidth];

        for (int i = 0; i < onsetEnvNew.length; i++) {
            System.arraycopy(onsetEnvNew[i], 0, onsetEnvPadded[i], padWidth, onsetEnvNew[0].length);
        }

        // Trim to match the input duration
        onsetEnv = new double[onsetEnvPadded.length][S[0].length];
        for (int i = 0; i < onsetEnvPadded.length; i++) {
            System.arraycopy(onsetEnvPadded[i], 0, onsetEnv[i], 0, S[0].length);
        }

        // Keep only first channel's data
        double[] output = new double[S[0].length];
        System.arraycopy(onsetEnv[0], 0, output, 0, S[0].length);

        return output;
    }

    /**
     * Compute a spectral flux onset strength envelope.<br>
     * This method uses <code>numFFT = 2048</code> and <code>hopLength = 512</code> for
     * computations.
     *
     * @param x          <b>Mono</b> audio time-series.
     * @param sampleRate Sample rate of the audio time-series.
     * @return Spectral flux onset strength envelope for the provided data.
     */
    public static double[] onsetStrength(double[] x, double sampleRate) {
        return onsetStrength(x, sampleRate, 2048, 512);
    }
}
