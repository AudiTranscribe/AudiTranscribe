/*
 * MusicKeyEstimator.java
 * Description: Handles the estimation of the music key of an audio signal.
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

package app.auditranscribe.music;

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.signal.feature_extraction.ChromaCQT;
import app.auditranscribe.utils.StatisticsUtils;
import app.auditranscribe.utils.UnitConversionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handles the estimation of the music key of an audio signal.
 */
public final class MusicKeyEstimator {
    // Constants
    // (Read `getKeyCorrelations()` Javadoc)
    final static double[] MAJOR_PROFILE = {5, 2, 3.5, 2, 4.5, 4, 2, 4.5, 2, 3.5, 1.5, 4};
    final static double[] MINOR_PROFILE = {5, 2, 3.5, 4.5, 2, 4, 2, 4.5, 3.5, 2, 1.5, 4};

    private MusicKeyEstimator() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that returns the most likely keys for the audio sequence.
     *
     * @param x          The audio signal.
     * @param sampleRate The sample rate of the audio signal.
     * @param numKeys    Number of music keys to return.<br>
     *                   This number cannot be less than 1 or more than 30.
     * @param task       The <code>CustomTask</code> object that is handling the generation. Pass in
     *                   <code>null</code> if no such task is being used.
     * @return A list of pairs.
     * <ul>
     *     <li>First element in each pair is a likely music key.</li>
     *     <li>Second element in each pair is the correlation coefficient of that key.</li>
     * </ul>
     * Keys are sorted in <b>decreasing</b> likelihood of being the actual music key.
     */
    public static List<Pair<MusicKey, Double>> getMostLikelyKeysWithCorrelation(
            double[] x, double sampleRate, int numKeys, CustomTask<?> task
    ) {
        // Check that `numKeys` is valid
        if ((numKeys < 1) || (numKeys > 30)) throw new ValueException("Invalid value for `numKeys`: " + numKeys);

        // First get the key correlations
        List<Triple<Integer, Boolean, Double>> correlations = getKeyCorrelations(x, sampleRate, task);

        // Now get the needed keys
        List<Pair<MusicKey, Double>> keys = new ArrayList<>();
        int corrIndex = 0;
        while (keys.size() < numKeys) {
            // Get the next triplet of correlation values
            Triple<Integer, Boolean, Double> triplet = correlations.get(corrIndex);
            corrIndex++;

            // Get the required properties from the triplet
            int keyOffset = triplet.value0();
            boolean isMinor = triplet.value1();
            double correlation = triplet.value2();

            // Attempt to match to music key(s) and add to master list
            List<MusicKey> possibleMatches = MusicKey.getPossibleMatches(keyOffset, isMinor);
            for (MusicKey possibleMatch : possibleMatches) {
                keys.add(new Pair<>(possibleMatch, correlation));
            }
        }

        // Truncate until the size is exactly `numKeys`
        while (keys.size() > numKeys) {
            keys.remove(keys.size() - 1);  // Remove the least likely key at the end
        }

        // Return the final list of `numKeys` keys
        return keys;
    }

    // Private methods

    /**
     * Gets a list of key correlations based on the input audio series.
     *
     * @param x          The audio signal.
     * @param sampleRate The sample rate of the audio signal.
     * @param task       The <code>CustomTask</code> object that is handling the generation. Pass in
     *                   <code>null</code> if no such task is being used.
     * @return A list of triplets, sorted in <b>descending order</b> by how likely the key is to be
     * the actual key.
     * <ul>
     *     <li>The first element in the triple is the 'offset' from C.</li>
     *     <li>The second element is a boolean, describing whether the key is a major or minor key
     *     (<code>true</code> if it is major and <code>false</code> otherwise)</li>
     *     <li>The third element is the correlation coefficient, describing the 'strength' of the
     *     match.</li>
     * </ul>
     * @implNote Uses the Krumhansl-Schmuckler key-finding algorithm to estimate the key. Key
     * profiles referenced from Temperley, D. (1999). What's Key for Key? The Krumhansl-Schmuckler
     * Key-Finding Algorithm Reconsidered. <em>Music Perception, 17</em>(1), 65–100.
     * <a href="https://doi.org/10.2307/40285812">DOI</a>. Online copy available
     * <a href="http://davidtemperley.com/wp-content/uploads/2015/11/temperley-mp99.pdf">here</a>.
     * Specifically referenced Page 74's key profiles.
     */
    private static List<Triple<Integer, Boolean, Double>> getKeyCorrelations(
            double[] x, double sampleRate, CustomTask<?> task
    ) {
        // Generate the chromagram
        double[][] chromagram = ChromaCQT.chromaCQT(
                x, sampleRate, 512, UnitConversionUtils.noteToFreq("C1"), 12,
                7, 24, task
        );

        // Compute the amount of each pitch class present in the time interval
        double[] chromaValues = new double[12];
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < chromagram[0].length; j++) {
                chromaValues[i] += chromagram[i][j];
            }
        }

        // Determine offset from root tonic
        List<Triple<Integer, Boolean, Double>> keyCorrelations = new ArrayList<>();

        for (int offset = 0; offset < 12; offset++) {
            // Get the profile of the key with the specified offset
            double[] testProfile = new double[12];
            for (int i = 0; i < 12; i++) {
                testProfile[i] = chromaValues[(offset + i) % 12];
            }

            // Compute correlation coefficients with the major and minor key profiles
            double majCor = StatisticsUtils.corrcoef(MAJOR_PROFILE, testProfile)[1][0];
            double minCor = StatisticsUtils.corrcoef(MINOR_PROFILE, testProfile)[1][0];

            // Add it to the list
            keyCorrelations.add(new Triple<>(
                    offset,
                    true,  // Is major
                    majCor
            ));
            keyCorrelations.add(new Triple<>(
                    offset,
                    false,  // Is minor
                    minCor
            ));
        }

        // Sort the key correlations
        keyCorrelations.sort(new SortKeyProfiles());

        return keyCorrelations;
    }

    // Helper classes
    @ExcludeFromGeneratedCoverageReport
    private static class SortKeyProfiles implements Comparator<Triple<Integer, Boolean, Double>> {
        @Override
        public int compare(
                Triple<Integer, Boolean, Double> o1, Triple<Integer, Boolean, Double> o2
        ) {
            // Sort in descending order
            if (o1.value2() > o2.value2()) return -1;
            else if (o1.value2() < o2.value2()) return 1;
            return 0;
        }
    }
}
