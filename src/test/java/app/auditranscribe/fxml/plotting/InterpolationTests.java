package app.auditranscribe.fxml.plotting;

import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.utils.MatrixUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpolationTests {
    @Test
    void interpolationExceptionTest() {
        // Define array
        double[][] array = new double[][]{
                {1, 1, 1},
                {1, 1, 1}
        };

        // Test exceptions
        assertThrowsExactly(LengthException.class, () -> {
            // New X length too short
            Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(array, 1, 4);
        });
        assertThrowsExactly(LengthException.class, () -> {
            // New Y length too short
            Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(array, 3, 2);
        });
    }

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
        double[][] I = new double[][]{
                {1, 2, 3},
                {4, 5, 6}
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
        double[][] interpICorrect = new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        };

        // Perform interpolation
        double[][] interpA = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(A, 3, 7);
        double[][] interpB = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(B, 4, 4);
        double[][] interpC = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(C, 3, 3);
        double[][] interpD = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(D, 5, 5);
        double[][] interpE = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(E, 5, 5);
        double[][] interpF = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(F, 1, 8);
        double[][] interpG = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(G, 6, 1);
        double[][] interpH = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(H, 1, 1);
        double[][] interpI = Interpolation.NEAREST_NEIGHBOUR.interpolation.interpolate(I, 2, 3);

        // Assertions
        assertArrayEquals(interpACorrect, interpA);
        assertArrayEquals(interpBCorrect, interpB);
        assertArrayEquals(interpCCorrect, interpC);
        assertArrayEquals(interpDCorrect, interpD);
        assertArrayEquals(interpECorrect, interpE);
        assertArrayEquals(interpFCorrect, interpF);
        assertArrayEquals(interpGCorrect, interpG);
        assertArrayEquals(interpHCorrect, interpH);
        assertArrayEquals(interpICorrect, interpI);
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
        double[][] I = new double[][]{
                {1, 2, 3},
                {4, 5, 6}
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
                {
                        1.0, 1.8571428571428572, 2.714285714285714, 3.571428571428571, 4.428571428571429, 5.285714285714286,
                        6.142857142857142, 7.0
                }
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
        double[][] interpICorrect = new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        };

        // Perform interpolation
        double[][] interpA = Interpolation.BILINEAR.interpolation.interpolate(A, 3, 7);
        double[][] interpB = Interpolation.BILINEAR.interpolation.interpolate(B, 4, 4);
        double[][] interpC = Interpolation.BILINEAR.interpolation.interpolate(C, 3, 3);
        double[][] interpD = Interpolation.BILINEAR.interpolation.interpolate(D, 5, 5);
        double[][] interpE = Interpolation.BILINEAR.interpolation.interpolate(E, 5, 5);
        double[][] interpF = Interpolation.BILINEAR.interpolation.interpolate(F, 1, 8);
        double[][] interpG = Interpolation.BILINEAR.interpolation.interpolate(G, 6, 1);
        double[][] interpH = Interpolation.BILINEAR.interpolation.interpolate(H, 1, 1);
        double[][] interpI = Interpolation.BILINEAR.interpolation.interpolate(I, 2, 3);

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
        assertArrayEquals(MatrixUtils.transpose(interpGCorrect)[0], MatrixUtils.transpose(interpG)[0], 1e-5);

        assertArrayEquals(interpHCorrect, interpH);
        assertArrayEquals(interpICorrect, interpI);
    }
}