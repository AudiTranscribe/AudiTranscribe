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

/**
 * Mathematical utilities.
 */
public final class MathUtils {
    private MathUtils() {
        // Private constructor to signal this is a utility class
    }

    // Arithmetic-Related methods

    /**
     * Method to calculate the log base 2 of the number <code>x</code>.
     *
     * @param x Number to take the log base 2 of.
     * @return Log base 2 of <code>x</code>.
     */
    public static double log2(double x) {
        return Math.log(x) * 1.442695040888963;  // ln x * (1/ln 2), to 16 sf
    }
}
