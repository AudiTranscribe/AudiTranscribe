/*
 * Audio.java
 *
 * Created on 2022-02-13
 * Updated on 2022-06-06
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
import java.nio.file.Files;
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
    public AudioProcessingMode audioProcessingMode;

    private final String audioFileName;
    private final String mp3FilePath;
    private final String wavFilePath;

    private final AudioInputStream audioStream;
    private final AudioFormat audioFormat;
    private final double sampleRate;
    private double duration = 0;

    private int numSamples;
    private double[] audioSamples;
    private double[] monoAudioSamples;  // Average of stereo samples

    public byte[] rawMP3Bytes;

    private final MediaPlayer mediaPlayer;

    /**
     * Initializes an <code>Audio</code> object based on a file.
     *
     * @param wavFile          File object representing the WAV file to be used when generating
     *                         samples.
     * @param mp3File          File object representing the MP3 file to be used when playing the
     *                         audio.
     * @param originalFileName The file name of the original audio file.
     * @throws IOException                   If there was a problem reading in the audio stream.
     * @throws UnsupportedAudioFileException If there was a problem reading in the audio file.
     * @throws ValueException                If both <code>wavFile</code> and <code>mp3File</code>
     *                                       are <code>null</code>.
     */
    public Audio(
            File wavFile, File mp3File, String originalFileName
    ) throws UnsupportedAudioFileException, IOException {
        // Determine the audio processing mode
        if (wavFile != null && mp3File != null) {
            audioProcessingMode = AudioProcessingMode.SAMPLES_AND_PLAYBACK;
        } else if (wavFile == null) {
            audioProcessingMode = AudioProcessingMode.SAMPLES_ONLY;
        } else if (mp3File == null) {
            audioProcessingMode = AudioProcessingMode.PLAYBACK_ONLY;
        } else {
            throw new ValueException("Both `wavFile` and `mp3File` cannot be null.");
        }

        // Update attributes
        audioFileName = originalFileName;
        mp3FilePath = mp3File != null ? mp3File.getAbsolutePath() : null;
        wavFilePath = wavFile != null ? wavFile.getAbsolutePath() : null;

        // Create the media player object if needed
        if (mp3File != null) {
            // Get the media player for the audio file
            MediaPlayer tempMediaPlayer;

            try {
                tempMediaPlayer = new MediaPlayer(new Media(mp3File.toURI().toString()));
            } catch (IllegalStateException e) {
                tempMediaPlayer = null;

                Logger logger = Logger.getLogger(this.getClass().getName());
                logger.log(Level.SEVERE, "JavaFX Toolkit not initialized. Audio playback will not work.");
            }

            mediaPlayer = tempMediaPlayer;

            // Get the raw bytes from the MP3 file
            rawMP3Bytes = Files.readAllBytes(mp3File.toPath());

        } else {
            mediaPlayer = null;
        }

        // Generate audio samples
        if (wavFile != null) {
            // Attempt to convert the input stream into an audio input stream
            InputStream bufferedIn = new BufferedInputStream(new FileInputStream(wavFile));
            audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            // Get the audio file's audio format and audio file's sample rate
            audioFormat = audioStream.getFormat();
            sampleRate = audioFormat.getSampleRate();

            // Generate audio samples
            generateSamples();
        } else {
            audioStream = null;
            audioFormat = null;
            sampleRate = Double.NaN;
        }
    }

    /**
     * Initializes an <code>Audio</code> object based on a file.
     *
     * @param audioFile      File object representing the audio file to be used.
     * @param audioFileName  The file name of the original audio file.
     * @param processingMode The processing mode when handling the audio file.
     *                       <ul>
     *                       <li><code>SAMPLES_ONLY</code>: Treat <code>audioFile</code> as a WAV file and only generate samples.</li>
     *                       <li><code>PLAYBACK_ONLY</code>: Treat <code>audioFile</code> as a MP3 file and only permit playback.</li>
     *                       </ul>
     *                       Treat any other option as a <code>ValueException</code>.
     * @throws ValueException                If the processing mode is not <code>SAMPLES_ONLY</code>
     *                                       or <code>PLAYBACK_ONLY</code>.
     * @throws IOException                   If there was a problem reading in the audio stream.
     * @throws UnsupportedAudioFileException If there was a problem reading in the audio file.
     */
    public static Audio initAudio(
            File audioFile, String audioFileName, AudioProcessingMode processingMode
    ) throws UnsupportedAudioFileException, IOException {
        if (processingMode == AudioProcessingMode.SAMPLES_ONLY) {
            return new Audio(audioFile, null, audioFileName);
        } else if (processingMode == AudioProcessingMode.PLAYBACK_ONLY) {
            return new Audio(null, audioFile, audioFileName);
        } else {
            throw new ValueException("Invalid audio processing mode.");
        }
    }

    // Getter methods

    public String getAudioFileName() {
        return audioFileName;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public double getDuration() {
        if (duration == 0) {
            duration = mediaPlayer.getTotalDuration().toSeconds();
        }

        return duration;
    }

    public double[] getMonoSamples() {
        return monoAudioSamples;
    }

    // Audio methods

    /**
     * Method that plays the audio.
     *
     * @throws InvalidObjectException If the media player was not initialized.
     */
    public void play() throws InvalidObjectException {
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
    public void pause() throws InvalidObjectException {
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
    public void stop() throws InvalidObjectException {
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
                numMonoSamples = numSamples;
                monoAudioSamples = new double[numSamples];
                System.arraycopy(audioSamples, 0, monoAudioSamples, 0, numSamples);
            }

            // Calculate the duration of the audio
            if (duration == 0) duration = numMonoSamples / sampleRate;

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

