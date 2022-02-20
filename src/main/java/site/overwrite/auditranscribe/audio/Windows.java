/*
 * Windows.java
 *
 * Created on 2022-02-13
 * Updated on 2022-02-20
 *
 * Description: Class to implement audio window functions.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.utils.Windowing;

import javax.sound.sampled.AudioFormat;
import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Class to implement audio window functions.
 */
public class Windows {
    // Public methods

    /**
     * Converts the samples in the <code>samples</code> array into windowed samples.
     *
     * @param samples     Array containing audio samples.
     * @param numSamples  Number of elements in the <code>samples</code> array.
     * @param frameLength Length of the frame.
     * @param hopLength   Number of steps to advance between frames.
     * @param type        The window function to use.
     * @param audioFormat Format of the audio file.
     * @return 2D array of windowed samples of shape (<code>ceil(numSamples/hopLength)</code>,
     * <code>frameLength</code>).
     * @throws InvalidParameterException If the window type is invalid.
     */
    public static float[][] generateWindowedSamples(float[] samples, int numSamples, int frameLength, int hopLength,
                                                    WindowType type, AudioFormat audioFormat) {
        // Generate the frames of the sample
        float[][] frames = Windowing.frame(samples, frameLength, hopLength);

        // Get the number of windows and number of channels
        int numWindows = frames.length;
        int numChannels = audioFormat.getChannels();

        // Apply window function to the samples
        float[][] windowedSamples = new float[numWindows][frameLength];

        for (int windowNum = 0; windowNum < numWindows; windowNum++) {
            // Determine the number of valid samples
            int numValidSamples;

            if (windowNum != numWindows - 1) {
                numValidSamples = frameLength;
            } else {
                numValidSamples = frameLength - (numSamples % frameLength);  // Final frame length
            }

            // Get a copy of the samples that need to be windowed
            float[] tempSamples = Arrays.copyOf(frames[windowNum], frameLength);

            // Apply window function on those samples
            if (type == WindowType.SINE_WINDOW) {
                sineWindow(tempSamples, numValidSamples, numChannels);
            } else if (type == WindowType.HANN_WINDOW) {
                hannWindow(tempSamples, numValidSamples, numChannels);
            } else if (type == WindowType.HAMMING_WINDOW) {
                hammingWindow(tempSamples, numValidSamples, numChannels);
            } else if (type != WindowType.NONE) {
                throw new InvalidParameterException("Unknown window type " + type.toString());
            }

            // Insert these samples into the `windowedSamples` array
            windowedSamples[windowNum] = tempSamples;
        }

        // Return the windowed samples
        return windowedSamples;
    }

    // Private methods

    /**
     * Helper method to apply the sine window function to the audio samples.<br>
     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
     * passed into the method.
     *
     * @param samples         Array containing audio samples.
     * @param numValidSamples Number of valid samples that can be windowed.
     * @param numChannels     Number of channels in the audio file.
     * @see <a href="https://en.wikipedia.org/wiki/Window_function#Sine_window">This article</a>
     * about the sine window function.
     */
    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
    private static void sineWindow(float[] samples, int numValidSamples, int numChannels) {
        int sampleLen = numValidSamples / numChannels;

        for (int ch = 0, k, i; ch < numChannels; ch++) {
            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
                samples[i] *= Math.sin(Math.PI * k++ / (sampleLen - 1));
            }
        }
    }

    /**
     * Helper method to apply the Hann window function to the audio samples.<br>
     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
     * passed into the method.
     *
     * @param samples         Array containing audio samples.
     * @param numValidSamples Number of valid samples that can be windowed.
     * @param numChannels     Number of channels in the audio file.
     * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This article</a>
     * about the Hann window function.
     */
    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
    private static void hannWindow(float[] samples, int numValidSamples, int numChannels) {
        for (int ch = 0, k, i; ch < numChannels; ch++) {
            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
                samples[i] = (float) (samples[i] * 0.5 * (1 - Math.cos(2.0 * Math.PI * k++ / samples.length)));
            }
        }
    }

    /**
     * Helper method to apply the Hamming window function to the audio samples.<br>
     * Note that this is an in-place method, i.e. it modifies the <code>samples</code> array that is
     * passed into the method.
     *
     * @param samples         Array containing audio samples.
     * @param numValidSamples Number of valid samples that can be windowed.
     * @param numChannels     Number of channels in the audio file.
     * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This article</a>
     * about the Hamming window function.
     */
    // Todo: see if can remove `numChannels` (since we know that the samples is mono)
    private static void hammingWindow(float[] samples, int numValidSamples, int numChannels) {
        for (int ch = 0, k, i; ch < numChannels; ch++) {
            for (i = ch, k = 0; i < numValidSamples; i += numChannels) {
                samples[i] = (float) (samples[i] * (0.54 - 0.46 * Math.cos(2.0 * Math.PI * k++ / samples.length)));
            }
        }
    }
}
