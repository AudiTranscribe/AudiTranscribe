/*
 * MelSpectrogram.java
 *
 * Created on 2022-05-31
 * Updated on 2022-06-01
 *
 * Description: Class that handles the Mel-based Spectrogram.
 */

package site.overwrite.auditranscribe.spectrogram.spectral_representations;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.utils.ArrayUtils;

/**
 * Class that handles the Mel-based Spectrogram.
 */
public class MelSpectrogram {
    // Public methods

    /**
     * Compute a mel-scaled spectrogram.<br>
     * To compute the mel-scaled spectrogram, its magnitude spectrogram <code>S</code> is first
     * computed, then converted into power-based spectrogram, and then mapped onto the mel scale.
     *
     * @param x          Audio time series.
     * @param sampleRate Sample rate of the audio signal.
     * @param numFFT     Length of the FFT window.
     * @param hopLength  Number of samples between successive frames.
     * @return Mel spectrogram.
     */
    public static double[][] melSpectrogram(double[] x, double sampleRate, int numFFT, int hopLength) {
        // Generate the STFT spectrogram
        Complex[][] S = STFT.stft(x, numFFT, hopLength, WindowFunction.HANN_WINDOW);

        // Convert the STFT spectrogram to power spectrogram
        double[][] P = new double[S.length][S[0].length];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                P[i][j] = Math.pow(S[i][j].abs(), 2);
            }
        }

        // Continue processing in the other method
        return melSpectrogram(P, sampleRate, numFFT);
    }

    /**
     * Compute a mel-scaled spectrogram.<br>
     * To compute the mel-scaled spectrogram, its magnitude spectrogram <code>S</code> is first
     * computed, then converted into power-based spectrogram, and then mapped onto the mel scale.
     *
     * @param x          Audio time series.
     * @param sampleRate Sample rate of the audio signal.
     * @return Mel spectrogram.
     */
    public static double[][] melSpectrogram(double[] x, double sampleRate) {
        return melSpectrogram(x, sampleRate, 2048, 512);
    }

    /**
     * Compute a mel-scaled spectrogram.<br>
     * A power spectrogram input P is mapped directly onto the mel basis by performing matrix
     * multiplication.
     *
     * @param P          Power spectrogram.
     * @param sampleRate Sample rate of the audio signal.
     * @param numFFT     Length of the FFT window.
     * @return Mel spectrogram.
     */
    public static double[][] melSpectrogram(double[][] P, double sampleRate, int numFFT) {
        // Generate the mel filters
        double[][] melFilters = melFilters(sampleRate, numFFT);

        // Finally, create the spectrogram and return
        return ArrayUtils.matmul(melFilters, P);
    }

    /**
     * Compute a mel-scaled spectrogram.<br>
     * A power spectrogram input P is mapped directly onto the mel basis by performing matrix
     * multiplication.
     *
     * @param P          Power spectrogram.
     * @param sampleRate Sample rate of the audio signal.
     * @return Mel spectrogram.
     */
    public static double[][] melSpectrogram(double[][] P, double sampleRate) {
        // Generate the mel filters
        double[][] melFilters = melFilters(sampleRate, 2048);

        // Finally, create the spectrogram and return
        return ArrayUtils.matmul(melFilters, P);
    }

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
}
