/*
 * OtherMathTest.java
 *
 * Created on 2022-03-12
 * Updated on 2022-03-13
 *
 * Description: Test `OtherMath.java`.
 */

package site.overwrite.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtherMathTest {
    @Test
    void log2() {
        assertEquals(2, OtherMath.round(OtherMath.log2(4), 3));
        assertEquals(2.322, OtherMath.round(OtherMath.log2(5), 3));
    }

    @Test
    void isInteger() {
        assertTrue(OtherMath.isInteger(3.));
        assertFalse(OtherMath.isInteger(123.456));
    }

    @Test
    void numTwoFactors() {
        assertEquals(4, OtherMath.numTwoFactors(16));
        assertEquals(4, OtherMath.numTwoFactors(80));
        assertEquals(0, OtherMath.numTwoFactors(-1));
    }

    @Test
    void roundFloat() {
        assertEquals(1.23f, OtherMath.round(1.23f, 2));
        assertEquals(1.23f, OtherMath.round(1.23456f, 2));
        assertEquals(1f, OtherMath.round(1f, 3));
    }

    @Test
    void roundDouble() {
        assertEquals(1.23, OtherMath.round(1.23, 2));
        assertEquals(1.23, OtherMath.round(1.23456, 2));
        assertEquals(1, OtherMath.round(1, 3));
    }

    @Test
    void matmul() {
        // Define matrices
        Complex[][] A = new Complex[][]{
                {new Complex(1, 0), new Complex(0, -1)},
                {new Complex(0, 1), new Complex(-1, 0)}
        };
        Complex[][] B = new Complex[][]{
                {new Complex(1, 0), new Complex(1, 1), new Complex(0, 1)},
                {new Complex(0, 1), new Complex(1, 1), new Complex(1, 0)}
        };
        Complex[][] C = new Complex[][]{
                {new Complex(1, 0), new Complex(1, 0)},
                {new Complex(0, 2), new Complex(0, 2)},
                {new Complex(-3, 0), new Complex(-3, 0)}
        };

        // Matrix multiplication results
        Complex[][] X = new Complex[][]{
                {new Complex(2, 0), new Complex(0, 0)},
                {new Complex(0, 0), new Complex(2, 0)}
        };
        Complex[][] Y = new Complex[][]{
                {new Complex(-1, -1), new Complex(-1, -1)},
                {new Complex(-5, 3), new Complex(-5, 3)}
        };
        Complex[][] Z = new Complex[][]{
                {new Complex(1, 1), new Complex(2, 2), new Complex(1, 1)},
                {new Complex(-2, 2), new Complex(-4, 4), new Complex(-2, 2)},
                {new Complex(-3, -3), new Complex(-6, -6), new Complex(-3, -3)}
        };

        // Assertions
        assertArrayEquals(X, OtherMath.matmul(A, A));
        assertArrayEquals(Y, OtherMath.matmul(B, C));
        assertArrayEquals(Z, OtherMath.matmul(C, B));
    }

    @Test
    void norm() {
        assertEquals(5., OtherMath.norm(new double[]{3, 4}), 0.01);
        assertEquals(19.105, OtherMath.norm(new double[]{10, 11, 12}), 0.001);
        assertEquals(16.882, OtherMath.norm(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9}), 0.001);
    }
}