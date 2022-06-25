/*
 * AbstractFilter.java
 *
 * Created on 2022-03-07
 * Updated on 2022-06-25
 *
 * Description: `AbstractFilter` class for resampling filters.
 */

package site.overwrite.auditranscribe.audio.filters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Abstract resampling filter class.
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
