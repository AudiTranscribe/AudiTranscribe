/*
 * IOConverters.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-01
 *
 * Description: Methods that converts Java objects/data into bytes for storage.
 */

package site.overwrite.auditranscribe.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

/**
 * Methods that converts Java objects/data into bytes for storage.
 */
public class IOConverters {
    // Public methods

    /**
     * Method that converts the integer <code>x</code> into an array of 4 bytes.
     *
     * @param x Integer to convert.
     * @return Array of four bytes, representing the integer.
     */
    public static byte[] intToBytes(int x) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(x).array();
    }

    /**
     * Method that converts the double <code>x</code> into an array of 8 bytes.
     *
     * @param x Double to convert.
     * @return Array of eight bytes, representing the double.
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
     * Method that converts a character into its byte representation.
     *
     * @param c Character.
     * @return Byte representing the character <code>c</code>.
     */
    public static byte charToByte(char c) {
        return (byte) c;
    }

    /**
     * Method that converts a string into its bytes.
     *
     * @param str String to convert.
     * @return Byte array, representing the characters of the string.
     */
    public static byte[] stringToBytes(String str) {
        return str.getBytes();
    }

    /**
     * Method that converts an array of 4 bytes into an integer.
     *
     * @param bytes Byte array to convert into an integer.
     * @return Integer represented by the 4 bytes.
     */
    public static int bytesToInt(byte[] bytes) {
        // Assert that there are exactly 4 bytes to convert
        if (bytes.length != 4) throw new InvalidParameterException("There must be exactly 4 bytes in the bytes array.");

        // Convert the bytes array to the integer
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    /**
     * Method that converts an array of 8 bytes into a double.
     *
     * @param bytes Byte array to convert into a double.
     * @return Double represented by the 8 bytes.
     */
    public static double bytesToDouble(byte[] bytes) {
        // Assert that there are exactly 8 bytes to convert
        if (bytes.length != 8) throw new InvalidParameterException("There must be exactly 8 bytes in the bytes array.");

        // Convert to double and return
        return ByteBuffer.wrap(bytes).getDouble();
    }

    /**
     * Method that converts a byte into its character representation.
     *
     * @param b Byte.
     * @return Character representation of the byte <code>b</code>.
     */
    public static char byteToChar(byte b) {
        return (char) b;
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
            output.append(byteToChar(aByte));
        }

        // Return the output string
        return output.toString();
    }
}
