/*
 * FFmpegHandler.java
 * Description: Methods that handle FFmpeg interactions.
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

import site.overwrite.auditranscribe.exceptions.audio.FFmpegCommandFailedException;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.StreamGobbler;
import site.overwrite.auditranscribe.misc.tuples.Pair;
import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Methods that handle FFmpeg interactions.
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
     * Method that attempts to find the FFmpeg installation path automatically by using the
     * command-line interface of FFmpeg.
     *
     * @return A string, representing the FFmpeg installation path.
     * @throws FFmpegNotFoundException If the program fails to find the FFmpeg installation.
     */
    public static String getPathToFFmpeg() throws FFmpegNotFoundException {
        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        if (OSMethods.getOS() == OSType.WINDOWS) {
            builder.command("cmd.exe", "/c", "where ffmpeg");
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
            if (exitCode != 0) throw new FFmpegNotFoundException("FFmpeg binary cannot be located.\n" + ffmpegPath[0]);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Return the ffmpeg path
        return ffmpegPath[0];
    }

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

        // Check if the provided FFmpeg path works
        try {
            // Build the process
            Process process = builder.start();

            // Check exit code of the command
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;  // Exited successfully
            } else {
                throw new IOException("FFmpeg path not at '" + ffmpegPath + "'.");
            }
        } catch (IOException | InterruptedException e) {
            return false;
        }
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
        // Obtain the processed input and output file paths
        Pair<String, String> filePathPair = processPaths(file, outputFilePath);
        String inputFilePath = filePathPair.value0();
        outputFilePath = filePathPair.value1();

        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                ffmpegPath,
                "-y",                 // Override output file
                "-i", inputFilePath,  // Specify input file
                "-b:a", "96k",        // Constant bitrate for MP3 and related files of 96,000 bits
                outputFilePath        // Specify output file
        );

        // Execute FFmpeg command
        boolean success = handleFFmpegCommandExec(builder);
        if (success) {
            return outputFilePath;
        } else {
            throw new FFmpegCommandFailedException("FFmpeg command " + builder.command() + " failed");
        }
    }

    /**
     * Method that adjusts the tempo the original audio file <code>file</code> and saves the
     * adjusted audio into a new audio file with extension <code>extension</code>.<br>
     * Note that this method produces a new audio file, <code>output.ext</code> (with the
     * extension <code>.ext</code>).
     *
     * @param file           File object representing the original audio file.
     * @param outputFilePath Absolute path to the output file, <b>including the extension</b>.
     * @return A string, representing the output file's path after processing it.
     */
    public String generateAltTempoAudio(File file, String outputFilePath, double tempo) {
        // Obtain the processed input and output file paths
        Pair<String, String> filePathPair = processPaths(file, outputFilePath);
        String inputFilePath = filePathPair.value0();
        outputFilePath = filePathPair.value1();

        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                ffmpegPath,
                "-y",                            // Override output file
                "-i", inputFilePath,             // Specify input file
                "-filter:a", "atempo=" + tempo,  // Specify tempo
                "-vn",                           // Disable video recording
                outputFilePath                   // Specify output file
        );

        // Execute FFmpeg command
        boolean success = handleFFmpegCommandExec(builder);
        if (success) {
            return outputFilePath;
        } else {
            throw new FFmpegCommandFailedException("FFmpeg command " + builder.command() + " failed");
        }
    }

    // Private methods

    /**
     * Helper method to process the paths.
     *
     * @param file           Input file.
     * @param outputFilePath Output file path, <b>including the extension</b>.
     * @return A pair. First value is the processed <em>input</em> path. Second value is the
     * processed <em>output</em> path.
     */
    private static Pair<String, String> processPaths(File file, String outputFilePath) {
        // Obtain the extension from the file path
        String[] split = outputFilePath.split("\\.");
        String extension = "." + split[split.length - 1];  // The extension is the last one

        // Convert the extension to lowercase
        extension = extension.toLowerCase();

        // Update the output file path
        outputFilePath = outputFilePath.substring(0, outputFilePath.length() - extension.length()) + extension;

        // Treat the paths
        String inputFilePath = IOMethods.treatPath(file.getAbsolutePath());
        outputFilePath = IOMethods.treatPath(outputFilePath);

        // Return as a pair
        return new Pair<>(inputFilePath, outputFilePath);
    }

    /**
     * Helper method that handles the FFmpeg output.
     *
     * @param builder Process builder object that is used to run the FFmpeg command.
     * @return A boolean, describing whether the FFmpeg command was successful (<code>true</code>)
     * or not (<code>false</code>).
     */
    private static boolean handleFFmpegCommandExec(ProcessBuilder builder) {
        // Specify the working directory
        builder.directory(new File(System.getProperty("user.home")));

        // Define variables
        try {
            // Build the process
            Process process = builder.start();

            // Check exit code of the command
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Exit code 0 => exited successfully => can return
                return true;
            } else {
                throw new IOException("Command execution failed");
            }
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
