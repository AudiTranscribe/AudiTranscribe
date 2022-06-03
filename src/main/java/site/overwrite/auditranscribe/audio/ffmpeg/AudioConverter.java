/*
 * AudioConverter.java
 *
 * Created on 2022-05-06
 * Updated on 2022-06-03
 *
 * Description: Methods that help to convert audio files to the correct format.
 */

package site.overwrite.auditranscribe.audio.ffmpeg;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import site.overwrite.auditranscribe.io.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

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
     * @throws IOException    If the program fails to find the ffmpeg installation.
     * @throws FFmpegNotFound If the program fails to find the ffmpeg installation.
     */
    public AudioConverter() throws IOException, FFmpegNotFound {
        // Continually attempt to get the ffmpeg path
        String ffmpegPath = null;
        while (ffmpegPath == null) {  // Todo: is this a good way of doing this?
            ffmpegPath = getPathToffmpeg();
        }

        // Create the ffmpeg instance
        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);

        // Define ffmpeg executor
        executor = new FFmpegExecutor(ffmpeg);
    }

    // Public methods

    /**
     * Method that converts the original audio file <code>file</code> into a new audio file with
     * extension <code>extension</code>.<br>
     * Note that this method produces a new audio file, <code>converted.ext</code> (with the
     * provided extension <code>.ext</code>).
     *
     * @param file           File object representing the original audio file.
     * @param outputFilePath Output file's path, <b>including the extension</b>.
     * @return A string, representing the output file's path after processing it.
     */
    public String convertAudio(File file, String outputFilePath) {
        // Obtain the extension from the file path
        String[] split = outputFilePath.split("\\.");
        String extension = "." + split[split.length - 1];  // The extension is the last one

        // Convert the extension to lowercase
        extension = extension.toLowerCase();

        // Obtain the filepath excluding the extension
        String filePath = outputFilePath.substring(0, outputFilePath.length() - extension.length());

        // Define the command to run
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(file.getAbsolutePath())
                .overrideOutputFiles(true)

                .addOutput(filePath + extension)
                .setFormat(EXTENSION_TO_CODEC.get(extension))

                .done();

        // Run the command
        executor.createJob(builder).run();

        // Return the output file
        return filePath + extension;
    }

    // Private methods

    /**
     * Method that attempts to find the ffmpeg installation path.
     *
     * @return A string, representing the ffmpeg installation path.
     * @throws FFmpegNotFound If the program fails to find the ffmpeg installation.
     */
    private String getPathToffmpeg() throws FFmpegNotFound {
        // Check the operating system
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "where ffmpeg");  // Todo: check if this works on windows
        } else {
            builder.command("sh", "-c", "which ffmpeg");
        }

        // Specify the working directory
        builder.directory(new File(System.getProperty("user.home")));

        // Define variables
        final String[] ffmpegPath = new String[1];
        try {
            // Build the process
            Process process = builder.start();

            // Define stream gobbler
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), s -> ffmpegPath[0] = s);

            // Start the process
            Executors.newSingleThreadExecutor().submit(streamGobbler);

            // Check exit code of the command
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new FFmpegNotFound("ffmpeg binary cannot be located.\n" + ffmpegPath[0]);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Return the ffmpeg path
        return ffmpegPath[0];
    }
}
