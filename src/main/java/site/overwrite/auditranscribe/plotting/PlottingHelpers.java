/*
 * PlottingHelpers.java
 *
 * Created on 2022-03-19
 * Updated on 2022-05-28
 *
 * Description: Helper functions for plotting.
 */

package site.overwrite.auditranscribe.plotting;

import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

/**
 * Helper functions for plotting.
 */
public class PlottingHelpers {
    // Public methods

    /**
     * Converts a frequency to a specific height on the canvas.
     *
     * @param freq      Original frequency.
     * @param minFreq   Lowest possible frequency. This will be assigned height 0.
     * @param maxFreq   Highest possible frequency. This will be assigned the maximum height.
     * @param maxHeight Maximum height.
     * @return Double representing the height on the spectrogram that the original frequency would
     * be at.
     */
    public static double freqToHeight(double freq, double minFreq, double maxFreq, double maxHeight) {
        // Take log base 2 of the frequency, the minimum frequency and maximum frequency
        double loggedFrequency = MathUtils.log2(freq);
        double loggedMinimum = MathUtils.log2(minFreq);
        double loggedMaximum = MathUtils.log2(maxFreq);

        // Scale accordingly and return. Since (0, 0) is the upper left we have to adjust to make (0, 0) to be the
        // lower left corner instead
        return (1 - (loggedFrequency - loggedMinimum) / (loggedMaximum - loggedMinimum)) * maxHeight;
    }

    /**
     * Converts a height on the spectrogram pane to an estimated frequency.
     *
     * @param height    Height on the spectrogram pane.
     * @param minFreq   Lowest possible frequency. This will be assigned height 0.
     * @param maxFreq   Highest possible frequency. This will be assigned the maximum height.
     * @param maxHeight Maximum height.
     * @return Estimated frequency.
     */
    public static double heightToFreq(double height, double minFreq, double maxFreq, double maxHeight) {
        // Compute the ratio of the given height and the spectrogram's height
        double heightRatio = height / maxHeight;

        // Return the estimated frequency
        return Math.pow(minFreq, heightRatio) * Math.pow(maxFreq, 1 - heightRatio);
    }

    /**
     * Converts the note number to a height on the spectrogram.
     *
     * @param noteNum    Original note number.
     * @param minNoteNum Smallest note number. This will be assigned height 0.
     * @param maxNoteNum Highest note number. This will be assigned the maximum height.
     * @param maxHeight  Maximum height.
     * @return Double representing the height on the spectrogram that the original frequency would
     * be at.
     */
    public static double noteNumToHeight(int noteNum, int minNoteNum, int maxNoteNum, double maxHeight) {
        // Convert all the notes to frequencies
        double freq = UnitConversionUtils.noteNumberToFreq(noteNum);
        double minFreq = UnitConversionUtils.noteNumberToFreq(minNoteNum);
        double maxFreq = UnitConversionUtils.noteNumberToFreq(maxNoteNum);

        // Now get the assigned height
        return freqToHeight(freq, minFreq, maxFreq, maxHeight);
    }
}
