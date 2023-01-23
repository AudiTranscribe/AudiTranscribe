/*
 * ByteConversionHandler.java
 * Description: Class that handles the conversion of bytes to different types.
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

package app.auditranscribe.io;

import app.auditranscribe.generic.exceptions.LengthException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Class that handles the conversion of bytes to different types.
 */
public final class ByteConversionHandler {
    private ByteConversionHandler() {
        // Private constructor to signal this is a utility class
    }

    // Conversion to bytes

    /**
     * Method that converts the boolean <code>x</code> into an array of 1 byte.
     *
     * @param x Boolean to convert.
     * @return Array of 1 byte, representing the boolean.
     */
    public static byte[] booleanToBytes(boolean x) {
        return new byte[]{(byte) (x ? 1 : 0)};
    }

    /**
     * Method that converts the short <code>x</code> into an array of 2 bytes.
     *
     * @param x Short to convert.
     * @return Array of 2 bytes, representing the short.
     */
    public static byte[] shortToBytes(short x) {
        return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(x).array();
    }

    /**
     * Method that converts the integer <code>x</code> into an array of 4 bytes.
     *
     * @param x Integer to convert.
     * @return Array of 4 bytes, representing the integer.
     */
    public static byte[] intToBytes(int x) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(x).array();
    }

    /**
     * Method that converts the double <code>x</code> into an array of 8 bytes.
     *
     * @param x Double to convert.
     * @return Array of 8 bytes, representing the double.
     * @see <a href="https://stackoverflow.com/a/13072387">This StackOverflow answer</a> for the
     * original implementation.
     */
    public static byte[] doubleToBytes(double x) {
        // Convert the double into long bits
        long lng = Double.doubleToLongBits(x);

        // Convert the long into bytes
        byte[] bytes = new byte[8];

        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) ((lng >> ((7 - i) * 8)) & 0xff);
        }

        // Return the finished array
        return bytes;
    }

    /**
     * Method that converts a string into its bytes.
     *
     * @param str String to convert.
     * @return Array of bytes, representing the characters of the string.
     */
    public static byte[] stringToBytes(String str) {
        return str.getBytes();
    }

    /**
     * Method that converts an 1D array of integers into a byte array.
     *
     * @param array 1D array of integers
     * @return Array of bytes, representing the 1D array of integers.
     */
    public static byte[] oneDimensionalIntegerArrayToBytes(int[] array) {
        // Get the length of the array
        int len = array.length;

        // Calculate the total number of bytes needed
        int numBytes = 4 * len + 4;  // Each integer takes 4 bytes, and +4 for the length of array

        // Create the byte array
        byte[] bytes = new byte[numBytes];

        // Write the length of the array into the bytes array
        byte[] lengthBytes = intToBytes(len);
        System.arraycopy(lengthBytes, 0, bytes, 0, 4);

        // For each element, copy its bytes into the byte array
        for (int i = 0; i < len; i++) {
            // Get the bytes that represent the element
            byte[] elemBytes = intToBytes(array[i]);

            // Write the bytes into the byte array
            System.arraycopy(elemBytes, 0, bytes, 4 * i + 4, 4);
        }

        // Return the byte array
        return bytes;
    }

    /**
     * Method that converts an 1D array of doubles into a byte array.
     *
     * @param array 1D array of doubles.
     * @return Array of bytes, representing the 1D array of doubles.
     */
    public static byte[] oneDimensionalDoubleArrayToBytes(double[] array) {
        // Get the length of the array
        int len = array.length;

        // Calculate the total number of bytes needed
        int numBytes = 8 * len + 4;  // Each double takes 8 bytes, and +4 for the length of array

        // Create the byte array
        byte[] bytes = new byte[numBytes];

        // Write the length of the array into the bytes array
        byte[] lengthBytes = intToBytes(len);
        System.arraycopy(lengthBytes, 0, bytes, 0, 4);

        // For each element, copy its bytes into the byte array
        for (int i = 0; i < len; i++) {
            // Get the bytes that represent the element
            byte[] elemBytes = doubleToBytes(array[i]);

            // Write the bytes into the byte array
            System.arraycopy(elemBytes, 0, bytes, 8 * i + 4, 8);
        }

        // Return the byte array
        return bytes;
    }

    /**
     * Method that converts an 2D array of integers into a byte array.
     *
     * @param array 2D array of integers. <b>This assumes that each subarray has the same length.</b>
     * @return Array of bytes, representing the 2D array of integers.
     */
    public static byte[] twoDimensionalIntegerArrayToBytes(int[][] array) {
        // Get the number of 1D arrays present in the main array
        int numSubarrays = array.length;
        int subarrayLength = array[0].length;  // Using assumption: each subarray has the same length

        // Calculate the total number of doubles
        int numIntegers = numSubarrays * subarrayLength;

        // Calculate the total number of bytes needed
        int numBytes = 4 * numIntegers  // Each integer takes 4 bytes to store
                + 4                     // Bytes to denote subarray length
                + 4;                    // Bytes to denote number of subarrays

        // Create the byte array
        byte[] bytes = new byte[numBytes];

        // Write the total number of subarrays and subarray length bytes into the bytes array
        byte[] numSubarraysBytes = intToBytes(numSubarrays);
        System.arraycopy(numSubarraysBytes, 0, bytes, 0, 4);

        byte[] subarrayLengthBytes = intToBytes(subarrayLength);
        System.arraycopy(subarrayLengthBytes, 0, bytes, 4, 4);

        // Copy each double's bytes into the master byte aray
        int numWrittenBytes = 8;  // We have already written 8 bytes in total

        for (int[] integers : array) {
            for (int integer : integers) {
                // Get the bytes that represent the integer
                byte[] integerBytes = intToBytes(integer);

                // Update the byte array
                System.arraycopy(integerBytes, 0, bytes, numWrittenBytes, 4);

                // Update the number of written bytes
                numWrittenBytes += 4;
            }
        }

        // Return the byte array
        return bytes;
    }

    // Conversion from bytes

    /**
     * Method that converts an array of 1 byte into a boolean.
     *
     * @param bytes Byte array to convert into a boolean.
     * @return Boolean represented by the single byte.
     * @throws LengthException If the <code>bytes</code> array does <b>not</b> have exactly 1 byte.
     */
    public static boolean bytesToBoolean(byte[] bytes) {
        // Assert that there are exactly 2 bytes to convert
        if (bytes.length != 1) throw new LengthException("There must be exactly 1 byte in the bytes array.");

        // Convert the bytes array to the boolean
        return bytes[0] == (byte) 1;
    }

    /**
     * Method that converts an array of 2 bytes into a short.
     *
     * @param bytes Byte array to convert into a short.
     * @return Short represented by the 2 bytes.
     * @throws LengthException If the bytes array does <b>not</b> have exactly 2 bytes.
     */
    public static short bytesToShort(byte[] bytes) {
        // Assert that there are exactly 2 bytes to convert
        if (bytes.length != 2) throw new LengthException("There must be exactly 2 bytes in the bytes array.");

        // Convert the bytes array to the short
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    /**
     * Method that converts an array of 4 bytes into an integer.
     *
     * @param bytes Byte array to convert into an integer.
     * @return Integer represented by the 4 bytes.
     * @throws LengthException If the <code>bytes</code> array does <b>not</b> have exactly 4 bytes.
     */
    public static int bytesToInt(byte[] bytes) {
        // Assert that there are exactly 4 bytes to convert
        if (bytes.length != 4) throw new LengthException("There must be exactly 4 bytes in the bytes array.");

        // Convert the bytes array to the integer
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    /**
     * Method that converts an array of 8 bytes into a double.
     *
     * @param bytes Byte array to convert into a double.
     * @return Double represented by the 8 bytes.
     * @throws LengthException If the <code>bytes</code> array does <b>not</b> have exactly 8 bytes.
     */
    public static double bytesToDouble(byte[] bytes) {
        // Assert that there are exactly 8 bytes to convert
        if (bytes.length != 8) throw new LengthException("There must be exactly 8 bytes in the bytes array.");

        // Convert to double and return
        return ByteBuffer.wrap(bytes).getDouble();
    }

    /**
     * Method that converts a byte array into a string.
     *
     * @param bytes Byte array.
     * @return String that was represented by the byte array.
     */
    public static String bytesToString(byte[] bytes) {
        // Create the output string
        StringBuilder output = new StringBuilder();
        for (byte aByte : bytes) {
            output.append((char) aByte);
        }

        // Return the output string
        return output.toString();
    }

    /**
     * Method that converts a byte array into a 1D integer array.
     *
     * @param bytes Byte array.
     * @return 1D integer array that was represented by the byte array.
     */
    public static int[] bytesToOneDimensionalIntegerArray(byte[] bytes) {
        // First 4 bytes represent the number of elements in the resulting array
        byte[] numElemBytes = Arrays.copyOfRange(bytes, 0, 4);
        int numElem = bytesToInt(numElemBytes);

        // Create the double array
        int[] array = new int[numElem];

        // Go through the remaining bytes and retrieve the doubles
        for (int i = 0; i < numElem; i++) {
            // Get the bytes that represent the current element
            byte[] currElemBytes = Arrays.copyOfRange(bytes, 4 * i + 4, 4 * (i + 1) + 4);

            // Convert the bytes into an integer and place into the array
            array[i] = bytesToInt(currElemBytes);
        }

        // Return the integer array
        return array;
    }

    /**
     * Method that converts a byte array into a 1D double array.
     *
     * @param bytes Byte array.
     * @return 1D double array that was represented by the byte array.
     */
    public static double[] bytesToOneDimensionalDoubleArray(byte[] bytes) {
        // First 4 bytes represent the number of elements in the resulting array
        byte[] numElemBytes = Arrays.copyOfRange(bytes, 0, 4);
        int numElem = bytesToInt(numElemBytes);

        // Create the double array
        double[] array = new double[numElem];

        // Go through the remaining bytes and retrieve the doubles
        for (int i = 0; i < numElem; i++) {
            // Get the bytes that represent the current element
            byte[] currElemBytes = Arrays.copyOfRange(bytes, 8 * i + 4, 8 * (i + 1) + 4);

            // Convert the bytes into a double and place into the array
            array[i] = bytesToDouble(currElemBytes);
        }

        // Return the double array
        return array;
    }

    /**
     * Method that converts a byte array into a 2D integer array.
     *
     * @param bytes Byte array.
     * @return 2D integer array that was represented by the byte array.
     */
    public static int[][] bytesToTwoDimensionalIntegerArray(byte[] bytes) {
        // First 4 bytes represent the number of subarrays in the resulting array
        byte[] numSubarraysBytes = Arrays.copyOfRange(bytes, 0, 4);
        int numSubarrays = bytesToInt(numSubarraysBytes);

        // Next 4 bytes represent the number of doubles in each subarray (i.e. subarray length)
        byte[] subarrayLengthBytes = Arrays.copyOfRange(bytes, 4, 8);
        int subarrayLength = bytesToInt(subarrayLengthBytes);

        // Create the integer array
        int[][] array = new int[numSubarrays][subarrayLength];

        // Go through the remaining bytes and retrieve the doubles
        for (int i = 0; i < numSubarrays; i++) {
            for (int j = 0; j < subarrayLength; j++) {
                // Get the bytes that represent the current element
                byte[] currElemBytes = Arrays.copyOfRange(
                        bytes,
                        8 + 4 * i * subarrayLength + j * 4,
                        8 + 4 * i * subarrayLength + (j + 1) * 4
                );

                // Convert the bytes into a double and place into the array
                array[i][j] = bytesToInt(currElemBytes);
            }
        }

        // Return the double array
        return array;
    }
}
