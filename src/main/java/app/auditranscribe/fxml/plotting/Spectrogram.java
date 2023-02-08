/*
 * Spectrogram.java
 * Description: Class that handles the creation and plotting of the spectrogram image.
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

package app.auditranscribe.fxml.plotting;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.fxml.plotting.interpolation.AbstractInterpolation;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.io.audt_file.v0x000500.data_encapsulators.QTransformDataObject0x000500;
import app.auditranscribe.misc.Complex;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.signal.FrequencyRangeGeneration;
import app.auditranscribe.signal.representations.QTransform;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.MatrixUtils;
import app.auditranscribe.utils.TypeConversionUtils;
import app.auditranscribe.utils.UnitConversionUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.logging.Level;

/**
 * Class that handles the creation and plotting of the spectrogram image.
 */
@ExcludeFromGeneratedCoverageReport
public class Spectrogram extends LoggableClass {
    // Constants
    final AbstractInterpolation INTERPOLATION_METHOD = Interpolation.BILINEAR.interpolation;

    final double TOP_DB = 80;
    final double INTENSITY_PRECISION = 1e-3;
    final double INVERSE_INTENSITY_PRECISION = 1 / INTENSITY_PRECISION;

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

    public byte[] qTransformBytes;
    public double minMagnitude;
    public double maxMagnitude;

