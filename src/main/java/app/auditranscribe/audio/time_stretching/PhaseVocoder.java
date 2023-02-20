/*
 * PhaseVocoder.java
 * Description: Implements a phase vocoder.
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

package app.auditranscribe.audio.time_stretching;

import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.utils.ArrayUtils;

public final class PhaseVocoder extends LoggableClass {
    private PhaseVocoder() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Phase vocoder.<br>
     * Given an STFT matrix <code>stftMatrix</code>, speed up by a factor of
     * <code>speedUpFactor</code>.
     *
     * @param stftMatrix    STFT matrix.
     * @param hopLength     The number of samples between successive columns of <code>stftMatrix</code>.
     * @param speedUpFactor Speed-up factor.<br>
     *                      <code>speedUpFactor > 1</code> is faster, and <code>speedUpFactor <
     *                      1</code> is slower.
     * @return Time-stretched STFT matrix.
     * @implNote See
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/spectrum.html#phase_vocoder">
     * Librosa's implementation</a> of the phase vocoder.
     */
    public static Complex[][] phaseVocoder(Complex[][] stftMatrix, int hopLength, double speedUpFactor) {
        int m = stftMatrix.length;
        int n = stftMatrix[0].length;

        double[] timeSteps = ArrayUtils.arange(0, n, speedUpFactor);  // Stretched time steps
        Complex[][] stretchedSTFT = new Complex[m][timeSteps.length];
        double[] phiAdvance = ArrayUtils.linspace(0, hopLength * Math.PI, m);  // Expected phase advance

        // Initialize phase accumulator to the first sample
        double[] phaseAcc = new double[m];
        for (int i = 0; i < m; i++) {
            phaseAcc[i] = stftMatrix[i][0].phase();
        }

        // Pad the `stftMatrix` on the right to simplify boundary logic
        Complex[][] paddedSTFT = new Complex[m][n + 1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n + 1; j++) {
                if (j != n) paddedSTFT[i][j] = stftMatrix[i][j];
                else paddedSTFT[i][j] = Complex.ZERO;
            }
        }

        // Iterate through the time steps
        for (int t = 0; t < timeSteps.length; t++) {
            double step = timeSteps[t];
            int colStartIndex = (int) step;

            // Weighting for linear magnitude interpolation
            double alpha = step - (int) step;  // Not the most precise but good enough

            for (int i = 0; i < m; i++) {
                // Obtain the elements we will be considering
                Complex elem1 = paddedSTFT[i][colStartIndex];
                Complex elem2 = paddedSTFT[i][colStartIndex + 1];

                // Compute final magnitude and phase
                double magnitude = (1. - alpha) * elem1.abs() + alpha * elem2.abs();
                double phase = phaseAcc[i];

                // Store to output
                stretchedSTFT[i][t] = Complex.fromMagnitudeAndPhase(magnitude, phase);

                // Compute phase advance
                double deltaPhase = elem2.phase() - elem1.phase() - phiAdvance[i];
                deltaPhase -= 2. * Math.PI * Math.round(deltaPhase / (2. * Math.PI));  // Make phase in principal range

                // Accumulate phase advance
                phaseAcc[i] += phiAdvance[i] + deltaPhase;
            }
        }

        return stretchedSTFT;
    }
}
