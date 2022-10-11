/*
 * ChromaCQT.java
 * Description: Class that implements Constant-Q Chromagram methods.
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

import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.generic.exceptions.ValueException;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

/**
 * Class that implements Constant-Q Chromagram methods.
 *
 * @implNote Adapted largely from
 * <a href="https://librosa.org/doc/main/_modules/librosa/feature/spectral.html#chroma_cqt">
 * Librosa's Implementation</a> of the CQT Chromagram.
 */
public final class ChromaCQT {
    private ChromaCQT() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Constant-Q chromagram.
     *
     * @param y             Audio time series.
     * @param sr            Sample rate.
     * @param hopLength     Number of samples between successive chroma frames.
     * @param fmin          Minimum frequency to analyze in the CQT.
     * @param numChroma     Number of chroma bins to produce.
     * @param numOctaves    Number of octaves to analyze above <code>fmin</code>.
     * @param binsPerOctave Number of bins per octave in the CQT. Must be an integer multiple of
     *                      <code>numChroma</code>.
     * @param task          The <code>CustomTask</code> object that is handling the generation. Pass
     *                      in <code>null</code> if no such task is being used.
     * @return The output chromagram.
     * @implNote See
     * <a href="https://librosa.org/doc/main/generated/librosa.feature.chroma_cqt.html">Librosa's
     * Implementation</a> of the Chroma CQT algorithm.
     */

    public static double[][] chromaCQT(
            double[] y, double sr, int hopLength, double fmin, int numChroma, int numOctaves, int binsPerOctave,
            CustomTask<?> task
    ) {
        return ChromaCQT.chromaCQT(y, sr, hopLength, fmin, numChroma, numOctaves, binsPerOctave, 0, task);
    }

    /**
     * Constant-Q chromagram.
     *
     * @param y             Audio time series.
     * @param sr            Sample rate.
     * @param hopLength     Number of samples between successive chroma frames.
     * @param fmin          Minimum frequency to analyze in the CQT.
     * @param numChroma     Number of chroma bins to produce.
     * @param numOctaves    Number of octaves to analyze above <code>fmin</code>.
     * @param binsPerOctave Number of bins per octave in the CQT. Must be an integer multiple of
     *                      <code>numChroma</code>.
     * @param threshold     Minimum chroma value for it to count.
     * @param task          The <code>CustomTask</code> object that is handling the generation. Pass
     *                      in <code>null</code> if no such task is being used.
     * @return The output chromagram.
     * @implNote See
     * <a href="https://librosa.org/doc/main/generated/librosa.feature.chroma_cqt.html">Librosa's
     * Implementation</a> of the Chroma CQT algorithm.
     */

    public static double[][] chromaCQT(
            double[] y, double sr, int hopLength, double fmin, int numChroma, int numOctaves, int binsPerOctave,
            double threshold, CustomTask<?> task
    ) {

        // Build the CQT
        Complex[][] cqt = CQT.cqt(
                y, sr, hopLength, fmin, numOctaves * binsPerOctave, binsPerOctave, Double.NaN,
                WindowFunction.HANN_WINDOW, task
        );

        // Keep only the magnitudes
        double[][] C = new double[cqt.length][];
        for (int i = 0; i < cqt.length; i++) {
            C[i] = new double[cqt[i].length];
            for (int j = 0; j < cqt[i].length; j++) {
                C[i][j] = cqt[i][j].abs();
            }
        }

        // Generate chroma map
        double[][] cqToChr = constantQToChroma(C.length, numChroma, binsPerOctave, fmin);

        // Map to chroma
        double[][] chroma = ArrayUtils.matmul(cqToChr, C);

        // Threshold the chroma values
        for (int i = 0; i < chroma.length; i++) {
            for (int j = 0; j < chroma[i].length; j++) {
                chroma[i][j] = Math.max(chroma[i][j], threshold);
            }
        }

        // Normalize the chroma values
        double[][] chromaTransposed = ArrayUtils.transpose(chroma);
        for (int i = 0; i < chromaTransposed.length; i++) {
            chromaTransposed[i] = ArrayUtils.lpNormalize(chromaTransposed[i], Double.POSITIVE_INFINITY);
        }
        return ArrayUtils.transpose(chromaTransposed);
    }

    // Private methods

    /**
     * Construct a linear transformation matrix to map Constant-Q bins onto chroma bins (i.e. pitch
     * classes).
     *
     * @param numInput      Number of input components (CQT bins).
     * @param numChroma     Number of output bins (per octave) in the chroma.
     * @param binsPerOctave How many bins per octave in the CQT.
     * @param fmin          Center frequency of the first constant-Q channel.
     * @return Transformation matrix that maps Constant-Q bins onto chroma bins.
     */
    private static double[][] constantQToChroma(int numInput, int numChroma, int binsPerOctave, double fmin) {
        // Compute the number of bins we are merging
        if (binsPerOctave % numChroma != 0) {
            throw new ValueException("Incompatible merge; input bins must be an integer multiple of output bins.");
        }
        int numMerge = binsPerOctave / numChroma;

        // Tile the identity to merge fractional bins
        double[][] cqToChr = new double[numChroma][numChroma * numMerge];
        for (int i = 0; i < numChroma; i++) {
            for (int j = 0; j < numMerge; j++) {
                cqToChr[i][i * numMerge + j] = 1;
            }
        }

        // Roll it left to center on the target bin
        cqToChr = ArrayUtils.roll(cqToChr, -(numMerge / 2), 1);

        // How many octaves are we repeating?
        int numOctaves = (int) Math.ceil((double) numInput / binsPerOctave);

        // Repeat and trim
        double[][] temp = ArrayUtils.tile(cqToChr, numOctaves);
        cqToChr = new double[numChroma][numInput];
        for (int i = 0; i < numChroma; i++) {
            System.arraycopy(temp[i], 0, cqToChr[i], 0, numInput);
        }

        // Find the note number of the first bin
        double midi0 = UnitConversionUtils.freqToNoteNumber(fmin) % 12;

        // Adjust the roll in terms of how many chroma we want out
        int roll = (int) Math.round(midi0 * (numChroma / 12.));

        // Apply the roll and return
        return ArrayUtils.roll(cqToChr, roll, 0);
    }
}
