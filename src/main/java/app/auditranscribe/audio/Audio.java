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

import app.auditranscribe.audio.exceptions.AudioPlaybackNotSupported;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import app.auditranscribe.audio.operators.*;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.misc.*;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.TypeConversionUtils;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Class that handles audio processing and audio playback.
 */
@ExcludeFromGeneratedCoverageReport
public class Audio extends LoggableClass {
    // Constants
    public static final int SAMPLES_BUFFER_SIZE = 1024;  // In number of samples
    public static final int[] VALID_PLAYBACK_BUFFER_SIZES = {1024, 2048, 4096};  // In bytes

    public static final int SLOWDOWN_PROCESSING_LENGTH = 2048;
    public static final int SLOWDOWN_ANALYSIS_LENGTH = 512;
    public static final SignalWindow SLOWDOWN_WINDOW = SignalWindow.HANN_WINDOW;

    final int OUT_CHANNEL_CAPACITY = 8192;  // In number of samples
    final int OUT_SEGMENT_LENGTH = 1024;  // In number of samples

    final int MAX_AUDIO_DURATION = 5;  // In minutes

    // Attributes
    private final File wavFile;

    private AudioInputStream audioStream;
    private final AudioFormat audioFormat;

    private final int numChannels;
    private final int frameSize;
    private final double frameRate;
    private final double sampleRate;
    private final double duration;  // In seconds

    final int bitsPerSample;
    final int bytesPerSample;

    private double timeToResumeAt;  // Time that the audio should continue playing at upon resuming
    private double prevElapsedTime;  // Time (in seconds) that was elapsed before a skip forwards/backwards

    private double volume = 1;
    private boolean paused = false;

    private boolean withPlayback = false;
    private SourceDataLine sourceDataLine;
    private StoppableThread audioPlaybackThread;

    private int numRawSamples;
    private int numMonoSamples;
    private double[] rawSamples;
    private double[] monoSamples;

    private final Vector<BlockingQueue<Byte>> outChannels = new Vector<>();
    private final Vector<TimeStretchOperator> channelOperators = new Vector<>();

