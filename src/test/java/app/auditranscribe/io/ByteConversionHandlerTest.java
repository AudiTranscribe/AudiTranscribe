package app.auditranscribe.io;

import app.auditranscribe.generic.exceptions.LengthException;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class ByteConversionHandlerTest {
    // Conversion to bytes
    @Test
    void booleanToBytes() {
        assertArrayEquals(new byte[]{0}, ByteConversionHandler.booleanToBytes(false));
        assertArrayEquals(new byte[]{1}, ByteConversionHandler.booleanToBytes(true));
    }

    @Test
    void shortToBytes() {
        assertArrayEquals(HexFormat.of().parseHex("0000"), ByteConversionHandler.shortToBytes((short) 0));
        assertArrayEquals(HexFormat.of().parseHex("04d2"), ByteConversionHandler.shortToBytes((short) 1234));
        assertArrayEquals(HexFormat.of().parseHex("7fff"), ByteConversionHandler.shortToBytes((short) 32767));
        assertArrayEquals(HexFormat.of().parseHex("fb2e"), ByteConversionHandler.shortToBytes((short) -1234));
        assertArrayEquals(HexFormat.of().parseHex("8000"), ByteConversionHandler.shortToBytes((short) -32768));
    }

    @Test
    void intToBytes() {
        assertArrayEquals(HexFormat.of().parseHex("00000000"), ByteConversionHandler.intToBytes(0));
        assertArrayEquals(HexFormat.of().parseHex("000004d2"), ByteConversionHandler.intToBytes(1234));
        assertArrayEquals(HexFormat.of().parseHex("499602d2"), ByteConversionHandler.intToBytes(1234567890));
        assertArrayEquals(HexFormat.of().parseHex("7fffffff"), ByteConversionHandler.intToBytes(2147483647));
        assertArrayEquals(HexFormat.of().parseHex("b669fd2e"), ByteConversionHandler.intToBytes(-1234567890));
        assertArrayEquals(HexFormat.of().parseHex("80000001"), ByteConversionHandler.intToBytes(-2147483647));
    }

    @Test
    void doubleToBytes() {
        assertArrayEquals(
                HexFormat.of().parseHex("40505b851eb851ec"),
                ByteConversionHandler.doubleToBytes(65.43)
        );
        assertArrayEquals(
                HexFormat.of().parseHex("c05edccccccccccd"),
                ByteConversionHandler.doubleToBytes(-123.45)
        );
        assertArrayEquals(
                HexFormat.of().parseHex("40c34a4587e7c06e"),
                ByteConversionHandler.doubleToBytes(9876.54321)
        );
        assertArrayEquals(
                HexFormat.of().parseHex("400921fb53c8d4f1"),
                ByteConversionHandler.doubleToBytes(3.14159265)
        );
        assertArrayEquals(
                HexFormat.of().parseHex("bf1588ccebd0259f"),
                ByteConversionHandler.doubleToBytes(-0.000082147128481)
        );
    }

    @Test
    void stringToBytes() {
        assertArrayEquals(
                new byte[]{(byte) 0x41, (byte) 0x7a, (byte) 0x20, (byte) 0x3f, (byte) 0x35},
                ByteConversionHandler.stringToBytes("Az ?5")
        );
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
                ByteConversionHandler.oneDimensionalIntegerArrayToBytes(array)
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
                ByteConversionHandler.oneDimensionalDoubleArrayToBytes(array)
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
                ByteConversionHandler.twoDimensionalIntegerArrayToBytes(array)
        );
    }

    // Conversion from bytes
    @Test
    void bytesToBoolean() {
        // Test conversion
        assertFalse(ByteConversionHandler.bytesToBoolean(new byte[]{0}));
        assertTrue(ByteConversionHandler.bytesToBoolean(new byte[]{1}));

        // Test exception
        assertThrowsExactly(LengthException.class, () -> ByteConversionHandler.bytesToBoolean(new byte[]{1, 2}));
    }

    @Test
    void bytesToShort() {
        // Test conversion
        assertEquals((short) 0, ByteConversionHandler.bytesToShort(HexFormat.of().parseHex("0000")));
        assertEquals((short) 1234, ByteConversionHandler.bytesToShort(HexFormat.of().parseHex("04d2")));
        assertEquals((short) 32767, ByteConversionHandler.bytesToShort(HexFormat.of().parseHex("7fff")));
        assertEquals((short) -1234, ByteConversionHandler.bytesToShort(HexFormat.of().parseHex("fb2e")));
        assertEquals((short) -32768, ByteConversionHandler.bytesToShort(HexFormat.of().parseHex("8000")));

        // Test exception
        assertThrowsExactly(LengthException.class, () -> ByteConversionHandler.bytesToShort(new byte[]{(byte) 0x12}));
    }

    @Test
    void bytesToInt() {
        // Test conversion
        assertEquals(0, ByteConversionHandler.bytesToInt(HexFormat.of().parseHex("00000000")));
        assertEquals(1234, ByteConversionHandler.bytesToInt(HexFormat.of().parseHex("000004d2")));
        assertEquals(1234567890, ByteConversionHandler.bytesToInt(HexFormat.of().parseHex("499602d2")));
        assertEquals(2147483647, ByteConversionHandler.bytesToInt(HexFormat.of().parseHex("7fffffff")));
        assertEquals(-1234567890, ByteConversionHandler.bytesToInt(HexFormat.of().parseHex("b669fd2e")));
        assertEquals(-2147483647, ByteConversionHandler.bytesToInt(HexFormat.of().parseHex("80000001")));

        // Test exception
        assertThrowsExactly(LengthException.class, () -> ByteConversionHandler.bytesToInt(new byte[]{(byte) 0x12}));
    }

    @Test
    void bytesToDouble() {
        // Test conversion
        assertEquals(
                65.43,
                ByteConversionHandler.bytesToDouble(HexFormat.of().parseHex("40505b851eb851ec")),
                1e-10
        );
        assertEquals(
                -123.45,
                ByteConversionHandler.bytesToDouble(HexFormat.of().parseHex("c05edccccccccccd")),
                1e-10
        );
        assertEquals(
                9876.54321,
                ByteConversionHandler.bytesToDouble(HexFormat.of().parseHex("40c34a4587e7c06e")),
                1e-10
        );
        assertEquals(
                3.14159265,
                ByteConversionHandler.bytesToDouble(HexFormat.of().parseHex("400921fb53c8d4f1")),
                1e-10
        );
        assertEquals(
                -0.000082147128481,
                ByteConversionHandler.bytesToDouble(HexFormat.of().parseHex("bf1588ccebd0259f")),
                1e-16
        );

        // Test exception
        assertThrowsExactly(LengthException.class, () -> ByteConversionHandler.bytesToDouble(new byte[]{(byte) 0x12}));
    }

    @Test
    void bytesToString() {
        assertEquals(
                "Az ?5",
                ByteConversionHandler.bytesToString(
                        new byte[]{(byte) 0x41, (byte) 0x7a, (byte) 0x20, (byte) 0x3f, (byte) 0x35}
                )
        );
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
                ByteConversionHandler.bytesToOneDimensionalIntegerArray(HexFormat.of().parseHex(hexStr))
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
                ByteConversionHandler.bytesToOneDimensionalDoubleArray(HexFormat.of().parseHex(hexStr))
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
                ByteConversionHandler.bytesToTwoDimensionalIntegerArray(HexFormat.of().parseHex(hexStr))
        );
    }
}