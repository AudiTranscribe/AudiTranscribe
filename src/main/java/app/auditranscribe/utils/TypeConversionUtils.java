/*
 * TypeConversionUtils.java
 * Description: Type conversion utility methods.
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
 * Type conversion utility methods.
 */
public final class TypeConversionUtils {
    private TypeConversionUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that converts primitive byte array to a non-primitive byte array.
     *
     * @param bytes The primitive byte array.
     * @return The non-primitive byte array.
     */
    public static Byte[] toByteArray(byte[] bytes) {
        Byte[] result = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[i];
        }
        return result;
    }

    /**
     * Method that converts a non-primitive byte array to a primitive byte array.
     *
     * @param bytes The non-primitive byte array.
     * @return The primitive byte array.
     */
    public static byte[] toByteArray(Byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[i];
        }
        return result;
    }

    /**
     * Method that converts a primitive integer array to a non-primitive integer array.
     *
     * @param ints The primitive integer array.
     * @return The non-primitive integer array.
     */
    public static Integer[] toIntegerArray(int[] ints) {
        Integer[] result = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i];
        }
        return result;
    }

    /**
     * Method that converts a non-primitive integer array to a primitive integer array.
     *
     * @param ints The non-primitive integer array.
     * @return The primitive integer array.
     */
    public static int[] toIntegerArray(Integer[] ints) {
        int[] result = new int[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i];
        }
        return result;
    }

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
     * Method that converts a double list to a double array.
     * @param doubles   List of doubles.
     * @return Array of doubles.
     */
    public static Double[] toDoubleArray(List<Double>doubles) {
        int n = doubles.size();
        Double[] result = new Double[n];
        for (int i = 0; i < n; i++) {
            result[i] = doubles.get(i);
        }
        return result;
    }

    /**
     * Method that converts a primitive boolean array to a non-primitive boolean array.
     *
     * @param booleans The primitive boolean array.
     * @return The non-primitive boolean array.
     */
    public static Boolean[] toBooleanArray(boolean[] booleans) {
        Boolean[] result = new Boolean[booleans.length];
        for (int i = 0; i < booleans.length; i++) {
            result[i] = booleans[i];
        }
        return result;
    }

    /**
     * Method that converts a non-primitive boolean array to a primitive boolean array.
     *
     * @param booleans The non-primitive boolean array.
     * @return The primitive boolean array.
     */
    public static boolean[] toBooleanArray(Boolean[] booleans) {
        boolean[] result = new boolean[booleans.length];
        for (int i = 0; i < booleans.length; i++) {
            result[i] = booleans[i];
        }
        return result;
    }
}
