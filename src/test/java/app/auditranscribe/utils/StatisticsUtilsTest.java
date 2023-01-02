package app.auditranscribe.utils;

import app.auditranscribe.generic.tuples.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsUtilsTest {
    @Test
    void median() {
        // Define arrays
        double[] array1 = {
                9, 2, 3, 4, 5,
                6, 5, 6, 7, 8,
                9, 8, 7, 4, 4,
                2, 9, 1, 2, 2,
                1, 2, 2, 3, 3
        };
        double[] array2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] array3 = {1234};
        double[] array4 = {};

        // Assertions
        assertEquals(4, StatisticsUtils.median(array1));
        assertEquals(5.5, StatisticsUtils.median(array2));
        assertEquals(1234, StatisticsUtils.median(array3));
        assertEquals(Double.NaN, StatisticsUtils.median(array4));
    }

    @Test
    void histogram() {
        // Test 1
        double[] data1 = {1.5, 2.5, 4.5, 5, 1};
        Pair<Integer[], Double[]> countsAndBins1 = StatisticsUtils.histogram(data1, 1, 5, 4);
        Integer[] counts1 = countsAndBins1.value0();
        Double[] bins1 = countsAndBins1.value1();

        assertArrayEquals(new double[]{1, 2, 3, 4, 5}, TypeConversionUtils.toDoubleArray(bins1), 1e-5);
        assertArrayEquals(new int[]{2, 1, 0, 2}, TypeConversionUtils.toIntegerArray(counts1));

        // Test 2
        double[] data2 = {-10, 0, 0.1, -0.2, 0.3, -0.4, 0.5, -0.6, 0.7, -0.8, 0.9, -1, 1, 0, 0, 0, 0, 0, 10};
        Pair<Integer[], Double[]> countsAndBins2 = StatisticsUtils.histogram(data2, -1, 1, 10);
        Integer[] counts2 = countsAndBins2.value0();
        Double[] bins2 = countsAndBins2.value1();

        assertArrayEquals(
                new double[]{-1, -0.8, -0.6, -0.4, -0.2, 0, 0.2, 0.4, 0.6, 0.8, 1},
                TypeConversionUtils.toDoubleArray(bins2),
                1e-5
        );
        assertArrayEquals(new int[]{2, 1, 1, 1, 6, 1, 1, 1, 1, 2}, TypeConversionUtils.toIntegerArray(counts2));

        // Test 3
        double[] data3 = {};
        Pair<Integer[], Double[]> countsAndBins3 = StatisticsUtils.histogram(data3, 1, 5, 4);
        Integer[] counts3 = countsAndBins3.value0();
        Double[] bins3 = countsAndBins3.value1();

        assertArrayEquals(new double[]{1, 2, 3, 4, 5}, TypeConversionUtils.toDoubleArray(bins3), 1e-5);
        assertArrayEquals(new int[]{0, 0, 0, 0}, TypeConversionUtils.toIntegerArray(counts3));
    }
}