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
     * Method that converts primitive byte array to a non-primitive byte array.
     *
     * @param bytes The primitive byte array.
     * @return The non-primitive byte array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static Byte[] toByteArray(byte[] bytes) {
        if (bytes == null) return null;

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
     * @return The primitive byte array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static byte[] toByteArray(Byte[] bytes) {
        if (bytes == null) return null;

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
     * @return The non-primitive integer array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static Integer[] toIntegerArray(int[] ints) {
        if (ints == null) return null;

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
     * @return The primitive integer array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static int[] toIntegerArray(Integer[] ints) {
        if (ints == null) return null;

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
     * @return The non-primitive double array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static Double[] toDoubleArray(double[] doubles) {
        if (doubles == null) return null;

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
     * @return The primitive double array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static double[] toDoubleArray(Double[] doubles) {
        if (doubles == null) return null;

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
     * @return Array of primitive doubles.<br>
     * If input is <code>null</code>, will return null.
     */
    public static double[] toDoubleArray(List<Double> doubles) {
        if (doubles == null) return null;

        int n = doubles.size();
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = doubles.get(i);
        }
        return result;
    }

    /**
     * Method that converts a primitive boolean array to a non-primitive boolean array.
     *
     * @param booleans The primitive boolean array.
     * @return The non-primitive boolean array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static Boolean[] toBooleanArray(boolean[] booleans) {
        if (booleans == null) return null;

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
     * @return The primitive boolean array.<br>
     * If input is <code>null</code>, will return null.
     */
    public static boolean[] toBooleanArray(Boolean[] booleans) {
        if (booleans == null) return null;

        boolean[] result = new boolean[booleans.length];
        for (int i = 0; i < booleans.length; i++) {
            result[i] = booleans[i];
        }
        return result;
    }
}
