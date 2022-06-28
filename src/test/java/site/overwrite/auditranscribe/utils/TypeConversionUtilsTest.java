/*
 * TypeConversionUtilsTest.java
 *
 * Created on 2022-06-28
 * Updated on 2022-06-28
 *
 * Description: Test `TypeConversionUtils.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeConversionUtilsTest {

    @Test
    void toByteArray() {
        // Define byte arrays to end up with
        byte[] array1 = new byte[] {
                (byte) 0x41, (byte) 0x55, (byte) 0x44, (byte) 0x49,
                (byte) 0x54, (byte) 0x52, (byte) 0x41, (byte) 0x4e,
                (byte) 0x53, (byte) 0x43, (byte) 0x52, (byte) 0x49,
                (byte) 0x42, (byte) 0x45, (byte) 0x0a, (byte) 0x0a,
                (byte) 0xad, (byte) 0x75, (byte) 0xc1, (byte) 0xbe
        };
        Byte[] array2 = new Byte[] {
                (byte) 0x41, (byte) 0x55, (byte) 0x44, (byte) 0x49,
                (byte) 0x54, (byte) 0x52, (byte) 0x41, (byte) 0x4e,
                (byte) 0x53, (byte) 0x43, (byte) 0x52, (byte) 0x49,
                (byte) 0x42, (byte) 0x45, (byte) 0x0a, (byte) 0x0a,
                (byte) 0xad, (byte) 0x75, (byte) 0xc1, (byte) 0xbe
        };

        // Assertions
        assertArrayEquals(array1, TypeConversionUtils.toByteArray(TypeConversionUtils.toByteArray(array1)));
        assertArrayEquals(array2, TypeConversionUtils.toByteArray(TypeConversionUtils.toByteArray(array2)));
    }

    @Test
    void toIntegerArray() {
        // Define integer arrays to end up with
        int[] array1 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                0, 9, 8, 7, 6, 5, 4, 3, 2, 1
        };
        Integer[] array2 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                0, 9, 8, 7, 6, 5, 4, 3, 2, 1
        };

        // Assertions
        assertArrayEquals(array1, TypeConversionUtils.toIntegerArray(TypeConversionUtils.toIntegerArray(array1)));
        assertArrayEquals(array2, TypeConversionUtils.toIntegerArray(TypeConversionUtils.toIntegerArray(array2)));
    }
}