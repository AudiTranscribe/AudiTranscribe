/*
 * MiscMath.java
 *
 * Created on 2022-02-15
 * Updated on 2022-02-20
 *
 * Description: Miscellaneous mathematical functions.
 */

package site.overwrite.auditranscribe.utils;

/**
 * Miscellaneous mathematical functions.
 */
public class MiscMath {
    /**
     * Method to calculate the log base 2 of the number <code>x</code>.
     * @param x Number to take the log base 2 of.
     * @return Log base 2 of <code>x</code>.
     */
    public static double log2(double x)
    {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Method to check if the double <code>x</code> is an integer.
     * @param x The number that should be tested.
     * @return Boolean whether the double is an integer or not. True if yes and false if no.
     */
    public static boolean isInteger(double x) {
        return x % 1 == 0;
    }
}
