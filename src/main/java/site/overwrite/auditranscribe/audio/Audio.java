/*
 * Audio.java
 *
 * Created on 2022-02-13
 * Updated on 2022-05-27
 *
 * Description: Class that handles audio processing and audio playback.
 */

package site.overwrite.auditranscribe.audio;

import javafx.util.Duration;
import site.overwrite.auditranscribe.exceptions.ValueException;
import site.overwrite.auditranscribe.utils.ArrayUtils;

import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audio class that handles audio processing and audio playback.
 */
public class Audio {
    // Constants
    public static final int SAMPLES_BUFFER_SIZE = 1024;  // In bits

    // Attributes
    private final String audioFilePath;
    private final String audioFileName;

    private final AudioInputStream audioStream;
    private final AudioFormat audioFormat;
    private final double sampleRate;
    private double duration;

    private int numSamples;
    private double[] audioSamples;

    private int numMonoSamples;
    private double[] monoAudioSamples;

    private final MediaPlayer mediaPlayer;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Initializes an <code>Audio</code> object based on a file.
     *
     * @param file           File object, representing the audio file.
     * @param useMediaPlayer Whether to use the media player to play the audio.
     * @throws IOException                   If there was a problem reading in the audio stream.
     * @throws UnsupportedAudioFileException If there was a problem reading in the audio file.
     */
    public Audio(File file, boolean useMediaPlayer) throws UnsupportedAudioFileException, IOException {
        // Set the audio file path and file name
        audioFileName = file.getName();
        audioFilePath = file.getAbsolutePath();

        // Attempt to convert the input stream into an audio input stream
        InputStream bufferedIn = new BufferedInputStream(new FileInputStream(file));
        audioStream = AudioSystem.getAudioInputStream(bufferedIn);

        // Get the audio file's audio format and audio file's sample rate
        audioFormat = audioStream.getFormat();
        sampleRate = audioFormat.getSampleRate();

        // Create the media player object if needed
        if (useMediaPlayer) {
            // Get the media player for the audio file
            MediaPlayer tempMediaPlayer;

            try {
                tempMediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
            } catch (IllegalStateException e) {
                tempMediaPlayer = null;
                logger.log(Level.SEVERE, "JavaFX Toolkit not initialized. Audio playback will not work.");
            }

            mediaPlayer = tempMediaPlayer;
        } else {
            mediaPlayer = null;
        }

        // Generate audio samples
        generateSamples();
    }

    /**
     * Initializes an <code>Audio</code> object based on a file.
     *
     * @param file File object, representing the audio file.
     * @throws IOException                   If there was a problem reading in the audio stream.
     * @throws UnsupportedAudioFileException If there was a problem reading in the audio file.
     */
    public Audio(File file) throws UnsupportedAudioFileException, IOException {
        this(file, true);
    }

    // Getter/Setter methods

    /**
     * Getter method that returns the <b>absolute</b> audio file path.
     *
     * @return <b>Absolute</b> path to the audio file.
     */
    public String getAudioFilePath() {
        return audioFilePath;
    }

    /**
     * Getter method that returns the audio file's name.
     *
     * @return Name of the audio file.
     */
    public String getAudioFileName() {
        return audioFileName;
    }

    /**
     * Getter method that returns the sample rate of the audio file.
     *
     * @return Float of the number of samples per second.
     */
    public double getSampleRate() {
        return sampleRate;
    }

    /**
     * Getter method for the duration of the audio in seconds.
     *
     * @return Duration of audio in seconds.
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Getter method for the total number of samples in the audio object.<br>
     * See {@link #getNumMonoSamples()} to get the number of <b>mono</b> samples.
     *
     * @return Total number of samples.
     */
    public int getNumSamples() {
        return numSamples;
    }

    /**
     * Getter method that returns all the samples of the audio object.<br>
     * See {@link #getMonoSamples()} to get the <b>mono</b>samples.
     *
     * @return All the samples of the audio object.
     */
    public double[] getSamples() {
        return audioSamples;
    }

    /**
     * Getter method for the number of <b>mono</b> samples in the audio object.
     *
     * @return Number of <b>mono</b> samples.
     */
    public int getNumMonoSamples() {
        return numMonoSamples;
    }

    /**
     * Getter method that returns the <b>mono</b> samples of the audio object.
     *
     * @return The <b>mono</b> samples of the audio object.
     */
    public double[] getMonoSamples() {
        return monoAudioSamples;
    }

    // Audio methods

    /**
     * Method that plays the audio.
     *
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void playAudio() throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
        }
    }

    /**
     * Method that pauses the current audio that is playing.
     *
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void pauseAudio() throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
        }
    }

    /**
     * Method that stops the audio.
     *
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void stopAudio() throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
        }
    }

    /**
     * Set the current audio's playback time to <code>playbackTime</code> <b>seconds</b>.
     *
     * @param playbackTime The playback time in seconds.
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void setAudioPlaybackTime(double playbackTime) throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(playbackTime * 1000));
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
        }
    }

    /**
     * Set the current audio's starting time to <code>startTime</code> <b>seconds</b>.
     *
     * @param startTime The start time of the audio in seconds.
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void setAudioStartTime(double startTime) throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.setStartTime(new Duration(startTime * 1000));
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
        }
    }

    /**
     * Method that gets the current audio time in <b>seconds</b>.
     *
     * @return Returns the current audio time in <b>seconds</b>.
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public double getCurrAudioTime() throws InvalidObjectException {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
        }
    }

    /**
     * Method that sets the volume to the volume provided.
     *
     * @param volume Volume value. This value should be in the interval [0, 1] where 0 means
     *               silent and 1 means full volume.
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void setPlaybackVolume(double volume) throws InvalidObjectException {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        } else {
            throw new InvalidObjectException("Media player was not initialised.");
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
    public static double[] resample(double[] x, double srOrig, double srFinal, Filter resType, boolean scale) throws ValueException {
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
        double[] interpWin = resType.filter.getHalfWin();
        int precision = resType.filter.getPrecision();

        int interpWinLen = interpWin.length;

        // Treat the interpolation window
        if (sampleRatio < 1) {
            // Multiply every element in the window by `sampleRatio`
            for (int i = 0; i < interpWinLen; i++) {
                interpWin[i] *= sampleRatio;
            }
        }

        // Calculate interpolation deltas
        double[] interpDeltas = new double[interpWinLen];

        for (int i = 0; i < interpWinLen - 1; i++) {
            interpDeltas[i] = interpWin[i + 1] - interpWin[i];
        }

        // Run resampling
        resampleF(x, y, sampleRatio, interpWin, interpDeltas, precision);

        // Fix the length of the samples array
        int correctNumSamples = (int) Math.ceil(sampleRatio * x.length);
        double[] yHat = ArrayUtils.fixLength(y, correctNumSamples);

        // Handle rescaling
        if (scale) {
            for (int i = 0; i < correctNumSamples; i++) {
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
    private static void resampleF(
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
                unpack(samples, transfer, bytes, numBytesRead);

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
                numMonoSamples = numSamples;
                monoAudioSamples = new double[numSamples];
                System.arraycopy(audioSamples, 0, monoAudioSamples, 0, numSamples);
            }

            // Calculate the duration of the audio
            duration = numMonoSamples / sampleRate;

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
    private void unpack(float[] samples, long[] transfer, byte[] bytes, int numValidBytes) {
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
}

