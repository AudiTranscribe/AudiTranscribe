/*
 * BPMEstimationHelpers.java
 * Description: Class containing helper methods for BPM estimation.
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

package site.overwrite.auditranscribe.music.bpm_estimation;

/**
 * Class containing helper methods for BPM estimation.
 */
public final class BPMEstimationHelpers {
    private BPMEstimationHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that computes the frequencies (in beats per minute) corresponding to an onset
     * auto-correlation or tempogram matrix.
     *
     * @param numBins    The number of lag bins.
     * @param hopLength  The number of samples between each bin.
     * @param sampleRate The audio sampling rate.
     * @return Vector of bin frequencies measured in BPM.
     */
    public static double[] tempoFrequencies(int numBins, double hopLength, double sampleRate) {
        double[] binFreqs = new double[numBins];
        binFreqs[0] = Double.POSITIVE_INFINITY;

        double scale =  60 * sampleRate / hopLength;

        for (int i = 1; i < numBins; i++) {
            binFreqs[i] = scale / i;
        }
        return binFreqs;
    }
}
