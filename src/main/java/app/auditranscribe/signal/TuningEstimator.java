/*
 * TuningEstimator.java
 * Description: Contains methods to estimate the tuning of an audio track.
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

import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.signal.representations.STFT;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods to estimate the tuning of an audio track.
 */
public final class TuningEstimator {
    // Constants
    final static int BINS_IN_HISTOGRAM = 100;  // Resolution of the tuning will be `1 / NUM_BINS_IN_HISTOGRAM`
    final static int BINS_PER_OCTAVE = 12;  // 1 per semitone

    final static int NUM_FFT = 2048;
    final static int HOP_LENGTH = NUM_FFT / 4;

    final static double FFT_MIN_FREQ = 150;
    final static double FFT_MAX_FREQ = 4000;

    private TuningEstimator() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Estimate the tuning of an audio time series or spectrogram input.
     *
     * @param x  Audio samples.
     * @param sr Sample rate of the audio.
     * @return Estimated tuning deviation in fractions of a bin.
     */
    public static double estimateTuning(double[] x, double sr) {
        return estimateTuning(x, sr, 0);
    }

    /**
     * Estimate the tuning of an audio time series or spectrogram input.
     *
     * @param x       Audio samples.
     * @param sr      Sample rate of the audio.
     * @param minFreq Minimum frequency value for a given frequency to be considered when computing
     *                the tuning.
     * @return Estimated tuning deviation in fractions of a bin.
     */
    public static double estimateTuning(double[] x, double sr, double minFreq) {
        // Get pitch-magnitude values
        Pair<Double[][], Double[][]> pitchAndMag = piptack(x, sr);
        Double[][] pitch = pitchAndMag.value0();
        Double[][] mag = pitchAndMag.value1();

        // Only count magnitude where frequency is above the minimum
        List<Double> tempRelevantPitches = new ArrayList<>();
        List<Double> tempRelevantMags = new ArrayList<>();
        for (int i = 0; i < pitch.length; i++) {
            for (int j = 0; j < pitch[i].length; j++) {
                if (pitch[i][j] > minFreq) {
                    tempRelevantPitches.add(pitch[i][j]);
                    tempRelevantMags.add(mag[i][j]);
                }
            }
        }

        // Determine threshold value
        double threshold;
        if (tempRelevantPitches.size() != 0) {
            threshold = StatisticsUtils.median(TypeConversionUtils.toDoubleArray(tempRelevantMags));
        } else {
            threshold = 0;
        }

        // Get the actually relevant pitches
        List<Double> relevantPitchesList = new ArrayList<>();
        for (int i = 0; i < tempRelevantPitches.size(); i++) {
            if (tempRelevantMags.get(i) >= threshold) relevantPitchesList.add(tempRelevantPitches.get(i));
        }
        double[] relevantPitches = TypeConversionUtils.toDoubleArray(relevantPitchesList);

        // Compute the residual relative to the number of bins
        double[] residuals = new double[relevantPitches.length];
        for (int i = 0; i < relevantPitches.length; i++) {
            double temp = BINS_PER_OCTAVE * UnitConversionUtils.hzToOctaves(relevantPitches[i]);
            double residual = temp - (int) temp;  // Not the most precise, but good enough for use

            // Are we on the wrong side of the semitone?
            // (A residual of 0.95 is more likely to be a deviation of -0.05 from the next tone up)
            residuals[i] = residual >= 0.5 ? residual - 1 : residual;
        }

        // Create histogram
        Pair<Integer[], Double[]> countsAndTuning = StatisticsUtils.histogram(
                residuals, -0.5, 0.5, BINS_IN_HISTOGRAM
        );
        Integer[] counts = countsAndTuning.value0();
        Double[] tuning = countsAndTuning.value1();

        // Return the histogram peak
        int peakCount = counts[0];
        int peakCountIndex = 0;

        for (int i = 1; i < counts.length; i++) {
            if (peakCount < counts[i]) {
                peakCount = counts[i];
                peakCountIndex = i;
            }
        }

        return tuning[peakCountIndex];
    }

    // Private methods

