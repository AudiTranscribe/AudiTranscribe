/*
 * Spectrogram.java
 * Description: Class that handles the creation of the spectrogram image.
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

package site.overwrite.auditranscribe.spectrogram;

import javafx.scene.image.WritableImage;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.plotting.Plotter;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.CQT;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.FrequencyBins;
import site.overwrite.auditranscribe.spectrogram.spectral_representations.VQT;
import site.overwrite.auditranscribe.misc.Complex;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import java.util.logging.Level;

/**
 * Class that handles the creation of the spectrogram image.
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

    final CustomTask<?> task;

    /**
     * Initialization method for a <code>Spectrogram</code> object.
     *
     * @param audioObj       The audio object.
     * @param minNoteNumber  Smallest note number.
     * @param maxNoteNumber  Largest note number.
     * @param binsPerOctave  Number of frequency bins per octave.
     * @param hopLength      Number of samples between successive columns.
     * @param numPxPerSecond Number of pixels of the spectrogram dedicated to each second of audio.
     * @param numPxPerOctave Number of pixels allocated for each octave.
     * @param task           The <code>CustomTask</code> object that is handling the generation.
     * @throws ValueException If the value of <code>maxNoteNumber - minNoteNumber + 1</code> is not
     *                        a multiple of 12.
     */
    public Spectrogram(
            Audio audioObj, int minNoteNumber, int maxNoteNumber, int binsPerOctave, int hopLength,
            double numPxPerSecond, double numPxPerOctave, CustomTask<?> task
    ) {
        // Validate that `maxNoteNumber - minNoteNumber + 1` is a multiple of 12
        int numNotes = maxNoteNumber - minNoteNumber + 1;
        if (numNotes % 12 != 0) {
            throw new ValueException(
                    "Number of notes is not a multiple of 12 (i.e. `maxNoteNumber - minNoteNumber + 1` is not a " +
                            "multiple of 12)"
            );
        }

        // Update attributes
        this.minNoteNumber = minNoteNumber;
        this.maxNoteNumber = maxNoteNumber;
        numOctaves = numNotes / 12;

        minFreq = UnitConversionUtils.noteNumberToFreq(minNoteNumber);
        maxFreq = UnitConversionUtils.noteNumberToFreq(maxNoteNumber);

        this.binsPerOctave = binsPerOctave;
        numFreqBins = numOctaves * binsPerOctave;

        sampleRate = audioObj.getSampleRate();
        this.hopLength = hopLength;

        this.task = task;

        MyLogger.log(Level.FINE, "Audio sample rate is " + sampleRate, this.getClass().toString());

        // Set the width and height of the image
        width = (int) (audioObj.getDuration() * numPxPerSecond);
        height = (int) (numOctaves * numPxPerOctave);

        MyLogger.log(
                Level.FINE,
                "Spectrogram width is " + width + " and height is " + height,
                this.getClass().toString()
        );

        // Get the mono samples
        samples = audioObj.getMonoSamples();

        // Get the frequency bins
        frequencyBins = FrequencyBins.getQTFreqBins(numFreqBins, binsPerOctave, minFreq);
    }

    /**
     * Creates a spectrogram object.<br>
     * In this case, this assumes that the samples of the spectrogram object have already been
     * processed and is saved in a 2D array of doubles, representing the matrix of Q-Transform
     * magnitudes. Thus, this object has no access to the actual audio and hence the
     * <code>sampleRate</code> and the <code>duration</code> of the audio must be provided.
     *
     * @param minNoteNumber  Smallest note number.
     * @param maxNoteNumber  Largest note number.
     * @param binsPerOctave  Number of frequency bins per octave.
     * @param hopLength      Number of samples between successive columns.
     * @param numPxPerSecond Number of pixels of the spectrogram dedicated to each second of audio.
     * @param numPxPerOctave Number of pixels allocated for each octave.
     * @param sampleRate     Audio's sampling rate.
     * @param duration       Duration of the audio.
     * @throws ValueException If the value of <code>maxNoteNumber - minNoteNumber + 1</code> is not
     *                        a multiple of 12.
     */
    public Spectrogram(
            int minNoteNumber, int maxNoteNumber, int binsPerOctave, int hopLength, double numPxPerSecond,
            double numPxPerOctave, double sampleRate, double duration, CustomTask<?> task
    ) {
        // Validate that `maxNoteNumber - minNoteNumber + 1` is a multiple of 12
        int numNotes = maxNoteNumber - minNoteNumber + 1;
        if (numNotes % 12 != 0) {
            throw new ValueException(
                    "Number of notes is not a multiple of 12 (i.e. `maxNoteNumber - minNoteNumber + 1` is not a " +
                            "multiple of 12)"
            );
        }

        // Update attributes
        this.minNoteNumber = minNoteNumber;
        this.maxNoteNumber = maxNoteNumber;
        numOctaves = numNotes / 12;

        minFreq = UnitConversionUtils.noteNumberToFreq(minNoteNumber);
        maxFreq = UnitConversionUtils.noteNumberToFreq(maxNoteNumber);

        this.binsPerOctave = binsPerOctave;
        numFreqBins = numOctaves * binsPerOctave;

        this.sampleRate = sampleRate;
        this.hopLength = hopLength;

        this.task = task;

        MyLogger.log(Level.FINE, "Audio sample rate is " + sampleRate, this.getClass().toString());

        // Set the width and height of the image
        width = (int) (duration * numPxPerSecond);
        height = (int) (numOctaves * numPxPerOctave);

        MyLogger.log(
                Level.FINE,
                "Spectrogram width is " + width + " and height is " + height,
                this.getClass().toString()
        );

        // We don't need samples in this case
        samples = null;
        MyLogger.log(
                Level.FINE,
                "Spectrogram creation occurring WITHOUT audio file; samples not available",
                this.getClass().toString()
        );

        // Get the frequency bins
        frequencyBins = FrequencyBins.getQTFreqBins(numFreqBins, binsPerOctave, minFreq);
    }

    // Public methods

    /**
     * Generates the spectrogram magnitudes for the given audio samples.
     *
     * @param windowFunction The window function to use.
     * @return The spectrogram magnitudes.
     * @throws NullPointerException If the maximum frequency value cannot be found in the computed
     *                              Q-transform frequency bins.
     */
    public double[][] getSpectrogramMagnitudes(WindowFunction windowFunction) throws NullPointerException {
        // Perform the spectrogram transform on the samples
        MyLogger.log(Level.FINE, "Starting spectral matrix generation", this.getClass().toString());

        Complex[][] QTMatrix;
        if (IS_CQT) {
            QTMatrix = CQT.cqt(
                    samples, sampleRate, hopLength, minFreq, numFreqBins, binsPerOctave, 0, windowFunction, task
            );
        } else {
            QTMatrix = VQT.vqt(
                    samples, sampleRate, hopLength, minFreq, numFreqBins, binsPerOctave, 0, GAMMA, windowFunction, task
            );
        }

        // Get also the frequency bins of the VQT
        // (Note that CQT and VQT frequency bins are the same)
        double[] freqBins = FrequencyBins.getQTFreqBins(numFreqBins, binsPerOctave, minFreq);

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

        // Compute the magnitudes and return
        MyLogger.log(Level.FINE, "Calculating magnitudes", this.getClass().toString());
        return calculateMagnitudes(QTMatrixFinal);
    }

    /**
     * Generates the spectrogram image for the given Q-Transform magnitude data.
     *
     * @param magnitudes  Magnitudes of the Q-Transform data.
     * @param colourScale The colour scale to use for the spectrogram.
     * @return The spectrogram image.
     */
    public WritableImage generateSpectrogram(double[][] magnitudes, ColourScale colourScale) {
        // Plot spectrogram data
        MyLogger.log(Level.FINE, "Plotting data", this.getClass().toString());
        Plotter plotter = new Plotter(colourScale, INTENSITY_PRECISION);
        plotter.plot(magnitudes, width, height);

        // Return the writable image
        MyLogger.log(Level.FINE, "Returning image", this.getClass().toString());
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
        double maxMagnitude = -Double.MAX_VALUE;

        double[][] magnitudes = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                // Get the decibel value for this amplitude
                double magnitudeVal = UnitConversionUtils.amplitudeToDecibel(moduli[i][j], maxModulus);

                // Add it into the magnitudes array
                magnitudes[i][j] = magnitudeVal;

                // Update maximum magnitude value as needed
                if (maxMagnitude < magnitudeVal) maxMagnitude = magnitudeVal;
            }
        }

        // Now fix the magnitude values
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                magnitudes[i][j] = Math.max(magnitudes[i][j], maxMagnitude - TOP_DB);
            }
        }

        // Return the magnitudes array
        return magnitudes;
    }
}
