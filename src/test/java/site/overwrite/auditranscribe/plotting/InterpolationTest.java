/*
 * InterpolationTest.java
 *
 * Created on 2022-03-14
 * Updated on 2022-04-13
 *
 * Description: Test `Interpolation.java`.
 */

package site.overwrite.auditranscribe.plotting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpolationTest {
    @Test
    void nearestNeighbour() {
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
                {3, 3, 4},
                {3, 3, 4},
                {5, 5, 6}
        };
        double[][] interpD = new double[][]{
                {1, 1, 1, 2, 2},
                {1, 1, 1, 2, 2},
                {1, 1, 1, 2, 2},
                {3, 3, 3, 4, 4},
                {3, 3, 3, 4, 4},
        };
        double[][] interpE = new double[][]{
                {1, 1, 2, 2, 3},
                {1, 1, 2, 2, 3},
                {1, 1, 2, 2, 3},
                {4, 4, 5, 5, 6},
                {4, 4, 5, 5, 6}
        };

        // Assertions
        assertArrayEquals(interpA, Interpolation.interpolate(A, 3, 7, InterpolationMethod.NEAREST_NEIGHBOUR));
        assertArrayEquals(interpB, Interpolation.interpolate(B, 4, 4, InterpolationMethod.NEAREST_NEIGHBOUR));
        assertArrayEquals(interpC, Interpolation.interpolate(C, 3, 3, InterpolationMethod.NEAREST_NEIGHBOUR));
        assertArrayEquals(interpD, Interpolation.interpolate(D, 5, 5, InterpolationMethod.NEAREST_NEIGHBOUR));
        assertArrayEquals(interpE, Interpolation.interpolate(E, 5, 5, InterpolationMethod.NEAREST_NEIGHBOUR));
    }

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

        // Assertions
        assertArrayEquals(interpA, Interpolation.interpolate(A, 3, 7, InterpolationMethod.BILINEAR));
        assertArrayEquals(interpB, Interpolation.interpolate(B, 4, 4, InterpolationMethod.BILINEAR));
        assertArrayEquals(interpC, Interpolation.interpolate(C, 3, 3, InterpolationMethod.BILINEAR));
        assertArrayEquals(interpD, Interpolation.interpolate(D, 5, 5, InterpolationMethod.BILINEAR));
        assertArrayEquals(interpE, Interpolation.interpolate(E, 5, 5, InterpolationMethod.BILINEAR));
    }
}