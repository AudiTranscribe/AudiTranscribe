/*
 * VQT.java
 * Description: Class that implements the Variable Q-Transform (VQT) algorithm.
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

import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Filter;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.exceptions.ValueException;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.spectrogram.SpectralHelpers;
import site.overwrite.auditranscribe.spectrogram.Wavelet;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.MathUtils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Class that implements the Variable Q-Transform (VQT) algorithm.
 *
 * @implNote Adapted largely from
 * <a href="https://librosa.org/doc/main/_modules/librosa/core/constantq.html#vqt">Librosa's
 * Implementation</a> of the VQT.
 * @see <a href="https://core.ac.uk/download/pdf/144846462.pdf">This paper</a> by Schoerkhuber,
 * Christian, and Anssi Klapuri. "Constant-Q transform toolbox for music processing." 7th Sound and
 * Music Computing Conference, Barcelona, Spain. 2010.
 */
public final class VQT extends ClassWithLogging {
    // Constants
    static final double BW_FASTEST = 0.85;

    private VQT() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Variable-Q Transform function.
     *
     * @param y              Audio time series.
     * @param sr             Sample rate of the audio.
     * @param hopLength      Number of samples between successive VQT columns.
     * @param fmin           Minimum frequency.
     * @param numBins        Number of frequency bins, starting at <code>fmin</code>.
     * @param binsPerOctave  Number of bins per octave.
     * @param tuning         Tuning offset in fractions of a bin.<br>
     *                       If <code>tuning = Double.NaN</code>, the <code>tuning</code> value will
     *                       be automatically estimated from the signal.<br>
     *                       The minimum frequency of the resulting Q-transform will be modified to
     *                       <code>fmin * Math.pow(2., tuning / binsPerOctave)</code>.
     * @param gamma          Bandwidth offset for determining filter lengths. <code>gamma = 0</code>
     *                       means that the gamma value will be derived automatically.
     * @param windowFunction Window function to apply to the basis filters.
     * @param task           The <code>CustomTask</code> object that is handling the generation.
     *                       Pass in <code>null</code> if no such task is being used.
     * @return Variable-Q value each frequency at each time.
     * @throws ValueException If: <ul>
     *                        <li>
     *                        The number of frequency bins is negative or zero.
     *                        </li>
     *                        <li>
     *                        The number of bins per octave is negative or zero.
     *                        </li>
     *                        <li>
     *                        The number of bins is not a multiple of the number of bins per octave.
     *                        </li>
     *                        <li>
     *                        The current number of frequency bins results in the highest frequency
     *                        exceeding the Nyquist frequency.
     *                        </li>
     *                        </ul>
     */
    public static Complex[][] vqt(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave, double tuning,
            double gamma, WindowFunction windowFunction, CustomTask<?> task
    ) {
        return vqt(y, sr, hopLength, fmin, numBins, binsPerOctave, tuning, gamma, windowFunction, task, false);
    }

