/*
 * FrequencyRangeGeneration.java
 * Description: Handles the calculation of center frequencies of some signal algorithms.
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

package app.auditranscribe.signal;

/**
 * Handles the calculation of center frequencies of some signal algorithms.
 */
public final class FrequencyRangeGeneration {
    private FrequencyRangeGeneration() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Compute the Fast Fourier Transform (FFT)'s bins' center frequencies.
     *
     * @param numFFT     FFT window size.
     * @param sampleRate Audio sampling rate.
     * @return Array of length <code>numFFT / 2 + 1</code> containing the sample frequencies.
     */
    public static double[] fftFreqBins(int numFFT, double sampleRate) {
        int numBins = numFFT / 2 + 1;
        double[] frequencies = new double[numBins];

        double scaleFactor = sampleRate / numFFT;
        for (int i = 0; i < numBins; i++) {
            frequencies[i] = i * scaleFactor;
        }
        return frequencies;
    }

    /**
     * Compute the center frequencies of Q-Transform bins.
     *
     * @param numBins       Number of Q-Transform bins.
     * @param binsPerOctave Number of bins per octave.
     * @param fmin          Minimum frequency.
     * @return Array of center frequencies for each Q-Transform bin.
     */
    public static double[] qTransformFreqBins(int numBins, int binsPerOctave, double fmin) {
        double octavesPerBin = 1. / binsPerOctave;

        double[] frequencies = new double[numBins];
        for (double i = 0; i < numBins; i++) {
            // Calculate the frequency of the current frequency bin
            double freq = fmin * Math.pow(2, i * octavesPerBin);

            // Append it to the list of frequencies
            frequencies[(int) i] = freq;
        }

        return frequencies;
    }
}
