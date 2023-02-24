package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeConversionUtilsTest {
    // Primitive - non-primitive type conversion
    @Test
    void toByteArray() {
        // Define byte arrays to end up with
        byte[] array1 = {
                (byte) 0x41, (byte) 0x55, (byte) 0x44, (byte) 0x49,
                (byte) 0x54, (byte) 0x52, (byte) 0x41, (byte) 0x4e,
                (byte) 0x53, (byte) 0x43, (byte) 0x52, (byte) 0x49,
                (byte) 0x42, (byte) 0x45, (byte) 0x0a, (byte) 0x0a,
                (byte) 0xad, (byte) 0x75, (byte) 0xc1, (byte) 0xbe
        };
        Byte[] array2 = {
                (byte) 0x53, (byte) 0x43, (byte) 0x52, (byte) 0x49,
                (byte) 0x42, (byte) 0x45, (byte) 0x0a, (byte) 0x0a,
                (byte) 0xad, (byte) 0x75, (byte) 0xc1, (byte) 0xbe,
                (byte) 0x41, (byte) 0x55, (byte) 0x44, (byte) 0x49,
                (byte) 0x54, (byte) 0x52, (byte) 0x41, (byte) 0x4e
        };

        // Assertions
        assertArrayEquals(array1, TypeConversionUtils.toByteArray(TypeConversionUtils.toByteArray(array1)));
        assertArrayEquals(array2, TypeConversionUtils.toByteArray(TypeConversionUtils.toByteArray(array2)));

        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toByteArray((byte[]) null));
        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toByteArray((Byte[]) null));
    }

    @Test
    void toIntegerArray() {
        // Define integer arrays to end up with
        int[] array1 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                0, 9, 8, 7, 6, 5, 4, 3, 2, 1
        };
        Integer[] array2 = {
                0, 9, 8, 7, 6, 5, 4, 3, 2, 1,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0
        };

        // Assertions
        assertArrayEquals(array1, TypeConversionUtils.toIntegerArray(TypeConversionUtils.toIntegerArray(array1)));
        assertArrayEquals(array2, TypeConversionUtils.toIntegerArray(TypeConversionUtils.toIntegerArray(array2)));

        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toIntegerArray((int[]) null));
        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toIntegerArray((Integer[]) null));
    }

    @Test
    void toDoubleArray() {
        // Define double arrays to end up with
        double[] array1 = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                0, 9, 8, 7, 6, 5, 4, 3, 2, 1
        };
        Double[] array2 = {
                0d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d,
                1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 0d
        };
        List<Double> list = List.of(
                1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 0d,
                0d, 9d, 8d, 7d, 6d, 5d, 4d, 3d, 2d, 1d
        );

        // Assertions
        assertArrayEquals(array1, TypeConversionUtils.toDoubleArray(TypeConversionUtils.toDoubleArray(array1)));
        assertArrayEquals(array2, TypeConversionUtils.toDoubleArray(TypeConversionUtils.toDoubleArray(array2)));
        assertArrayEquals(array1, TypeConversionUtils.toDoubleArray(list));

        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toDoubleArray((double[]) null));
        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toDoubleArray((Double[]) null));
        assertNull(TypeConversionUtils.toDoubleArray((List<Double>) null));
    }

    @Test
    void toBooleanArray() {
        // Define boolean arrays to end up with
        boolean[] array1 = {
                false, true, true, false, true, false, true, false, false, false,
                true, false, true, false, false, false, true, false, true, false
        };
        Boolean[] array2 = {
                true, false, false, true, false, false, false, false, true, false,
                false, false, false, false, false, true, false, false, false, false
        };

        // Assertions
        assertArrayEquals(array1, TypeConversionUtils.toBooleanArray(TypeConversionUtils.toBooleanArray(array1)));
        assertArrayEquals(array2, TypeConversionUtils.toBooleanArray(TypeConversionUtils.toBooleanArray(array2)));

        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toBooleanArray((boolean[]) null));
        //noinspection ConstantValue
        assertNull(TypeConversionUtils.toBooleanArray((Boolean[]) null));
    }

    // Other type conversions
    @Test
    void doubleArrayToFloatArray() {
        // Define testing arrays
        double[] array1 = {1.2, 3.4, 5.6, 7.89};
        double[] array2 = {1e5, 1e6, 1e7, 1e8, 1e9};

        // Define correct outputs
        float[] correct1 = {1.2f, 3.4f, 5.6f, 7.89f};
        float[] correct2 = {1e5f, 1e6f, 1e7f, 1e8f, 1e9f};

        // Perform conversion
        float[] floatArr1 = TypeConversionUtils.doubleArrayToFloatArray(array1);
        float[] floatArr2 = TypeConversionUtils.doubleArrayToFloatArray(array2);

        // Check outputs
        assertEquals(correct1.length, floatArr1.length);
        assertEquals(correct2.length, floatArr2.length);

        for (int i = 0; i < correct1.length; i++) {
            assertEquals(correct1[i], floatArr1[i], 1e-5, "Mismatch at index " + i);
        }

        for (int i = 0; i < correct2.length; i++) {
            assertEquals(correct2[i], floatArr2[i], 1e-5, "Mismatch at index " + i);
        }
    }

    @Test
    void floatArrayToDoubleArray() {
        // Define testing arrays
        float[] array1 = {1.2f, 3.4f, 5.6f, 7.89f};
        float[] array2 = {1e5f, 1e6f, 1e7f, 1e8f, 1e9f};

        // Define correct outputs
        double[] correct1 = {1.2, 3.4, 5.6, 7.89};
        double[] correct2 = {1e5, 1e6, 1e7, 1e8, 1e9};

        // Perform conversion
        double[] doubleArr1 = TypeConversionUtils.floatArrayToDoubleArray(array1);
        double[] doubleArr2 = TypeConversionUtils.floatArrayToDoubleArray(array2);

        // Check outputs
        assertEquals(correct1.length, doubleArr1.length);
        assertEquals(correct2.length, doubleArr2.length);

        for (int i = 0; i < correct1.length; i++) {
            assertEquals(correct1[i], doubleArr1[i], 1e-5, "Mismatch at index " + i);
        }

        for (int i = 0; i < correct2.length; i++) {
            assertEquals(correct2[i], doubleArr2[i], 1e-5, "Mismatch at index " + i);
        }
    }
}