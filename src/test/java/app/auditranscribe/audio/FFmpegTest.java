package app.auditranscribe.audio;

import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FFmpegTest {
    @Test
    @Order(1)
    void ensureFFmpegHandlerNotInitialized() {
        assertThrowsExactly(
                FFmpeg.NotInitializedException.class,
                () -> FFmpeg.convertAudio(null, "fake-file.wav")
        );
    }

    @Test
    @Order(2)
    void getPathToFFmpeg() {
        // Check if the FFmpeg binary can be accessed using CLI
        if (FFmpeg.checkFFmpegPath("ffmpeg")) {
            // Make sure that the method is ABLE TO GET that path and DOES NOT THROW an exception
            assertDoesNotThrow(FFmpeg::getPathToFFmpeg);
        } else {
            // Make sure that the method CANNOT get the path and THROWS an exception
            assertThrowsExactly(FFmpeg.BinaryNotFoundException.class, FFmpeg::getPathToFFmpeg);
        }
    }

    @Test
    @Order(3)
    void checkFFmpegPath() {
        try {
            assertTrue(FFmpeg.checkFFmpegPath("ffmpeg"));  // Should exist => true
        } catch (AssertionError e) {
            assertTrue(FFmpeg.checkFFmpegPath(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath));
        }

        assertFalse(FFmpeg.checkFFmpegPath("fake-ffmpeg-path"));  // IO Exception should be thrown => false
        assertFalse(FFmpeg.checkFFmpegPath("git"));  // Should give error code 1 => false
    }

    @Test
    @Order(4)
    void ffmpegHandlerInitFailureTest() {
        // Try to define a handler with a non-existent FFmpeg binary
        assertThrowsExactly(
                FFmpeg.BinaryNotFoundException.class,
                () -> FFmpeg.initFFmpegHandler("not-ffmpeg")
        );
    }

    @Test
    @Order(5)
    void convertAudio() throws FFmpeg.BinaryNotFoundException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("test-files/general/audio/A440.mp3"));

        // Get the absolute path to the testing folder
        String testingFolderPath = testFile.getParent();

        // Initialize the FFmpeg handler
        try {
            FFmpeg.initFFmpegHandler("ffmpeg");
        } catch (FFmpeg.BinaryNotFoundException e) {
            // Try to get the path from the settings file
            FFmpeg.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Determine the output path
        String outputFilePath = FFmpeg.convertAudio(
                testFile, IOMethods.joinPaths(testingFolderPath, "test-converted.WAV")
        );

        // Check the output file path, and ensure that the extension is no longer in capitals
        String correctOutputPath = IOMethods.joinPaths(testingFolderPath, "test-converted.wav");
        assertEquals(correctOutputPath, outputFilePath);

        // Remove the file
        IOMethods.delete(correctOutputPath);
    }

    @Test
    @Order(5)
    void convertAudioFailureTest() throws FFmpeg.BinaryNotFoundException {
        // Initialize the FFmpeg handler
        try {
            FFmpeg.initFFmpegHandler("ffmpeg");
        } catch (FFmpeg.BinaryNotFoundException e) {
            // Try to get the path from the settings file
            FFmpeg.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
        }

        // Try to test on a non-existent MP3 file
        assertThrowsExactly(FFmpeg.CommandFailedException.class, () -> FFmpeg.convertAudio(
                new File("non-existent-file.mp3"), "no-output.mp3"
        ));
    }
}