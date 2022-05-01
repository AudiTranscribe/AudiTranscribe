/*
 * IOConvertersTest.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-01
 *
 * Description: Test `IOConverters.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class IOConvertersTest {
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
}