    private byte[] rawMP3Bytes;

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
            File wavFile, ProcessingMode... processingModes
    ) throws UnsupportedAudioFileException, IOException, AudioTooLongException {
        // Convert the given processing modes as a list
        List<ProcessingMode> modes = List.of(processingModes);

        // Attempt to convert the input stream into an audio input stream
        this.wavFile = wavFile;
        audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(wavFile)));

        // Get the audio file's audio format, and get the format's properties
        audioFormat = audioStream.getFormat();

        numChannels = audioFormat.getChannels();
        frameSize = audioFormat.getFrameSize();
        frameRate = audioFormat.getFrameRate();
        sampleRate = audioFormat.getSampleRate();

        // Calculate the number of bytes needed to store each sample
        bitsPerSample = audioFormat.getSampleSizeInBits();
        bytesPerSample = AudioHelpers.numBytesForNumBits(bitsPerSample);

        // Compute the duration of the audio file
        long frames = audioStream.getFrameLength();
        duration = frames / frameRate;  // In seconds

        // Check if duration is too long
        double durationInMinutes = duration / 60;

        if (durationInMinutes > MAX_AUDIO_DURATION) {
            throw new AudioTooLongException(
                    "Audio file is too long (audio was " + durationInMinutes + " minutes but maximum allowed " +
                            "is " + MAX_AUDIO_DURATION + " minutes)"
            );
        }

        // Generate audio samples if requested
        if (modes.contains(ProcessingMode.WITH_SAMPLES)) {
            generateSamples();
        }

        // Allow audio playback if requested
        if (modes.contains(ProcessingMode.WITH_PLAYBACK)) {
            withPlayback = true;
            setAudioPlaybackThread();

            // Update out channels
            for (int i = 0; i < numChannels; i++) {
                outChannels.add(new LinkedBlockingQueue<>(OUT_CHANNEL_CAPACITY * bytesPerSample));
            }
        } else {
            sourceDataLine = null;
            audioPlaybackThread = null;
        }
    }

    // Getter/Setter methods

    public double getSampleRate() {
        return sampleRate;
    }

    public int getNumRawSamples() {
        return numRawSamples;
    }

    public int getNumMonoSamples() {
        return numMonoSamples;
    }

    public double[] getMonoSamples() {
        return monoSamples;
    }

    public void setMP3Bytes(byte[] rawMP3Bytes) {
        this.rawMP3Bytes = rawMP3Bytes;
    }

    public double getDuration() {
        return duration;
    }

    public double getCurrentTime() {
        return sourceDataLine.getMicrosecondPosition() / 1e6 - prevElapsedTime;  // Todo: work with slowed audio
    }

    // Audio playback methods

    /**
     * Starts playing the audio.
     */
    public void play() {
        if (!withPlayback) throw new AudioPlaybackNotSupported();
        if (sourceDataLine == null) setupSourceDataLine();

        if (audioPlaybackThread.isStarted()) {
            seekToTime(timeToResumeAt);
            paused = false;
        } else {
            updatePlaybackVolume(volume);
            sourceDataLine.start();
            audioPlaybackThread.start();
        }
    }

    /**
     * Pauses the audio.
     */
    public void pause() {
        paused = true;
        timeToResumeAt = getCurrentTime();
        sourceDataLine.flush();
        clearChannelsBuffers();
    }

    /**
     * Stops playing the audio.
     */
    public void stop() {
        if (!withPlayback) throw new AudioPlaybackNotSupported();
        try {
            // Halt all threads
            audioPlaybackThread.interrupt();
            sourceDataLine.drain();
            sourceDataLine.close();
            audioStream.close();
            paused = false;

            // Stop and clear all operators' stuff
            resetOperators();
            clearChannelsBuffers();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that seeks the audio to the new time.
     *
     * @param seekTime Time to seek the audio to.
     */
    public void seekToTime(double seekTime) {
        // If the current time is earlier than the seek time, we want to seek forwards
        double currTime = getCurrentTime();
        if (currTime < seekTime) {
            seekForwards(seekTime - currTime);
        } else {  // Want to seek to earlier part of audio; seek backwards
            seekBackwards(seekTime);
        }

        // Update the previously elapsed time and the time to resume at
        prevElapsedTime = sourceDataLine.getMicrosecondPosition() / 1e6 - seekTime;  // Todo: work with slowed audio
        timeToResumeAt = seekTime;
    }

    /**
     * Method that sets the playback.<br>
     * Also sets the slowed audio's media player's volume if it is provided.
     *
     * @param volume Volume value. This value should be in the interval [0, 2] where 0 means
     *               silent and 2 means <b>twice</b> full volume.
     */
    public void setVolume(double volume) {
        this.volume = volume;
        if (sourceDataLine != null) {
            updatePlaybackVolume(volume);
        }
    }

    /**
     * Method that toggles slowed audio.
     *
     * @param slowed Whether the audio that is playing should be slowed or not.
     */
    public void toggleSlowedAudio(boolean slowed) {
        for (TimeStretchOperator op : channelOperators) op.setStretchFactor(slowed ? 2 : 1);
    }

    /**
     * Method that resets the playback system entirely.
     */
    public void resetPlaybackSystem() {
        prevElapsedTime = 0;
        setupSourceDataLine();
        resetAudioStream();
        setAudioPlaybackThread();
    }

    // Audio device methods

    /**
     * Method that filters audio devices based on the desired line.
     *
     * @param supportedLine Line to filter devices for.
     * @return List of <code>Mixer.Info</code> objects, representing supported devices.
     */
    public static List<Mixer.Info> filterDevices(Line.Info supportedLine) {
        List<Mixer.Info> result = new ArrayList<>();
        Mixer.Info[] infos = AudioSystem.getMixerInfo();

        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(supportedLine)) {
                result.add(info);
            }
        }

        return result;
    }

    /**
     * Method that lists output audio devices for the system.
     *
     * @return List of <code>Mixer.Info</code> objects, representing supported output devices.
     */
    public static List<Mixer.Info> listOutputAudioDevices() {
        return filterDevices(new Line.Info(SourceDataLine.class));
    }

    /**
     * Gets the specified output audio device within a list.
     *
     * @param audioDevices Devices to search the audio device within.
     * @param infoMap      Map of the info of the desired audio device.
     * @return A <code>Mixer.Info</code> object, representing the info for the audio device.<br>
     * If not found, returns the default audio device.
     */
    public static Mixer.Info getOutputAudioDevice(List<Mixer.Info> audioDevices, Map<String, String> infoMap) {
        for (Mixer.Info device : audioDevices) {
            if (Objects.equals(device.getName(), infoMap.get("name")) &&
                    Objects.equals(device.getVendor(), infoMap.get("vendor")) &&
                    Objects.equals(device.getVersion(), infoMap.get("version"))) {
                return device;
            }
        }
        return audioDevices.get(0);
    }

    /**
     * Gets the specified output audio device from the list of all audio devices.
     *
     * @param infoMap Map of the info of the desired audio device.
     * @return A <code>Mixer.Info</code> object, representing the info for the audio device.<br>
     * If not found, returns the default audio device.
     */
    public static Mixer.Info getOutputAudioDevice(Map<String, String> infoMap) {
        return getOutputAudioDevice(listOutputAudioDevices(), infoMap);
    }

    // Byte conversion methods

    /**
     * Helper method that converts the WAV bytes into MP3 bytes.
     *
     * @param ffmpegPath The path to the ffmpeg executable.
     * @throws FFmpegNotFoundException If FFmpeg was not found at the specified path.
     * @throws IOException             If writing to the final audio file encounters an error.
     */
    public byte[] wavBytesToMP3Bytes(String ffmpegPath) throws FFmpegNotFoundException, IOException {
        // Check if we have already processed the audio
        if (rawMP3Bytes != null) {
            log(Level.FINE, "Returning previously processed MP3 bytes");
        } else {
            log(Level.FINE, "Converting WAV bytes to MP3 bytes");

            // Read the raw WAV bytes from the WAV file
            byte[] rawWAVBytes = Files.readAllBytes(wavFile.toPath());

            // Ensure that the temporary directory exists
            IOMethods.createFolder(IOConstants.TEMP_FOLDER_PATH);
            log(Level.FINE, "Temporary folder created: " + IOConstants.TEMP_FOLDER_PATH);

            // Initialize the FFmpeg handler (if not done already)
            FFmpegHandler.initFFmpegHandler(ffmpegPath);

            // Generate the output path to the MP3 file
            String inputPath = IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "temp-1.wav");
            String outputPath = IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "temp-2.mp3");

            // Write WAV bytes into a file specified at the input path
            IOMethods.createFile(inputPath);
            Files.write(Paths.get(inputPath), rawWAVBytes);

            // Convert the original WAV file to a temporary MP3 file
            outputPath = FFmpegHandler.convertAudio(new File(inputPath), outputPath);

            // Read the raw MP3 bytes into a temporary file
            byte[] rawMP3Bytes = Files.readAllBytes(Paths.get(outputPath));

            // Delete the temporary files
            IOMethods.delete(inputPath);
            IOMethods.delete(outputPath);

            // Return the raw MP3 bytes
            log(Level.FINE, "Done converting WAV to MP3 bytes");
            return rawMP3Bytes;
        }

        return rawMP3Bytes;
    }

    // Miscellaneous public methods

    /**
     * Deletes the WAV file used for the audio processing.<br>
     * <b>Warning</b>: Attempting playback after deletion will result in <em>a lot</em> of errors.
     */
    public void deleteWAVFile() {
        boolean successfullyDeleted = IOMethods.delete(wavFile);

        if (successfullyDeleted) {
            log(Level.FINE, "Successfully deleted '" + wavFile + "'");
        } else {
            log(Level.WARNING, "Failed to delete '" + wavFile + "' now; will attempt delete after exit");
        }
    }

    /**
     * Method that operators are to answer to, once they have processed the required data.
     *
     * @param channelNum Audio channel number.
     * @param reply      Reply from the operator.
     */
    public void answer(int channelNum, double[] reply) {
        BlockingQueue<Byte> queue = outChannels.get(channelNum);
        try {
            byte[] replyBytes = AudioHelpers.packBytes(
                    TypeConversionUtils.doubleArrayToFloatArray(reply), bitsPerSample, audioFormat
            );
            for (Byte b : replyBytes) {
                queue.put(b);
            }
        } catch (InterruptedException e) {
            logException(e);
        }
    }

    // Private methods

    /**
     * Helper method that sets up the source data line for writing to.
     */
    private void setupSourceDataLine() {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        Mixer audioDevice = AudioSystem.getMixer(getOutputAudioDevice(
                DataFiles.SETTINGS_DATA_FILE.data.audioDeviceInfo
        ));

        try {
            sourceDataLine = (SourceDataLine) audioDevice.getLine(info);
            sourceDataLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method that resets the audio stream to the beginning.
     */
    private void resetAudioStream() {
        if (audioStream != null) {
            try {
                audioStream.close();
                audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(wavFile)));
                setupOperators();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method that updates the source data line's volume.<br>
     * Also updates the slowed audio's media player's volume if it is provided.
     *
     * @param volume Volume value. This value should be in the interval [0, 2] where 0 means
     *               silent and 2 means <b>twice</b> full volume.
     */
    private void updatePlaybackVolume(double volume) {
        FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        if (volume == 0) {
            volumeControl.setValue(Float.NEGATIVE_INFINITY);  // 'Gain' of -infinity dB
        } else {
            volumeControl.setValue(20f * (float) Math.log10(volume));  // Gain of this amount of dB
        }
    }

    /**
     * Helper method that sets the audio playback thread.
     */
    private void setAudioPlaybackThread() {
        Audio audio = this;

        audioPlaybackThread = new StoppableThread() {
            // Get playback buffer size
            final int playbackBufferSize = DataFiles.SETTINGS_DATA_FILE.data.playbackBufferSize;
            final byte[] bufferBytes = new byte[playbackBufferSize * bytesPerSample];
            int numBytesRead;

            @Override
            public void runner() {
                try {
                    boolean readThisIteration = true;
                    while (running.get()) {
                        if (!paused) {
                            if (readThisIteration) {
                                // Read bytes from audio stream
                                if (numBytesRead != -1) numBytesRead = audioStream.read(bufferBytes);

                                // Separate into channels
                                ArrayList<byte[]> channels = new ArrayList<>();
                                for (int i = 0; i < numChannels; i++) {
                                    channels.add(extractChannel(bufferBytes, i));
                                }

                                // Call operators to work on the channels' data
                                for (int i = numChannels - 1; i >= 0; i--) {
                                    byte[] data = channels.get(i);
                                    float[] samplesAsFloats = AudioHelpers.unpackBytes(
                                            data,
                                            numBytesRead / numChannels,
                                            bitsPerSample,
                                            audioFormat
                                    );
                                    channelOperators.get(i).call(
                                            audio, i, TypeConversionUtils.floatArrayToDoubleArray(samplesAsFloats)
                                    );
                                }
                            }

                            // Check if enough data is in `outChannels`
                            int outSegmentLength = OUT_SEGMENT_LENGTH * bytesPerSample;
                            boolean enoughData = true;
                            for (BlockingQueue<Byte> bq : outChannels) {
                                if (bq.size() < outSegmentLength) {
                                    enoughData = false;
                                    break;
                                }
                            }

                            // If enough, interleave processed samples and write to source data line
                            if (enoughData) {
                                ArrayList<byte[]> outputSegments = new ArrayList<>();
                                for (BlockingQueue<Byte> bq : outChannels) {
                                    byte[] segment = new byte[outSegmentLength];
                                    for (int i = 0; i < segment.length; i++) {
                                        segment[i] = bq.take();
                                    }
                                    outputSegments.add(segment);
                                }
                                byte[] interleavedChannels = interleaveChannels(outputSegments);
                                sourceDataLine.write(interleavedChannels, 0, interleavedChannels.length);
                            }

                            // Determine if we read this iteration
                            readThisIteration = true;
                            for (Operator op : channelOperators) {
                                if (op.remainingCapacity() < bufferBytes.length / bytesPerSample / numChannels) {
                                    readThisIteration = false;
                                    break;
                                }
                            }

                            // Halt if we read no more bytes and there is no more bytes to process
                            if (numBytesRead == -1 && !enoughData && !readThisIteration) break;
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logException(e);
                }
            }
        };
    }

    /**
     * Generates the audio sample data from the provided audio file.
     */
    private void generateSamples() {
        try {
            // Get the number of bytes that corresponds to each sample
            final int bytesPerSample = AudioHelpers.numBytesForNumBits(audioFormat.getSampleSizeInBits());

            // Get the total number of samples
            numRawSamples = audioStream.available() / bytesPerSample;

            // Calculate the number of samples needed for each window
            int numSamplesPerBuffer = SAMPLES_BUFFER_SIZE * numChannels;
            int numBuffers = MathUtils.ceilDiv(numRawSamples, numSamplesPerBuffer);

            // Define helper arrays
            byte[] bytes = new byte[numSamplesPerBuffer * bytesPerSample];
            float[] finalSamples = new float[numBuffers * numSamplesPerBuffer];  // Stores the final samples

            // Get samples
            int numBytesRead;
            int cycleNum = 0;  // Number of times we read from the audio stream
            while ((numBytesRead = audioStream.read(bytes)) != -1) {
                // Unpack the bytes into samples
                float[] samples = AudioHelpers.unpackBytes(bytes, numBytesRead, bitsPerSample, audioFormat);

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
            rawSamples = new double[numRawSamples];

            for (int i = 0; i < numRawSamples; i++) {
                rawSamples[i] = finalSamples[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (numChannels > 1) {
                // Calculate the number of mono samples there are
                numMonoSamples = numRawSamples / numChannels;

                // Fill in the mono audio samples array
                monoSamples = new double[numMonoSamples];

                for (int i = 0; i < numMonoSamples; i++) {
                    double sampleSum = rawSamples[i * numChannels];
                    for (int j = 1; j < numChannels; j++) {
                        sampleSum += rawSamples[i * numChannels + j];
                    }
                    monoSamples[i] = sampleSum / numChannels;
                }
            } else {  // Single-channel
                monoSamples = new double[numRawSamples];
                System.arraycopy(rawSamples, 0, monoSamples, 0, numRawSamples);
            }

            resetAudioStream();
        }
    }

    /**
     * Helper method that assists with skipping forwards in the audio.
     *
     * @param secondsToSkip Number of seconds to skip forwards from the current position.
     * @implNote Adapted from <a href="https://stackoverflow.com/a/52596824">this StackOverflow
     * answer</a>.
     */
    private void seekForwards(double secondsToSkip) {
        // Compute the number of bytes to skip
        long bytesToSkip = (long) (frameSize * frameRate * secondsToSkip);

        // Now skip until the correct number of bytes have been skipped
        try {
            long justSkipped;
            while (bytesToSkip > 0 && (justSkipped = audioStream.skip(bytesToSkip)) > 0) {
                bytesToSkip -= justSkipped;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method that assists with seeking backwards in the audio.
     *
     * @param timeToSeekTo Time to seek to in the audio.
     */
    private void seekBackwards(double timeToSeekTo) {
        resetAudioStream();
        seekForwards(timeToSeekTo);
    }

    /**
     * Helper method that extracts the bytes for a specific channel.
     *
     * @param rawBytes   The raw bytes that were read from the audio stream.
     * @param channelNum The channel number to extract.
     * @return Bytes belonging to that channel.
     */
    private byte[] extractChannel(byte[] rawBytes, int channelNum) {
        if (numChannels == 1) return rawBytes;

        byte[] output = new byte[rawBytes.length / numChannels];
        int bytePosition = 0;
        int outIndex = 0;
        for (byte datum : rawBytes) {
            if (bytePosition >= channelNum * bytesPerSample &&
                    bytePosition < (channelNum + numChannels - 1) * bytesPerSample) {
                output[outIndex] = datum;
                outIndex++;
            }
            bytePosition++;
            if (bytePosition == numChannels * bytesPerSample) {
                bytePosition = 0;
            }
        }
        return output;
    }

    /**
     * Interleave the different channels' bytes into one singular byte array.
     *
     * @param channels All channels' bytes.
     * @return Interleaved bytes array.
     */
    private byte[] interleaveChannels(ArrayList<byte[]> channels) {
        // Check if channels are the same length
        int first = channels.get(0).length;
        for (byte[] chan : channels) {
            if (chan.length != first) {
                throw new LengthException("Channel lengths were different");
            }
        }

        // Interleave channels
        byte[] output = new byte[first * channels.size()];
        int outIdx = 0;
        int chanIdx = 0;
        while (chanIdx + bytesPerSample <= first) {
            for (byte[] channel : channels) {
                System.arraycopy(channel, chanIdx, output, outIdx, bytesPerSample);
                outIdx += bytesPerSample;
            }
            chanIdx += bytesPerSample;
        }
        return output;
    }

    /**
     * Helper method that sets up all the operators, if they were not already set up.
     */
    private void setupOperators() {
        if (channelOperators.size() < numChannels) {
            for (int i = 0; i < numChannels; i++) {
                TimeStretchOperator op = new PhaseVocoderOperator(
                        1., SLOWDOWN_PROCESSING_LENGTH, SLOWDOWN_ANALYSIS_LENGTH, SLOWDOWN_WINDOW
                );
                channelOperators.add(op);
                new Thread(op).start();
            }
        }
    }

    /**
     * Helper method that resets all the operators.
     */
    private void resetOperators() {
        for (Operator op : channelOperators) op.stop();
        channelOperators.clear();
    }

    /**
     * Helper method that clears all channels' buffers.<br>
     * This clears both the out channels' queues as well as the channel operators' buffers.
     */
    private void clearChannelsBuffers() {
        for (BlockingQueue<Byte> bq : outChannels) bq.clear();
        for (Operator op : channelOperators) op.clearBuffers();
    }

    // Helper classes

    /**
     * Enum that contains different audio processing modes.
     */
    public enum ProcessingMode {WITH_SAMPLES, WITH_PLAYBACK}
}
