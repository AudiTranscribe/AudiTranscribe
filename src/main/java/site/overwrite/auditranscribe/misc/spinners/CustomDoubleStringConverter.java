/*
 * CustomDoubleStringConverter.java
 *
 * Created on 2022-06-17
 * Updated on 2022-06-17
 *
 * Description: Custom converter that converts a string to a double.
 */

package site.overwrite.auditranscribe.misc.spinners;

import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;

/**
 * Custom converter that converts a string to a double.
 */
public class CustomDoubleStringConverter extends StringConverter<Double> {
    // Attributes
    private final DecimalFormat df;

    /**
     * Initialization method for a <code>CustomDoubleStringConverter</code>.
     */
    public CustomDoubleStringConverter(int decimalPlaces) {
        // Create the decimal format
        this.df = new DecimalFormat("#." + String.join("", Collections.nCopies(decimalPlaces, "#")));
    }

    @Override
    public String toString(Double value) {
        // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        }

        return df.format(value);
    }

    @Override
    public Double fromString(String value) {
        // If the specified value is null or zero-length, return null
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.length() < 1) {
            return null;
        }

        // Attempt to parse the specified value as a double
        try {
            Double.parseDouble(value);  // Do this first to catch any invalid strings
        } catch (NumberFormatException e) {
            return null;
        }

        // Perform the requested parsing
        try {
            return df.parse(value).doubleValue();
        } catch (ParseException ex) {
            return null;
        }
    }
}
