/*
 * Spectrogram.java
 *
 * Created on 2022-02-12
 * Updated on 2022-04-16
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spectrogram class to handle spectrogram creation.
 */
public class Spectrogram {
    // Constants
    final double INTENSITY_PRECISION = 0.001;
    final double TOP_DB = 80;

    final boolean IS_CQT = false;
    final double GAMMA = 0;  // If not `IS_CQT` and `GAMMA` is 0 then gamma will be determined automatically

    // Attributes
    public final int minNoteNumber;
    public final int maxNoteNumber;

    final double minFreq;
    final double maxFreq;

    public final int numOctaves;
    public final int binsPerOctave;
    public final int numFreqBins;

    final double[] samples;
    final double sampleRate;

    public final int width;
    public final int height;

    final int hopLength;

    public double[] frequencyBins;

    final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a spectrogram object.
     *
     * @param audioObj       The audio object.
     * @param minNoteNumber  Smallest note number.
     * @param maxNoteNumber  Largest note number.
     * @param binsPerOctave  Number of frequency bins per octave.
     * @param numPxPerSecond Number of pixels of the spectrogram dedicated to each second of audio.
     * @param numPxPerOctave Number of pixels allocated for each octave.
     * @param hopLength      Number of samples between successive columns.
     * @throws InvalidParameterException If <code>maxNoteNumber - minNoteNumber + 1</code> is not a
     *                                   multiple of 12.
     * @throws InvalidParameterException If the image height is too large.
     */
    public Spectrogram(
            Audio audioObj, int minNoteNumber, int maxNoteNumber, int binsPerOctave, double numPxPerSecond,
            double numPxPerOctave, int hopLength
    ) {
        // Validate that `maxNoteNumber - minNoteNumber + 1` is a multiple of 12
        int numNotes = maxNoteNumber - minNoteNumber + 1;
        if (numNotes % 12 != 0) {
            throw new InvalidParameterException(
                    "Number of notes is not a multiple of 12 (i.e. `maxNoteNumber - minNoteNumber + 1` is not a " +
                            "multiple of 12)"
            );
        }

        // Update attributes
        this.minNoteNumber = minNoteNumber;
        this.maxNoteNumber = maxNoteNumber;
        numOctaves = numNotes / 12;

        minFreq = UnitConversion.noteNumberToFreq(minNoteNumber);
        maxFreq = UnitConversion.noteNumberToFreq(maxNoteNumber);

        this.binsPerOctave = binsPerOctave;
        numFreqBins = numOctaves * binsPerOctave;

        sampleRate = audioObj.getSampleRate();
        this.hopLength = hopLength;

        logger.log(Level.FINE, "Audio sample rate = " + sampleRate);

        // Set the width and height of the image
        width = (int) (audioObj.getDuration() * numPxPerSecond);
        height = (int) (numOctaves * numPxPerOctave);

        logger.log(Level.FINE, "Spectrogram width = " + width + " and height = " + height);

        // Get the mono samples
        samples = audioObj.getMonoSamples();

        // Get the frequency bins
        frequencyBins = VQT.getFreqBins(numFreqBins, binsPerOctave, minFreq);
    }

    // Public methods

    /**
     * Generates the spectrogram image for the given audio samples.
     *
     * @param window      The window function to use.
     * @param colourScale The colour scale to use for the spectrogram.
     * @return The spectrogram image.
     * @throws NullPointerException If the maximum frequency value cannot be found in the computed
     *                              Q-transform frequency bins.
     */
    public WritableImage generateSpectrogram(Window window, ColourScale colourScale) {
        // Perform the spectrogram transform on the samples
        logger.log(Level.FINE, "Starting spectral matrix generation");
        Complex[][] QTMatrix;
        if (IS_CQT) {
            QTMatrix = CQT.cqt(
                    samples, sampleRate, hopLength, minFreq, numFreqBins, binsPerOctave, window
            );
        } else {
            QTMatrix = VQT.vqt(
                    samples, sampleRate, hopLength, minFreq, numFreqBins, binsPerOctave, GAMMA, window
            );
        }

        // Get also the frequency bins of the VQT
        double[] freqBins = VQT.getFreqBins(numFreqBins, binsPerOctave, minFreq);  // CQT and VQT bins are the same

        // Find the highest permitted frequency bin
        int highestPermittedIndex = -1;
        for (int i = numFreqBins - 1; i > 0; i--) {
            if (freqBins[i] - maxFreq <= 1e-8) {  // 1e-8 is the 'epsilon' value
                highestPermittedIndex = i;
                break;
            }
        }

        if (highestPermittedIndex == -1) {
            throw new NullPointerException("Highest frequency " + maxFreq + " cannot be found in frequency bins");
        }

        // Keep only required frequency bins from the `QTMatrix`
        Complex[][] QTMatrixFinal = new Complex[highestPermittedIndex + 1][QTMatrix[0].length];

        for (int i = 0; i <= highestPermittedIndex; i++) {
            System.arraycopy(QTMatrix[i], 0, QTMatrixFinal[i], 0, QTMatrix[0].length);
        }

        // Compute the magnitudes
        logger.log(Level.FINE, "Calculating magnitudes");
        double[][] magnitudes = calculateMagnitudes(QTMatrixFinal);

        // Plot spectrogram data
        logger.log(Level.FINE, "Plotting data");
        Plotter plotter = new Plotter(colourScale, INTENSITY_PRECISION);
        plotter.plot(magnitudes, width, height);

        // Return the writable image
        logger.log(Level.FINE, "Returning image");
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
        double maxDB = -Double.MAX_VALUE;

        double[][] magnitudes = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                // Get the decibel value for this amplitude
                double dbVal = UnitConversion.amplitudeToDecibel(moduli[i][j], maxModulus);

                // Add it into the magnitudes array
                magnitudes[i][j] = dbVal;

                // Update maximum decibel value as needed
                if (maxDB < dbVal) maxDB = dbVal;
            }
        }

        // Now fix the decibel values
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                magnitudes[i][j] = Math.max(magnitudes[i][j], maxDB - TOP_DB);
            }
        }

        // Return the magnitudes array
        return magnitudes;
    }
}
