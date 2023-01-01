/*
 * UnitConversionUtils.java
 * Description: Unit conversion utilities.
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
 * Unit conversion utilities.
 */
public final class UnitConversionUtils {
    private UnitConversionUtils() {
        // Private constructor to signal this is a utility class
    }

    // Audio unit conversion

    /**
     * Method that converts a frequency in Hertz (Hz) into (fractional) octave numbers.<br>
     * This method assumes that there is no tuning deviation from A440 (i.e.
     * <code>tuning = 0</code>).
     *
     * @param hz Frequency in Hertz.
     * @return Octave number for the specified frequency.
     */
    public static double hzToOctaves(double hz) {
        return MathUtils.log2(hz) - 4.781359713524660;  // log2(hz) + log2(16/440), to 16 sf
    }
}
