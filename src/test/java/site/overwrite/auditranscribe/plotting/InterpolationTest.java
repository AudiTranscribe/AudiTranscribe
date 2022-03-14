/*
 * InterpolationTest.java
 *
 * Created on 2022-03-14
 * Updated on 2022-03-14
 *
 * Description: Test `Interpolation.java`.
 */

package site.overwrite.auditranscribe.plotting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpolationTest {
    @Test
    void bilinear() {
        // Define arrays
        double[][] A = new double[][]{
                {1, 1, 1},
                {1, 1, 1}
        };
        double[][] B = new double[][]{
                {2, 2, 2},
                {2, 2, 2}
        };
        double[][] C = new double[][]{
                {3, 4},
                {5, 6}
        };
        double[][] D = new double[][]{
                {1, 2},
                {3, 4}
        };
        double[][] E = new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        };


        double[][] interpA = new double[][]{
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1}
        };
        double[][] interpB = new double[][]{
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2}
        };
        double[][] interpC = new double[][]{
                {3, 3.5, 4},
                {4, 4.5, 5},
                {5, 5.5, 6}
        };
        double[][] interpD = new double[][]{
                {1, 1.25, 1.5, 1.75, 2},
                {1.5, 1.75, 2, 2.25, 2.5},
                {2, 2.25, 2.5, 2.75, 3},
                {2.5, 2.75, 3, 3.25, 3.5},
                {3, 3.25, 3.5, 3.75, 4}
        };
        double[][] interpE = new double[][]{
                {1, 1.5, 2, 2.5, 3},
                {1.75, 2.25, 2.75, 3.25, 3.75},
                {2.5, 3, 3.5, 4, 4.5},
                {3.25, 3.75, 4.25, 4.75, 5.25},
                {4, 4.5, 5, 5.5, 6}
        };

        assertArrayEquals(interpA, Interpolation.bilinear(A, 3, 7));
        assertArrayEquals(interpB, Interpolation.bilinear(B, 4, 4));
        assertArrayEquals(interpC, Interpolation.bilinear(C, 3, 3));
        assertArrayEquals(interpD, Interpolation.bilinear(D, 5, 5));
        assertArrayEquals(interpE, Interpolation.bilinear(E, 5, 5));
    }
}