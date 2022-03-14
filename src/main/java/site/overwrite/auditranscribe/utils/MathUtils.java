/*
 * MathUtils.java
 *
 * Created on 2022-02-15
 * Updated on 2022-03-15
 *
 * Description: Class containing useful mathematical utility functions.
 */

package site.overwrite.auditranscribe.utils;

import java.security.InvalidParameterException;

/**
 * Class containing useful mathematical utility functions.
 */
public class MathUtils {
    // Arithmetic-Related methods

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
     * Method to calculate the log base <code>n</code> of the number <code>x</code>.
     *
     * @param x Number to take the log base <code>n</code> of.
     * @param n The base of the logarithm.
     * @return Log base <code>n</code> of <code>x</code>.
     * @throws InvalidParameterException If the base of the logarithm is not positive, or is 1.
     */
    public static double logN(double x, double n) {
        // Validate `n`
        if (n <= 0 || n == 1) {
            throw new InvalidParameterException("Invalid value for the base of logarithm: " + n);
        }

        // Return the logarithm
        return Math.log(x) / Math.log(n);
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

    // Data-related methods

    /**
     * Linearly interpolate the two values <code>a</code> and <code>b</code> by the scaling factor
     * <code>x</code>.
     *
     * @param a First value.
     * @param b Second value.
     * @param x Scaling factor.
     * @return An <b>integer</b> representing the linearly interpolated value.<br>
     * If <code>x = 0</code> then this will return <code>a</code>.
     * If <code>x = 1</code> then this will return <code>b</code>.
     */
    public static int intLerp(int a, int b, double x) {
        return (int) lerp(a, b, x);
    }

    /**
     * Linearly interpolate the two values <code>a</code> and <code>b</code> by the scaling factor
     * <code>x</code>.
     *
     * @param a First value.
     * @param b Second value.
     * @param x Scaling factor.
     * @return A <b>double</b> representing the linearly interpolated value.<br>
     * If <code>x = 0</code> then this will return <code>a</code>.
     * If <code>x = 1</code> then this will return <code>b</code>.
     */
    public static double lerp(double a, double b, double x) {
        return (a + (b - a) * x);
    }

    /**
     * Normalises the value <code>x</code> to the range 0 to 1 inclusive.
     *
     * @param x   Value to normalise.
     * @param min Minimum possible value of <code>x</code>.
     * @param max Maximum possible value of <code>x</code>.
     * @return Normalised value of <code>x</code>.
     */
    public static double normalise(double x, double min, double max) {
        return (x - min) / (max - min);
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

    // Checking-related methods

    /**
     * Method to check if the double <code>x</code> is an integer.
     *
     * @param x The number that should be tested.
     * @return Boolean whether the double is an integer or not. True if yes and false if no.
     */
    public static boolean isInteger(double x) {
        return x % 1 == 0;
    }

    // Misc methods

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
}
