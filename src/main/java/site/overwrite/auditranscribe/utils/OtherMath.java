/*
 * OtherMath.java
 *
 * Created on 2022-02-15
 * Updated on 2022-03-13
 *
 * Description: Other useful mathematical functions and constants.
 */

package site.overwrite.auditranscribe.utils;

import java.security.InvalidParameterException;

/**
 * Miscellaneous mathematical functions.
 */
public class OtherMath {
    // Methods

    /**
     * Method to calculate the log base 2 of the number <code>x</code>.
     *
     * @param x Number to take the log base 2 of.
     * @return Log base 2 of <code>x</code>.
     */
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Method to check if the double <code>x</code> is an integer.
     *
     * @param x The number that should be tested.
     * @return Boolean whether the double is an integer or not. True if yes and false if no.
     */
    public static boolean isInteger(double x) {
        return x % 1 == 0;
    }

    /**
     * Return how many times the integer <code>x</code> can be evenly divided by 2.
     * Returns 0 for non-positive integers.
     *
     * @param x The integer in question.
     * @return Number of times <code>x</code> can be divided by 2 if <code>x</code> is positive;
     * zero otherwise.
     */
    public static int numTwoFactors(int x) {
        // If `x` is not positive, then return 0 by definition
        if (x <= 0) return 0;

        // Compute the number of factors of two
        int numTwos = 0;
        while (x % 2 == 0) {
            numTwos++;
            x /= 2;
        }

        // Return the number of factors of two
        return numTwos;
    }

    /**
     * Rounds the float <code>x</code> to <code>dp</code> decimal places.
     *
     * @param x  The float.
     * @param dp Number of decimal places to round to.
     * @return Rounded float.
     */
    public static float round(float x, int dp) {
        return (float) round((double) x, dp);
    }

    /**
     * Rounds the double <code>x</code> to <code>dp</code> decimal places.
     *
     * @param x  The double.
     * @param dp Number of decimal places to round to.
     * @return Rounded double.
     */
    public static double round(double x, int dp) {
        return Math.round(x * Math.pow(10, dp)) / Math.pow(10, dp);
    }

    /**
     * Matrix multiply two complex numbered matrices.
     * Todo: see if can make this more efficient.
     *
     * @param A The first matrix.
     * @param B The second matrix.
     * @return The multiplied matrix.
     * @throws InvalidParameterException If the matrix sizes are not suitable for multiplication.
     * @see <a href="https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm">This article</a>
     * on the naiive implementation on the Matrix Multiplication algorithm.
     */
    public static Complex[][] matmul(Complex[][] A, Complex[][] B) {
        // Check if the matrices can be multiplied
        if (A[0].length != B.length) {
            throw new InvalidParameterException("Matrix sizes not suitable for multiplication");
        }

        // Otherwise, perform matrix multiplication
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;

        Complex[][] output = new Complex[A.length][B[0].length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                // Calculate the value of the current element
                Complex currElem = new Complex(0, 0);
                for (int k = 0; k < m; k++) {
                    currElem = currElem.plus(Complex.times(A[i][k], B[k][j]));
                }

                // Set the current element to the output matrix
                output[i][j] = currElem;
            }
        }

        // Return the output
        return output;
    }

    /**
     * Computes the Euclidian norm of the given array.
     *
     * @param array Array of numbers.
     * @return Euclidian norm of the array.
     */
    public static double norm(double[] array) {
        double norm = 0;
        for (double elem : array) {
            norm += elem * elem;
        }

        return Math.sqrt(norm);
    }
}
