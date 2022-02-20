/*
 * Spectrogram.java
 *
 * Created on 2022-02-12
 * Updated on 2022-02-20
 *
 * Description: Spectrogram class.
 */

package site.overwrite.auditranscribe.spectrogram;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.WindowType;
import site.overwrite.auditranscribe.audio.Windows;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.MiscMath;

import javax.sound.sampled.AudioFormat;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.security.InvalidParameterException;

/**
 * Spectrogram class to handle spectrogram creation.
 */
public class Spectrogram {
    // Attributes
    private final Audio audio;
    private final int numSamples;

    private final int pxPerSecond;
    public final int height;
    public final int width;

    private final int frameLength;
    private final int hopLength;

    public int[] colourMap;

    public BufferedImage image;
    public int[] pixels;  // Pixels of the spectrogram image

    public int numFrequencyBins;
    public double[] frequencyBins;

    /**
     * Creates a spectrogram object.
     *
     * @param audioObj       The audio object.
     * @param numPxPerSecond Number of pixels of the spectrogram dedicated to each second of audio
     * @param imageHeight    Height of the spectrogram.
     * @param frameLength    Length of the frame.
     * @param hopLength      Number of steps to advance between frames.
     * @throws InvalidParameterException If the image height is too large.
     */
    // Todo: use pixels per frequency bin?
    public Spectrogram(Audio audioObj, int numPxPerSecond, int imageHeight, int frameLength, int hopLength) {
        // Assert that the `imageHeight` is low enough
        if (frameLength / 2 + 1 < imageHeight) {
            throw new InvalidParameterException("The image height is too large for the frame length " +
                    "(needs to be lower than " + (frameLength / 2 + 1) + ")");
        }

        // Update attributes
        audio = audioObj;
        pxPerSecond = numPxPerSecond;
        height = imageHeight;

        this.frameLength = frameLength;
        this.hopLength = hopLength;

        // Calculate the width of the image
        width = (int) (audioObj.getDuration() * this.pxPerSecond);

        // Define the colour map array
        colourMap = new int[height];

        // Get the number of mono samples
        numSamples = audioObj.getNumMonoSamples();

        // Define the image that will show the spectrogram
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();  // Ensure `pixels` is linked to `image`

        // Calculate frequency bins
        calculateBins();
    }

    // Public methods

