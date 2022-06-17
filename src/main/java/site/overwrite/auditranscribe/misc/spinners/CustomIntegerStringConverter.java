/*
 * CustomIntegerStringConverter.java
 *
 * Created on 2022-06-17
 * Updated on 2022-06-17
 *
 * Description: Custom converter that converts a string to an integer.
 */

package site.overwrite.auditranscribe.misc.spinners;

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

    // Overwritten methods
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

