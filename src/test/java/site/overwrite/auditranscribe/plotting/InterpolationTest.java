/*
 * InterpolationTest.java
 *
 * Created on 2022-03-14
 * Updated on 2022-06-03
 *
 * Description: Test `Interpolation.java`.
 */

package site.overwrite.auditranscribe.plotting;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.utils.ArrayUtils;

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
        double[][] F = new double[][]{
                {1, 2, 3, 4, 5, 6, 7}
        };
        double[][] G = new double[][]{
                {1},
                {2},
                {3},
                {4},
                {5}
        };
        double[][] H = new double[][]{
                {123.456}
        };

        // Define correct interpolation results
        double[][] interpACorrect = new double[][]{
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1}
        };
        double[][] interpBCorrect = new double[][]{
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2}
        };
        double[][] interpCCorrect = new double[][]{
                {3, 3, 4},
                {3, 3, 4},
                {5, 5, 6}
        };
        double[][] interpDCorrect = new double[][]{
                {1, 1, 1, 2, 2},
                {1, 1, 1, 2, 2},
                {1, 1, 1, 2, 2},
                {3, 3, 3, 4, 4},
                {3, 3, 3, 4, 4},
        };
        double[][] interpECorrect = new double[][]{
                {1, 1, 2, 2, 3},
                {1, 1, 2, 2, 3},
                {1, 1, 2, 2, 3},
                {4, 4, 5, 5, 6},
                {4, 4, 5, 5, 6}
        };
        double[][] interpFCorrect = new double[][]{
                {1, 2, 3, 4, 4, 5, 6, 7}
        };
        double[][] interpGCorrect = new double[][]{
                {1},
                {2},
                {3},
                {3},
                {4},
                {5}
        };
        double[][] interpHCorrect = new double[][]{
                {123.456}
        };

        // Perform interpolation
        double[][] interpA = Interpolation.interpolate(A, 3, 7, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpB = Interpolation.interpolate(B, 4, 4, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpC = Interpolation.interpolate(C, 3, 3, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpD = Interpolation.interpolate(D, 5, 5, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpE = Interpolation.interpolate(E, 5, 5, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpF = Interpolation.interpolate(F, 1, 8, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpG = Interpolation.interpolate(G, 6, 1, InterpolationMethod.NEAREST_NEIGHBOUR);
        double[][] interpH = Interpolation.interpolate(H, 1, 1, InterpolationMethod.NEAREST_NEIGHBOUR);

        // Assertions
        assertArrayEquals(interpACorrect, interpA);
        assertArrayEquals(interpBCorrect, interpB);
        assertArrayEquals(interpCCorrect, interpC);
        assertArrayEquals(interpDCorrect, interpD);
        assertArrayEquals(interpECorrect, interpE);
        assertArrayEquals(interpFCorrect, interpF);
        assertArrayEquals(interpGCorrect, interpG);
        assertArrayEquals(interpHCorrect, interpH);
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
        double[][] F = new double[][]{
                {1, 2, 3, 4, 5, 6, 7}
        };
        double[][] G = new double[][]{
                {1},
                {2},
                {3},
                {4},
                {5}
        };
        double[][] H = new double[][]{
                {123.456}
        };

        // Define correct interpolation results
        double[][] interpACorrect = new double[][]{
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1}
        };
        double[][] interpBCorrect = new double[][]{
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2}
        };
        double[][] interpCCorrect = new double[][]{
                {3, 3.5, 4},
                {4, 4.5, 5},
                {5, 5.5, 6}
        };
        double[][] interpDCorrect = new double[][]{
                {1, 1.25, 1.5, 1.75, 2},
                {1.5, 1.75, 2, 2.25, 2.5},
                {2, 2.25, 2.5, 2.75, 3},
                {2.5, 2.75, 3, 3.25, 3.5},
                {3, 3.25, 3.5, 3.75, 4}
        };
        double[][] interpECorrect = new double[][]{
                {1, 1.5, 2, 2.5, 3},
                {1.75, 2.25, 2.75, 3.25, 3.75},
                {2.5, 3, 3.5, 4, 4.5},
                {3.25, 3.75, 4.25, 4.75, 5.25},
                {4, 4.5, 5, 5.5, 6}
        };
        double[][] interpFCorrect = new double[][]{
                {1.0, 1.8571428571428572, 2.714285714285714, 3.571428571428571, 4.428571428571429, 5.285714285714286, 6.142857142857142, 7.0}
        };
        double[][] interpGCorrect = new double[][]{
                {1},
                {1.8},
                {2.6},
                {3.4},
                {4.2},
                {5}
        };
        double[][] interpHCorrect = new double[][]{
                {123.456}
        };

        // Perform interpolation
        double[][] interpA = Interpolation.interpolate(A, 3, 7, InterpolationMethod.BILINEAR);
        double[][] interpB = Interpolation.interpolate(B, 4, 4, InterpolationMethod.BILINEAR);
        double[][] interpC = Interpolation.interpolate(C, 3, 3, InterpolationMethod.BILINEAR);
        double[][] interpD = Interpolation.interpolate(D, 5, 5, InterpolationMethod.BILINEAR);
        double[][] interpE = Interpolation.interpolate(E, 5, 5, InterpolationMethod.BILINEAR);
        double[][] interpF = Interpolation.interpolate(F, 1, 8, InterpolationMethod.BILINEAR);
        double[][] interpG = Interpolation.interpolate(G, 6, 1, InterpolationMethod.BILINEAR);
        double[][] interpH = Interpolation.interpolate(H, 1, 1, InterpolationMethod.BILINEAR);

        // Assertions
        assertArrayEquals(interpACorrect, interpA);
        assertArrayEquals(interpBCorrect, interpB);
        assertArrayEquals(interpCCorrect, interpC);
        assertArrayEquals(interpDCorrect, interpD);
        assertArrayEquals(interpECorrect, interpE);

        assertEquals(1, interpF.length);
        assertEquals(8, interpF[0].length);
        assertArrayEquals(interpFCorrect[0], interpF[0], 1e-5);

        assertEquals(6, interpG.length);
        assertEquals(1, interpG[0].length);
        assertArrayEquals(ArrayUtils.transpose(interpGCorrect)[0], ArrayUtils.transpose(interpG)[0], 1e-5);

        assertArrayEquals(interpHCorrect, interpH);
    }
}