/*
 * Windowing.java
 *
 * Created on 2022-02-13
 * Updated on 2022-03-11
 *
 * Description: Class to implement audio windowing functions.
 */

package site.overwrite.auditranscribe.audio;

import javafx.util.Pair;
import site.overwrite.auditranscribe.audio.windows.AbstractWindow;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.Helpers;
import site.overwrite.auditranscribe.utils.ArrayAdjustment;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.OtherMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to implement audio windowing functions.
 */
public class Windowing {
    // Public methods

    /**
     * Return length of each filter in a wavelet basis.<br><br>
     * Assumes <code>freqs</code> are all positive and in strictly ascending order.
     *
     * @param freqs         Array containing the centers of all the frequency bins.
     * @param sr            Sample rate.
     * @param window        Window function to use.
     * @param isCQT         Whether this is a CQT or not.
     * @param gammaValue    Default gamma value to use.
     * @param fallbackAlpha Fallback alpha value if the alpha value cannot be calculated.
     * @param filterScale   Scaling factor for the filter.
     * @return Pair of values. First value represents the wavelet lengths. Second value represents
     * frequency cutoff.
     * @see <a href="https://www.sciencedirect.com/science/article/abs/pii/037859559090170T">This
     * paper</a> by Glasberg, Brian R., and Brian CJ Moore. "Derivation of auditory filter shapes
     * from notched-noise data." Hearing research 47.1-2 (1990): 103-138.
     */
    public static Pair<double[], Double> computeWaveletLengths(double[] freqs, double sr, Window window, boolean isCQT, double gammaValue, double fallbackAlpha, double filterScale) {
        // Check the number of frequencies provided
        int numFreqs = freqs.length;
        double[] alphas = new double[numFreqs];

        if (numFreqs >= 2) {  // We need at least 2 frequencies to infer alpha
            // Compute the log2 of the provided frequencies
            double[] logFreqs = new double[numFreqs];
            for (int i = 0; i < numFreqs; i++) {
                logFreqs[i] = OtherMath.log2(freqs[i]);
            }

            // Approximate the local octave resolution
            double[] bpo = new double[numFreqs];

            bpo[0] = 1 / (logFreqs[1] - logFreqs[0]);  // First element case
            bpo[numFreqs - 1] = 1 / (logFreqs[numFreqs - 1] - logFreqs[numFreqs - 2]);  // Last element case

            for (int i = 1; i < numFreqs - 1; i++) {  // Intermediate elements case
                bpo[i] = 2 / (logFreqs[i + 1] - logFreqs[i - 1]);
            }

            // Calculate alphas for each frequency bin
            for (int i = 0; i < numFreqs; i++) {
                alphas[i] = Helpers.computeAlpha(bpo[i]);
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
        double freqCutoff = Double.MIN_VALUE;
        double bandwidth = window.window.getBandwidth();

        for (int i = 0; i < numFreqs; i++) {
            double possibleCutoff = freqs[i] * (1 + 0.5 * bandwidth / Q[i]) + 0.5 * gammas[i];
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
     * @param freqs       Array containing the centers of all the frequency bins.
     * @param sr          Sample rate.
     * @param window      Windowing function to use.
     * @param filterScale Scaling factor for the filter.
     * @param padFFT      Whether to pad in preparation for FFT.
     * @param norm        p-value for the LP norm.
     * @param isCQT       Whether this is a CQT or not.
     * @param gamma       Gamma value.
     * @param alpha       Alpha value.
     * @return Pair of values. First value is a 2D array of the filter. Second value is the lengths
     * of each of the filters.
     * @implNote Taken from <a href="https://librosa.org/doc/0.9.1/_modules/librosa/filters.html#wavelet">Librosa's Implementation</a>.
     * @see <a href="https://www.sciencedirect.com/science/article/abs/pii/037859559090170T">This
     * paper</a> by Glasberg, Brian R., and Brian CJ Moore. "Derivation of auditory filter shapes
     * from notched-noise data." Hearing research 47.1-2 (1990): 103-138.
     */
    public static Pair<Complex[][], double[]> computeWaveletBasis(double[] freqs, double sr, Window window, double filterScale, boolean padFFT, double norm, boolean isCQT, double gamma, double alpha) {
        // Pass-through parameters to get the filter lengths
        Pair<double[], Double> waveletLengthsResult = computeWaveletLengths(
                freqs,
                sr,
                window,
                isCQT,
                gamma,
                alpha,
                filterScale
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
                double indexVal = ((double) j / signalLength - 1) * (upperBound - lowerBound) + lowerBound;

                // Compute current signal value
                sig[j] = Complex.exp(new Complex(0, indexVal * (2 * Math.PI * freq / sr)));
            }

            // Apply the windowing function
            double[] windowArray = window.window.generateWindow(signalLength, false);
            for (int j = 0; j < signalLength; j++) {
                sig[j] = sig[j].scale(windowArray[j]);
            }

            // Normalise
            sig = ArrayAdjustment.normalise(sig, norm);

            // Append the signal to the `filters` list
            filters.add(sig);
        }

        // Find the maximum length
        double maxLenDouble = Double.MIN_VALUE;  // Double for now; we'll change to integer later
        for (double elem : lengths) if (maxLenDouble < elem) maxLenDouble = elem;

        // Update the maximum length
        int maxLen;
        if (padFFT) {
            maxLen = (int) Math.pow(2, Math.ceil(OtherMath.log2(maxLenDouble)));
        } else {
            maxLen = (int) Math.ceil(maxLenDouble);
        }

        // Pad and stack
        Complex[][] filtersFinal = new Complex[numLengths][maxLen];
        for (int i = 0; i < numLengths; i++) {
            filtersFinal[i] = ArrayAdjustment.padCenter(filters.get(i), maxLen);
        }

        // Return needed data
        return new Pair<>(filtersFinal, lengths);
    }

//    /**
//     * Converts the samples in the <code>samples</code> array into windowed samples.
//     *
//     * @param samples     Array containing audio samples.
//     * @param numSamples  Number of elements in the <code>samples</code> array.
//     * @param frameLength Length of the frame.
//     * @param hopLength   Number of steps to advance between frames.
//     * @param window      The window function to use.
//     * @param audioFormat Format of the audio file.
//     * @return 2D array of windowed samples of shape (<code>ceil(numSamples/hopLength)</code>,
//     * <code>frameLength</code>).
//     */
//    // Todo: remove?
//    public static float[][] generateWindowedSamples(float[] samples, int numSamples, int frameLength, int hopLength,
//                                                    Window window, AudioFormat audioFormat) {
//        // Generate the frames of the sample
//        float[][] frames = ArrayAdjustment.frameOld(samples, frameLength, hopLength);
//
//        // Get the number of windows and number of channels
//        int numWindows = frames.length;
//        int numChannels = audioFormat.getChannels();
//
//        // Apply window function to the samples
//        float[][] windowedSamples = new float[numWindows][frameLength];
//
//        for (int windowNum = 0; windowNum < numWindows; windowNum++) {
//            // Determine the number of valid samples
//            int numValidSamples;
//
//            if (windowNum != numWindows - 1) {
//                numValidSamples = frameLength;
//            } else {
//                numValidSamples = frameLength - (numSamples % frameLength);  // Final frame length
//            }
//
//            // Get a copy of the samples that need to be windowed
//            float[] tempSamples = Arrays.copyOf(frames[windowNum], frameLength);
//
//            // Apply window function on those samples
//            applyWindow(tempSamples, numValidSamples, numChannels, window);
//
//            // Insert these samples into the `windowedSamples` array
//            windowedSamples[windowNum] = tempSamples;
//        }
//
//        // Return the windowed samples
//        return windowedSamples;
//    }

    // Private methods

    /**
     * Helper method to apply the window function to the audio samples.
     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
     * passed into the method.
     *
     * @param samples         Array containing audio samples.
     * @param numValidSamples Number of valid samples that can be windowed.
     * @param numChannels     Number of channels in the audio file.
     * @param abstractWindow          Window to apply.
     */
    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
    private static void applyWindow(float[] samples, int numValidSamples, int numChannels, AbstractWindow abstractWindow) {
        // Generate the window array
        double[] windowArray = abstractWindow.generateWindow(samples.length, false);

        // Apply window to samples
        for (int ch = 0, k, i; ch < numChannels; ch++) {
            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
                samples[i] = samples[i] * (float) windowArray[k];
            }
        }
    }

//    /**
//     * Helper method to apply the sine window function to the audio samples.<br>
//     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
//     * passed into the method.
//     *
//     * @param samples         Array containing audio samples.
//     * @param numValidSamples Number of valid samples that can be windowed.
//     * @param numChannels     Number of channels in the audio file.
//     * @see <a href="https://en.wikipedia.org/wiki/Window_function#Sine_window">This article</a>
//     * about the sine window function.
//     */
//    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
//    private static void sineWindow(float[] samples, int numValidSamples, int numChannels) {
//        int sampleLen = numValidSamples / numChannels;
//
//        for (int ch = 0, k, i; ch < numChannels; ch++) {
//            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
//                samples[i] *= Math.sin(Math.PI * k++ / (sampleLen - 1));
//            }
//        }
//    }
//
//    /**
//     * Helper method to apply the Hann window function to the audio samples.<br>
//     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
//     * passed into the method.
//     *
//     * @param samples         Array containing audio samples.
//     * @param numValidSamples Number of valid samples that can be windowed.
//     * @param numChannels     Number of channels in the audio file.
//     * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This article</a>
//     * about the Hann window function.
//     */
//    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
//    private static void hannWindow(float[] samples, int numValidSamples, int numChannels) {
//        for (int ch = 0, k, i; ch < numChannels; ch++) {
//            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
//                samples[i] = (float) (samples[i] * 0.5 * (1 - Math.cos(2.0 * Math.PI * k++ / samples.length)));
//            }
//        }
//    }
//
//    /**
//     * Helper method to apply the Hamming window function to the audio samples.<br>
//     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
//     * passed into the method.
//     *
//     * @param samples         Array containing audio samples.
//     * @param numValidSamples Number of valid samples that can be windowed.
//     * @param numChannels     Number of channels in the audio file.
//     * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This article</a>
//     * about the Hamming window function.
//     */
//    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
//    private static void hammingWindow(float[] samples, int numValidSamples, int numChannels) {
//        for (int ch = 0, k, i; ch < numChannels; ch++) {
//            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
//                samples[i] = (float) (samples[i] * (0.54 - 0.46 * Math.cos(2.0 * Math.PI * k++ / samples.length)));
//            }
//        }
//    }
}
