package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeConversionUtilsTest {
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
}