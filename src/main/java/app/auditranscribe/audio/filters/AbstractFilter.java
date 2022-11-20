/*
 * AbstractFilter.java
 * Description: An abstract filter class for containing resampling filters' attributes and methods.
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

package app.auditranscribe.audio.filters;

import app.auditranscribe.io.IOMethods;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * An abstract filter class for containing resampling filters' attributes and methods.
 */
public abstract class AbstractFilter {
    // Attributes
    private double[] halfWindow;
    private int precision;
    private double rolloff;

    // Public methods

    /**
     * Gets the right wing (i.e. right side window) of the interpolation filter.
     *
     * @return The right wing of the interpolation filter.
     */
    public double[] getHalfWindow() {
        // Create a copy of the original
        double[] halfWinCopy = new double[halfWindow.length];
        System.arraycopy(halfWindow, 0, halfWinCopy, 0, halfWindow.length);

        // Return the copy
        return halfWinCopy;
    }

    /**
     * Gets the precision of the filter.
     *
     * @return The number of samples between zero-crossings of the filter.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Gets the rolloff factor of the filter.
     *
     * @return A double, representing the rolloff factor of the filter.
     */
    public double getRolloff() {
        return rolloff;
    }

    // Private methods

    /**
     * Define all the attributes of this filter.
     *
     * @param dataFilePath Path (with reference to the resources' directory) to the JSON data file
     *                     that contains this data.
     * @throws IOException         If the data file path is incorrect.
     * @throws JsonSyntaxException If the syntax of the filter file is incorrect.
     */
    public void defineAttributes(String dataFilePath) throws IOException {
        // Create the GSON loader object
        Gson gson = new Gson();

        // Attempt to get the input stream
        InputStream inputStream = IOMethods.getInputStream(dataFilePath);

        // Check if the input stream is null or not
        if (inputStream == null) {
            throw new IOException("Cannot find the data file '" + dataFilePath + "'.");
        }

        try (Reader reader = new InputStreamReader(inputStream)) {
            // Try loading the filter data
            FilterData filterData = gson.fromJson(reader, FilterData.class);

            // Set attributes
            halfWindow = filterData.halfWindow;
            precision = filterData.precision;
            rolloff = filterData.rolloff;
        } catch (JsonSyntaxException e) {
            throw new IOException(e);
        }
    }
}
