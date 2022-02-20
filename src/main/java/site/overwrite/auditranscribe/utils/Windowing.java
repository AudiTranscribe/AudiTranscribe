/*
 * Windowing.java
 *
 * Created on 2022-02-16
 * Updated on 2022-02-20
 *
 * Description: Windowing utilities, especially for spectrogram data.
 *
 * Todo: this is a terrible class name; need to rename.
 */

package site.overwrite.auditranscribe.utils;

/**
 * Windowing utilities for spectrogram data.
 */
public class Windowing {
    /**
     * Slice a data array into (overlapping) frames.
     * @param array         Array to frame.
     * @param frameLength   Length of the frame.
     * @param hopLength     Number of steps to advance between frames.
     * @return Framed view of <code>array</code>.
     * @implNote See <a href="https://stackoverflow.com/a/38163917">this StackOverflow answer</a>
     *           for implementation details in Python.
     */
    public static float[][] frame(float[] array, int frameLength, int hopLength) {
        // Calculate the length of the framed array
        int finalArrayLength = (int) Math.ceil((double) array.length / hopLength);

        // Create the blank array to store the framed data in
        float[][] framed = new float[finalArrayLength][frameLength];

        // Fill in the array
        int length = array.length;

        for (int index = 0, endFrameIndex = 0; endFrameIndex < length; index++, endFrameIndex += hopLength) {  // Iterate through the `framed` array
            for (int i = 0; i < frameLength; i++) {  // Iterate through the frame
                // Validate the value of `i`
                if (i + endFrameIndex < length) {
                    framed[index][i] = array[i + endFrameIndex];
                }
            }
        }

        // Return the `framed` array
        return framed;
    }
}
