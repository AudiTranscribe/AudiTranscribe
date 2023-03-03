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

package app.auditranscribe.fxml.spinners;

/**
 * Custom converter that converts a string to an integer.
 */
public class CustomIntegerStringConverter extends CustomStringConverter<Integer> {
    /**
     * Initialization method for a <code>CustomIntegerStringConverter</code>.
     */
    public CustomIntegerStringConverter() {
    }

    /**
     * Initialization method for a <code>CustomIntegerStringConverter</code>.
     *
     * @param prefix Prefix to add before the number.
     * @param suffix Suffix to add after the number.
     */
    public CustomIntegerStringConverter(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    // Public methods
    @Override
    public String convertValueToString(Integer value) {
        return Integer.toString(value);
    }

    @Override
    public Integer convertStringToValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

