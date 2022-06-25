/*
 * FFmpegHandler.java
 *
 * Created on 2022-05-06
 * Updated on 2022-06-25
 *
 * Description: Methods that help handle the FFmpeg commands and methods.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Methods that help handle the FFmpeg commands and methods.
 */
public class FFmpegHandler {
    // Constants
    public static final List<String> VALID_EXTENSIONS = List.of(new String[]{
            ".wav", ".mp3", ".flac", ".ogg", ".aif", ".aiff"
    });

    // Attributes
    String ffmpegPath;

    /**
     * Initialization method for the audio converter.
     *
     * @param ffmpegPath The path to the FFmpeg binary.
     * @throws FFmpegNotFoundException If FFmpeg was not found at the specified path.
     */
    public FFmpegHandler(String ffmpegPath) throws FFmpegNotFoundException {
        // Check FFmpeg path
        if (checkFFmpegPath(ffmpegPath)) {
            this.ffmpegPath = ffmpegPath;
        } else {
            throw new FFmpegNotFoundException("Could not find FFmpeg at '" + ffmpegPath + "'.");
        }
    }

    // Public methods

    /**
     * Method that checks if the specified path contains the FFmpeg binary.
     *
     * @param ffmpegPath Absolute path to the FFmpeg binary.
     * @return A boolean, <code>true</code> if the FFmpeg binary was found, and <code>false</code>
     * otherwise.
     */
    public static boolean checkFFmpegPath(String ffmpegPath) {
        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(ffmpegPath, "-version");

        // Specify the working directory
        builder.directory(new File(System.getProperty("user.home")));

        // Define variables
        try {
            // Build the process
            Process process = builder.start();

            // Check exit code of the command
            int exitCode = process.waitFor();
            if (exitCode == 0) return true;  // Exited successfully

        } catch (IOException | InterruptedException e) {
            return false;
        }
        return false;
    }

    /**
     * Method that converts the original audio file <code>file</code> into a new audio file with
     * extension <code>extension</code>.<br>
     * Note that this method produces a new audio file, <code>converted.ext</code> (with the
     * extension <code>.ext</code>).
     *
     * @param file           File object representing the original audio file.
     * @param outputFilePath Absolute path to the output file, <b>including the extension</b>.
     * @return A string, representing the output file's path after processing it.
     */
    public String convertAudio(File file, String outputFilePath) {
        // Obtain the extension from the file path
        String[] split = outputFilePath.split("\\.");
        String extension = "." + split[split.length - 1];  // The extension is the last one

        // Convert the extension to lowercase
        extension = extension.toLowerCase();

        // Update the output file path
        outputFilePath = outputFilePath.substring(0, outputFilePath.length() - extension.length()) + extension;

        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                ffmpegPath,
                "-y",                          // Override output file
                "-i", file.getAbsolutePath().replace("%20", " "),  // Specify input file
                "-b:a", "96k",                 // Constant bitrate for MP3 and related files of 96,000 bits
                outputFilePath.replace("%20", " ")                 // Specify output file
        );

        // Specify the working directory
        builder.directory(new File(System.getProperty("user.home")));

        // Define variables
        try {
            // Build the process
            Process process = builder.start();

            // Check exit code of the command
            int exitCode = process.waitFor();
            if (exitCode == 0) return outputFilePath;  // Exit code 0 => exited successfully => can return

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("FFmpeg command " + builder.command() + " failed");
        }

        return null;
    }
}
