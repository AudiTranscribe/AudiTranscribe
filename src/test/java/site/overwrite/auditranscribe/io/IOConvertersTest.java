/*
 * IOConvertersTest.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-04
 *
 * Description: Test `IOConverters.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class IOConvertersTest {
    @Test
    void intToBytes() {
        assertArrayEquals(HexFormat.of().parseHex("00000000"), IOConverters.intToBytes(0));
        assertArrayEquals(HexFormat.of().parseHex("000004d2"), IOConverters.intToBytes(1234));
        assertArrayEquals(HexFormat.of().parseHex("499602d2"), IOConverters.intToBytes(1234567890));
        assertArrayEquals(HexFormat.of().parseHex("7fffffff"), IOConverters.intToBytes(2147483647));
        assertArrayEquals(HexFormat.of().parseHex("b669fd2e"), IOConverters.intToBytes(-1234567890));
        assertArrayEquals(HexFormat.of().parseHex("80000001"), IOConverters.intToBytes(-2147483647));
    }

    @Test
    void doubleToBytes() {
        assertArrayEquals(HexFormat.of().parseHex("40505b851eb851ec"), IOConverters.doubleToBytes(65.43));
        assertArrayEquals(HexFormat.of().parseHex("c05edccccccccccd"), IOConverters.doubleToBytes(-123.45));
        assertArrayEquals(HexFormat.of().parseHex("40c34a4587e7c06e"), IOConverters.doubleToBytes(9876.54321));
        assertArrayEquals(HexFormat.of().parseHex("400921fb53c8d4f1"), IOConverters.doubleToBytes(3.14159265));
        assertArrayEquals(HexFormat.of().parseHex("bf1588ccebd0259f"), IOConverters.doubleToBytes(-0.000082147128481));
    }

    @Test
    void charToByte() {
        assertEquals((byte) 0x41, IOConverters.charToByte('A'));
        assertEquals((byte) 0x7a, IOConverters.charToByte('z'));
        assertEquals((byte) 0x20, IOConverters.charToByte(' '));
        assertEquals((byte) 0x3f, IOConverters.charToByte('?'));
        assertEquals((byte) 0x35, IOConverters.charToByte('5'));
    }

    @Test
    void stringToBytes() {
        assertArrayEquals(new byte[]{(byte) 0x41, (byte) 0x7a, (byte) 0x20, (byte) 0x3f, (byte) 0x35}, IOConverters.stringToBytes("Az ?5"));
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
                IOConverters.oneDimensionalDoubleArrayToBytes(array)
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
                IOConverters.twoDimensionalDoubleArrayToBytes(array)
        );
    }

    @Test
    void bytesToInt() {
        assertEquals(0, IOConverters.bytesToInt(HexFormat.of().parseHex("00000000")));
        assertEquals(1234, IOConverters.bytesToInt(HexFormat.of().parseHex("000004d2")));
        assertEquals(1234567890, IOConverters.bytesToInt(HexFormat.of().parseHex("499602d2")));
        assertEquals(2147483647, IOConverters.bytesToInt(HexFormat.of().parseHex("7fffffff")));
        assertEquals(-1234567890, IOConverters.bytesToInt(HexFormat.of().parseHex("b669fd2e")));
        assertEquals(-2147483647, IOConverters.bytesToInt(HexFormat.of().parseHex("80000001")));
    }

    @Test
    void bytesToDouble() {
        assertEquals(65.43, IOConverters.bytesToDouble(HexFormat.of().parseHex("40505b851eb851ec")), 1e-10);
        assertEquals(-123.45, IOConverters.bytesToDouble(HexFormat.of().parseHex("c05edccccccccccd")), 1e-10);
        assertEquals(9876.54321, IOConverters.bytesToDouble(HexFormat.of().parseHex("40c34a4587e7c06e")), 1e-10);
        assertEquals(3.14159265, IOConverters.bytesToDouble(HexFormat.of().parseHex("400921fb53c8d4f1")), 1e-10);
        assertEquals(-0.000082147128481, IOConverters.bytesToDouble(HexFormat.of().parseHex("bf1588ccebd0259f")), 1e-16);
    }

    @Test
    void byteToChar() {
        assertEquals('A', IOConverters.byteToChar((byte) 0x41));
        assertEquals('z', IOConverters.byteToChar((byte) 0x7a));
        assertEquals(' ', IOConverters.byteToChar((byte) 0x20));
        assertEquals('?', IOConverters.byteToChar((byte) 0x3f));
        assertEquals('5', IOConverters.byteToChar((byte) 0x35));
    }

    @Test
    void bytesToString() {
        assertEquals("Az ?5", IOConverters.bytesToString(new byte[]{(byte) 0x41, (byte) 0x7a, (byte) 0x20, (byte) 0x3f, (byte) 0x35}));
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
                IOConverters.bytesToOneDimensionalDoubleArray(HexFormat.of().parseHex(hexStr))
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
                IOConverters.bytesToTwoDimensionalDoubleArray(HexFormat.of().parseHex(hexStr))
        );
    }
}