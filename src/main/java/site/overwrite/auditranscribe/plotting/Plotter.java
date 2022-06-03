/*
 * Plotter.java
 *
 * Created on 2022-02-18
 * Updated on 2022-06-03
 *
 * Description: Class that contains plotting functions.
 */

package site.overwrite.auditranscribe.plotting;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.MathUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that contains plotting functions.<br>
 * This is a class that helps generate an image, given spectrogram magnitude data.
 */
public class Plotter {
    // Constants
    public final InterpolationMethod interpolationMethod = InterpolationMethod.BILINEAR;

    // Attributes
    private final double inverseIntensityPrecision;

    private int numDifferentColours;
    private final ColourScale colourScale;
    private int[] colourMap;

    private BufferedImage bufferedImage;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    // Constructor

    /**
     * Creates a plotter object.
     *
     * @param colourScale        The colour scale to use for the spectrogram.
     * @param intensityPrecision The level of precision to be used when calculating relative
     *                           intensity of the spectrogram data. For example, 0.01 means a
     *                           precision of 0.01 for intensity (i.e. 1/0.01 = 100 different
     *                           interpolated colours will be used to represent the different
     *                           intensities)
     */
    public Plotter(ColourScale colourScale, double intensityPrecision) {
        // Update attributes
        this.colourScale = colourScale;
        inverseIntensityPrecision = 1 / intensityPrecision;

        // Generate the colourmap
        generateColourMap();
        logger.log(Level.FINE, "Colourmap generated");
    }

    // Getter/setter methods

    /**
     * Gets the writable image of the spectrogram.
     *
     * @return Writable spectrogram image.
     */
    public WritableImage getImage() {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    // Public methods

    /**
     * Method that plots the given magnitudes onto the <code>Plotter</code>'s own image.<br>
     * <b>Note: the generated image still needs to be obtained via the {@link #getImage()}
     * function.</b>
     *
     * @param spectrogramMagnitudes Magnitude data of the spectrogram as a 2D array. Dimensions of
     *                              the array should be of the form (Number of VQT Bins, Number of
     *                              Windows).
     * @param imgWidth              Width of the image in pixels.
     * @param imgHeight             Height of the image in pixels.
     */
    public void plot(double[][] spectrogramMagnitudes, int imgWidth, int imgHeight) {
        // Define the image that will show the spectrogram
        bufferedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        logger.log(Level.FINE, "Defined buffer image");

        // Get the pixels of the spectrogram image
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        logger.log(Level.FINE, "Got pixels of buffer image");

        // Generate the values of each packet on the image
        /*
         * Note on terminology used here:
         * - A packet represents the magnitude data for one pixel.
         * - A packet does NOT contain the RGB values for a pixel. It only contains a `double` value
         *   representing the relative 'intensity' that should be shown on the pixel.
         */
        double[][] packets = Interpolation.interpolate(spectrogramMagnitudes, imgHeight, imgWidth, interpolationMethod);
        logger.log(Level.FINE, "Interpolated spectrogram magnitudes");

        // Transpose packets
        // (Make dimensions (width, height) instead of (height, width) for easier indexing)
        packets = ArrayUtils.transpose(packets);
        logger.log(Level.FINE, "Image packets generated");

        // Get min and max of packet values
        double minPacketVal = Double.MAX_VALUE;
        double maxPacketVal = -Double.MAX_VALUE;

        for (double[] row : packets) {
            for (double packet : row) {
                if (minPacketVal > packet) minPacketVal = packet;
                if (maxPacketVal < packet) maxPacketVal = packet;
            }
        }
        logger.log(Level.FINE, "Got min and max packet values");

        // Normalise packet values
        for (int w = 0; w < imgWidth; w++) {
            for (int h = 0; h < imgHeight; h++) {
                packets[w][h] = MathUtils.normalize(packets[w][h], minPacketVal, maxPacketVal);
            }
        }
        logger.log(Level.FINE, "Image packets normalised");

        // Finally, modify the image according to the packet specifications
        for (int h = 0; h < imgHeight; h++) {
            for (int w = 0; w < imgWidth; w++) {
                // Calculate intensity of the frequency bin at that spot
                int intensity = (int) Math.ceil(packets[w][imgHeight - h - 1] * inverseIntensityPrecision);

                // Set the pixel value
                pixels[h * imgWidth + w] = colourMap[numDifferentColours - intensity - 1];  // Reverse intensity order
            }
        }
        logger.log(Level.FINE, "Image pixels set");
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

        // Calculate the number of increments between each successive defined colour in the colour scale
        int amount = numDifferentColours / (colourScale.colours.length - 1) + 1;

        // Generate the colourmap
        int colourIndex = 0;  // Index of the colour in the colour scale
        int colour1 = 0, colour2 = 0;

        for (int i = 0; i < numDifferentColours; i++) {
            // Check if the current height is one where the colour is strictly defined by the colour scale
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
        int r = MathUtils.intLerp(((colour1 & 0xFF0000) >> 16), ((colour2 & 0xFF0000) >> 16), x);
        int g = MathUtils.intLerp(((colour1 & 0x00FF00) >> 8), ((colour2 & 0x00FF00) >> 8), x);
        int b = MathUtils.intLerp((colour1 & 0x0000FF), (colour2 & 0x0000FF), x);

        // Concatenate them together to form the final colour
        return (r << 16) | (g << 8) | b;
    }
}
