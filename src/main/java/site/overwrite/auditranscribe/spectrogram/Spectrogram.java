/*
 * Spectrogram.java
 *
 * Created on 2022-02-12
 * Updated on 2022-03-15
 *
 * Description: Spectrogram class.
 */

package site.overwrite.auditranscribe.spectrogram;

import javafx.scene.image.WritableImage;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.plotting.Plotter;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.CQT;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.VQT;
import site.overwrite.auditranscribe.utils.Complex;
import site.overwrite.auditranscribe.utils.UnitConversion;

import java.security.InvalidParameterException;

/**
 * Spectrogram class to handle spectrogram creation.
 */
public class Spectrogram {
    // Constants
    final int BINS_PER_OCTAVE = 24;
    final int NUM_FREQ_BINS = 240;
    final double INTENSITY_PRECISION = 0.001;

    final double MINIMUM_FREQUENCY = UnitConversion.noteToFreq("C0");

    final boolean IS_CQT = false;
    final double GAMMA = 0;  // If not `IS_CQT` and `GAMMA` is 0 then gamma will be determined automatically

    // Attributes
    private final double[] samples;
    private final double sampleRate;

    public final int width;
    public final int height;

    private final int hopLength;

    public double[] frequencyBins;

    /**
     * Creates a spectrogram object.
     *
     * @param audioObj       The audio object.
     * @param numPxPerSecond Number of pixels of the spectrogram dedicated to each second of audio
     * @param imageHeight    Height of the spectrogram.
     * @param hopLength      Number of samples between successive columns.
     * @throws InvalidParameterException If the image height is too large.
     */
    public Spectrogram(Audio audioObj, int numPxPerSecond, int imageHeight, int hopLength) {
        // Update attributes
        sampleRate = audioObj.getSampleRate();
        this.hopLength = hopLength;

        // Set the width and height of the image
        width = (int) (audioObj.getDuration() * numPxPerSecond);
        height = imageHeight;

        // Get the mono samples
        samples = audioObj.getMonoSamples();

        // Get the frequency bins
        frequencyBins = VQT.getFreqBins(NUM_FREQ_BINS, BINS_PER_OCTAVE, MINIMUM_FREQUENCY);
    }

    // Public methods

    /**
     * Generates the spectrogram image for the given audio samples.
     *
     * @param window      The window function to use
     * @param colourScale The colour scale to use for the spectrogram.
     * @return The spectrogram image.
     */
    public WritableImage generateSpectrogram(Window window, ColourScale colourScale) {
        // Perform the spectrogram transform on the samples
        Complex[][] QTMatrix;
        if (IS_CQT) {
            QTMatrix = CQT.cqt(
                    samples, sampleRate, hopLength, MINIMUM_FREQUENCY, NUM_FREQ_BINS, BINS_PER_OCTAVE, window
            );
        } else {
            QTMatrix = VQT.vqt(
                    samples, sampleRate, hopLength, MINIMUM_FREQUENCY, NUM_FREQ_BINS, BINS_PER_OCTAVE, GAMMA, window
            );
        }

        // Compute the magnitudes
        double[][] magnitudes = calculateMagnitudes(QTMatrix);

        // Plot spectrogram data
        Plotter plotter = new Plotter(colourScale, INTENSITY_PRECISION);
        plotter.plot(magnitudes, width, height);

        // Return the writable image
        return plotter.getImage();
    }

    // Private methods

    /**
     * Helper method that calculates the decibel magnitudes of the spectral matrix.
     *
     * @param spectralMatrix The matrix containing the spectral data.
     * @return Matrix containing decibel data.
     */
    private double[][] calculateMagnitudes(Complex[][] spectralMatrix) {
        // Get dimensions of the spectral matrix
        int numRows = spectralMatrix.length;
        int numCols = spectralMatrix[0].length;

        // Get the modulus of every complex number
        double[][] moduli = new double[numRows][numCols];
        double maxModulus = -Double.MAX_VALUE;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                // Get current element's modulus
                double modulus = spectralMatrix[i][j].abs();

                // Set the element at that index
                moduli[i][j] = modulus;

                // Update `maxModulus`
                if (maxModulus < modulus) maxModulus = modulus;
            }
        }

        // Now convert all moduli into decibel numbers
        double[][] magnitudes = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                magnitudes[i][j] = UnitConversion.amplitudeToDecibel(moduli[i][j], maxModulus);
            }
        }

        // Return the magnitudes array
        return magnitudes;
    }
}
