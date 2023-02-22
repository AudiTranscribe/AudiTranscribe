/*
 * QTransform.java
 * Description: Class that implements Q-Transform algorithms.
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

package app.auditranscribe.signal.representations;

import app.auditranscribe.audio.AudioHelpers;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.signal.FrequencyRangeGeneration;
import app.auditranscribe.signal.SignalHelpers;
import app.auditranscribe.signal.TuningEstimator;
import app.auditranscribe.signal.Wavelet;
import app.auditranscribe.signal.resampling_filters.Filter;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.MatrixUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Class that implements Q-Transform algorithms.
 *
 * @see <a href="https://core.ac.uk/download/pdf/144846462.pdf">This paper</a> by Schoerkhuber,
 * Christian, and Anssi Klapuri. "Constant-Q transform toolbox for music processing." 7th Sound and
 * Music Computing Conference, Barcelona, Spain. 2010.
 */

public final class QTransform extends LoggableClass {
    // Constants
    static final double BW_FASTEST = 0.85;

    private QTransform() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Constant-Q Transform function.
     *
     * @param y              Audio time series.
     * @param sr             Sample rate of the audio.
     * @param hopLength      Number of samples between successive CQT columns.
     * @param fmin           Minimum frequency.
     * @param numBins        Number of frequency bins, starting at <code>fmin</code>.
     * @param binsPerOctave  Number of bins per octave.
     * @param tuning         Tuning offset in fractions of a bin.<br>
     *                       If <code>tuning = Double.NaN</code>, the <code>tuning</code> value will
     *                       be automatically estimated from the signal.<br>
     *                       The minimum frequency of the resulting CQT will be modified to
     *                       <code>fmin * Math.pow(2., tuning / binsPerOctave)</code>.
     * @param windowFunction Signal window function to apply to the basis filters.
     * @param task           The <code>CustomTask</code> object that is handling the generation.
     *                       Pass in <code>null</code> if no such task is being used.
     * @return Constant-Q value each frequency at each time.
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
     * @implNote Adapted largely from
     * <a href="http://librosa.org/doc/main/_modules/librosa/core/constantq.html#cqt">Librosa's
     * Implementation</a> of the CQT.
     */
    public static Complex[][] cqt(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave, double tuning,
            SignalWindow windowFunction, CustomTask<?> task
    ) {
        // CQT is the special case of VQT where `gamma` is zero
        return vqtHelper(
                y, sr, hopLength, fmin, numBins, binsPerOctave, tuning, 0, windowFunction, task, true
        );
    }

    /**
     * Variable-Q Transform function.
     *
     * @param y              Audio time series.
     * @param sr             Sample rate of the audio.
     * @param hopLength      Number of samples between successive QTransform columns.
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
     * @param windowFunction Signal window function to apply to the basis filters.
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
     * @implNote Adapted largely from
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/constantq.html#vqt">Librosa's
     * Implementation</a> of the VQT.
     */
    public static Complex[][] vqt(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave, double tuning,
            double gamma, SignalWindow windowFunction, CustomTask<?> task
    ) {
        return vqtHelper(
                y, sr, hopLength, fmin, numBins, binsPerOctave, tuning, gamma, windowFunction, task, false
        );
    }

    // Private methods

    /**
     * Helper function that handles the Variable-Q Transform function.
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
     * @param windowFunction Signal window function to apply to the basis filters.
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
     * @implNote Adapted largely from
     * <a href="https://librosa.org/doc/main/_modules/librosa/core/constantq.html#vqt">Librosa's
     * Implementation</a> of the VQT.
     */
    private static Complex[][] vqtHelper(
            double[] y, double sr, int hopLength, double fmin, int numBins, int binsPerOctave, double tuning,
            double gamma, SignalWindow windowFunction, CustomTask<?> task, boolean isCQT
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
            tuning = TuningEstimator.estimateTuning(y, sr);
        }

        // Apply tuning correction
        fmin = fmin * Math.pow(2., tuning / binsPerOctave);

        // Compute number of octaves that we are processing
        int numOctaves = getNumOctaves(numBins, binsPerOctave);

        // Get the QTransform frequency bins
        double[] freqs = FrequencyRangeGeneration.qTransformFreqBins(numBins, binsPerOctave, fmin);

        // Get the frequencies for the top octave
        double[] freqsTop = new double[binsPerOctave];
        System.arraycopy(freqs, numBins - binsPerOctave, freqsTop, 0, binsPerOctave);

        // Calculate the relative difference in frequency between any two consecutive bands, alpha
        double alpha = SignalHelpers.computeAlpha(binsPerOctave);

        // Compute filter cutoff
        Pair<double[], Double> waveletLengthsResponse = Wavelet.computeWaveletLengths(
                freqs, sr, windowFunction, 1., isCQT, gamma, alpha
        );
        double filterCutoff = waveletLengthsResponse.value1();

        // Determine required resampling quality
        double nyquistFrequency = sr / 2;

        if (filterCutoff > nyquistFrequency) {
            throw new ValueException(
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

        // Define QTransform response array
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

            // Compute the QTransform filter response and append it to the list
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
                    fftBasis[i][j] = fftBasis[i][j].times(Math.sqrt(sr / mySR));
                }
            }

