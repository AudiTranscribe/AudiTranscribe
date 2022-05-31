/*
 * MelSpectrogram.java
 *
 * Created on 2022-05-31
 * Updated on 2022-06-01
 *
 * Description: Class that handles the Mel-based Spectrogram.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

/**
 * Class that handles the Mel-based Spectrogram.
 */
public class MelSpectrogram {
    /**
     * Create a Mel filter-bank.<br>
     * This produces a linear transformation matrix to project FFT bins onto Mel-frequency bins.
     *
     * @param sampleRate Sample rate of the audio signal.
     * @param numFFT     Number of FFT components.
     * @param numMels    Number of Mel bands to generate.
     * @param minFreq    Lowest frequency (in Hz)
     * @param maxFreq    highest frequency (in Hz).
     * @return Mel transform matrix (Shape is <code>(numMels, 1 + numFFT/2)</code>).
     */
    public static double[][] melFilters(double sampleRate, int numFFT, int numMels, double minFreq, double maxFreq) {
        // Initialize the weights matrix
        double[][] weights = new double[numMels][1 + numFFT / 2];

        // Get center frequencies of each FFT bin
        double[] fftFreqs = FrequencyBins.getFFTFreqBins(numFFT, sampleRate);

        // 'Center frequencies' of mel bands - uniformly spaced between limits
        double[] melFreqs = FrequencyBins.getMelFreqBins(numMels + 2, minFreq, maxFreq);

        // Calculate differences between each mel band center frequency
        double[] freqDiffs = new double[numMels + 1];  // Because the length of `melFreqs` is `numMels + 2`
        for (int i = 0; i < numMels + 1; i++) {
            freqDiffs[i] = melFreqs[i + 1] - melFreqs[i];
        }

        // Compute ramps
        double[][] ramps = new double[numMels + 2][numFFT / 2 + 1];
        for (int i = 0; i < numMels + 2; i++) {
            for (int j = 0; j < numFFT / 2 + 1; j++) {
                ramps[i][j] = melFreqs[i] - fftFreqs[j];
            }
        }

        // Update the weights matrix
        for (int i = 0; i < numMels; i++) {
            for (int j = 0; j < numFFT / 2 + 1; j++) {
                // Lower and upper slopes for all bins
                double lower = -ramps[i][j] / freqDiffs[i];
                double upper = ramps[i + 2][j] / freqDiffs[i + 1];

                // Then intersect them with each other and zero
                weights[i][j] = Math.max(0, Math.min(lower, upper));
            }
        }

        // Slaney-style mel is scaled to be approx constant energy per channel
        for (int i = 0; i < numMels; i++) {
            double enorm = 2. / (melFreqs[i + 2] - melFreqs[i]);
            for (int j = 0; j < numFFT / 2 + 1; j++) {
                weights[i][j] *= enorm;
            }
        }

        // Return the matrix of weights
        return weights;
    }

    /**
     * Create a Mel filter-bank.<br>
     * This produces a linear transformation matrix to project FFT bins onto Mel-frequency bins.
     *
     * @param sampleRate Sample rate of the audio signal.
     * @param numFFT     Number of FFT components.
     * @return Mel transform matrix (Shape is <code>(numMels, 1 + numFFT/2)</code>).
     */
    public static double[][] melFilters(double sampleRate, int numFFT) {
        return melFilters(sampleRate, numFFT, 128, 0, sampleRate / 2);
    }

    // Todo: add actual mel spectrogram code
}