    /**
     * Variable-Q Transform function.
     *
     * @param y              Audio time series.
     * @param sr             Sample rate of the audio.
     * @param hopLength      Number of samples between successive Q-Transform columns.
     * @param fmin           Minimum frequency.
     * @param numBins        Number of frequency bins, starting at <code>fmin</code>.
     * @param binsPerOctave  Number of bins per octave.
     * @param tuning         Tuning offset in fractions of a bin.<br>
     *                       If <code>tuning = Double.NaN</code>, the <code>tuning</code> value will
     *                       be automatically estimated from the signal.<br>
     *                       The minimum frequency of the resulting Q-transform will be modified to
     *                       <code>fmin * Math.pow(2., tuning / binsPerOctave)</code>.
     * @param gamma          Bandwidth offset for determining filter lengths. If <code>isCQT</code>is
     *                       true and <code>gamma = 0</code>, produces the Constant-Q Transform
     *                       (CQT). Otherwise, <code>gamma = 0</code> means that the gamma value will
     *                       be derived automatically.
     * @param windowFunction Window function to apply to the basis filters.
     * @param task           The <code>CustomTask</code> object that is handling the generation.
     *                       Pass in <code>null</code> if no such task is being used.
     * @param isCQT          Whether this is a Constant-Q Transform or not.
     * @return Variable-Q value each frequency at each time.
     * @throws ValueException If: <ul>
     *                        <li>
     *                        The number of frequency bins is negative or zero.
     *                        </li>
     *                        <li>
     *                        The number of bins per octave is negative or zero.
     *                        </li>
     *                        <li>
     *                        The number of bins is not a multiple of the number of bins per octave.
     *                        </li>
     *                        <li>
     *                        The current number of frequency bins results in the highest frequency
     *                        exceeding the Nyquist frequency.
     *                        </li>
     *                        </ul>
     */
    public static Complex[][] vqt(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave, double tuning,
            double gamma, WindowFunction windowFunction, CustomTask<?> task, boolean isCQT
    ) {
        // Validate parameters
        if (numBins <= 0) {
            throw new ValueException("Number of frequency bins cannot be negative or zero.");
        }

        if (binsPerOctave <= 0) {
            throw new ValueException("Number of bins per octave cannot be negative or zero");
        }

        if (numBins % binsPerOctave != 0) {
            throw new ValueException("Number of bins is not a multiple of the number of bins per octave.");
        }

        // Estimate tuning value from signal if needed
        if (Double.isNaN(tuning)) {
            tuning = SpectralHelpers.estimateTuning(y, sr);
        }

        // Apply tuning correction
        fmin = fmin * Math.pow(2., tuning / binsPerOctave);

        // Compute number of octaves that we are processing
        int numOctaves = getNumOctaves(numBins, binsPerOctave);

        // Get the VQT frequency bins
        double[] freqs = FrequencyBins.getQTFreqBins(numBins, binsPerOctave, fmin);

        // Get the frequencies for the top octave
        double[] freqsTop = new double[binsPerOctave];
        System.arraycopy(freqs, numBins - binsPerOctave, freqsTop, 0, binsPerOctave);

        // Calculate the relative difference in frequency between any two consecutive bands, alpha
        double alpha = SpectralHelpers.computeAlpha(binsPerOctave);

        // Compute filter cutoff
        Pair<double[], Double> waveletLengthsResponse = Wavelet.computeWaveletLengths(
                freqs, sr, windowFunction, 1., isCQT, gamma, alpha
        );
        double filterCutoff = waveletLengthsResponse.value1();

        // Determine required resampling quality
        double nyquistFrequency = sr / 2;

        if (filterCutoff > nyquistFrequency) {
            throw new InvalidParameterException(
                    "Wavelet basis with max frequency " + freqsTop[binsPerOctave - 1] +
                            " would exceed the Nyquist frequency " + nyquistFrequency +
                            ". Try reducing the number of frequency bins."
            );
        }

        // Resample audio
        Filter filter;
        if (filterCutoff < BW_FASTEST * nyquistFrequency) {
            filter = Filter.KAISER_FAST;
        } else {
            filter = Filter.KAISER_BEST;
        }

        Triple<double[], Double, Integer> earlyDownsampleResponse = earlyDownsample(
                y, sr, hopLength, filter, numOctaves, nyquistFrequency, filterCutoff
        );
        y = earlyDownsampleResponse.value0();
        sr = earlyDownsampleResponse.value1();
        hopLength = earlyDownsampleResponse.value2();

        // Define VQT response array
        List<Complex[][]> vqtResponses = new ArrayList<>();

        // Handle first octave specially if the filter type is NOT `KAISER_FAST`
        int startingOctave = 0;

        if (filter != Filter.KAISER_FAST) {
            // Get the frequencies of the top octave
            double[] freqsOct = new double[binsPerOctave];
            System.arraycopy(freqs, numBins - binsPerOctave, freqsOct, 0, binsPerOctave);

            // Do the top octave before resampling to allow for fast resampling
            Triple<Complex[][], Integer, double[]> fftFilterResponse = vqtFilterFFT(
                    sr, freqsOct, windowFunction, isCQT, gamma, alpha
            );
            Complex[][] fftBasis = fftFilterResponse.value0();
            int numFFT = fftFilterResponse.value1();

            // Compute the VQT filter response and append it to the list
            vqtResponses.add(vqtResponse(y, numFFT, hopLength, fftBasis));

            // Update values
            startingOctave = 1;
            filter = Filter.KAISER_FAST;

            // Update task progress
            if (task != null) task.updateProgress(1, numOctaves);
        }

        // Iterate down the octaves
        double[] myY = y;
        double mySR = sr;
        int myHopLength = hopLength;

        for (int octave = startingOctave; octave < numOctaves; octave++) {  // Starts from the HIGHEST frequencies
            // Get the frequencies of the current octave
            double[] freqsOct = new double[binsPerOctave];
            System.arraycopy(freqs, numBins - binsPerOctave * (octave + 1), freqsOct, 0, binsPerOctave);

            // Get the FFT basis and the `numFFT` for this octave
            Triple<Complex[][], Integer, double[]> fftFilterResponse = vqtFilterFFT(
                    mySR, freqsOct, windowFunction, isCQT, gamma, alpha
            );
            Complex[][] fftBasis = fftFilterResponse.value0();
            int numFFT = fftFilterResponse.value1();

            // Re-scale the filters to compensate for downsampling
            for (int i = 0; i < fftBasis.length; i++) {
                for (int j = 0; j < fftBasis[0].length; j++) {
                    fftBasis[i][j] = fftBasis[i][j].scale(Math.sqrt(sr / mySR));
                }
            }

            // Compute the VQT filter response and append to the list
            Complex[][] response = vqtResponse(myY, numFFT, myHopLength, fftBasis);
            vqtResponses.add(response);

            // Update variables
            if (myHopLength % 2 == 0) {
                myHopLength /= 2;
                mySR /= 2.;
                myY = Audio.resample(myY, 2, 1, filter, true);
            }

            // Update task progress
            if (task != null) task.updateProgress(octave + 1, numOctaves);
        }

        // Trim and stack the VQT responses
        Complex[][] V = trimAndStack(vqtResponses, numBins);

        // Get the maximum column size
        int maxCol = V[0].length;

        // Recompute lengths here because early downsampling may have changed our sampling rate
        waveletLengthsResponse = Wavelet.computeWaveletLengths(
                freqs, sr, windowFunction, 1., isCQT, gamma, alpha
        );
        double[] lengths = waveletLengthsResponse.value0();

        // Scale `V` back to normal
        for (int i = 0; i < numBins; i++) {
            double scaleFactor = Math.sqrt(lengths[i]);

            for (int j = 0; j < maxCol; j++) {
                V[i][j] = V[i][j].divides(scaleFactor);
            }
        }

        // Return VQT matrix
        log(
                Level.FINE, "VQT Matrix generated; has shape (" + V.length + ", " + V[0].length + ")", VQT.class.getName()
        );
        return V;
    }

