/*
 * ByteConversionUtilsTest.java
 * Description: Test `ByteConversionUtils.java`.
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

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.generic.exceptions.LengthException;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class ByteConversionUtilsTest {
    @Test
    void intToBytes() {
        assertArrayEquals(HexFormat.of().parseHex("00000000"), ByteConversionUtils.intToBytes(0));
        assertArrayEquals(HexFormat.of().parseHex("000004d2"), ByteConversionUtils.intToBytes(1234));
        assertArrayEquals(HexFormat.of().parseHex("499602d2"), ByteConversionUtils.intToBytes(1234567890));
        assertArrayEquals(HexFormat.of().parseHex("7fffffff"), ByteConversionUtils.intToBytes(2147483647));
        assertArrayEquals(HexFormat.of().parseHex("b669fd2e"), ByteConversionUtils.intToBytes(-1234567890));
        assertArrayEquals(HexFormat.of().parseHex("80000001"), ByteConversionUtils.intToBytes(-2147483647));
    }

    @Test
    void doubleToBytes() {
        assertArrayEquals(HexFormat.of().parseHex("40505b851eb851ec"), ByteConversionUtils.doubleToBytes(65.43));
        assertArrayEquals(HexFormat.of().parseHex("c05edccccccccccd"), ByteConversionUtils.doubleToBytes(-123.45));
        assertArrayEquals(HexFormat.of().parseHex("40c34a4587e7c06e"), ByteConversionUtils.doubleToBytes(9876.54321));
        assertArrayEquals(HexFormat.of().parseHex("400921fb53c8d4f1"), ByteConversionUtils.doubleToBytes(3.14159265));
        assertArrayEquals(HexFormat.of().parseHex("bf1588ccebd0259f"), ByteConversionUtils.doubleToBytes(-0.000082147128481));
    }

    @Test
    void charToByte() {
        assertEquals((byte) 0x41, ByteConversionUtils.charToByte('A'));
        assertEquals((byte) 0x7a, ByteConversionUtils.charToByte('z'));
        assertEquals((byte) 0x20, ByteConversionUtils.charToByte(' '));
        assertEquals((byte) 0x3f, ByteConversionUtils.charToByte('?'));
        assertEquals((byte) 0x35, ByteConversionUtils.charToByte('5'));
    }

    @Test
    void stringToBytes() {
        assertArrayEquals(new byte[]{(byte) 0x41, (byte) 0x7a, (byte) 0x20, (byte) 0x3f, (byte) 0x35}, ByteConversionUtils.stringToBytes("Az ?5"));
    }

    @Test
    void oneDimensionalIntegerArrayToBytes() {
        // Define the integer array
        int[] array = {0, 1234, 1234567890, 2147483647, -1234567890, -2147483647};

        // Define the correct hexadecimal string
        String hexStr = "00000006" +
                "00000000" +
                "000004d2" +
                "499602d2" +
                "7fffffff" +
                "b669fd2e" +
                "80000001";

        // Run test
        assertArrayEquals(
                HexFormat.of().parseHex(hexStr),
                ByteConversionUtils.oneDimensionalIntegerArrayToBytes(array)
        );
    }

    @Test
    void oneDimensionalDoubleArrayToBytes() {
        // Define the double array
        double[] array = {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481};

        // Define the correct hexadecimal string
        String hexStr = "00000005" +
                "40505b851eb851ec" +
                "c05edccccccccccd" +
                "40c34a4587e7c06e" +
                "400921fb53c8d4f1" +
                "bf1588ccebd0259f";

        // Run test
        assertArrayEquals(
                HexFormat.of().parseHex(hexStr),
                ByteConversionUtils.oneDimensionalDoubleArrayToBytes(array)
        );
    }

    @Test
    void twoDimensionalDoubleArrayToBytes() {
        // Define the double array
        double[][] array = {
                {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
                {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
                {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
        };

        // Define the correct hexadecimal string
        String hexStr = "00000003" + "00000005"
                + "40505b851eb851ec" + "c05edccccccccccd" + "40c34a4587e7c06e" + "400921fb53c8d4f1" + "bf1588ccebd0259f"
                + "40505b851eb851ec" + "40c34a4587e7c06e" + "400921fb53c8d4f1" + "bf1588ccebd0259f" + "c05edccccccccccd"
                + "40505b851eb851ec" + "c05edccccccccccd" + "400921fb53c8d4f1" + "bf1588ccebd0259f" + "40c34a4587e7c06e";

        // Run test
        assertArrayEquals(
                HexFormat.of().parseHex(hexStr),
                ByteConversionUtils.twoDimensionalDoubleArrayToBytes(array)
        );
    }

    @Test
    void twoDimensionalIntegerArrayToBytes() {
        // Define the integer array
        int[][] array = {
                {1, 10, 100, 1000, 10000},
                {10000, 1000, 100, 10, 1},
                {7273818, 481289, 1249898, 81289489, 912849489}
        };

        // Define the correct hexadecimal string
        String hexStr = "00000003" + "00000005"
                + "00000001" + "0000000a" + "00000064" + "000003e8" + "00002710"
                + "00002710" + "000003e8" + "00000064" + "0000000a" + "00000001"
                + "006efd5a" + "00075809" + "0013126a" + "04d86111" + "3668fa51";

        // Run test
        assertArrayEquals(
                HexFormat.of().parseHex(hexStr),
                ByteConversionUtils.twoDimensionalIntegerArrayToBytes(array)
        );
    }

    @Test
    void bytesToInt() {
        // Test conversion
        assertEquals(0, ByteConversionUtils.bytesToInt(HexFormat.of().parseHex("00000000")));
        assertEquals(1234, ByteConversionUtils.bytesToInt(HexFormat.of().parseHex("000004d2")));
        assertEquals(1234567890, ByteConversionUtils.bytesToInt(HexFormat.of().parseHex("499602d2")));
        assertEquals(2147483647, ByteConversionUtils.bytesToInt(HexFormat.of().parseHex("7fffffff")));
        assertEquals(-1234567890, ByteConversionUtils.bytesToInt(HexFormat.of().parseHex("b669fd2e")));
        assertEquals(-2147483647, ByteConversionUtils.bytesToInt(HexFormat.of().parseHex("80000001")));

        // Test exception
        assertThrowsExactly(LengthException.class, () -> ByteConversionUtils.bytesToInt(new byte[]{(byte) 0x12}));
    }

    @Test
    void bytesToDouble() {
        // Test conversion
        assertEquals(65.43, ByteConversionUtils.bytesToDouble(HexFormat.of().parseHex("40505b851eb851ec")), 1e-10);
        assertEquals(-123.45, ByteConversionUtils.bytesToDouble(HexFormat.of().parseHex("c05edccccccccccd")), 1e-10);
        assertEquals(9876.54321, ByteConversionUtils.bytesToDouble(HexFormat.of().parseHex("40c34a4587e7c06e")), 1e-10);
        assertEquals(3.14159265, ByteConversionUtils.bytesToDouble(HexFormat.of().parseHex("400921fb53c8d4f1")), 1e-10);
        assertEquals(-0.000082147128481, ByteConversionUtils.bytesToDouble(HexFormat.of().parseHex("bf1588ccebd0259f")), 1e-16);

        // Test exception
        assertThrowsExactly(LengthException.class, () -> ByteConversionUtils.bytesToDouble(new byte[]{(byte) 0x12}));
    }

    @Test
    void byteToChar() {
        assertEquals('A', ByteConversionUtils.byteToChar((byte) 0x41));
        assertEquals('z', ByteConversionUtils.byteToChar((byte) 0x7a));
        assertEquals(' ', ByteConversionUtils.byteToChar((byte) 0x20));
        assertEquals('?', ByteConversionUtils.byteToChar((byte) 0x3f));
        assertEquals('5', ByteConversionUtils.byteToChar((byte) 0x35));
    }

    @Test
    void bytesToString() {
        assertEquals("Az ?5", ByteConversionUtils.bytesToString(new byte[]{(byte) 0x41, (byte) 0x7a, (byte) 0x20, (byte) 0x3f, (byte) 0x35}));
    }

    @Test
    void bytesToOneDimensionalIntegerArray() {
        // Define the hexadecimal string
        String hexStr = "00000006" +
                "00000000" +
                "000004d2" +
                "499602d2" +
                "7fffffff" +
                "b669fd2e" +
                "80000001";

        // Define the correct integer array
        int[] array = {0, 1234, 1234567890, 2147483647, -1234567890, -2147483647};

        // Run test
        assertArrayEquals(
                array,
                ByteConversionUtils.bytesToOneDimensionalIntegerArray(HexFormat.of().parseHex(hexStr))
        );
    }

    @Test
    void bytesToOneDimensionalDoubleArray() {
        // Define the hexadecimal string
        String hexStr = "00000005" +
                "40505b851eb851ec" +
                "c05edccccccccccd" +
                "40c34a4587e7c06e" +
                "400921fb53c8d4f1" +
                "bf1588ccebd0259f";

        // Define the correct double array
        double[] array = {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481};

        // Run test
        assertArrayEquals(
                array,
                ByteConversionUtils.bytesToOneDimensionalDoubleArray(HexFormat.of().parseHex(hexStr))
        );
    }

    @Test
    void bytesToTwoDimensionalDoubleArray() {
        // Define the hexadecimal string
        String hexStr = "00000003" + "00000005"
                + "40505b851eb851ec" + "c05edccccccccccd" + "40c34a4587e7c06e" + "400921fb53c8d4f1" + "bf1588ccebd0259f"
                + "40505b851eb851ec" + "40c34a4587e7c06e" + "400921fb53c8d4f1" + "bf1588ccebd0259f" + "c05edccccccccccd"
                + "40505b851eb851ec" + "c05edccccccccccd" + "400921fb53c8d4f1" + "bf1588ccebd0259f" + "40c34a4587e7c06e";

        // Define the correct double array
        double[][] array = {
                {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
                {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
                {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
        };

        // Run test
        assertArrayEquals(
                array,
                ByteConversionUtils.bytesToTwoDimensionalDoubleArray(HexFormat.of().parseHex(hexStr))
        );
    }

    @Test
    void bytesToTwoDimensionalIntegerArray() {
        // Define the hexadecimal string
        String hexStr = "00000003" + "00000005"
                + "00000001" + "0000000a" + "00000064" + "000003e8" + "00002710"
                + "00002710" + "000003e8" + "00000064" + "0000000a" + "00000001"
                + "006efd5a" + "00075809" + "0013126a" + "04d86111" + "3668fa51";

        // Define the correct integer array
        int[][] array = {
                {1, 10, 100, 1000, 10000},
                {10000, 1000, 100, 10, 1},
                {7273818, 481289, 1249898, 81289489, 912849489}
        };

        // Run test
        assertArrayEquals(
                array,
                ByteConversionUtils.bytesToTwoDimensionalIntegerArray(HexFormat.of().parseHex(hexStr))
        );
    }
}