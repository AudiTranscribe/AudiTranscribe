/*
 * FFmpeg.java
 * Description: Class that handles FFmpeg interactions.
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

import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.generic.tuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Class that handles FFmpeg interactions.
 */
public final class FFmpeg extends LoggableClass {
    // Constants
    public static final List<String> VALID_EXTENSIONS = List.of(new String[]{
            ".wav", ".mp3", ".flac", ".ogg", ".aif", ".aiff"
    });

    // Attributes
    public static FFmpeg handler;

    private final String ffmpegPath;

    /**
     * Initialization method for the FFmpeg handler.
     *
     * @param ffmpegPath Path to the FFmpeg binary.
     */
    private FFmpeg(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    // Public methods

    /**
     * Method that initializes a global FFmpeg handler.
     *
     * @param ffmpegPath Path to the FFmpeg binary.
     * @throws BinaryNotFoundException If FFmpeg was not found at the specified path.
     */

    public static void initFFmpegHandler(String ffmpegPath) throws BinaryNotFoundException {
        if (handler == null) {
            if (checkFFmpegPath(ffmpegPath)) {
                handler = new FFmpeg(ffmpegPath);
            } else {
                throw new BinaryNotFoundException("Could not find FFmpeg at '" + ffmpegPath + "'.");
            }
        }
    }

    /**
     * Method that attempts to find the FFmpeg installation path automatically by using the
     * command-line interface of FFmpeg.
     *
     * @return A string, representing the path to the FFmpeg binary.
     * @throws BinaryNotFoundException If the program fails to find the FFmpeg binary.
     */
    @ExcludeFromGeneratedCoverageReport
    public static String getPathToFFmpeg() throws BinaryNotFoundException {
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
                throw new BinaryNotFoundException("FFmpeg binary could not be found automatically.");
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
    public static String convertAudio(File file, String outputFilePath) {
        checkIfHandlerWasInitialized();
        return handler.convertAudioHelper(file, outputFilePath);
    }

    // Private methods

    /**
     * Helper method that checks whether the global FFmpeg handler was initialized.
     *
     * @throws NotInitializedException If the handler has not been initialized.
     */
    private static void checkIfHandlerWasInitialized() {
        if (handler == null) throw new NotInitializedException(
                "The global FFmpeg handler has yet to be initialized"
        );
    }

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
    @ExcludeFromGeneratedCoverageReport
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
    private String convertAudioHelper(File file, String outputFilePath) {
        // Obtain the processed input and output file paths
        Pair<String, String> filePathPair = processPaths(file, outputFilePath);
        String inputFilePath = filePathPair.value0();
        outputFilePath = filePathPair.value1();

        // Generate the command to execute
        String[] command = {
                ffmpegPath,
                "-y",                 // Override output file
                "-i", inputFilePath,  // Specify input file
                "-b:a", "128k",       // Constant bitrate for MP3 and related files of 128,000 bits
                outputFilePath        // Specify output file
        };

        // Execute FFmpeg command
        boolean success = handleFFmpegCommandExec(command);
        if (success) {
            log(Level.FINE, "Successfully converted '" + file.getName() + "'");
            return outputFilePath;
        } else {
            throw new CommandFailedException("FFmpeg command " + Arrays.toString(command) + " failed");
        }
    }

    // Exceptions

    /**
     * Exception that is thrown when the FFmpeg command fails to run.
     */
    @ExcludeFromGeneratedCoverageReport
    public static class CommandFailedException extends RuntimeException {
        public CommandFailedException(String message) {
            super(message);
        }
    }

    /**
     * Exception that is thrown if the global FFmpeg handler has yet to be initialized.
     */
    @ExcludeFromGeneratedCoverageReport
    public static class NotInitializedException extends RuntimeException {
        public NotInitializedException(String message) {
            super(message);
        }
    }

    /**
     * Exception that is thrown when the FFmpeg binary path is not found.
     */
    @ExcludeFromGeneratedCoverageReport
    public static class BinaryNotFoundException extends Exception {
        public BinaryNotFoundException(String message) {
            super(message);
        }
    }
}
