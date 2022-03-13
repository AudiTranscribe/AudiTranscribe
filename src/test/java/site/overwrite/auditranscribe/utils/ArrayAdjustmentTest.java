/*
 * ArrayAdjustmentTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-12
 *
 * Description: Test `ArrayAdjustment.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayAdjustmentTest {
    @Test
    void framing() {
        // Perfect length array framing
        double[] array1 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        double[][] framedHorizontal1 = ArrayAdjustment.frame(array1, 3, 2, false);
        double[][] framedVertical1 = ArrayAdjustment.frame(array1, 3, 2, true);

        assertArrayEquals(new double[][]{{1, 2, 3}, {3, 4, 5}, {5, 6, 7}, {7, 8, 9}, {9, 10, 11}}, framedHorizontal1);
        assertArrayEquals(new double[][]{{1, 3, 5, 7, 9}, {2, 4, 6, 8, 10}, {3, 5, 7, 9, 11}}, framedVertical1);

        // Imperfect length array framing
        double[] array2 = new double[]{1, 2, 3, 4, 5, 6};
        double[][] framedHorizontal2 = ArrayAdjustment.frame(array2, 3, 2, false);
        double[][] framedVertical2 = ArrayAdjustment.frame(array2, 3, 2, true);

        assertArrayEquals(new double[][]{{1, 2, 3}, {3, 4, 5}}, framedHorizontal2);
        assertArrayEquals(new double[][]{{1, 3}, {2, 4}, {3, 5}}, framedVertical2);

        // (Another) Imperfect length array framing
        double[] array3 = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[][] framedHorizontal3 = ArrayAdjustment.frame(array3, 9, 3, false);
        double[][] framedVertical3 = ArrayAdjustment.frame(array3, 9, 3, true);

        assertArrayEquals(new double[][]{{1, 2, 3, 4, 5, 6, 7, 8, 9}, {4, 5, 6, 7, 8, 9, 10, 11, 12}, {7, 8, 9, 10, 11, 12, 13, 14, 15}, {10, 11, 12, 13, 14, 15, 16, 17, 18}}, framedHorizontal3);
        assertArrayEquals(new double[][]{{1, 4, 7, 10}, {2, 5, 8, 11}, {3, 6, 9, 12}, {4, 7, 10, 13}, {5, 8, 11, 14}, {6, 9, 12, 15}, {7, 10, 13, 16}, {8, 11, 14, 17}, {9, 12, 15, 18}}, framedVertical3);
    }
}