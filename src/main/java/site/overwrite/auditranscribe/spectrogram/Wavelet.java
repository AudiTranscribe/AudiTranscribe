/*
 * Wavelet.java
 *
 * Created on 2022-02-13
 * Updated on 2022-05-14
 *
 * Description: Class to implement audio windowing functions.
 */

package site.overwrite.auditranscribe.spectrogram;

import javafx.util.Pair;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.SpectralHelpers;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to implement audio windowing functions.
 */
public class Wavelet {
    // Public methods

    /**
     * Return length of each filter in a wavelet basis.<br><br>
     * Assumes <code>freqs</code> are all positive and in strictly ascending order.
     *
     * @param freqs          Array containing the centers of all the frequency bins.
     * @param sr             Sample rate.
     * @param windowFunction Window function to use.
     * @param filterScale    Scaling factor for the filter.
     * @param isCQT          Whether this is a CQT or not.
     * @param gammaValue     Default gamma value to use.
     * @param fallbackAlpha  Fallback alpha value if the alpha value cannot be calculated.
     * @return Pair of values. First value represents the wavelet lengths. Second value represents
     * frequency cutoff.
     * @see <a href="https://www.sciencedirect.com/science/article/abs/pii/037859559090170T">This
     * paper</a> by Glasberg, Brian R., and Brian CJ Moore. "Derivation of auditory filter shapes
     * from notched-noise data." Hearing research 47.1-2 (1990): 103-138.
     */
    public static Pair<double[], Double> computeWaveletLengths(
            double[] freqs, double sr, WindowFunction windowFunction, double filterScale, boolean isCQT,
            double gammaValue, double fallbackAlpha
    ) {
        // Check the number of frequencies provided
        int numFreqs = freqs.length;
        double[] alphas = new double[numFreqs];

        if (numFreqs >= 2) {  // We need at least 2 frequencies to infer alpha
            // Compute the log2 of the provided frequencies
            double[] logFreqs = new double[numFreqs];
            for (int i = 0; i < numFreqs; i++) {
                logFreqs[i] = MathUtils.log2(freqs[i]);
            }

            // Approximate the local octave resolution
            double[] bpo = new double[numFreqs];

            bpo[0] = 1. / (logFreqs[1] - logFreqs[0]);  // First element case
            bpo[numFreqs - 1] = 1. / (logFreqs[numFreqs - 1] - logFreqs[numFreqs - 2]);  // Last element case

            for (int i = 1; i < numFreqs - 1; i++) {  // Intermediate elements case
                bpo[i] = 2. / (logFreqs[i + 1] - logFreqs[i - 1]);
            }

            // Calculate alphas for each frequency bin
            for (int i = 0; i < numFreqs; i++) {
                alphas[i] = SpectralHelpers.computeAlpha(bpo[i]);
            }
        } else {
            alphas = new double[]{fallbackAlpha};
        }

        // Compute gamma coefficients if not provided
        double[] gammas = new double[numFreqs];

        if (!isCQT && gammaValue == 0) {  // This means that gamma has not been calculated yet
            double coefficient = 24.7 / 0.108;  // This special constant is from the paper
            for (int i = 0; i < numFreqs; i++) {
                gammas[i] = coefficient * alphas[i];
            }
        } else if (isCQT) {
            for (int i = 0; i < numFreqs; i++) {
                gammas[i] = 0;
            }
        } else {
            for (int i = 0; i < numFreqs; i++) {
                gammas[i] = gammaValue;
            }
        }

        // Compute Q-Factor matrix
        double[] Q = new double[numFreqs];
        for (int i = 0; i < numFreqs; i++) {
            Q[i] = filterScale / alphas[i];
        }

        // Find frequency cutoff
        double freqCutoff = -Double.MAX_VALUE;
        double bandwidth = windowFunction.window.getBandwidth();

        for (int i = 0; i < numFreqs; i++) {
            double possibleCutoff = freqs[i] * (1. + 0.5 * bandwidth / Q[i]) + 0.5 * gammas[i];
            freqCutoff = Math.max(possibleCutoff, freqCutoff);
        }

        // Convert frequencies to filter lengths
        double[] lengths = new double[numFreqs];
        for (int i = 0; i < numFreqs; i++) {
            lengths[i] = Q[i] * sr / (freqs[i] + gammas[i] / alphas[i]);
        }

        // Return needed values
        return new Pair<>(lengths, freqCutoff);
    }