    /**
     * Generates the spectrogram image for the given audio samples.
     *
     * @param windowType  The window function to use.
     * @param colourScale The colour scale to use for the spectrogram.
     * @return The spectrogram image.
     */
    public WritableImage generateSpectrogram(WindowType windowType, ColourScale colourScale) {
        // Make the colour map
        generateColourMap(colourScale);

        // Generate windowed samples
        float[][] windowedSamples = generateWindowedSamples(windowType);

        // Get the number of windows
        int numWindows = windowedSamples.length;

        // Generate FFT magnitudes
        double[][] magnitudes = new double[numWindows][height];

        for (int i = 0; i < numWindows; i++) {
            magnitudes[i] = processFFTOnWindow(windowedSamples[i]);
        }

        // Generate the values of each packet on the image
        /*
         * Note on terminology used here:
         * - A packet represents the magnitude data for one pixel.
         * - The packet does NOT contain the RGB values for the pixel. It only contains a `double` value representing
         *   the (average) value of the magnitudes of its constituent windows.
         */

//        double pixelsPerWindow = pxPerSecond * (frameLength / sampleRate);  // Same as packets per window
        double pixelsPerWindow = (audio.getDuration() * pxPerSecond) / numWindows;  // Same as packets per window
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
                halt = modifyPackets(widthIndex, windowNum, numWindows, magnitudes, packets, 1);

                // Decrement what is left of the current window by 1
                windowRemaining--;

                // Increment width index by 1
                widthIndex++;
            } else {  // Not enough to cover one width index
                // Set pixels on current width index proportional to `windowRemaining`
                halt = modifyPackets(widthIndex, windowNum, numWindows, magnitudes, packets, windowRemaining);

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
                halt = modifyPackets(widthIndex, windowNum, numWindows, magnitudes, packets, 1 - carryOver);

                // Subtract `1 - carryOver` from `windowRemaining`
                windowRemaining -= 1 - carryOver;

                // Reset `carryOver`
                carryOver = 0;

                // Increment the width index
                widthIndex++;
            }
        }

        // Finally, modify the image according to the packet specifications
        double scale = (double) (height - 1) / 100;

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                // Calculate 'intensity' of the frequency bin at that spot
                int intensity = height - 1 - Math.abs((int) (packets[w][height - h - 1] * scale));
                if (intensity < 0) intensity = 0;

                // Set the pixel value
                pixels[h * width + w] = colourMap[intensity];
            }
        }

        // Return the spectrogram image
        return SwingFXUtils.toFXImage(image, null);
    }

    // Private methods

    /**
     * Helper function that generates the windowed samples of the audio object.
     *
     * @param windowType The window function to use.
     * @return Windowed samples.
     * @throws RuntimeException if <code>frameLength</code> is <b>not</b> a power of 2.
     */
    private float[][] generateWindowedSamples(WindowType windowType) {
        // Check if the frame length is a power of 2
        if (!MiscMath.isInteger(MiscMath.log2(frameLength))) {
            throw new RuntimeException("The frame length has to be a power of 2.");
        }

        // Get the audio object's samples
        float[] samples = audio.getMonoSamples();
        AudioFormat format = audio.getAudioFormat();

        // Generate and return windowed samples
        return Windows.generateWindowedSamples(samples, numSamples, frameLength, hopLength, windowType, format);
    }

    /**
     * Run the FFT on the specified window.
     *
     * @param samplesInWindow Array of samples that are in the window to be processed.
     * @return Array of magnitudes for generation of the spectrogram.
     */
    private double[] processFFTOnWindow(float[] samplesInWindow) {
        // Convert the floats to complex numbers
        Complex[] data = new Complex[samplesInWindow.length];
        for (int i = 0; i < samplesInWindow.length; i++) {
            data[i] = new Complex(samplesInWindow[i], 0);
        }

        // Run the FFT on the data
        Complex[] niz = FFT.fft(data);

        // Generate the magnitude value for the appropriate height
        double numFreqBinsPerHeight = (double) numFrequencyBins / height;
        double[] magnitudes = new double[height];
        double max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;

        for (int i = 0; i < height; i++) {
            // Calculate the start and end indices for the search
            int numBinsToConsider = (int) Math.ceil(numFreqBinsPerHeight);
            int startIndex = (int) Math.floor(i * numFreqBinsPerHeight);
            int endIndex = startIndex + numBinsToConsider - 1;
            double maxModulus = Integer.MIN_VALUE;

            for (int j = startIndex; j <= endIndex; j++) {
                // Compute the modulus of the complex number
                double modulus = niz[j].abs();

                // Compare the current modulus with the maximum modulus
                if (modulus > maxModulus) maxModulus = modulus;
            }

            // Convert to decibel
            if (maxModulus == 0) magnitudes[i] = 0;
            else magnitudes[i] = 20 * Math.log10(maxModulus);  // Todo: use more accurate decibel computation

            // Update min and max
            if (magnitudes[i] > max) max = magnitudes[i];
            if (magnitudes[i] < min) min = magnitudes[i];
        }

        if (min < 0) {
            min = Math.abs(min);
            for (int i = 0; i < height; i++) {
                magnitudes[i] += min;
            }
        }

        return magnitudes;
    }

    /**
     * Calculate the FFT frequency bins.
     *
     * @implNote Adapted from <a href="http://librosa.org/doc/main/_modules/librosa/core/convert.html#fft_frequencies">
     * this Python code</a> from the Librosa library.
     */
    private void calculateBins() {
        // Get the sample rate of the audio object
        float sampleRate = audio.getSampleRate();

        // Calculate the size of the `frequencyBins` array
        numFrequencyBins = frameLength / 2 + 1;

        // Generate the frequency bins array
        frequencyBins = new double[numFrequencyBins];

        for (int i = 0; i < numFrequencyBins; i++) {
            frequencyBins[i] = i * (double) (sampleRate / frameLength);
        }
    }

    /**
     * Generates the colour map for the spectrogram.
     *
     * @param colourScale The colour scale to use for the colourmap.
     */
    // Todo: make colour map be based on 'maximum intensity' and not on heigh
    private void generateColourMap(ColourScale colourScale) {
        // Calculate the number of 'heights' between each successive defined colour in the
        // colour scale
        int amount = height / (colourScale.colours.length - 1) + 1;

        // Generate the colourmap
        int colourIndex = 0;  // Index of the colour in the colour scale
        int colour1 = 0, colour2 = 0;

        for (int i = 0; i < height; i++) {
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
     * Linearly interpolate the two colours by the scaling factor <code>x</code>.
     *
     * @param colour1 First colour.
     * @param colour2 Second colour.
     * @param x       Scaling factor.
     * @return The linearly interpolated colour.<br>
     * If <code>x = 0</code> then this will return <code>colour1</code>.
     * If <code>x = 1</code> then this will return <code>colour2</code>.
     */
    private int lerpColour(int colour1, int colour2, double x) {
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
}
