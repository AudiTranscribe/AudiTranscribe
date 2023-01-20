/*
 * PlottingHelpers.java
 * Description: Helper functions for plotting.
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

package app.auditranscribe.plotting;

import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.UnitConversionUtils;

/**
 * Helper functions for plotting.
 */
public final class PlottingHelpers {
    private PlottingHelpers() {
        // Private constructor to signal this is a utility class
    }

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

    /**
     * Converts a height on the spectrogram to a note number.<br>
     * Note that the height returned is measured <b>from the top of the screen</b>. So a height of
     * 123 for a spectrogram pane with maximum height 200 is actually 77 <b>from the bottom</b>.
     *
     * @param height     Height on the spectrogram pane.
     * @param minNoteNum Smallest note number. This will be assigned height 0.
     * @param maxNoteNum Highest note number. This will be assigned the maximum height.
     * @param maxHeight  Maximum height.
     * @return Integer representing the note number.
     */
    public static double heightToNoteNum(double height, int minNoteNum, int maxNoteNum, double maxHeight) {
        // Convert all the notes to frequencies
        double minFreq = UnitConversionUtils.noteNumberToFreq(minNoteNum);
        double maxFreq = UnitConversionUtils.noteNumberToFreq(maxNoteNum);
        double freq = heightToFreq(height, minFreq, maxFreq, maxHeight);

        // Now get the estimated note number
        return UnitConversionUtils.freqToNoteNumber(freq);
    }

    /**
     * Calculates the difference in height between two consecutive notes.
     *
     * @param height     Height of the pane.
     * @param minNoteNum Smallest note number.
     * @param maxNoteNum Largest note number.
     * @return Difference in height between two consecutive notes.
     */
    public static double getHeightDifference(double height, double minNoteNum, double maxNoteNum) {
        return height / (maxNoteNum - minNoteNum);
    }
}
