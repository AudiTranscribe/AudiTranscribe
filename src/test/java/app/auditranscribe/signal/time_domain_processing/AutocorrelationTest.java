package app.auditranscribe.signal.time_domain_processing;

import app.auditranscribe.misc.Complex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutocorrelationTest {
    @Test
    void autocorrelation() {
        // Define matrices
        double[][] realMatrix1 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        double[][] realMatrix2 = {
                {1, 2, 3, 4, 5},
                {6, 7, 8, 9, 10}
        };

        Complex[][] complexMatrix1 = {
                {Complex.ONE, new Complex(0, 2), new Complex(-3, 0)},
                {new Complex(0, -4), new Complex(5, 0), new Complex(0, 6)},
                {new Complex(-7, 0), new Complex(0, -8), new Complex(9, 0)}
        };
        Complex[][] complexMatrix2 = {
                {Complex.ZERO, Complex.ONE},
                {new Complex(0, 1), new Complex(0, -1)},
                {new Complex(2, 2), new Complex(2, -2)},
                {new Complex(0, 1), new Complex(0, -1)},
                {Complex.ZERO, Complex.ONE}
        };

        // Generate outputs
        double[][] realMatrix1Output = Autocorrelation.autocorrelation(realMatrix1);
        double[][] realMatrix2Output = Autocorrelation.autocorrelation(realMatrix2);

        Complex[][] complexMatrix1Output = Autocorrelation.autocorrelation(complexMatrix1);
        Complex[][] complexMatrix2Output = Autocorrelation.autocorrelation(complexMatrix2);

        // Check output sizes
        assertEquals(3, realMatrix1Output.length);
        assertEquals(3, realMatrix1Output[0].length);

        assertEquals(2, realMatrix2Output.length);
        assertEquals(5, realMatrix2Output[0].length);

        assertEquals(3, complexMatrix1Output.length);
        assertEquals(3, complexMatrix1Output[0].length);

        assertEquals(5, complexMatrix2Output.length);
        assertEquals(2, complexMatrix2Output[0].length);

        // Check output values
        assertEquals(66, realMatrix1Output[0][0], 1e-5);
        assertEquals(93, realMatrix1Output[0][1], 1e-5);
        assertEquals(126, realMatrix1Output[0][2], 1e-5);
        assertEquals(32, realMatrix1Output[1][0], 1e-5);
        assertEquals(50, realMatrix1Output[1][1], 1e-5);
        assertEquals(72, realMatrix1Output[1][2], 1e-5);
        assertEquals(7, realMatrix1Output[2][0], 1e-5);
        assertEquals(16, realMatrix1Output[2][1], 1e-5);
        assertEquals(27, realMatrix1Output[2][2], 1e-5);

        assertEquals(37, realMatrix2Output[0][0], 1e-5);
        assertEquals(53, realMatrix2Output[0][1], 1e-5);
        assertEquals(73, realMatrix2Output[0][2], 1e-5);
        assertEquals(97, realMatrix2Output[0][3], 1e-5);
        assertEquals(125, realMatrix2Output[0][4], 1e-5);
        assertEquals(6, realMatrix2Output[1][0], 1e-5);
        assertEquals(14, realMatrix2Output[1][1], 1e-5);
        assertEquals(24, realMatrix2Output[1][2], 1e-5);
        assertEquals(36, realMatrix2Output[1][3], 1e-5);
        assertEquals(50, realMatrix2Output[1][4], 1e-5);

        assertEquals(new Complex(66), complexMatrix1Output[0][0].roundNicely(5));
        assertEquals(new Complex(93), complexMatrix1Output[0][1].roundNicely(5));
        assertEquals(new Complex(126), complexMatrix1Output[0][2].roundNicely(5));
        assertEquals(new Complex(0, -32), complexMatrix1Output[1][0].roundNicely(5));
        assertEquals(new Complex(0, -50), complexMatrix1Output[1][1].roundNicely(5));
        assertEquals(new Complex(0, -72), complexMatrix1Output[1][2].roundNicely(5));
        assertEquals(new Complex(-7), complexMatrix1Output[2][0].roundNicely(5));
        assertEquals(new Complex(-16), complexMatrix1Output[2][1].roundNicely(5));
        assertEquals(new Complex(-27), complexMatrix1Output[2][2].roundNicely(5));

        assertEquals(new Complex(10), complexMatrix2Output[0][0].roundNicely(5));
        assertEquals(new Complex(12), complexMatrix2Output[0][1].roundNicely(5));
        assertEquals(new Complex(4), complexMatrix2Output[1][0].roundNicely(5));
        assertEquals(new Complex(4), complexMatrix2Output[1][1].roundNicely(5));
        assertEquals(new Complex(1), complexMatrix2Output[2][0].roundNicely(5));
        assertEquals(new Complex(5), complexMatrix2Output[2][1].roundNicely(5));
        assertEquals(Complex.ZERO, complexMatrix2Output[3][0].roundNicely(5));
        assertEquals(Complex.ZERO, complexMatrix2Output[3][1].roundNicely(5));
        assertEquals(Complex.ZERO, complexMatrix2Output[4][0].roundNicely(5));
        assertEquals(new Complex(1), complexMatrix2Output[4][1].roundNicely(5));
    }
}