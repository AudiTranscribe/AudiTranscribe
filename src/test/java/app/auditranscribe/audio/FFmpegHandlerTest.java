package app.auditranscribe.audio;

import app.auditranscribe.audio.exceptions.FFmpegCommandFailedException;
import app.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;

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
        assertThrowsExactly(
                FFmpegNotFoundException.class,
                () -> FFmpegHandler.initFFmpegHandler("not-ffmpeg")
        );
    }

    @Test
    @Order(4)
    void convertAudio() throws FFmpegNotFoundException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/A440.mp3"));

        // Get the absolute path to the testing folder
        String testingFolderPath = testFile.getParent();

        // Initialize the FFmpeg handler
        try {
            FFmpegHandler.initFFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            FFmpegHandler.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Determine the output path
        String outputFilePath = FFmpegHandler.convertAudio(
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
        // Initialize the FFmpeg handler
        try {
            FFmpegHandler.initFFmpegHandler("ffmpeg");
        } catch (FFmpegNotFoundException e) {
            // Try to get the path from the settings file
            FFmpegHandler.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Try to test on a non-existent MP3 file
        assertThrowsExactly(FFmpegCommandFailedException.class, () -> FFmpegHandler.convertAudio(
                new File("non-existent-file.mp3"), "no-output.mp3"
        ));
    }
}