/*
 * Audio.java
 * Description: Class that handles audio processing and audio playback.
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

import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.utils.MathUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Class that handles audio processing and audio playback.
 */
@ExcludeFromGeneratedCoverageReport
public class Audio extends LoggableClass {
    // Constants
    public static final int SAMPLES_BUFFER_SIZE = 2048;  // In bits; 2048 = 2^11
    final int MAX_AUDIO_DURATION = 5;  // In minutes

    // Attributes
    private final AudioInputStream audioStream;
    private final AudioFormat audioFormat;
    private final double sampleRate;

    private double duration = 0;  // In seconds

    private int numRawSamples;
    private int numMonoSamples;
    private double[] rawAudioSamples;
    private double[] monoAudioSamples;

    /**
     * Initializes an <code>Audio</code> object based on a file.
     *
     * @param wavFile         File object representing the WAV file to be used.
     * @param processingModes The processing modes when handling the audio file.<br>
     *                        Any number of processing modes can be included.
     *                        <ul>
     *                        <li>
     *                            <code>SAMPLES</code>: Generate audio samples.
     *                        </li>
     *                        <li>
     *                            <code>PLAYBACK</code>: Allow audio playback.
     *                        </li>
     *                        </ul>
     * @throws IOException                   If there was a problem reading in the audio stream.
     * @throws UnsupportedAudioFileException If there was a problem reading in the audio file.
     * @throws AudioTooLongException         If the audio file exceeds the maximum audio duration
     *                                       permitted.
     */
    public Audio(
            File wavFile, AudioProcessingMode... processingModes
    ) throws UnsupportedAudioFileException, IOException, AudioTooLongException {
        // Convert the given processing modes as a list
        List<AudioProcessingMode> modes = List.of(processingModes);

        // Generate audio samples
        if (modes.contains(AudioProcessingMode.WITH_SAMPLES)) {
            // Attempt to convert the input stream into an audio input stream
            InputStream bufferedIn = new BufferedInputStream(new FileInputStream(wavFile));
            audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            // Get the audio file's audio format and audio file's sample rate
            audioFormat = audioStream.getFormat();
            sampleRate = audioFormat.getSampleRate();

            // Compute the duration of the audio file
            long frames = audioStream.getFrameLength();
            duration = frames / audioFormat.getFrameRate();  // In seconds

            // Check if duration is too long
            double durationInMinutes = duration / 60;

            if (durationInMinutes > MAX_AUDIO_DURATION) {
                throw new AudioTooLongException(
                        "Audio file is too long (audio was " + durationInMinutes + " minutes but maximum allowed " +
                                "is " + MAX_AUDIO_DURATION + " minutes)"
                );
            }

            // Generate audio samples
            generateSamples();

        } else {
            audioStream = null;
            audioFormat = null;
            sampleRate = Double.NaN;
        }
    }

    // Todo: continue with playback support

    // Getter/Setter methods

    public double getSampleRate() {
        return sampleRate;
    }

    public double[] getMonoSamples() {
        return monoAudioSamples;
    }

    // Private methods

