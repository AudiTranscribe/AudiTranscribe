/*
 * MusicKeyEstimator.java
 * Description: Class that handles the estimation of the music key.
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

package site.overwrite.auditranscribe.music;

import org.javatuples.Triplet;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.ChromaCQT;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.StatisticalUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class that handles the estimation of the music key.
 */
public final class MusicKeyEstimator {
    // Constants
    final static double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88};
    final static double[] MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};

    private MusicKeyEstimator() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Gets a list of key correlations based on the input audio series.
     *
     * @param y    Audio time series.
     * @param sr   Sample rate of the audio.
     * @param task The <code>CustomTask</code> object that is handling the generation. Pass in
     *             <code>null</code> if no such task is being used.
     * @return A list of triplets, sorted in <b>descending order</b> by how likely the key is to be
     * the actual key.
     * <ul>
     *     <li>The first element in the triple is the 'offset' from C.</li>
     *     <li>The second element is a boolean, describing whether the key is a major or minor key
     *     (<code>true</code> if it is major and <code>false</code> otherwise)</li>
     *     <li>The third element is the correlation coefficient, describing the 'strength' of the
     *     match.</li>
     * </ul>
     * @implNote Uses the Krumhansl-Schmuckler key-finding algorithm to estimate the key.
     */
    public static List<Triplet<Integer, Boolean, Double>> getKeyCorrelations(
            double[] y, double sr, CustomTask<?> task
    ) {
        // Generate the chromagram
        double[][] chromagram = ChromaCQT.chromaCQT(
                y, sr, 512, UnitConversionUtils.noteToFreq("C1"), 12, 7, 24,
                task
        );

        // Compute the amount of each pitch class present in the time interval
        double[] chromaVals = new double[12];
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < chromagram[0].length; j++) {
                chromaVals[i] += chromagram[i][j];
            }
        }

        // Determine offset from root tonic
        List<Triplet<Integer, Boolean, Double>> keyCorrelations = new ArrayList<>();

        for (int offset = 0; offset < 12; offset++) {
            // Get the profile of the key with the specified offset
            double[] testProfile = new double[12];
            for (int i = 0; i < 12; i++) {
                testProfile[i] = chromaVals[(offset + i) % 12];
            }

            // Compute correlation coefficients with the major and minor key profiles
            double majCor = StatisticalUtils.corrcoef(MAJOR_PROFILE, testProfile)[1][0];
            double minCor = StatisticalUtils.corrcoef(MINOR_PROFILE, testProfile)[1][0];

            // Add it to the list
            keyCorrelations.add(new Triplet<>(
                    offset,
                    true,  // Is major
                    majCor
            ));
            keyCorrelations.add(new Triplet<>(
                    offset,
                    false,  // Is minor
                    minCor
            ));
        }

        // Sort the key correlations
        keyCorrelations.sort(new SortKeyProfiles());

        return keyCorrelations;
    }

    static class SortKeyProfiles implements Comparator<Triplet<Integer, Boolean, Double>> {
        @Override
        public int compare(
                Triplet<Integer, Boolean, Double> o1, Triplet<Integer, Boolean, Double> o2
        ) {
            return o1.getValue2() > o2.getValue2() ? -1 : 0;  // Sort in descending order
        }
    }
}
