/*
 * AudioHelpers.java
 * Description: Helper methods used in the `audio` package.
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

package app.auditranscribe.audio;

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.signal.resampling_filters.Filter;

import javax.sound.sampled.AudioFormat;
import java.security.InvalidParameterException;

/**
 * Helper methods used in the <code>audio</code> package.
 */
@ExcludeFromGeneratedCoverageReport
public final class AudioHelpers {
    private AudioHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Audio sampling methods

    /**
     * Resample a signal from <code>srOld</code> to <code>srNew</code>.
     *
     * @param x      Original signal that needs to be resampled.
     * @param srOld  Old sample rate of the signal.
     * @param srNew  New sample rate of the signal.
     * @param filter Resampling filter to apply to the signal.
     * @param scale  Whether to scale the final sample array.
     * @return Array representing the resampled signal.
     * @throws ValueException If: <ul>
     *                        <li>
     *                        Either <code>srOld</code> or <code>srNew</code> is not positive.
     *                        </li>
     *                        <li>
     *                        The input signal length is too short to be resampled to the desired
     *                        sample rate.
     *                        </li>
     *                        </ul>
     * @implNote See <a href="https://github.com/bmcfee/resampy/blob/1d1a08/resampy/core.py">
     * Resampy's resampling source code</a>, where the main core of the code was taken from.
     */
    public static double[] resample(
            double[] x, double srOld, double srNew, Filter filter, boolean scale
    ) throws ValueException {
        // Validate sample rates
        if (srOld <= 0) throw new ValueException("Invalid old sample rate " + srOld);
        if (srNew <= 0) throw new ValueException("Invalid new sample rate " + srNew);

        // Calculate sample ratio
        double ratio = srNew / srOld;

        // Calculate final array length and check if it is okay
        int finalLength = (int) (ratio * x.length);
        if (finalLength < 1) {
            throw new InvalidParameterException(
                    "Input signal length of " + x.length + " too small to resample from " + srOld + " to " + srNew
            );
        }

        // Generate output array in storage
        double[] y = new double[finalLength];

        // Get the interpolation window and precision of the specified resampling filter
        double[] interpWin = filter.filter.getHalfWindow();
        int precision = filter.filter.getPrecision();

        int interpWinLength = interpWin.length;

        // Treat the interpolation window
        if (ratio < 1) {
            for (int i = 0; i < interpWinLength; i++) {
                interpWin[i] *= ratio;
            }
        }

        // Calculate interpolation deltas
        double[] interpDeltas = new double[interpWinLength];

        for (int i = 0; i < interpWinLength - 1; i++) {
            interpDeltas[i] = interpWin[i + 1] - interpWin[i];
        }

        // Run resampling
        resamplingHelper(x, y, ratio, interpWin, interpDeltas, precision);

        // Fix the length of the samples array
        int correctedNumSamples = (int) Math.ceil(ratio * x.length);
        double[] yHat = new double[correctedNumSamples];
        System.arraycopy(y, 0, yHat, 0, Math.min(finalLength, correctedNumSamples));

        // Handle rescaling
        if (scale) {
            for (int i = 0; i < correctedNumSamples; i++) {
                yHat[i] /= Math.sqrt(ratio);
            }
        }

        // Return the resampled array
        return yHat;
    }

    // Audio byte conversion

