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

package app.auditranscribe.audio;

import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.audio.exceptions.FFmpegCommandFailedException;
import app.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import app.auditranscribe.generic.ClassWithLogging;
import app.auditranscribe.generic.tuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Methods that handle FFmpeg interactions.
 */
public class FFmpegHandler extends ClassWithLogging {
    // Constants
    public static final List<String> VALID_EXTENSIONS = List.of(new String[]{
            ".wav", ".mp3", ".flac", ".ogg", ".aif", ".aiff"
    });

    // Attributes
    public final String ffmpegPath;

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
    @ExcludeFromGeneratedCoverageReport
    public static String getPathToFFmpeg() throws FFmpegNotFoundException {
        // Get the operating system in question
        OSType os = OSMethods.getOS();

        // Generate the command to execute
        String[] command;
        if (os == OSType.WINDOWS) {
            command = new String[]{"cmd.exe", "/c", "where ffmpeg"};
        } else {
            command = new String[]{"sh", "-c", "which ffmpeg"};
        }

        // Define variables
        final String[] ffmpegPath = new String[1];
        try {
            // Execute the command on the standard runtime
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);

            // Create a reader for the output
            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Read the output from the command
            String temp;
            StringBuilder sb = new StringBuilder();
            while ((temp = stdOutput.readLine()) != null) {
                sb.append(temp);  // Add every line of the output
            }

            // Set the FFmpeg path
            temp = sb.toString();
            if (temp.equals("")) {
                throw new FFmpegNotFoundException("FFmpeg binary could not be found automatically.");
            }
            ffmpegPath[0] = temp;
        } catch (IOException e) {
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
        try {
            // Execute the command on the standard runtime
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{ffmpegPath, "-version"});

            if (process.waitFor() == 0) {
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
        String[] command = {
                ffmpegPath,
                "-y",                 // Override output file
                "-i", inputFilePath,  // Specify input file
                "-b:a", "96k",        // Constant bitrate for MP3 and related files of 96,000 bits
                outputFilePath        // Specify output file
        };

        // Execute FFmpeg command
        boolean success = handleFFmpegCommandExec(command);
        if (success) {
            log(Level.FINE, "Successfully converted '" + file.getName() + "'");
            return outputFilePath;
        } else {
            throw new FFmpegCommandFailedException("FFmpeg command " + Arrays.toString(command) + " failed");
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
        String[] command = {
                ffmpegPath,
                "-y",                            // Override output file
                "-i", inputFilePath,             // Specify input file
                "-filter:a", "atempo=" + tempo,  // Specify tempo
                "-vn",                           // Disable video recording
                outputFilePath                   // Specify output file
        };

        // Execute FFmpeg command
        boolean success = handleFFmpegCommandExec(command);
        if (success) {
            log(
                    Level.FINE,
                    "Successfully generated alternate tempo audio for '" +
                            file.getName() + "' at " + tempo + "x tempo"
            );
            return outputFilePath;
        } else {
            throw new FFmpegCommandFailedException("FFmpeg command " + Arrays.toString(command) + " failed");
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
     * Helper method that handles the FFmpeg command execution.
     *
     * @param command The command to execute.
     * @return A boolean, describing whether the FFmpeg command was successful (<code>true</code>)
     * or not (<code>false</code>).
     */
    private boolean handleFFmpegCommandExec(String[] command) {
        try {
            // Execute the command on the standard runtime
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
