/*
 * SpectralHelpers.java
 * Description: Helper methods for the spectral representation functions.
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

package site.overwrite.auditranscribe.spectrogram;

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.misc.tuples.Pair;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.FrequencyBins;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.STFT;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.StatisticalUtils;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for the spectral representation functions.
 */
public final class SpectralHelpers {
    private SpectralHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Computes the alpha coefficient.
     *
     * @param binsPerOctave Number of frequency bins per octave.
     * @return The alpha coefficient.
     * @implNote Implementation of this method is from the paper by Glasberg, Brian R., and Brian
     * CJ Moore. "Derivation of auditory filter shapes from notched-noise data". Hearing research
     * 47.1-2 (1990): 103-138.
     */
    public static double computeAlpha(double binsPerOctave) {
        double r = Math.pow(2, 1 / binsPerOctave);
        return (Math.pow(r, 2) - 1) / (Math.pow(r, 2) + 1);
    }

    /**
     * Estimate the tuning of an audio time series or spectrogram input.
     *
     * @param x  Audio signal.
     * @param sr Audio sampling rate of <code>x</code>.
     * @return Estimated tuning deviation in fractions of a bin.
     */
    public static double estimateTuning(double[] x, double sr) {
        // Constants
        double resolution = 0.01;
        int binsPerOctave = 12;

        // Get pitch-magnitude values
        Pair<Double[][], Double[][]> pitchAndMag = piptack(x, sr, 150, 4000);
        Double[][] pitch = pitchAndMag.value0();
        Double[][] mag = pitchAndMag.value1();

        // Only count magnitude where frequency is > 0
        List<Double> tempRelevantPitches = new ArrayList<>();
        List<Double> tempRelevantMags = new ArrayList<>();
        for (int i = 0; i < pitch.length; i++) {
            for (int j = 0; j < pitch[i].length; j++) {
                if (pitch[i][j] > 0) {
                    tempRelevantPitches.add(pitch[i][j]);
                    tempRelevantMags.add(mag[i][j]);
                }
            }
        }

        // Determine threshold value
        double threshold;
        if (tempRelevantPitches.size() != 0) {
            threshold = StatisticalUtils.median(
                    TypeConversionUtils.toDoubleArray(
                            TypeConversionUtils.toDoubleArray(tempRelevantMags)
                    )
            );
        } else {
            threshold = 0;
        }

        // Get the actually relevant pitches
        List<Double> relevantPitchesList = new ArrayList<>();
        for (int i = 0; i < tempRelevantPitches.size(); i++) {
            if (tempRelevantMags.get(i) >= threshold) relevantPitchesList.add(tempRelevantPitches.get(i));
        }
        double[] relevantPitches = TypeConversionUtils.toDoubleArray(
                TypeConversionUtils.toDoubleArray(relevantPitchesList)
        );

        // Compute the residual relative to the number of bins
        double[] residuals = new double[relevantPitches.length];
        for (int i = 0; i < relevantPitches.length; i++) {
            double temp = binsPerOctave * UnitConversionUtils.hzToOctaves(relevantPitches[i]);
            double residual = temp - (int) temp;  // Not very precise, but good enough

            // Are we on the wrong side of the semitone?
            // (A residual of 0.95 is more likely to be a deviation of -0.05 from the next tone up)
            residuals[i] = residual >= 0.5 ? residual - 1 : residual;
        }

        // Create histogram
        Pair<Integer[], Double[]> countsAndTuning = ArrayUtils.histogram(
                residuals, -0.5, 0.5, (int) Math.ceil(1 / resolution)
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
     * @param x    Audio signal.
     * @param sr   Audio sampling rate of the audio signal.
     * @param fmin Lower frequency cutoff.
     * @param fmax Upper frequency cutoff.
     * @return A pair of matrices.<br>
     * The first matrix is the pitch matrix, which contains instantaneous frequencies.<br>
     * The second matrix contains the corresponding magnitudes.
     * @implNote This implementation uses the parabolic interpolation method described by
     * <a href="https://ccrma.stanford.edu/~jos/sasp/Sinusoidal_Peak_Interpolation.html">this
     * article</a> by Center for Computer Research in Music and Acoustics (CCRMA),
     * Stanford University.
     */
    private static Pair<Double[][], Double[][]> piptack(double[] x, double sr, double fmin, double fmax) {
        // Constants
        int numFFT = 2048;
        int hopLength = numFFT / 4;

        // Generate the STFT spectrogram
        Complex[][] stft = STFT.stft(x, numFFT, hopLength, WindowFunction.HANN_WINDOW);

        // Obtain only the magnitudes
        double[][] S = new double[stft.length][];

        for (int i = 0; i < stft.length; i++) {
            S[i] = new double[stft[i].length];
            for (int j = 0; j < stft[i].length; j++) {
                S[i][j] = stft[i][j].abs();
            }
        }

        // Truncate to feasible region
        fmin = Math.max(fmin, 0);
        fmax = Math.min(fmax, sr / 2);

        // Get the frequency bins of the FFT
        double[] fftFreqs = FrequencyBins.getFFTFreqBins(numFFT, sr);

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

        // Compute skew difference
        double[][] dskew = new double[S.length][S[0].length];
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                dskew[i][j] = 0.5 * avg[i][j] * shift[i][j];
            }
        }

        // Pre-allocate output
        Double[][] pitches = new Double[S.length][S[0].length];
        Double[][] mags = new Double[S.length][S[0].length];

        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                pitches[i][j] = 0d;
                mags[i][j] = 0d;
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
        double[][] transposedS = ArrayUtils.transpose(S);
        boolean[][] maskedLocalMaxTransposed = new boolean[S[0].length][S.length];

        for (int i = 0; i < S[0].length; i++) {
            // Generate the masked `S` array
            double[] relevantRow = new double[S.length];
            for (int j = 0; j < S.length; j++) {
                relevantRow[j] = transposedS[i][j] > refVals[i] ? transposedS[i][j] : 0;
            }
            maskedLocalMaxTransposed[i] = ArrayUtils.localMaximum(relevantRow);
        }

        boolean[][] maskedLocalMax = ArrayUtils.transpose(maskedLocalMaxTransposed);

        // Get relevant indices
        List<Integer> idxI = new ArrayList<>();
        List<Integer> idxJ = new ArrayList<>();
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[0].length; j++) {
                // Get the current FFT frequency
                double currFFTFreq = fftFreqs[i];
                if ((fmin <= currFFTFreq) && (currFFTFreq < fmax) && (maskedLocalMax[i][j])) {
                    idxI.add(i);
                    idxJ.add(j);
                }
            }
        }

        // Store pitch and magnitudes
        for (Integer i : idxI) {
            for (Integer j : idxJ) {
                pitches[i][j] = (i + shift[i][j]) * (sr / numFFT);
                mags[i][j] = S[i][j] + dskew[i][j];
            }
        }

        // Return as a pair
        return new Pair<>(pitches, mags);
    }
}