    /**
     * Generates the audio sample data from the provided audio file.
     */
    private void generateSamples() {
        try {
            // Get the number of bytes that corresponds to each sample
            final int bytesPerSample = numBytesForNumBits(audioFormat.getSampleSizeInBits());

            // Get the total number of samples
            numRawSamples = audioStream.available() / bytesPerSample;

            // Calculate the number of samples needed for each window
            int numSamplesPerBuffer = SAMPLES_BUFFER_SIZE * audioFormat.getChannels();
            int numBuffers = MathUtils.ceilDiv(numRawSamples, numSamplesPerBuffer);

            // Create a `finalSamples` array to store the samples
            float[] finalSamples = new float[numBuffers * numSamplesPerBuffer];

            // Define helper arrays
            float[] samples = new float[numSamplesPerBuffer];
            long[] transfer = new long[numSamplesPerBuffer];
            byte[] bytes = new byte[numSamplesPerBuffer * bytesPerSample];

            // Get samples
            int numBytesRead;
            int cycleNum = 0;  // Number of times we read from the audio stream
            while ((numBytesRead = audioStream.read(bytes)) != -1) {
                // Unpack the bytes into samples
                unpackBytes(samples, transfer, bytes, numBytesRead);

                // Add it to the master list of samples
                if (numBytesRead / bytesPerSample >= 0) {
                    System.arraycopy(samples, 0, finalSamples, cycleNum * numSamplesPerBuffer,
                            numBytesRead / bytesPerSample);
                }

                cycleNum++;
            }

            // Shorten the `finalSamples` array to fit the required size
            finalSamples = Arrays.copyOf(finalSamples, numRawSamples);

            // Convert everything to double and place it into the raw audio samples array
            // (We convert to double because most signal processing algorithms here use doubles)
            rawAudioSamples = new double[numRawSamples];

            for (int i = 0; i < numRawSamples; i++) {
                rawAudioSamples[i] = finalSamples[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Remove stereo samples if they are there
            if (audioFormat.getChannels() == 2) {  // Stereo
                // Calculate the number of mono samples there are
                numMonoSamples = numRawSamples / 2;

                // Fill in the mono audio samples array
                monoAudioSamples = new double[numMonoSamples];

                for (int i = 0; i < numMonoSamples; i++) {
                    monoAudioSamples[i] = (rawAudioSamples[i * 2] + rawAudioSamples[i * 2 + 1]) / 2;
                }
            } else {  // Mono
                // Fill in the mono audio samples array
                monoAudioSamples = new double[numRawSamples];
                System.arraycopy(rawAudioSamples, 0, monoAudioSamples, 0, numRawSamples);
            }

            // Close the audio stream
            if (audioStream != null) {
                try {
                    audioStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns the minimum number of bytes that are needed to fully store the number of bits
     * specified.
     *
     * @param numBits Number of bits to store.
     * @return Required number of bytes.
     */
    private static int numBytesForNumBits(int numBits) {
        return numBits + 7 >> 3;
    }

    /**
     * Unpacks the set of bytes from a file (the array <code>bytes</code>) into audio sample data
     * (into the array <code>samples</code>).
     *
     * @param samples       (Initially) empty array that stores the samples. Fixed in length at
     *                      <code>SAMPLES_BUFFER_SIZE * audioFormat.getChannels()</code> float
     *                      data.
     * @param transfer      (Initially) empty array that helps move data within the function. Fixed
     *                      in length at <code>samples.length</code> long data.
     * @param bytes         Array of bytes that is read in from the audio file. Fixed in length at
     *                      <code>samples.length * bytesPerSample</code> bytes.
     * @param numValidBytes Number of valid bytes in the <code>bytes</code> array.
     * @implNote See the <a href="https://tinyurl.com/stefanSpectrogramOriginal">original
     * implementation on GitHub</a>. This code was largely adapted from that source.
     */
    private void unpackBytes(float[] samples, long[] transfer, byte[] bytes, int numValidBytes) {
        if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
                && audioFormat.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
            // `samples` is already good; no need to process
            return;
        }

        // Calculate the number of bytes needed to store each sample
        final int bitsPerSample = audioFormat.getSampleSizeInBits();
        final int bytesPerSample = numBytesForNumBits(bitsPerSample);

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
        final long fullScale = (long) Math.pow(2., bitsPerSample - 1);

        // The OR is not quite enough to convert; signage needs to be corrected
        if (audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            /*
             * If the samples were signed, they must be extended to the 64-bit long.
             *
             * The arithmetic right shift in Java will fill the left bits with 1's if the Most Significant Bit (MSB) is
             * set, so sign extend by first shifting left so that if the sample is supposed to be negative, it will
             * shift the sign bit in to the 64-bit MSB then shift back and fill with 1's.
             *
             * As an example, imagining these were 4-bit samples originally and the destination is 8-bit, if we have a
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

            final long signShift = 64L - bitsPerSample;

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
            samples[i] = (float) transfer[i] / (float) fullScale;
        }
    }
}
