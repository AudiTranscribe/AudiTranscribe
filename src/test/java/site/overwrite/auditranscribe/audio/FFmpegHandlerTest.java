/*
 * FFmpegHandlerTest.java
 * Description: Test `FFmpegHandler.java`.
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

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import site.overwrite.auditranscribe.audio.exceptions.FFmpegCommandFailedException;
import site.overwrite.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FFmpegHandlerTest {
    @Test
    @Order(1)
    void getPathToFFmpeg() {
        // Check if the FFmpeg binary can be accessed using CLI
        if (FFmpegHandler.checkFFmpegPath("ffmpeg")) {
            // Make sure that the method is ABLE TO GET that path and DOES NOT THROW an exception
            assertDoesNotThrow(FFmpegHandler::getPathToFFmpeg);
        } else {
            // Make sure that the method CANNOT get the path and THROWS an exception
            assertThrowsExactly(FFmpegNotFoundException.class, FFmpegHandler::getPathToFFmpeg);
        }

        // Todo: force test failure to obtain FFmpeg path automatically
    }

    @Test
    @Order(2)
    void checkFFmpegPath() {
        try {
            assertTrue(FFmpegHandler.checkFFmpegPath("ffmpeg"));  // Should exist => true
        } catch (AssertionError e) {
            assertTrue(FFmpegHandler.checkFFmpegPath(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath));
        }
        assertFalse(FFmpegHandler.checkFFmpegPath("fake-ffmpeg-path"));  // IO Exception should be thrown => false
        assertFalse(FFmpegHandler.checkFFmpegPath("git"));  // Should give error code 1 => false
    }

    @Test
    @Order(3)
    void ffmpegHandlerInitFailureTest() {
        // Try to define a handler with a non-existent FFmpeg binary
        assertThrowsExactly(FFmpegNotFoundException.class, () -> new FFmpegHandler("not-ffmpeg"));
    }

    @Test
    @Order(4)
    void convertAudio() throws FFmpegNotFoundException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("testing-files/audio/A440.mp3"));

        // Get the absolute path to the testing folder
        String testingFolderPath = testFile.getParent();

        // Determine the FFmpeg path
        FFmpegHandler handler;
        try {
            handler = new FFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            handler = new FFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Determine the output path
        String outputFilePath = handler.convertAudio(
                testFile, IOMethods.joinPaths(testingFolderPath, "test-converted.WAV")
        );

        // Check the output file path, and ensure that the extension is no longer in capitals
        String correctOutputPath = IOMethods.joinPaths(testingFolderPath, "test-converted.wav");
        assertEquals(correctOutputPath, outputFilePath);

        // Remove the file
        IOMethods.delete(correctOutputPath);
    }

    @Test
    @Order(4)
    void convertAudioFailureTest() throws FFmpegNotFoundException {
        // Determine the FFmpeg path
        FFmpegHandler handler;
        try {
            handler = new FFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            handler = new FFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Try to test on a non-existent MP3 file
        FFmpegHandler finalHandler = handler;
        assertThrowsExactly(FFmpegCommandFailedException.class, () -> finalHandler.convertAudio(
                new File("non-existent-file.mp3"), "no-output.mp3"
        ));
    }

    @Test
    @Order(5)
    void generateAltTempoAudio() throws FFmpegNotFoundException, IOException {
        // Get a testing audio file
        File testFile = new File(IOMethods.getAbsoluteFilePath("testing-files/audio/137bpmNoisyShort.wav"));

        // Get the absolute path to the testing folder
        String testingFolderPath = testFile.getParent();

        // Determine the FFmpeg path
        FFmpegHandler handler;
        try {
            handler = new FFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            handler = new FFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Determine the output paths
        String outputFilePath1 = handler.generateAltTempoAudio(
                testFile, IOMethods.joinPaths(testingFolderPath, "test-output-sped-up.WAV"), 2
        );
        String outputFilePath2 = handler.generateAltTempoAudio(
                testFile, IOMethods.joinPaths(testingFolderPath, "test-output-slowed.WAV"), 0.5
        );

        // Check the output file path, and ensure that the extension is no longer in capitals
        String correctOutputPath1 = IOMethods.joinPaths(testingFolderPath, "test-output-sped-up.wav");
        String correctOutputPath2 = IOMethods.joinPaths(testingFolderPath, "test-output-slowed.wav");

        assertEquals(correctOutputPath1, outputFilePath1);
        assertEquals(45048, Files.size(Path.of(correctOutputPath1)), 2.5e3);

        assertEquals(correctOutputPath2, outputFilePath2);
        assertEquals(172970, Files.size(Path.of(correctOutputPath2)), 2.5e3);

        // Remove the files
        IOMethods.delete(correctOutputPath1);
        IOMethods.delete(correctOutputPath2);
    }

    @Test
    @Order(5)
    void generateAltTempoAudioFailureTest() throws FFmpegNotFoundException {
        // Determine the FFmpeg path
        FFmpegHandler handler;
        try {
            handler = new FFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            handler = new FFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Try to test on a non-existent MP3 file
        FFmpegHandler finalHandler = handler;
        assertThrowsExactly(FFmpegCommandFailedException.class, () -> finalHandler.generateAltTempoAudio(
                new File("non-existent-file.mp3"), "no-output.mp3", 1.234
        ));
    }
}