    // Private Methods

    /**
     * Gets the number of octaves required to store <code>numBins</code>.
     *
     * @param numBins       Number of Variable-Q bins.
     * @param binsPerOctave Number of bins per octave.
     * @return Number of octaves required to store <code>numBins</code>.
     */
    private static int getNumOctaves(int numBins, int binsPerOctave) {
        return (int) Math.ceil((double) numBins / binsPerOctave);
    }

    /**
     * Perform early downsampling on the audio samples.
     *
     * @param y            Audio time series.
     * @param sr           Sample rate of the audio.
     * @param hopLength    Number of samples between successive VQT columns.
     * @param filter       Filter window.
     * @param numOctaves   Number of octaves to consider
     * @param nyquist      Nyquist frequency value.
     * @param filterCutoff Highest frequency value before cutoff begins.
     * @return Three values. First is the downsampled audio time series. Second is the new sample
     * rate. Third is the new hop length.
     * @throws ValueException If The input signal length is too short for a
     *                        <code>numOctaves</code>-octave VQT.
     */

    private static Triple<double[], Double, Integer> earlyDownsample(
            double[] y, double sr, int hopLength, Filter filter, int numOctaves, double nyquist, double filterCutoff
    ) {
        // Compute the number of early downsampling operations
        int downsampleCount1 = Math.max(0, (int) Math.ceil(MathUtils.log2(BW_FASTEST * nyquist / filterCutoff)) - 2);
        int downsampleCount2 = Math.max(0, MathUtils.numTwoFactors(hopLength) - numOctaves + 1);

        int downsampleCount = Math.min(downsampleCount1, downsampleCount2);

        // Actually perform the downsampling
        double[] yNew;

        if (filter == Filter.KAISER_FAST) {
            if (downsampleCount > 0) {
                // Compute how much to downsample by
                int downsampleFactor = (int) Math.pow(2, downsampleCount);

                // Check if the signal can actually be downsampled
                if (y.length < downsampleFactor) {
                    throw new ValueException(
                            "Input signal length of " + y.length + " is too short for " + numOctaves + "-octave VQT"
                    );
                }

                // Downsample hop length and the sample rate
                hopLength /= downsampleFactor;
                double newSr = sr / downsampleFactor;

                // Downsample audio sample
                yNew = Audio.resample(y, sr, newSr, filter, true);
                sr = newSr;
            } else {
                yNew = y;
            }
        } else {
            // Not possible for `downsampleCount > 0` due to how `downsampleCount1` is calculated
            yNew = y;
        }

        // Return needed values
        return new Triple<>(yNew, sr, hopLength);
    }

