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

package site.overwrite.auditranscribe.audio;

import javafx.util.Duration;
import site.overwrite.auditranscribe.exceptions.audio.AudioIsSamplesOnlyException;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.utils.ArrayUtils;

import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import site.overwrite.auditranscribe.utils.MathUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Audio class that handles audio processing and audio playback.
 */
public class Audio {
    // Constants
    public static final int SAMPLES_BUFFER_SIZE = 1024;  // In bits; 1024 = 2^10
    public static final double MAX_AUDIO_LENGTH = 5;  // Maximum length of audio in minutes

    // Attributes
    private final AudioInputStream audioStream;
    private final AudioFormat audioFormat;
    private final double sampleRate;
    private double duration = 0;  // In seconds

    private final byte[] rawWAVBytes;  // Would be an empty array unless set
    private byte[] rawMP3Bytes;

    private int numSamples;
    private double[] audioSamples;
    private double[] monoAudioSamples;  // Average of stereo samples

    private final MediaPlayer mediaPlayer;

    /**
     * Initializes an <code>Audio</code> object based on a file.
     *
     * @param wavFile         File object representing the WAV file to be used for both samples
     *                        generation and audio playback.
     * @param processingModes The processing modes when handling the audio file. Any number of
     *                        processing modes can be included.
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

        // Create the media player object if needed
        if (modes.contains(AudioProcessingMode.PLAYBACK)) {
            // Get the media player for the audio file
            MediaPlayer tempMediaPlayer;

            try {
                tempMediaPlayer = new MediaPlayer(new Media(wavFile.toURI().toString()));
            } catch (IllegalStateException e) {
                tempMediaPlayer = null;

                MyLogger.log(
                        Level.SEVERE,
                        "JavaFX Toolkit not initialized. Audio playback will not work.",
                        this.getClass().toString()
                );
            }

            // Update attributes
            mediaPlayer = tempMediaPlayer;

        } else {
            mediaPlayer = null;
        }

        // Generate audio samples
        if (modes.contains(AudioProcessingMode.SAMPLES)) {
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

            if (durationInMinutes > MAX_AUDIO_LENGTH) {
                throw new AudioTooLongException(
                        "Audio file is too long (audio was " + durationInMinutes + " minutes but maximum allowed " +
                                "is " + MAX_AUDIO_LENGTH + " minutes)"
                );
            }

            // Generate audio samples
            generateSamples();
        } else {
            audioStream = null;
            audioFormat = null;
            sampleRate = Double.NaN;
        }

        // Save the audio file's raw WAV bytes
        rawWAVBytes = Files.readAllBytes(wavFile.toPath());
    }

    // Getter/Setter methods

    public double getSampleRate() {
        return sampleRate;
    }

    /**
     * @return Audio duration in <b>seconds</b>, correct to the nearest millisecond.
     */
    public double getDuration() {
        if (duration == 0) {
            duration = mediaPlayer.getTotalDuration().toSeconds();
        }

        return MathUtils.round(duration, 3);
    }

    public void setDuration(double duration) {
        if (duration <= 0) throw new ValueException("Duration must be greater than 0.");
        this.duration = duration;
    }

    public double[] getMonoSamples() {
        return monoAudioSamples;
    }

    public void setRawMP3Bytes(byte[] rawMP3Bytes) {
        this.rawMP3Bytes = rawMP3Bytes;
    }

    // Audio methods

