/*
 * Plotter.java
 *
 * Created on 2022-02-18
 * Updated on 2022-02-22
 *
 * Description: Class that helps generate the image.
 */

package site.overwrite.auditranscribe.spectrogram;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Plotting class for spectrogram-like data.
 */
public class Plotter {
    private double intensityPrecision;
    private double inverseIntensityPrecision;

    private int numDifferentColours;
    private ColourScale colourScale;
    private int[] colourMap;

    private int width;
    private int height;
    private BufferedImage bufferedImage;

    /**
     * Creates a plotter object.
     * @param colourScale           The colour scale to use for the spectrogram.
     * @param intensityPrecision    The level of precision to be used when calculating relative
     *                              intensity of the spectrogram data. For example, 0.01 means a
     *                              precision of 0.01 for intensity (i.e. 1/0.01 = 100 different
     *                              interpolated colours will be used to represent the different
     *                              intensities)
     */
    public Plotter(ColourScale colourScale, double intensityPrecision) {
        // Update attributes
        this.colourScale = colourScale;
        this.intensityPrecision = intensityPrecision;

        inverseIntensityPrecision = 1 / intensityPrecision;

        // Generate the colourmap
        generateColourMap();
    }

    // Getter/setter methods

    /**
     * Get the colour scale used for the plotting.
     * @return Colour scale used.
     */
    public ColourScale getColourScale() {
        return colourScale;
    }

    /**
     * Updates the plotter's colour scale.
     * @param colourScale   The new colour scale to use.
     */
    public void setColourScale(ColourScale colourScale) {
        // Update colour scale
        this.colourScale = colourScale;

        // Update colour map
        generateColourMap();
    }

    /**
     * Get the precision of the intensity for use in the colourmap.
     * @return  The precision of the intensity (e.g. 0.01, 0.0001)
     */
    public double getIntensityPrecision() {
        return intensityPrecision;
    }

    /**
     * Updates the intensity's precision for use in the colourmap.
     * @param intensityPrecision    The level of precision to be used when calculating relative
     *                              intensity of the spectrogram data. For example, 0.01 means a
     *                              precision of 0.01 for intensity (i.e. 1/0.01 = 100 different
     *                              interpolated colours will be used to represent the different
     *                              intensities)
     */
    public void setIntensityPrecision(double intensityPrecision) {
        // Update the intensity precision and inverse intensity precision
        this.intensityPrecision = intensityPrecision;
        inverseIntensityPrecision = 1 / intensityPrecision;

        // Update colour map
        generateColourMap();
    }

    /**
     * Gets the buffered image of the spectrogram.<br>
     * This method <b>does not</b> return the actual writable image. That is returned by
     * {@link #getImage()}.
     * @return  The buffered image of the spectrogram.
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * Gets the writable image of the spectrogram.
     * @return  Writable spectrogram image.
     */
    public WritableImage getImage() {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    // Public methods

    /**
     * Method that plots the given magnitudes onto the <code>Plotter</code>'s own image.<br>
     * <b>Note: the generated image still needs to be obtained via the {@link #getImage()} function.</b>
     * @param spectrogramMagnitudes Magnitude data of the spectrogram as a 2D array.
     * @param imgWidth              Width of the image.
     * @param imgHeight             Height of the image.
     */
    public void plot(double[][] spectrogramMagnitudes, int imgWidth, int imgHeight) {
        // Update `width` and `height` attributes
        this.width = imgWidth;
        this.height = imgHeight;

        // Define the image that will show the spectrogram
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Pixels of the spectrogram image
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();  // Ensure `pixels` is linked to `image`

        // Get number of windows
        int numWindows = spectrogramMagnitudes.length;  // The first length is the number of windows

        // Generate the values of each packet on the image
        /*
         * Note on terminology used here:
         * - A packet represents the magnitude data for one pixel.
         * - The packet does NOT contain the RGB values for the pixel. It only contains a `double` value representing
         *   the (average) value of the magnitudes of its constituent windows.
         */

        double pixelsPerWindow = (double) width / numWindows;  // Same as packets per window
        double windowRemaining = pixelsPerWindow;  // How much of the current window is remaining
        double carryOver = 0;  // What is carried over if there was excess

        int windowNum = 0;
        int widthIndex = 0;  // The number of pixels from the leftmost part of the image, minus 1
        boolean halt = false;  // Flag to tell the while loop to halt

        double[][] packets = new double[width][height];
        while (windowNum < numWindows && !halt) {
            // Check how much of the current window remains
            if (windowRemaining >= 1) {
                // Set the pixels on the current width index
                halt = modifyPackets(widthIndex, windowNum, numWindows, spectrogramMagnitudes, packets, 1);

                // Decrement what is left of the current window by 1
                windowRemaining--;

                // Increment width index by 1
                widthIndex++;
            } else {  // Not enough to cover one width index
                // Set pixels on current width index proportional to `windowRemaining`
                halt = modifyPackets(widthIndex, windowNum, numWindows, spectrogramMagnitudes, packets, windowRemaining);

                // Update carry over
                carryOver = windowRemaining;

                // Reset `windowRemaining` to the initial value
                windowRemaining = pixelsPerWindow;

                // Increment window number
                windowNum++;  // Move on to the next window
            }

            // Check if there was a carry over
            if (carryOver > 0) {
                // Set pixels on current width index proportional to `1 - carryOver`
                halt = modifyPackets(widthIndex, windowNum, numWindows, spectrogramMagnitudes, packets, 1 - carryOver);

                // Subtract `1 - carryOver` from `windowRemaining`
                windowRemaining -= 1 - carryOver;

                // Reset `carryOver`
                carryOver = 0;

                // Increment the width index
                widthIndex++;
            }
        }

        // Get min and max of packet values
        double minPacketVal = Integer.MAX_VALUE;
        double maxPacketVal = Integer.MIN_VALUE;

        for (double[] row: packets) {
            for (double packet: row) {
                if (minPacketVal > packet) minPacketVal = packet;
                if (maxPacketVal < packet) maxPacketVal = packet;
            }
        }

        // Normalise packet values
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                packets[w][h] = normalize(packets[w][h], minPacketVal, maxPacketVal);
            }
        }

