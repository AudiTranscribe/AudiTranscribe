/*
 * TypeConversionUtils.java
 *
 * Created on 2022-06-05
 * Updated on 2022-06-28
 *
 * Description: Type conversion utility methods.
 */

package site.overwrite.auditranscribe.utils;

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
}
