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

import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.misc.tuples.Pair;
import site.overwrite.auditranscribe.misc.tuples.Triple;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.ChromaCQT;
import site.overwrite.auditranscribe.utils.StatisticalUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class that handles the estimation of the music key.
 */
public class MusicKeyEstimator {
    // Constants
    final static double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88};
    final static double[] MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};

    // Attributes
    private final double[] samples;
    private final double sampleRate;

    /**
     * Initialization method for a new <code>MusicKeyEstimator</code> object.
     *
     * @param y  Audio time series.
     * @param sr Sample rate of the audio.
     */
    public MusicKeyEstimator(double[] y, double sr) {
        this.samples = y;
        this.sampleRate = sr;
    }

    // Public methods

    /**
     * Method that returns the most likely keys for the audio sequence.
     *
     * @param numKeys Number of music keys to return.<br>
     *                This number cannot be less than 1 or more than 30.
     * @param task    The <code>CustomTask</code> object that is handling the generation. Pass in
     *                <code>null</code> if no such task is being used.
     * @return A list of <code>MusicKey</code> objects in <b>decreasing</b> likelihood of being the
     * actual music key.
     */
    public List<MusicKey> getMostLikelyKeys(int numKeys, CustomTask<?> task) {
        // Get the keys with correlation
        List<Pair<MusicKey, Double>> keysWithCorr = getMostLikelyKeysWithCorrelation(numKeys, task);

        // Get only the keys
        List<MusicKey> keys = new ArrayList<>(keysWithCorr.size());
        for (Pair<MusicKey, Double> pair : keysWithCorr) {
            keys.add(pair.value0());
        }
        return keys;
    }

    /**
     * Method that returns the most likely keys for the audio sequence.
     *
     * @param numKeys Number of music keys to return.<br>
     *                This number cannot be less than 1 or more than 30.
     * @param task    The <code>CustomTask</code> object that is handling the generation. Pass in
     *                <code>null</code> if no such task is being used.
     * @return A list of pairs.<br>
     * First element in each pair is a likely music key. Second element is the correlation of that key.<br>
     * Keys are sorted in <b>decreasing </b> likelihood of being the actual music key.
     */
    public List<Pair<MusicKey, Double>> getMostLikelyKeysWithCorrelation(int numKeys, CustomTask<?> task) {
        // Check that `numKeys` is valid
        if ((numKeys < 1) || (numKeys > 30)) throw new ValueException("Invalid value for `numKeys`: " + numKeys);

        // First get the key correlations
        List<Triple<Integer, Boolean, Double>> correlations = getKeyCorrelations(task);

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
    private List<Triple<Integer, Boolean, Double>> getKeyCorrelations(CustomTask<?> task) {
        // Generate the chromagram
        double[][] chromagram = ChromaCQT.chromaCQT(
                samples, sampleRate, 512, UnitConversionUtils.noteToFreq("C1"), 12,
                7, 24, task
        );

        // Compute the amount of each pitch class present in the time interval
        double[] chromaVals = new double[12];
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < chromagram[0].length; j++) {
                chromaVals[i] += chromagram[i][j];
            }
        }

        // Determine offset from root tonic
        List<Triple<Integer, Boolean, Double>> keyCorrelations = new ArrayList<>();

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
    private static class SortKeyProfiles implements Comparator<Triple<Integer, Boolean, Double>> {
        @Override
        public int compare(
                Triple<Integer, Boolean, Double> o1, Triple<Integer, Boolean, Double> o2
        ) {
            return o1.value2() > o2.value2() ? -1 : 0;  // Sort in descending order
        }
    }
}
