/*
 * AUDTFileHelpers.java
 * Description: Helper methods for writing to / reading from AUDT files.
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

package site.overwrite.auditranscribe.io.audt_file;

import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.utils.MathUtils;

import java.util.List;

/**
 * Helper methods for writing to / reading from AUDT files.
 */
public final class AUDTFileHelpers {
    // Constants
    static final int INT_CONVERSION_MINIMUM = Integer.MIN_VALUE;
    static final int INT_CONVERSION_MAXIMUM = Integer.MAX_VALUE;

    private AUDTFileHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that helps to add bytes into a bytes list.
     *
     * @param byteList  List of bytes to modify.
     * @param byteArray Array of bytes to add.
     */
    public static void addBytesIntoBytesList(List<Byte> byteList, byte[] byteArray) {
        for (byte b : byteArray) {
            byteList.add(b);
        }
    }

    /**
     * Method that converts a 2D double array into a 2D integer array to save space when saving the
     * file.
     *
     * @param array 2D double array to convert.
     * @return Triplet of values. First value is the 2D integer array. Second value is the minimum
     * value of the doubles array. Final value is the maximum value of the doubles array.
     */
    public static Triple<Integer[][], Double, Double> doubles2DtoInt2D(double[][] array) {
        // Get array size
        int m = array.length;
        int n = array[0].length;

        // Get minimum and maximum array values
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double[] row : array) {
            for (double value : row) {
                if (value < min) min = value;
                if (value > max) max = value;
            }
        }

        // Convert the doubles to integers
        Integer[][] intArray = new Integer[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // There should not be any overflow because the max and min values are integers
                intArray[i][j] = (int) MathUtils.normalize(
                        array[i][j], min, max, INT_CONVERSION_MINIMUM, INT_CONVERSION_MAXIMUM
                );
            }
        }

        // Return the integer array, minimum value, and maximum value
        return new Triple<>(intArray, min, max);
    }

    /**
     * Method that converts a 2D integer array into a 2D double array to retrieve the
     * pseudo-original data before conversion.
     *
     * @param array    2D integer array to convert.
     * @param minValue Minimum value of the original doubles array.
     * @param maxValue Maximum value of the original doubles array.
     * @return 2D double array.
     */
    public static double[][] int2DtoDoubles2D(int[][] array, double minValue, double maxValue) {
        // Get array size
        int m = array.length;
        int n = array[0].length;

        // Convert the integers to doubles
        double[][] doubleArray = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                doubleArray[i][j] = MathUtils.normalize(
                        array[i][j], INT_CONVERSION_MINIMUM, INT_CONVERSION_MAXIMUM, minValue, maxValue
                );
            }
        }

        // Return the double array
        return doubleArray;
    }
}