    /**
     * Unpacks the raw audio bytes in the array <code>bytes</code> into audio sample data.
     *
     * @param bytes         Array of bytes that is read in from the audio file.
     * @param numValidBytes Number of valid bytes in the <code>bytes</code> array.
     * @param bitsPerSample Sample size in bits.
     * @param audioFormat   Format of the audio.
     * @return Array that stores the audio samples.
     * @implNote See the <a href="https://tinyurl.com/stefanSpectrogramOriginal">original
     * implementation on GitHub</a>. This code was largely adapted from that source.
     */
    public static float[] unpackBytes(
            byte[] bytes, int numValidBytes, int bitsPerSample, AudioFormat audioFormat
    ) {
        // Calculate the number of bytes needed to store each sample
        int bytesPerSample = numBytesForNumBits(bitsPerSample);

        // Declare the samples array
        float[] samples = new float[bytes.length / bytesPerSample];

        // Check if we need to process the `samples` array
        AudioFormat.Encoding encoding = audioFormat.getEncoding();
        if (encoding != AudioFormat.Encoding.PCM_SIGNED && encoding != AudioFormat.Encoding.PCM_UNSIGNED) {
            // `samples` is already good; no need to process
            return samples;
        }

        // Declare a transfer array for moving data within the function
        long[] transfer = new long[samples.length];

        /*
         * This isn't the most DRY way to do this, but it's more efficient. The helper array `transfer` allows the logic
         * to be split up without being too repetitive.
         *
         * There are two loops converting bytes to raw long samples. Integral primitives in Java get sign extended when
         * they are promoted to a larger type, so the `& 0xffL` mask keeps them intact.
         */
        if (audioFormat.isBigEndian()) {
            for (int i = 0, k = 0, b; i < numValidBytes; i += bytesPerSample, k++) {
                // Reset the current element's value to zero, so what was originally in `transfer` doesn't matter
                transfer[k] = 0L;

                // Update transfer
                int least = i + bytesPerSample - 1;
                for (b = 0; b < bytesPerSample; b++) {
                    transfer[k] |= (bytes[least - b] & 0xffL) << (8 * b);
                }
            }
        } else {
            for (int i = 0, k = 0, b; i < numValidBytes; i += bytesPerSample, k++) {
                // Reset the current element's value to zero, so what was originally in `transfer` doesn't matter
                transfer[k] = 0L;

                // Update transfer
                for (b = 0; b < bytesPerSample; b++) {
                    transfer[k] |= (bytes[i + b] & 0xffL) << (8 * b);
                }
            }
        }

        // Calculate scaling factor to normalize the samples to the interval [-1, 1]
        long fullScale = (long) Math.pow(2., bitsPerSample - 1);

        // The OR is not quite enough to convert; signage needs to be corrected
        if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
            /*
             * If the samples were signed, they must be extended to the 64-bit long.
             *
             * The arithmetic right shift in Java will fill the left bits with 1's if the Most Significant Bit (MSB) is
             * set, so sign extend by first shifting left so that if the sample is supposed to be negative, it will
             * shift the sign bit in to the 64-bit MSB then shift back and fill with 1's.
             *
             * As an example, imagine these were 4-bit samples originally and the destination is 8-bit. If we have a
             * hypothetical sample -5 that ought to be negative, the left shift looks like this:
             *
             *    00001011
             * <<  (8 - 4)
             * ===========
             *    10110000
             *
             * (Except the destination is 64-bit and the original bit depth from the file could be anything.)
             *
             * And the right shift now fills with 1's:
             *
             *    10110000
             * >>  (8 - 4)
             * ===========
             *    11111011
             */

            long signShift = 64L - bitsPerSample;
            for (int i = 0; i < transfer.length; i++) {
                transfer[i] = ((transfer[i] << signShift) >> signShift);
            }
        } else {
            /*
             * Unsigned samples are easier since they will be read correctly in to the long. So just sign them by
             * subtracting `fullScale` (which equals `Math.pow(2., bitsPerSample - 1)`) so the center is 0.
             */

            for (int i = 0; i < transfer.length; i++) {
                transfer[i] -= fullScale;
            }
        }

        // Finally, normalise range to [-1, 1]
        for (int i = 0; i < transfer.length; i++) {
            samples[i] = (float) transfer[i] / fullScale;
        }

