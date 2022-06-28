/*
 * MathUtils.java
 *
 * Created on 2022-02-15
 * Updated on 2022-06-28
 *
 * Description: Mathematical utility methods.
 */

package site.overwrite.auditranscribe.utils;

import site.overwrite.auditranscribe.exceptions.generic.ValueException;

import java.math.BigDecimal;

/**
 * Mathematical utility methods.
 */
public final class MathUtils {
    private MathUtils() {
        // Private constructor to signal this is a utility class
    }

    // General mathematical methods

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
     * @throws ValueException If the base of the logarithm is not positive, or is 1.
     */
    public static double logN(double x, double n) {
        // Validate `n`
        if (n <= 0 || n == 1) {
            throw new ValueException("Invalid value for the base of logarithm: " + n);
        }

        // Return the logarithm
        return Math.log(x) / Math.log(n);
    }

    /**
     * Method that quickly calculates the modulo of a number by a
     * <a href="https://en.wikipedia.org/wiki/Mersenne_prime">Mersenne Prime</a>.
     *
     * @param x Number to find the modulo of.
     * @param n Exponent of "2" in the mersenne prime expression "2<sup>n</sup> - 1". This assumes
     *          that <b>"2<sup>n</sup> - 1" is a Mersenne prime</b>, as no further checks on its
     *          primality are conducted.
     * @return Value of <code>x</code> modulo 2<sup>n</sup> - 1.
     * @see <a href="https://ariya.io/2007/02/modulus-with-mersenne-prime">This article</a> on the
     * implementation of the modulo with Mersenne primes.
     */
    public static int modWithMersennePrime(int x, int n) {
        int p = (int) (Math.pow(2, n) - 1);  // Mersenne prime
        int i = (x & p) + (x >> n);          // Bit shift magic
        return (i >= p) ? (i - p) : i;
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

    /**
     * Computes the arithmetic mean of the given array.
     *
     * @param array Array of numbers.
     * @return Arithmetic mean of the array.
     */
    public static double mean(double[] array) {
        double sum = 0;
        for (double elem : array) {
            sum += elem;
        }

        return sum / array.length;
    }

    /**
     * Returns the index of the maximum value of an array.
     *
     * @param array Array of numbers.
     * @return Index of the maximum value of the array.
     */
    public static int argmax(double[] array) {
        int argmax = 0;
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                argmax = i;
            }
        }

        return argmax;
    }

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
     * Normalizes the value <code>x</code> to the range <code>zMin</code> to <code>zMax</code>
     * inclusive.
     *
     * @param x    Value to normalize.
     * @param xMin Minimum possible value of <code>x</code>.
     * @param xMax Maximum possible value of <code>x</code>.
     * @param zMin Minimum normalized value.
     * @param zMax Maximum normalized value.
     * @return Normalized value of <code>x</code>.
     */
    public static double normalize(double x, double xMin, double xMax, double zMin, double zMax) {
        return ((x - xMin) / (xMax - xMin)) * (zMax - zMin) + zMin;
    }

    /**
     * Normalizes the value <code>x</code> to the range 0 to 1 inclusive.
     *
     * @param x   Value to normalise.
     * @param min Minimum possible value of <code>x</code>.
     * @param max Maximum possible value of <code>x</code>.
     * @return Normalized value of <code>x</code>.
     */
    public static double normalize(double x, double min, double max) {
        return normalize(x, min, max, 0, 1);
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

    /**
     * Method that wraps a value to the appropriate value within the range <code>min</code> to
     * <code>max</code>, where <code>min</code> is inclusive and <code>max</code> is exclusive.
     *
     * @param value Value to wrap.
     * @param min   Minimum value.
     * @param max   Maximum value.
     * @return Wrapped value.
     * @throws ValueException If:<ul>
     *                        <li>
     *                            The maximum value is not a positive integer.
     *                        </li>
     *                        <li>
     *                            The minimum value is larger than or equal to the maximum value.
     *                        </li>
     *                        </ul>
     */
    public static int wrapValue(int value, int min, int max) {
        // Check if the values are valid
        if (max <= 0) {
            throw new ValueException("The maximum value must be a positive integer.");
        }

        if (min >= max) {
            throw new ValueException("The minimum value must be smaller than the maximum value.");
        }

        // Perform actual computation
        int r = value % max;
        if (r < min) {
            r += max - min;
        }
        return r;
    }

    /**
     * Method that wraps a value to the maximum value if the value is smaller than the maximum value
     * and wraps to the minimum if the value is larger than the minimum value.
     *
     * @param value Value to wrap.
     * @param min   Minimum value.
     * @param max   Maximum value.
     * @return Wrapped value.
     * @throws ValueException If:<ul>
     *                        <li>
     *                            The maximum value is not a positive number.
     *                        </li>
     *                        <li>
     *                            The minimum value is larger than the maximum value.
     *                        </li>
     *                        </ul>
     */
    public static BigDecimal wrapValue(BigDecimal value, BigDecimal min, BigDecimal max) {
        // Check if the values are valid
        if (max.doubleValue() <= 0) {
            throw new ValueException("The maximum value must be a positive number.");
        }

        if (min.compareTo(max) >= 0) {
            throw new ValueException("The minimum value must be smaller than to the maximum value.");
        }

        // Perform actual computation
        if (value.compareTo(min) < 0) {
            return max;
        } else if (value.compareTo(max) > 0) {
            return min;
        }
        return value;
    }
}
