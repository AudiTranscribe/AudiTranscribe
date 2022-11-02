/*
 * FrequencyBins.java
 * Description: Methods that generate frequency bins for different spectral representations.
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

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.utils.UnitConversionUtils;

/**
 * Methods that generate frequency bins for different spectral representations.
 */
public final class FrequencyBins {
    private FrequencyBins() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Compute the Discrete Fourier Transform sample frequencies.
     *
     * @param numFFT     Number of FFT bins.
     * @param sampleRate Sample rate.
     * @return Array of length <code>numFFT / 2 + 1</code> containing the sample frequencies.
     */
    public static double[] getFFTFreqBins(int numFFT, double sampleRate) {
        int numBins = numFFT / 2 + 1;
        double[] frequencies = new double[numBins];

        double scaleFactor = sampleRate / numFFT;
        for (int i = 0; i < numBins; i++) {
            frequencies[i] = i * scaleFactor;
        }
        return frequencies;
    }

    /**
     * Compute the center frequencies of Q-Transform (QT) bins.
     *
     * @param numBins       Number of Q-Transform bins.
     * @param binsPerOctave Number of bins per octave.
     * @param fmin          Minimum frequency.
     * @return Array of center frequencies for each QT bin.
     */
    public static double[] getQTFreqBins(int numBins, int binsPerOctave, double fmin) {
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

    /**
     * Compute an array of acoustic frequencies tuned to the mel scale.
     *
     * @param numMels Number of mel bins.
     * @param minFreq Minimum frequency (in Hz).
     * @param maxFreq Maximum frequency (in Hz).
     * @return Array of acoustic frequencies tuned to the mel scale.
     */
    public static double[] getMelFreqBins(int numMels, double minFreq, double maxFreq) {
        // Convert the min and max frequencies to mel
        double minMel = UnitConversionUtils.hzToMel(minFreq);
        double maxMel = UnitConversionUtils.hzToMel(maxFreq);

        // Define normalization factor
        double normFactor = (maxMel - minMel) / (numMels - 1);

        // Define the output mel matrix
        double[] melBins = new double[numMels];
        for (int i = 0; i < numMels; i++) {
            // Compute the current mel value
            double mel = minMel + normFactor * i;

            // Convert that mel value into Hz and add to the array
            melBins[i] = UnitConversionUtils.melToHz(mel);
        }

        // Return the array of mel bins
        return melBins;
    }
}
