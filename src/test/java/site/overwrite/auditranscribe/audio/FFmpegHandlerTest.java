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

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegCommandFailedException;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FFmpegHandlerTest {
    @Test
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

        // Try to define a handler with a non-existent FFmpeg binary
        assertThrowsExactly(FFmpegNotFoundException.class, () -> new FFmpegHandler("not-ffmpeg"));
    }
}