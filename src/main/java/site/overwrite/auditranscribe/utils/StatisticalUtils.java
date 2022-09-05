/*
 * StatisticalUtils.java
 * Description: Utilities used for statistical analysis.
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

package site.overwrite.auditranscribe.utils;

import site.overwrite.auditranscribe.exceptions.generic.LengthException;

import java.util.Arrays;

/**
 * Utilities used for statistical analysis.
 */
public final class StatisticalUtils {
    private StatisticalUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Compute the sum of the array <code>x</code>.
     * @param x Array to compute the sum of.
     * @return The sum of the elements in the array.
     */
    public static double sum(double[] x) {
        double sum = 0;
        for (double v : x) {
            sum += v;
        }
        return sum;
    }

    public static double average(double[] x) {
        return sum(x) / x.length;
    }

    /**
     * Calculates the median of an array.
     *
     * @param array The array.
     * @return The median value of the double array.
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

    public static double[][] cov(double[] x, double[] y) {
        // The delta degrees of freedom is 0
        int ddof = 1;

        // Ensure that `x` and `y` have elements
        if (x.length == 0 || y.length == 0) {
            throw new LengthException("The arrays must have elements inside them.");
        }

        // Ensure that both `x` and `y` have the same length
        if (x.length != y.length) {
            throw new LengthException("The arrays must have the same length.");
        }

        // Form the joined matrix for processing
        int n = x.length;
        double[][] M = new double[2][n];
        for (int i = 0; i < n; i++) {
            M[0][i] = x[i];
            M[1][i] = y[i];
        }

        // Compute the average and the weight sum
        double[] avg = new double[2];
        avg[0] = average(x);
        avg[1] = average(y);

        // Determine the normalization
        double fact = M[0].length - ddof;

        // Adjust the matrix by the average amount
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < x.length; j++) {
                M[i][j] -= avg[i];
            }
        }

        // Compute the covariance matrix
        double[][] C = ArrayUtils.matmul(M, ArrayUtils.transpose(M));
        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < C[i].length; j++) {
                C[i][j] /= fact;
            }
        }

        return C;
    }
}