    /**
     * Pitch tracking on thresholded parabolically-interpolated STFT.
     *
     * @param x  Audio samples.
     * @param sr Sample rate of the audio.
     * @return A pair of real-valued matrices.<br>
     * The first matrix is the pitch matrix, which contains instantaneous frequencies.<br>
     * The second matrix contains the corresponding magnitudes.
     * @implNote This implementation uses the parabolic interpolation method described by
     * <a href="https://ccrma.stanford.edu/~jos/sasp/Sinusoidal_Peak_Interpolation.html">this
     * article</a> by the Center for Computer Research in Music and Acoustics (CCRMA),
     * Stanford University.
     */
    private static Pair<Double[][], Double[][]> piptack(double[] x, double sr) {
        // Get STFT magnitudes
        double[][] S = STFT.stftMags(x, NUM_FFT, HOP_LENGTH, SignalWindow.HANN_WINDOW);

        // Truncate to feasible region
        double maxFreq = Math.min(FFT_MAX_FREQ, sr / 2);

        // Get the frequency bins of the FFT
        double[] fftFreqs = FrequencyRangeGeneration.fftFreqBins(NUM_FFT, sr);

        // Perform parabolic interpolation
        Pair<Double[][], Double[][]> parabolicInterpPair = parabolicInterp(S);
        Double[][] avg = parabolicInterpPair.value0();
        Double[][] shift = parabolicInterpPair.value1();

        // Compute skew difference
        double[][] deltaSkew = new double[S.length][S[0].length];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                deltaSkew[i][j] = 0.5 * avg[i][j] * shift[i][j];
            }
        }

        // Pre-allocate output
        Double[][] pitches = new Double[S.length][S[0].length];
        Double[][] mags = new Double[S.length][S[0].length];

        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                pitches[i][j] = 0.;
                mags[i][j] = 0.;
            }
        }

        // Determine the reference value matrix
        double[] refVals = new double[S[0].length];

        for (int j = 0; j < S[0].length; j++) {
            double max = -1;
            for (double[] magnitude : S) {
                if (magnitude[j] > max) max = magnitude[j];
            }
            refVals[j] = 0.1 * max;  // 0.1 is the threshold value
        }

        // Generate masked local maxima of the spectrogram magnitudes
        boolean[][] maskedLocalMaxima = generateMaskedLocalMaxima(S, refVals);

        // Get relevant indices
        List<Pair<Integer, Integer>> indices = new ArrayList<>();
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                // Get the current FFT frequency
                double currFFTFreq = fftFreqs[i];
                if ((FFT_MIN_FREQ <= currFFTFreq) && (currFFTFreq < maxFreq) && (maskedLocalMaxima[i][j])) {
                    indices.add(new Pair<>(i, j));
                }
            }
        }

        // Store pitch and magnitudes
        double scaleFactor = sr / NUM_FFT;  // So we don't have to recompute this multiple times later

        for (Pair<Integer, Integer> pair : indices) {
            // Get the i and j index
            int i = pair.value0();
            int j = pair.value1();

            // Set pitch and magnitude
            pitches[i][j] = (i + shift[i][j]) * scaleFactor;
            mags[i][j] = S[i][j] + deltaSkew[i][j];
        }

        // Return as a pair
        return new Pair<>(pitches, mags);
    }

    /**
     * Perform parabolic interpolation on the STFT magnitudes.
     *
     * @param S STFT magnitudes matrix.
     * @return A pair of real-valued matrices. First value is the 'average' matrix. The second value
     * is the 'shift' matrix.
     */
    private static Pair<Double[][], Double[][]> parabolicInterp(double[][] S) {
        // Perform parabolic interpolation
        double[][] avg = new double[S.length][S[0].length];
        avg[0] = new double[S[0].length];
        for (int i = 1; i < S.length - 1; i++) {
            for (int j = 0; j < S[0].length; j++) {
                avg[i][j] = 0.5 * (S[i + 1][j] - S[i - 1][j]);
            }
        }
        avg[S.length - 1] = new double[S[0].length];

        double[][] shift = new double[S.length][S[0].length];
        shift[0] = new double[S[0].length];
        for (int i = 1; i < S.length - 1; i++) {
            for (int j = 0; j < S[0].length; j++) {
                double shiftVal = 2 * S[i][j] - S[i - 1][j] - S[i + 1][j];
                shift[i][j] = shiftVal != 0 ? avg[i][j] / shiftVal : avg[i][j];  // Suppress divide-by-zeros
            }
        }
        shift[S.length - 1] = new double[S[0].length];

        // Convert into needed format
        Double[][] avgOut = new Double[S.length][S[0].length];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                avgOut[i][j] = avg[i][j];
            }
        }

        Double[][] shiftOut = new Double[S.length][S[0].length];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                shiftOut[i][j] = shift[i][j];
            }
        }

        // Return the converted arrays
        return new Pair<>(avgOut, shiftOut);
    }

    /**
     * Generates a boolean mask of the local maxima in the STFT magnitudes matrix.
     *
     * @param S         STFT magnitudes matrix.
     * @param refValues Reference values for each row.
     * @return A boolean mask determining whether the element is a local maxima.
     */
    private static boolean[][] generateMaskedLocalMaxima(double[][] S, double[] refValues) {
        double[][] transposedS = MatrixUtils.transpose(S);
        boolean[][] maskedLocalMaxTransposed = new boolean[S[0].length][S.length];

        for (int i = 0; i < S[0].length; i++) {
            double[] relevantRow = new double[S.length];
            for (int j = 0; j < S.length; j++) {
                relevantRow[j] = transposedS[i][j] > refValues[i] ? transposedS[i][j] : 0;
            }
            maskedLocalMaxTransposed[i] = ArrayUtils.findLocalMaxima(relevantRow);
        }

        return MatrixUtils.transpose(maskedLocalMaxTransposed);
    }
}