        // Finally, modify the image according to the packet specifications
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                // Calculate intensity of the frequency bin at that spot
                int intensity = (int) Math.ceil(packets[w][height - h - 1] * inverseIntensityPrecision);

                // Set the pixel value
                pixels[h * width + w] = colourMap[numDifferentColours - intensity - 1];  // Reverse ordering of intensity
            }
        }
    }

    // Private methods
    /**
     * Generates the colour map for the spectrogram.
     */
    private void generateColourMap() {
        // Determine number of different colours to use to represent the intensities
        numDifferentColours = (int) inverseIntensityPrecision + 1;  // To account for endpoints

        // Define the colourmap array
        colourMap = new int[numDifferentColours];

        // Calculate the number of increments between each successive defined colour in the
        // colour scale
        int amount = numDifferentColours / (colourScale.colours.length - 1) + 1;

        // Generate the colourmap
        int colourIndex = 0;  // Index of the colour in the colour scale
        int colour1 = 0, colour2 = 0;

        for (int i = 0; i < numDifferentColours; i++) {
            // Check if the current height is one where the colour is strictly defined by the
            // colour scale
            if (i % amount == 0 && colourIndex < colourScale.colours.length - 1) {
                // Update the in-between colours
                colour1 = colourScale.colours[colourIndex];
                colour2 = colourScale.colours[colourIndex + 1];

                colourIndex++;
            }

            // Calculate how far between the two colours we are
            // (0 means entirely `colour1`; 1 means entirely `colour2`)
            double x = (double) (i % amount) / amount;

            // Compute the colour that should be defined at this height
            int color = lerpColour(colour1, colour2, x);

            // Update the colourmap
            colourMap[i] = color;
        }
    }

    /**
     * Helper method that helps modify the packets' values on the spectrogram image based on the input magnitudes.
     * Note that this does <b>not</b> replace the values of the packets, it <em><b>adds</b></em> to the value.
     *
     * @param widthIndex The number of pixels from the leftmost part of the image, minus 1.
     * @param windowNum  The current window number.
     * @param numWindows Number of windows that are in the `magnitudes` array.
     * @param magnitudes The magnitudes array generated by the FFT algorithm.
     * @param packets    The packets array to update.
     * @param scale      How much to scale the magnitudes by. Should be a number between 0 and 1 inclusive.
     * @return Halt flag.<br>
     * Note that <code>true</code> means "Halt" and <code>false</code> means "Don't Halt".
     */
    private boolean modifyPackets(int widthIndex, int windowNum, int numWindows, double[][] magnitudes, double[][] packets, double scale) {
        // Check if the `widthIndex` is invalid
        if (widthIndex >= width) {
            return true;
        }

        // Check if `windowNum` exceeded its possible value
        if (windowNum >= numWindows) {
            // Update packet value for the final thing
            for (int i = 0; i < height; i++) {
                packets[widthIndex][i] += scale * magnitudes[windowNum - 1][i];
            }
            return true;
        }

        // Update packet value
        for (int i = 0; i < height; i++) {
            packets[widthIndex][i] += scale * magnitudes[windowNum][i];
        }

        return false;
    }

    /**
     * Linearly interpolate the two colours by the scaling factor <code>x</code>.
     *
     * @param colour1 First colour.
     * @param colour2 Second colour.
     * @param x       Scaling factor.
     * @return The linearly interpolated colour.<br>
     * If <code>x = 0</code> then this will return <code>colour1</code>.
     * If <code>x = 1</code> then this will return <code>colour2</code>.
     */
    private static int lerpColour(int colour1, int colour2, double x) {
        // Lerp the individual red, green, and blue components
        int r = lerp(((colour1 & 0xFF0000) >> 16), ((colour2 & 0xFF0000) >> 16), x);
        int g = lerp(((colour1 & 0x00FF00) >> 8), ((colour2 & 0x00FF00) >> 8), x);
        int b = lerp((colour1 & 0x0000FF), (colour2 & 0x0000FF), x);

        // Concat them together to form the final colour
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Linearly interpolate the two values <code>a</code> and <code>b</code> by the scaling factor
     * <code>x</code>.
     *
     * @param a First value.
     * @param b Second value.
     * @param x Scaling factor.
     * @return The linearly interpolated value.<br>
     * If <code>x = 0</code> then this will return <code>a</code>.
     * If <code>x = 1</code> then this will return <code>b</code>.
     */
    private static int lerp(int a, int b, double x) {
        return (int) (a + (b - a) * x);
    }

    /**
     * Normalises the value <code>x</code> to the range 0 to 1 inclusive.
     * @param x     Value to normalise.
     * @param min   Minimum possible value of <code>x</code>.
     * @param max   Maximum possible value of <code>x</code>.
     * @return  Normalised value of <code>x</code>.
     */
    private static double normalize(double x, double min, double max) {
        return (x - min) / (max - min);
    }
}