    /**
     * Method that plays the audio.
     */
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    /**
     * Method that pauses the current audio that is playing.
     */
    public void pause() throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    /**
     * Method that stops the audio.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    /**
     * Set the current audio's playback time to <code>playbackTime</code> <b>seconds</b>.
     *
     * @param playbackTime The playback time in seconds.
     */
    public void setAudioPlaybackTime(double playbackTime) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(playbackTime * 1000));
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    /**
     * Set the current audio's starting time to <code>startTime</code> <b>seconds</b>.
     *
     * @param startTime The start time of the audio in seconds.
     */
    public void setAudioStartTime(double startTime) {
        if (mediaPlayer != null) {
            mediaPlayer.setStartTime(new Duration(startTime * 1000));
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    /**
     * Method that gets the current audio time in <b>seconds</b>, correct to the nearest
     * millisecond.
     *
     * @return Returns the current audio time in <b>seconds</b>, correct to the nearest millisecond.
     */
    public double getCurrAudioTime() {
        if (mediaPlayer != null) {
            return MathUtils.round(mediaPlayer.getCurrentTime().toSeconds(), 3);
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    /**
     * Method that sets the volume to the volume provided.
     *
     * @param volume Volume value. This value should be in the interval [0, 1] where 0 means
     *               silent and 1 means full volume.
     */
    public void setPlaybackVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        } else {
            throw new AudioIsSamplesOnlyException("Media player was not initialized.");
        }
    }

    // Public methods

    /**
     * Resample a signal <code>x</code> from <code>srOrig</code> to <code>srFinal</code>.
     *
     * @param x       Original signal that needs to be resampled.
     * @param srOrig  Original sample rate of the signal.
     * @param srFinal Sample rate of the final signal.
     * @param resType Resampling type, also known as the filter window's name.
     * @param scale   Whether to scale the final sample array.
     * @return Array representing the resampled signal.
     * @throws ValueException If: <ul>
     *                        <li>
     *                        Either <code>srOrig</code> or <code>srFinal</code> is not
     *                        positive.
     *                        </li>
     *                        <li>
     *                        The input signal length is too short to be resampled to the
     *                        desired sample rate.
     *                        </li>
     *                        </ul>
     * @see <a href="https://github.com/bmcfee/resampy/blob/ccb8557/resampy/core.py">Resampy</a>,
     * where the main core of the code was taken from.
     */
    public static double[] resample(
            double[] x, double srOrig, double srFinal, Filter resType, boolean scale
    ) throws ValueException {
        // Validate sample rates
        if (srOrig <= 0) throw new ValueException("Invalid original sample rate " + srOrig);
        if (srFinal <= 0) throw new ValueException("Invalid final sample rate " + srFinal);

        // Calculate sample ratio
        double sampleRatio = srFinal / srOrig;

        // Calculate final array length and check if it is okay
        int finalLength = (int) (sampleRatio * x.length);
        if (finalLength < 1) {
            throw new InvalidParameterException(
                    "Input signal length of " + x.length + " too small to resample from " + srOrig + " to " + srFinal
            );
        }

        // Generate output array in storage
        double[] y = new double[finalLength];

        // Get the interpolation window and precision of the specified `resType`
        double[] interpWin = resType.filter.getHalfWindow();
        int precision = resType.filter.getPrecision();

        int interpWinLength = interpWin.length;

        // Treat the interpolation window
        if (sampleRatio < 1) {
            // Multiply every element in the window by `sampleRatio`
            for (int i = 0; i < interpWinLength; i++) {
                interpWin[i] *= sampleRatio;
            }
        }

        // Calculate interpolation deltas
        double[] interpDeltas = new double[interpWinLength];

        for (int i = 0; i < interpWinLength - 1; i++) {
            interpDeltas[i] = interpWin[i + 1] - interpWin[i];
        }

        // Run resampling
        resamplingHelper(x, y, sampleRatio, interpWin, interpDeltas, precision);

        // Fix the length of the samples array
        int correctedNumSamples = (int) Math.ceil(sampleRatio * x.length);
        double[] yHat = ArrayUtils.fixLength(y, correctedNumSamples);

        // Handle rescaling
        if (scale) {
            for (int i = 0; i < correctedNumSamples; i++) {
                yHat[i] /= Math.sqrt(sampleRatio);
            }
        }

        // Return the resampled array
        return yHat;
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
     * @implNote See <a href="https://github.com/bmcfee/resampy/blob/ccb8557/resampy/interpn.py">
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

    /**
     * Generates the audio sample data from the provided audio file.
     */
    private void generateSamples() {
        try {
            // Get the number of bytes that corresponds to each sample
            final int bytesPerSample = numBytesForNumBits(audioFormat.getSampleSizeInBits());

            // Get the total number of samples
            numSamples = audioStream.available() / bytesPerSample;

            // Calculate the number of samples needed for each window
            int numSamplesPerBuffer = SAMPLES_BUFFER_SIZE * audioFormat.getChannels();
            int numBuffers = (int) Math.ceil((float) numSamples / numSamplesPerBuffer);

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
            finalSamples = Arrays.copyOf(finalSamples, numSamples);

            // Convert everything to double and place it into `audioSamples`
            audioSamples = new double[numSamples];

            for (int i = 0; i < numSamples; i++) {
                audioSamples[i] = finalSamples[i];
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Remove stereo samples if they are there
            int numMonoSamples;
            if (audioFormat.getChannels() == 2) {  // Stereo
                // Calculate the number of mono samples there are
                numMonoSamples = numSamples / 2;

                // Fill in the mono audio samples array
                monoAudioSamples = new double[numMonoSamples];

                for (int i = 0; i < numMonoSamples; i++) {
                    // Take average of left and right channels' samples
                    monoAudioSamples[i] = (audioSamples[i * 2] + audioSamples[i * 2 + 1]) / 2;
                }
            } else {  // Mono
                // Fill in the mono audio samples array
                monoAudioSamples = new double[numSamples];
                System.arraycopy(audioSamples, 0, monoAudioSamples, 0, numSamples);
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
     * @see <a href="https://tinyurl.com/stefanSpectrogramOriginal">Original implementation on
     * GitHub</a>. This code was largely adapted from that source.
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

        // Calculate scaling factor to normalize the samples to the interval [-1f, 1f]
        final long fullScale = (long) Math.pow(2.0, bitsPerSample - 1);

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
             * Unsigned samples are easier since they will be read correctly in to the long. So just sign them:
             * subtract `Math.pow(2., bitsPerSample - 1)` so the center is 0.
             */

            for (int i = 0; i < transfer.length; i++) {
                transfer[i] -= fullScale;
            }
        }

        // Finally, normalise range to [-1f, 1f]
        for (int i = 0; i < transfer.length; i++) {
            samples[i] = (float) transfer[i] / (float) fullScale;
        }
    }

    /**
     * Helper method that converts the audio object into MP3 bytes.
     *
     * @param ffmpegPath The path to the ffmpeg executable.
     * @throws FFmpegNotFoundException If FFmpeg was not found at the specified path.
     * @throws IOException             If writing to the final audio file encounters an error.
     */
    public byte[] wavBytesToMP3Bytes(String ffmpegPath) throws FFmpegNotFoundException, IOException {
        // Check if we have already processed the audio
        if (rawMP3Bytes != null) {
            MyLogger.log(Level.FINE, "Returning previously processed MP3 bytes", this.getClass().toString());
            return rawMP3Bytes;
        }

        MyLogger.log(Level.FINE, "Converting WAV bytes to MP3 bytes", this.getClass().toString());

        // Ensure that the temporary directory exists
        IOMethods.createFolder(IOConstants.TEMP_FOLDER_PATH);
        MyLogger.log(
                Level.FINE,
                "Temporary folder created: " + IOConstants.TEMP_FOLDER_PATH,
                this.getClass().toString()
        );

        // Define a new FFmpeg handler
        FFmpegHandler FFmpegHandler = new FFmpegHandler(ffmpegPath);

        // Generate the output path to the MP3 file
        String inputPath = IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "temp-1.wav");
        String outputPath = IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "temp-2.mp3");

        // Write WAV bytes into a file specified at the input path
        IOMethods.createFile(inputPath);
        Files.write(Paths.get(inputPath), rawWAVBytes);

        // Convert the original WAV file to a temporary MP3 file
        outputPath = FFmpegHandler.convertAudio(new File(inputPath), outputPath);

        // Read the raw MP3 bytes into a temporary file
        rawMP3Bytes = Files.readAllBytes(Paths.get(outputPath));

        // Delete the temporary files
        IOMethods.delete(inputPath);
        IOMethods.delete(outputPath);

        // Return the raw MP3 bytes
        MyLogger.log(Level.FINE, "Done converting", this.getClass().toString());
        return rawMP3Bytes;
    }
}

