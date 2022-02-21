/*
 * Spectrogram.java
 *
 * Created on 2022-02-12
 * Updated on 2022-02-22
 *
 * Description: Spectrogram class.
 */

package site.overwrite.auditranscribe.spectrogram;

import javafx.scene.image.WritableImage;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.WindowType;
import site.overwrite.auditranscribe.audio.Windows;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.MiscMath;

import javax.sound.sampled.AudioFormat;
import java.security.InvalidParameterException;

/**
 * Spectrogram class to handle spectrogram creation.
 */
public class Spectrogram {
    // Attributes
    private final Audio audio;
    private final int numSamples;

    public final int height;
    public final int width;

    private final int frameLength;
    private final int hopLength;

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
        height = imageHeight;

        this.frameLength = frameLength;
        this.hopLength = hopLength;

        // Calculate the width of the image
        width = (int) (audioObj.getDuration() * numPxPerSecond);

        // Get the number of mono samples
        numSamples = audioObj.getNumMonoSamples();

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
        // Generate windowed samples
        float[][] windowedSamples = generateWindowedSamples(windowType);

        // Get the number of windows
        int numWindows = windowedSamples.length;

        // Generate FFT magnitudes
        double[][] magnitudes = new double[numWindows][height];

        for (int i = 0; i < numWindows; i++) {
            magnitudes[i] = processFFTOnWindow(windowedSamples[i]);
        }

        // Plot spectrogram data
        Plotter plotter = new Plotter(colourScale, 0.001);  // Todo: make `intensityPrecision` variable instead of constant
        plotter.plot(magnitudes, width, height);

        // Return the writable image
        return plotter.getImage();
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
            else magnitudes[i] = 10 * Math.log10(maxModulus);  // Todo: use more accurate decibel computation

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
}
