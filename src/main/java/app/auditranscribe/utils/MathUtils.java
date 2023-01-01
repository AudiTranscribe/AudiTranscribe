/*
 * MathUtils.java
 * Description: Mathematical utilities.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.ValueException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mathematical utilities.
 */
public final class MathUtils {
    private MathUtils() {
        // Private constructor to signal this is a utility class
    }

    // Arithmetic-related methods

    /**
     * Method to calculate the log base 2 of the number <code>x</code>.
     *
     * @param x Number to take the log base 2 of.
     * @return Log base 2 of <code>x</code>.
     */
    public static double log2(double x) {
        return Math.log(x) * 1.442695040888963;  // ln x * (1/ln 2), to 16 sf
    }

    // Checking-related methods

    /**
     * Method to check if the integer <code>x</code> is a power of 2.<br>
     * This assumes that <code>x</code> is positive.
     *
     * @param x Integer to check.
     * @return A boolean, <code>true</code> if the integer is a power of 2 and <code>false</code>
     * otherwise.
     */
    public static boolean isPowerOf2(int x) {
        if (x <= 0) throw new ValueException("The provided integer must be positive");

        /*
        The following check works because if `x` is a power of 2 (say 128) it would look like this:
            10000000
        Note that `x - 1` would then look like
            01111111
        and so computing the bitwise and (&) of `x` and `x-1` would yield 0.
         */
        return (x & (x - 1)) == 0;
    }

    // Miscellaneous mathematical methods

    /**
     * Rounds the double <code>x</code> to <code>dp</code> decimal places.
     *
     * @param x  The double.
     * @param dp Number of decimal places to round <code>x</code> to.
     * @return Rounded double.
     * @throws ValueException If the number of decimal places <code>dp</code> is less than 0.
     * @implNote If <code>x</code> is <code>NaN</code> then the rounding will also return
     * <code>NaN</code>.
     */
    public static double round(double x, int dp) {
        if (Double.isNaN(x)) return Double.NaN;
        if (dp < 0) throw new ValueException("Invalid number of decimal places: " + dp);

        BigDecimal bd = new BigDecimal(Double.toString(x));
        bd = bd.setScale(dp, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
