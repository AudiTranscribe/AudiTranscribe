/*
 * FFmpegHandlerTest.java
 *
 * Created on 2022-05-06
 * Updated on 2022-06-24
 *
 * Description: Test `FFmpegHandler.java`.
 */

package site.overwrite.auditranscribe.audio;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FFmpegHandlerTest {
    @Test
    void convertAudio() throws FFmpegNotFoundException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("testing-audio-files/A440.mp3"));

        // Get the absolute path to the testing folder
        String testingFolderPath = testFile.getParent();

        // Create a FFmpeg handler
        FFmpegHandler handler = new FFmpegHandler("ffmpeg");
        String outputFilePath = handler.convertAudio(testFile, testingFolderPath + "test-converted.WAV");

        // Check the output file path, and ensure that the extension is no longer in capitals
        assertEquals(testingFolderPath + "test-converted.wav", outputFilePath);

        // Remove the file
        assertTrue(
                (new File(testingFolderPath + "test-converted.wav")).delete(),
                "Failed to delete the converted file."
        );
    }
}