    /**
     * Construct a wavelet basis using windowed complex sinusoids.<br>
     * This function constructs a wavelet filterbank at a specified set of center
     * frequencies.<br><br>
     * Assumes <code>freqs</code> are all positive and in strictly ascending order.
     *
     * @param freqs          Array containing the centers of all the frequency bins.
     * @param sr             Sample rate.
     * @param windowFunction Window function to use.
     * @param filterScale    Scaling factor for the filter.
     * @param padFFT         Whether to pad in preparation for FFT.
     * @param norm           p-value for the LP norm.
     * @param isCQT          Whether this is a CQT or not.
     * @param gamma          Gamma value.
     * @param alpha          Alpha value.
     * @return Pair of values. First value is a 2D array of the filter. Second value is the lengths
     * of each of the filters.
     * @implNote From <a href="https://librosa.org/doc/0.9.1/_modules/librosa/filters.html#wavelet">
     * Librosa's Implementation</a>. This code is a Java version of that code.
     * @see <a href="https://www.sciencedirect.com/science/article/abs/pii/037859559090170T">This
     * paper</a> by Glasberg, Brian R., and Brian CJ Moore. "Derivation of auditory filter shapes
     * from notched-noise data." Hearing research 47.1-2 (1990): 103-138.
     */
    public static Pair<Complex[][], double[]> computeWaveletBasis(
            double[] freqs, double sr, WindowFunction windowFunction, double filterScale, boolean padFFT, double norm, boolean isCQT,
            double gamma, double alpha
    ) {
        // Pass-through parameters to get the filter lengths
        Pair<double[], Double> waveletLengthsResult = computeWaveletLengths(
                freqs, sr, windowFunction, filterScale, isCQT, gamma, alpha
        );
        double[] lengths = waveletLengthsResult.getKey();
        int numLengths = lengths.length;

        // Build the filters
        List<Complex[]> filters = new ArrayList<>();
        for (int i = 0; i < numLengths; i++) {
            // Get `ilen` and `freq`
            double ilen = lengths[i];
            double freq = freqs[i];

            // Compute the length of the signal
            int lowerBound = (int) Math.floor(-ilen / 2);
            int upperBound = (int) Math.floor(ilen / 2);
            int signalLength = upperBound - lowerBound;

            // Build the filter
            Complex[] sig = new Complex[signalLength];

            for (int j = 0; j < signalLength; j++) {
                // Calculate current 'index' value
                int indexVal = j + lowerBound;

                // Compute current signal value
                sig[j] = Complex.exp(new Complex(0, indexVal * (2 * Math.PI * freq / sr)));
            }

            // Apply the windowing function
            double[] windowArray = windowFunction.window.generateWindow(signalLength, false);
            for (int j = 0; j < signalLength; j++) {
                sig[j] = sig[j].scale(windowArray[j]);
            }

            // Normalise
            sig = ArrayUtils.lpNormalise(sig, norm);

            // Append the signal to the `filters` list
            filters.add(sig);
        }

        // Find the maximum length
        double maxLenDouble = Double.MIN_VALUE;  // Double for now; we'll change to integer later
        for (double elem : lengths) if (maxLenDouble < elem) maxLenDouble = elem;

        // Update the maximum length
        int maxLen;
        if (padFFT) {
            maxLen = (int) Math.pow(2, Math.ceil(MathUtils.log2(maxLenDouble)));
        } else {
            maxLen = (int) Math.ceil(maxLenDouble);
        }

        // Pad and stack
        Complex[][] filtersFinal = new Complex[numLengths][maxLen];
        for (int i = 0; i < numLengths; i++) {
            filtersFinal[i] = ArrayUtils.padCenter(filters.get(i), maxLen);
        }

        // Return needed data
        return new Pair<>(filtersFinal, lengths);
    }
}
