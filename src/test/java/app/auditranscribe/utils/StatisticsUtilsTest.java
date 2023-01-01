package app.auditranscribe.utils;

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
}