    private CustomTask<?> task;

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
     * @throws ValueException If the value of <code>maxNoteNumber - minNoteNumber + 1</code> is not
     *                        a multiple of 12.
     */
    public Spectrogram(
            Audio audioObj, int minNoteNumber, int maxNoteNumber, int binsPerOctave, int hopLength,
            double numPxPerSecond, double numPxPerOctave
    ) {
        // Validate that `maxNoteNumber - minNoteNumber + 1` is a multiple of 12
        int numNotes = maxNoteNumber - minNoteNumber + 1;
        if (numNotes % 12 != 0) {
            throw new ValueException(
                    "Number of notes is not a multiple of 12 (i.e., `maxNoteNumber - minNoteNumber + 1` is not a " +
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

        log(Level.FINE, "Audio sample rate is " + sampleRate);

        // Set the width and height of the image
        width = (int) (audioObj.getDuration() * numPxPerSecond);
        height = (int) (numOctaves * numPxPerOctave);

        log(Level.FINE, "Spectrogram width is " + width + " and height is " + height);

        // Get the mono samples
        samples = audioObj.getMonoSamples();

        // Get the frequency bins
        frequencyBins = FrequencyRangeGeneration.qTransformFreqBins(numFreqBins, binsPerOctave, minFreq);
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
            double numPxPerOctave, double sampleRate, double duration
    ) {
        // Validate that `maxNoteNumber - minNoteNumber + 1` is a multiple of 12
        int numNotes = maxNoteNumber - minNoteNumber + 1;
        if (numNotes % 12 != 0) {
            throw new ValueException(
                    "Number of notes is not a multiple of 12 (i.e., `maxNoteNumber - minNoteNumber + 1` is not a " +
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

        log(Level.FINE, "Audio sample rate is " + sampleRate);

        // Set the width and height of the image
        width = (int) (duration * numPxPerSecond);
        height = (int) (numOctaves * numPxPerOctave);

        log(Level.FINE, "Spectrogram width is " + width + " and height is " + height);

        // We don't need samples in this case
        samples = null;
        log(Level.FINE, "Spectrogram creation occurring WITHOUT audio file; samples not available");

        // Get the frequency bins
        frequencyBins = FrequencyRangeGeneration.qTransformFreqBins(numFreqBins, binsPerOctave, minFreq);
    }

    // Getter/setter methods
    public void setTask(CustomTask<?> task) {
        this.task = task;
    }


    // Public methods

    /**
     * Generates the spectrogram image.<br>
     * Assumes an <code>Audio</code> object was provided in the constructor, as that object's data
     * will be used to generate the magnitude data, before plotting the spectrogram.
     *
     * @param windowFunction The signal window function to use on the signal data.
     * @param colourScale    The colour scale to use for the spectrogram.
     * @return The spectrogram image.
     */
    public WritableImage generateSpectrogram(SignalWindow windowFunction, ColourScale colourScale) {
        double[][] magnitudes = generateMagnitudes(windowFunction);

        Triple<Byte[], Double, Double> convertedTuple =
                QTransformDataObject0x000500.magnitudesToByteData(magnitudes, task);
        qTransformBytes = TypeConversionUtils.toByteArray(convertedTuple.value0());
        minMagnitude = convertedTuple.value1();
        maxMagnitude = convertedTuple.value2();

        return plot(magnitudes, generateColourMap(colourScale));
    }

    /**
     * Generates the spectrogram image.<br>
     * Assumes that the attributes <code>qTransformBytes</code>, <code>minMagnitude</code>, and
     * <code>maxMagnitude</code> have been set. These values will be used to reconstruct the
     * magnitude data, which will then be used to plot the spectrogram.
     *
     * @param colourScale The colour scale to use for the spectrogram.
     * @return The spectrogram image.
     */
    public WritableImage generateSpectrogram(ColourScale colourScale) {
        double[][] magnitudes = QTransformDataObject0x000500.byteDataToMagnitudes(
                qTransformBytes, minMagnitude, maxMagnitude
        );
        return plot(magnitudes, generateColourMap(colourScale));
    }

    // Private methods

    /**
     * Helper method that generates the spectrogram magnitudes for the given audio samples.
     *
     * @param windowFunction The signal window function to use.
     * @return The spectrogram magnitudes.
     * @throws NullPointerException If the maximum frequency value cannot be found in the computed
     *                              Q-transform frequency bins.
     */
    private double[][] generateMagnitudes(SignalWindow windowFunction) throws NullPointerException {
        // Perform the spectrogram transform on the samples
        log(Level.FINE, "Starting spectral matrix generation");

        Complex[][] QTMatrix;
        if (IS_CQT) {
            QTMatrix = QTransform.cqt(
                    samples, sampleRate, hopLength, minFreq, numFreqBins, binsPerOctave, 0, windowFunction, task
            );
        } else {
            QTMatrix = QTransform.vqt(
                    samples, sampleRate, hopLength, minFreq, numFreqBins, binsPerOctave, 0, GAMMA,
                    windowFunction, task
            );
        }

        // Get also the frequency bins of the VQT
        // (Note that CQT and VQT frequency bins are the same)
        double[] freqBins = FrequencyRangeGeneration.qTransformFreqBins(numFreqBins, binsPerOctave, minFreq);

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
        log(Level.FINE, "Calculating magnitudes");
        return calculateMagnitudes(QTMatrixFinal);
    }

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

    /**
     * Helper method that generates the colour map for the spectrogram.
     *
     * @param colourScale The colour scale to use for plotting.
     * @return An array of integers, representing the color map generated from the provided colour
     * scale.
     */
    private int[] generateColourMap(ColourScale colourScale) {
        // Determine number of different colours to use to represent the intensities
        int numDifferentColours = (int) INVERSE_INTENSITY_PRECISION + 1;  // To account for endpoints

        // Define the colourmap array
        int[] colourMap = new int[numDifferentColours];

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

            // Calculate how far we are between the two colours
            // (0 means entirely `colour1`; 1 means entirely `colour2`)
            double x = (double) (i % amount) / amount;

            // Compute the colour that should be defined at this height
            int color = lerpColour(colour1, colour2, x);

            // Update the colourmap
            colourMap[i] = color;
        }

        log(Level.FINE, "Colourmap generated");
        return colourMap;
    }

    /**
     * Helper method that linearly interpolates two colours by the scaling factor <code>alpha</code>.
     *
     * @param colour1 First colour.
     * @param colour2 Second colour.
     * @param alpha   Scaling factor.
     * @return The linearly interpolated colour.<br>
     * If <code>alpha = 0</code> then this will return <code>colour1</code>.
     * If <code>alpha = 1</code> then this will return <code>colour2</code>.
     */
    private static int lerpColour(int colour1, int colour2, double alpha) {
        // Lerp the individual red, green, and blue components
        int r = MathUtils.intLerp(((colour1 & 0xFF0000) >> 16), ((colour2 & 0xFF0000) >> 16), alpha);
        int g = MathUtils.intLerp(((colour1 & 0x00FF00) >> 8), ((colour2 & 0x00FF00) >> 8), alpha);
        int b = MathUtils.intLerp((colour1 & 0x0000FF), (colour2 & 0x0000FF), alpha);

        // Concatenate them together to form the final colour
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Helper method that plots the given magnitudes on a <code>WritableImage</code>.
     *
     * @param magnitudes Magnitude data of the spectrogram as a 2D array. Dimensions of the array
     *                   should be of the form (Number of VQT Bins, Number of Windows).
     * @param colourMap  Colour map to use when plotting the spectrogram.
     * @return Writable spectrogram image.
     */
    private WritableImage plot(double[][] magnitudes, int[] colourMap) {
        // Define the image that will show the spectrogram
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        log(Level.FINE, "Defined buffer image");

        // Get the pixels of the spectrogram image
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        log(Level.FINE, "Got pixels of buffer image");

        // Generate the values of each packet on the image
        /*
         * Note on terminology used here:
         * - A packet represents the magnitude data for one pixel.
         * - A packet does NOT contain the RGB values for a pixel. It only contains a `double` value
         *   representing the relative 'intensity' that should be shown on the pixel.
         */
        double[][] packets = INTERPOLATION_METHOD.interpolate(magnitudes, height, width);
        log(Level.FINE, "Interpolated spectrogram magnitudes");

        // Transpose packets
        // (Make dimensions (width, height) instead of (height, width) for easier indexing)
        packets = MatrixUtils.transpose(packets);
        log(Level.FINE, "Image packets generated");

        // Get min and max of packet values
        double minPacketVal = Double.MAX_VALUE;
        double maxPacketVal = -Double.MAX_VALUE;

        for (double[] row : packets) {
            for (double packet : row) {
                if (minPacketVal > packet) minPacketVal = packet;
                if (maxPacketVal < packet) maxPacketVal = packet;
            }
        }
        log(Level.FINE, "Got min and max packet values");

        // Normalise packet values
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                packets[w][h] = MathUtils.normalize(packets[w][h], minPacketVal, maxPacketVal);
            }
        }
        log(Level.FINE, "Image packets normalised");

        // Finally, modify the image according to the packet specifications
        int numDifferentColours = colourMap.length;

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                // Calculate intensity of the frequency bin at that spot
                int intensity = (int) Math.ceil(packets[w][height - h - 1] * INVERSE_INTENSITY_PRECISION);

                // Set the pixel value
                pixels[h * width + w] = colourMap[numDifferentColours - intensity - 1];  // Reverse intensity order
            }
        }
        log(Level.FINE, "Image pixels set");

        // Return the writable image
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
