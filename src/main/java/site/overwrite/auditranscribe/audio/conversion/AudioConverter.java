/*
 * AudioConverter.java
 *
 * Created on 2022-05-06
 * Updated on 2022-05-30
 *
 * Description: Methods that help to convert audio files to the correct format.
 */

package site.overwrite.auditranscribe.audio.conversion;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Methods that help to convert audio files to the correct format.
 */
public class AudioConverter {
    // Constants
    final Map<String, String> EXTENSION_TO_CODEC = Map.ofEntries(
            entry(".wav", "wav"),
            entry(".mp3", "mp3"),
            entry(".flac", "flac"),
            entry(".aif", "aac"),
            entry(".aiff", "aac")
    );

    final Map<String, String> CODEC_TO_EXTENSION = Map.ofEntries(
            entry("wav", ".wav"),
            entry("mp3", ".mp3"),
            entry("flac", ".flac"),
            entry("aac", ".aif")  // Let's assume that we just use ".aif"
    );

    // Attributes
    FFmpegExecutor executor;

    /**
     * Initialization method for the audio converter.
     *
     * @param ffmpegPath  Path to the ffmpeg binary.
     * @throws IOException If the program fails to find the ffmpeg installation.
     */
    public AudioConverter(String ffmpegPath) throws IOException {
        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
        executor = new FFmpegExecutor(ffmpeg);
    }

    // Public methods

    /**
     * Method that converts the original audio file <code>file</code> into a new audio file with
     * extension <code>extension</code>.<br>
     * Note that this method produces a new audio file, <code>converted.ext</code> (with the
     * provided extension <code>.ext</code>).
     *
     * @param file      File object representing the original audio file.
     * @param extension Extension of the resulting audio object. <b>Note that the extension
     *                  <em>should have a dot</em> before the extension string</b> (e.g.
     *                  <code>.wav</code>, <code>.mp3</code>)
     */
    public void convertAudio(File file, String extension) {
        // Convert the extension to lowercase
        extension = extension.toLowerCase();

        // Define the command to run
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(file.getAbsolutePath())
                .overrideOutputFiles(true)

                .addOutput("converted" + extension)
                .setFormat(EXTENSION_TO_CODEC.get(extension))

                .done();

        // Run the command
        executor.createJob(builder).run();
    }
}
