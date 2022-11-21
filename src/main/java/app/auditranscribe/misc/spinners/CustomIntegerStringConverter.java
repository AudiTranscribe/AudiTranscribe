/*
 * CustomIntegerStringConverter.java
 * Description: Custom converter that converts a string to an integer.
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

package app.auditranscribe.misc.spinners;

import javafx.util.StringConverter;

/**
 * Custom converter that converts a string to an integer.
 */
public class CustomIntegerStringConverter extends StringConverter<Integer> {
    /**
     * Initialization method for a <code>CustomIntegerStringConverter</code>.
     */
    public CustomIntegerStringConverter() {
    }

    // Overridden methods
    @Override
    public Integer fromString(String value) {
        // If the specified value is null or zero-length, return null
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.length() < 1) {
            return null;
        }

        // Attempt to parse the specified value as an integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(Integer value) {
        // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        } else {
            return Integer.toString(value);
        }
    }
}

