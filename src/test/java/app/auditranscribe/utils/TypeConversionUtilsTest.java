package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeConversionUtilsTest {
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
    }
}