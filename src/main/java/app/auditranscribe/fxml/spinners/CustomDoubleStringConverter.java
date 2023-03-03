/*
 * CustomDoubleStringConverter.java
 * Description: Custom converter that converts a string to a double.
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;

/**
 * Custom converter that converts a string to a double.
 */
public class CustomDoubleStringConverter extends CustomStringConverter<Double> {
    // Attributes
    private final DecimalFormat df;

    /**
     * Initialization method for a <code>CustomDoubleStringConverter</code>.
     *
     * @param decimalPlaces Number of decimal places to show.
     */
    public CustomDoubleStringConverter(int decimalPlaces) {
        this.df = new DecimalFormat("#." + String.join("", Collections.nCopies(decimalPlaces, "#")));
    }

    /**
     * Initialization method for a <code>CustomDoubleStringConverter</code>.
     *
     * @param decimalPlaces Number of decimal places to show.
     * @param prefix        Prefix to add before the number.
     * @param suffix        Suffix to add after the number.
     */
    public CustomDoubleStringConverter(int decimalPlaces, String prefix, String suffix) {
        this.df = new DecimalFormat("#." + String.join("", Collections.nCopies(decimalPlaces, "#")));
        this.prefix = prefix;
        this.suffix = suffix;
    }

    // Public methods
    @Override
    public String convertValueToString(Double value) {
        return df.format(value);
    }

    @Override
    public Double convertStringToValue(String value) {
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