            // Compute the QTransform filter response and append to the list
            Complex[][] response = vqtResponse(myY, numFFT, myHopLength, fftBasis);
            vqtResponses.add(response);

            // Update variables
            if (myHopLength % 2 == 0) {
                myHopLength /= 2;
                mySR /= 2.;
                myY = AudioHelpers.resample(myY, 2, 1, filter, true);
            }

            // Update task progress
            if (task != null) task.updateProgress(octave + 1, numOctaves);
        }

        // Trim and stack the QTransform responses
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

        // Return QTransform matrix
        log(
                Level.FINE,
                "QTransform Matrix generated; has shape (" + V.length + ", " + V[0].length + ")",
                QTransform.class.getName()
        );
        return V;
    }

    /**
     * Gets the number of octaves required to store <code>numBins</code>.
     *
     * @param numBins       Number of Variable-Q bins.
     * @param binsPerOctave Number of bins per octave.
     * @return Number of octaves required to store <code>numBins</code>.
     */
    private static int getNumOctaves(int numBins, int binsPerOctave) {
        return MathUtils.ceilDiv(numBins, binsPerOctave);
    }

    /**
     * Perform early downsampling on the audio samples.
     *
     * @param y            Audio time series.
     * @param sr           Sample rate of the audio.
     * @param hopLength    Number of samples between successive QTransform columns.
     * @param filter       Filter window.
     * @param numOctaves   Number of octaves to consider
     * @param nyquist      Nyquist frequency value.
     * @param filterCutoff Highest frequency value before cutoff begins.
     * @return Three values. First is the downsampled audio time series. Second is the new sample
     * rate. Third is the new hop length.
     * @throws ValueException If The input signal length is too short for a
     *                        <code>numOctaves</code>-octave QTransform.
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
                            "Input signal length of " + y.length + " is too short for " + numOctaves + "-octave QTransform"
                    );
                }

                // Downsample hop length and the sample rate
                hopLength /= downsampleFactor;
                double newSr = sr / downsampleFactor;

                // Downsample audio sample
                yNew = AudioHelpers.resample(y, sr, newSr, filter, true);
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
     * Generate the variable-Q filter basis for the FFT.
     *
     * @param sr             Sample rate.
     * @param freqs          Centre frequencies of the frequency bins.
     * @param windowFunction Signal window function to use for resampling.
     * @param isCQT          Whether this is a CQT or not.
     * @param gamma          Gamma value.
     * @param alpha          Alpha value.
     * @return Triplet of values. First value is a 2D array of complex coefficients, representing
     * the FFT basis. Second value is an integer, representing the number of FFT frequency bins.
     * Third value is an array of doubles, representing the filters' lengths.
     */
    private static Triple<Complex[][], Integer, double[]> vqtFilterFFT(
            double sr, double[] freqs, SignalWindow windowFunction, boolean isCQT, double gamma, double alpha
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
                basis[i][j] = basis[i][j].times(normalisationFactor);
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
     * Compute the QTransform filter response with a target STFT hop.
     *
     * @param y         Array of samples.
     * @param numFFT    Number of bins to use in the FFT.
     * @param hopLength Step size between each hop.
     * @param fftBasis  Complex basis to use for the FFT.
     * @return QTransform filter response.
     */
    private static Complex[][] vqtResponse(double[] y, int numFFT, int hopLength, Complex[][] fftBasis) {
        // Get the STFT matrix
        Complex[][] D = STFT.stft(y, numFFT, hopLength, SignalWindow.ONES_WINDOW);

        // Matrix multiply `fftBasis` with `D` and return the result
        return MatrixUtils.matmul(fftBasis, D);
    }

    /**
     * Helper function to trim and stack a collection of QTransform responses.
     *
     * @param vqtResponses Collection of QTransform responses.
     * @param numBins      Number of bins for the QTransform.
     * @return Trimmed and stacked array of QTransform responses.
     */
    private static Complex[][] trimAndStack(List<Complex[][]> vqtResponses, int numBins) {
        // Get the maximum permitted number columns
        // (We take the minimum to avoid weird index errors later when processing magnitudes)
        int maxPermittedNumCol = Integer.MAX_VALUE;

        for (Complex[][] vqtResponse : vqtResponses) {
            maxPermittedNumCol = Math.min(vqtResponse[0].length, maxPermittedNumCol);
        }

        // Generate output QTransform matrix
        Complex[][] vqtOut = new Complex[numBins][maxPermittedNumCol];

        // Copy per-octave data into output array
        int end = numBins;
        for (Complex[][] response : vqtResponses) {  // Note: FIRST element represents the HIGHEST FREQUENCY bin
            // By default, take the whole octave
            int numOctaves = response.length;

            // It is guaranteed that `end >= numOctaves` since `end = numBins` is a multiple of `numOctaves` and the
            // number of QTransform responses is `numOctaves / numBins`, so we just copy until `numOctaves`
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

        // Return the stacked QTransform matrix
        return vqtOut;
    }
}