    /**
     * Generate the frequency domain variable-Q filter basis.
     *
     * @param sr             Sample rate.
     * @param freqs          Centre frequencies of the frequency bins.
     * @param windowFunction Window function to use.
     * @param isCQT          Whether this is a CQT or not.
     * @param gamma          Gamma value.
     * @param alpha          Alpha value.
     * @return Triplet of values. First value is a 2D array of complex coefficients, representing
     * the FFT basis. Second value is an integer, representing the number of FFT frequency bins.
     * Third value is a double array, representing the filters' lengths.
     */
    private static Triple<Complex[][], Integer, double[]> vqtFilterFFT(
            double sr, double[] freqs, WindowFunction windowFunction, boolean isCQT, double gamma, double alpha
    ) {
        // Get the frequency and lengths of the wavelet basis
        Pair<Complex[][], double[]> waveletBasisResponse = Wavelet.computeWaveletBasis(
                freqs, sr, windowFunction, 1, true, 1, isCQT, gamma, alpha
        );
        Complex[][] basis = waveletBasisResponse.value0();
        double[] lengths = waveletBasisResponse.value1();

        // Number of FFT bins is the second element of the shape of the basis
        int fftWindowLength = basis.length;
        int numFFT = basis[0].length;

        // Re-normalize bases with respect to the FFT window length
        for (int i = 0; i < fftWindowLength; i++) {
            double normalisationFactor = lengths[i] / numFFT;

            for (int j = 0; j < numFFT; j++) {
                basis[i][j] = basis[i][j].scale(normalisationFactor);
            }
        }

        // FFT and retain only the non-negative frequencies
        Complex[][] fftBasis = new Complex[fftWindowLength][numFFT];
        for (int i = 0; i < fftWindowLength; i++) {
            fftBasis[i] = FFT.rfft(basis[i]);
        }

        // Return required data
        return new Triple<>(fftBasis, numFFT, lengths);
    }

    /**
     * Compute the VQT filter response with a target STFT hop.
     *
     * @param y         Array of samples.
     * @param numFFT    Number of bins to use in the FFT.
     * @param hopLength Step size between each hop.
     * @param fftBasis  Basis to use for the FFT.
     * @return VQT filter response.
     */
    private static Complex[][] vqtResponse(double[] y, int numFFT, int hopLength, Complex[][] fftBasis) {
        // Get the STFT matrix
        Complex[][] D = STFT.stft(y, numFFT, hopLength, WindowFunction.ONES_WINDOW);

        // Matrix multiply `fftBasis` with `D` and return the result
        return ArrayUtils.matmul(fftBasis, D);
    }

    /**
     * Helper function to trim and stack a collection of VQT responses.
     *
     * @param vqtResponses Collection of VQT responses.
     * @param numBins      Number of bins for the VQT.
     * @return Trimmed and stacked array of VQT responses.
     */
    private static Complex[][] trimAndStack(List<Complex[][]> vqtResponses, int numBins) {
        // Get the maximum permitted number columns
        // (We take the minimum to avoid weird index errors later when processing magnitudes)
        int maxPermittedNumCol = Integer.MAX_VALUE;

        for (Complex[][] vqtResponse : vqtResponses) {
            maxPermittedNumCol = Math.min(vqtResponse[0].length, maxPermittedNumCol);
        }

        // Generate output VQT matrix
        Complex[][] vqtOut = new Complex[numBins][maxPermittedNumCol];

        // Copy per-octave data into output array
        int end = numBins;
        for (Complex[][] response : vqtResponses) {  // Note: FIRST element represents the HIGHEST FREQUENCY bin
            // By default, take the whole octave
            int numOctaves = response.length;

            // It is guaranteed that `end >= numOctaves` since `end = numBins` is a multiple of `numOctaves` and the
            // number of VQT responses is `numOctaves / numBins`, so we just copy until `numOctaves`
            for (int i = 0; i < numOctaves; i++) {
                System.arraycopy(
                        response[i],
                        0,
                        vqtOut[end - numOctaves + i],
                        0,
                        maxPermittedNumCol
                );
            }

            end -= numOctaves;
        }

        // Return the stacked VQT matrix
        return vqtOut;
    }
}
