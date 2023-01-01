/*
 * StatisticsUtils.java
 * Description: Statistics utilities.
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

package app.auditranscribe.utils;

import java.util.Arrays;

/**
 * Statistics utilities.
 */
public final class StatisticsUtils {
    private StatisticsUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Calculates the median of an array.
     *
     * @param array The array.
     * @return The median value of the double array.<br>
     * Returns <code>Double.NaN</code> if the array is empty.
     * @implNote This method will in-place sort the provided array in ascending order.
     */
    public static double median(double[] array) {
        // If there is no array we don't have to do anything
        if (array.length == 0) return Double.NaN;

        // Sort the array first
        Arrays.sort(array);

        // Find the median
        int n = array.length;
        if (n % 2 == 1) {
            return array[n / 2];
        } else {
            return 0.5 * (array[n / 2] + array[n / 2 - 1]);
        }
    }
}
