/*
 * FFmpegHandlerTest.java
 *
 * Created on 2022-05-06
 * Updated on 2022-06-28
 *
 * Description: Test `FFmpegHandler.java`.
 */

package site.overwrite.auditranscribe.audio;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FFmpegHandlerTest {
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
}