        return samples;
    }

    /**
     * Packs the provided samples into raw byte data.
     *
     * @param samples       Samples to pack into byte data.
     * @param bitsPerSample Sample size in bits.
     * @param audioFormat   Format of the audio.
     * @return Audio bytes.
     */
    public static byte[] packBytes(float[] samples, int bitsPerSample, AudioFormat audioFormat) {
        // Calculate the number of bytes needed to store each sample
        int bytesPerSample = numBytesForNumBits(bitsPerSample);

        // Declare a transfer array for moving data within the function
        long[] transfer = new long[samples.length];

        // Calculate scaling factor to normalize the samples to the interval [-1, 1]
        long fullScale = (long) Math.pow(2., bitsPerSample - 1);

        // Un-normalize samples
        for (int i = 0; i < transfer.length; i++) {
            transfer[i] = (long) (samples[i] * fullScale);
        }

        // Fix signing
        AudioFormat.Encoding encoding = audioFormat.getEncoding();

        if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
            /*
             * De-extend the samples by truncating the extension from the 64-bit long.
             *
             * The number of bits that were extended is `64L - bitsPerSample`. So there are that many unused bytes
             * before the actual sample value. Hence, a bitwise 'and' with the number with `bitsPerSample` 1s at the end
             * should yield the original sample.
             *
             * As an example, imagine these were 4-bit samples originally and the destination is 8-bit. If we have a
             * hypothetical sample -5, the 'unpacked' value would be
             *      11111011
             *
             * The mask that we will be considering has four 1's at the end
             *      00001111
             *
             * so the original sample can be retrieved by performing a bitwise 'and':
             *      11111011
             *    & 00001111
             * =============
             *      00001011
             */

            long bitmask = (1L << bitsPerSample) - 1;  // Generate `bitsPerSample` 1s

            for (int i = 0; i < transfer.length; i++) {
                transfer[i] &= bitmask;  // Apply bitmask to each
            }
        } else {
            // Convert the signed values to unsigned values by adding the `fullScale` value
            for (int i = 0; i < transfer.length; i++) {
                transfer[i] += fullScale;
            }
        }

        // Convert the long values back into bytes
        byte[] bytes = new byte[transfer.length * bytesPerSample];

        if (audioFormat.isBigEndian()) {
            for (int i = 0, k = 0, b; i < bytes.length; i += bytesPerSample, k++) {
                int least = i + bytesPerSample - 1;
                for (b = 0; b < bytesPerSample; b++) {
                    bytes[least - b] = (byte) ((transfer[k] >> (8 * b)) & 0xffL);  // `0xffL` removes 'higher' values
                }
            }
        } else {
            for (int i = 0, k = 0, b; i < bytes.length; i += bytesPerSample, k++) {
                for (b = 0; b < bytesPerSample; b++) {
                    bytes[i + b] = (byte) ((transfer[k] >> (8 * b)) & 0xffL);  // `0xffL` removes 'higher' values
                }
            }
        }

        return bytes;
    }

    // Miscellaneous public methods

    /**
     * Returns the minimum number of bytes that are needed to fully store the number of bits
     * specified.
     *
     * @param numBits Number of bits to store.
     * @return Required number of bytes.
     */
    public static int numBytesForNumBits(int numBits) {
        return numBits + 7 >> 3;
    }

    // Private methods

    /**
     * Helper method that resamples the audio samples array <code>x</code> and places it into the
     * final array <code>y</code>.
     *
     * @param x            Initial array of audio samples.
     * @param y            Final array to store resampled samples.
     * @param sampleRatio  The ratio between the initial and final sample rates.
     * @param interpWin    Interpolation window, based off the selected <code>resType</code>.
     * @param interpDeltas Deltas between consecutive elements in <code>interpWin</code>.
     * @param precision    Precision constant.
     * @implNote See <a href="https://github.com/bmcfee/resampy/blob/1d1a08/resampy/interpn.py">
     * Resampy's Source Code</a> for the original implementation of this function in Python.
     */
    private static void resamplingHelper(
            double[] x, double[] y, double sampleRatio, double[] interpWin,
            double[] interpDeltas, int precision
    ) {
        // Define constants that will be needed later
        double scale = Math.min(sampleRatio, 1.);
        double timeIncrement = 1. / sampleRatio;
        int indexStep = (int) (scale * precision);

        int nWin = interpWin.length;
        int nOrig = x.length;
        int nOut = y.length;

        // Define 'loop variables'
        int n, offset;
        double timeRegister = 0;
        double frac, indexFrac, eta, weight;

        // Start resampling process
        for (int t = 0; t < nOut; t++) {
            // Grab the top bits as an index to the input buffer
            n = (int) timeRegister;

            // Grab the fractional component of the time index
            frac = scale * (timeRegister - n);

            // Offset into the filter
            indexFrac = frac * precision;
            offset = (int) indexFrac;

            // Interpolation factor
            eta = indexFrac - offset;

            // Compute the left wing of the filter response
            int iMax = Math.min(n + 1, (nWin - offset) / indexStep);

            for (int i = 0; i < iMax; i++) {
                weight = interpWin[offset + i * indexStep] + eta * interpDeltas[offset + i * indexStep];
                y[t] += weight * x[n - i];
            }

            // Invert P
            frac = scale - frac;

            // Offset into the filter
            indexFrac = frac * precision;
            offset = (int) indexFrac;

            // Interpolation factor
            eta = indexFrac - offset;

            // Compute the right wing of the filter response
            int jMax = Math.min(nOrig - n - 1, (nWin - offset) / indexStep);

            for (int j = 0; j < jMax; j++) {
                weight = interpWin[offset + j * indexStep] + eta * interpDeltas[offset + j * indexStep];
                y[t] += weight * x[n + j + 1];
            }

            // Increment the time register
            timeRegister += timeIncrement;
        }
    }
}
