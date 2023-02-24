/*
 * CustomStringConverter.java
 * Description: Custom converter that supports prefixes and suffixes.
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

import javafx.util.StringConverter;

/**
 * Custom converter that supports prefixes and suffixes.
 *
 * @param <T> Type to convert a string to.
 */
public abstract class CustomStringConverter<T> extends StringConverter<T> {
    // Attributes
    public String prefix = "";
    public String suffix = "";

    // Public methods

    /**
     * Method that converts a non-null value into a string.<br>
     * This should <b>not</b> append the prefix and suffix.
     *
     * @param value Value to convert.
     * @return Converted value as a string.
     */
    public abstract String convertValueToString(T value);

    /**
     * Method that converts a non-null value into a value.<br>
     * This method assumes that the value does <b>not</b> include the prefix and suffix.
     *
     * @param value String to convert.
     * @return Converted string as the value.
     */
    public abstract T convertStringToValue(String value);

    @Override
    public String toString(T value) {
        // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        } else {
            return prefix + convertValueToString(value) + suffix;
        }
    }

    @Override
    public T fromString(String value) {
        // If the specified value is null return null
        if (value == null) {
            return null;
        }

        // Trim both ends, then strip prefix and suffix
        value = value.trim();
        value = value.substring(prefix.length(), prefix.length() + (value.length() - suffix.length()));

        // If value's length is too small return null
        if (value.length() < 1) {
            return null;
        }

        // Parse the provided value
        return convertStringToValue(value);
    }
}
