/*
 * TypeConversionUtils.java
 * Description: Type conversion utilities.
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

import java.util.List;

/**
 * Type conversion utilities.
 */
public final class TypeConversionUtils {
    private TypeConversionUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that converts a primitive double array to a non-primitive double array.
     *
     * @param doubles The primitive double array.
     * @return The non-primitive double array.
     */
    public static Double[] toDoubleArray(double[] doubles) {
        Double[] result = new Double[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            result[i] = doubles[i];
        }
        return result;
    }

    /**
     * Method that converts a non-primitive double array to a primitive double array.
     *
     * @param doubles The non-primitive double array.
     * @return The primitive double array.
     */
    public static double[] toDoubleArray(Double[] doubles) {
        double[] result = new double[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            result[i] = doubles[i];
        }
        return result;
    }

    /**
     * Method that converts a list of non-primitive doubles to a primitive double array.
     *
     * @param doubles The list of non-primitive doubles.
     * @return Array of primitive doubles.
     */
    public static double[] toDoubleArray(List<Double> doubles) {
        int n = doubles.size();
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = doubles.get(i);
        }
        return result;
    }
}
