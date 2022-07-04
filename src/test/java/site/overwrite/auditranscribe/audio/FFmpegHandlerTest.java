/*
 * FFmpegHandlerTest.java
 *
 * Created on 2022-05-06
 * Updated on 2022-07-04
 *
 * Description: Test `FFmpegHandler.java`.
 */

package site.overwrite.auditranscribe.audio;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegCommandFailedException;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;

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
            assertTrue(FFmpegHandler.checkFFmpegPath(new SettingsFile().data.ffmpegInstallationPath));
        }
        assertFalse(FFmpegHandler.checkFFmpegPath("not-the-ffmpeg-path"));  // IO Exception should be thrown => false
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
            handler = new FFmpegHandler(new SettingsFile().data.ffmpegInstallationPath);
        }

        // Determine the output path
        String outputFilePath = handler.convertAudio(
                testFile, IOMethods.joinPaths(testingFolderPath, "test-converted.WAV")
        );

        // Check the output file path, and ensure that the extension is no longer in capitals
        String correctOutputPath = IOMethods.joinPaths(testingFolderPath, "test-converted.wav");
        assertEquals(correctOutputPath, outputFilePath);

        // Remove the file
        IOMethods.deleteFile(correctOutputPath);
    }

    @Test
    void convertAudioFailureTest() throws FFmpegNotFoundException {
        // Determine the FFmpeg path
        FFmpegHandler handler;
        try {
            handler = new FFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            handler = new FFmpegHandler(new SettingsFile().data.ffmpegInstallationPath);
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