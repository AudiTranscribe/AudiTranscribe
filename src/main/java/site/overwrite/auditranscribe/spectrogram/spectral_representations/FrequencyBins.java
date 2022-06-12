/*
 * FrequencyBins.java
 *
 * Created on 2022-05-31
 * Updated on 2022-05-31
 *
 * Description: Methods that generate frequency bins for different spectral representations.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.utils.UnitConversionUtils;

/**
 * Methods that generate frequency bins for different spectral representations.
 */
public class FrequencyBins {
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

        for (int i = 0; i < numBins; i++) {
            frequencies[i] = i * (sampleRate / numFFT);
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
        double[] frequencies = new double[numBins];

        for (double i = 0; i < numBins; i++) {
            // Calculate the frequency of the current frequency bin
            double freq = fmin * Math.pow(2, i / binsPerOctave);

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

        // Define the output mel matrix
        double[] melBins = new double[numMels];
        for (int i = 0; i < numMels; i++) {
            // Compute the current mel value
            double mel = minMel + (maxMel - minMel) * i / (numMels - 1);

            // Convert that mel value into Hz and add to the array
            melBins[i] = UnitConversionUtils.melToHz(mel);
        }

        // Return the array of mel bins
        return melBins;
    }
}
