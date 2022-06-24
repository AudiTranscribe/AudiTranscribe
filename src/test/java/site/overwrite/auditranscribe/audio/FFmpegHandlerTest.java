/*
 * FFmpegHandlerTest.java
 *
 * Created on 2022-05-06
 * Updated on 2022-06-24
 *
 * Description: Test `FFmpegHandler.java`.
 */

package site.overwrite.auditranscribe.audio;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class FFmpegHandlerTest {
    @Disabled
    @Test
    void convertAudio() throws IOException, FFmpegNotFoundException, NoSuchAlgorithmException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("testing-audio-files/A440.mp3"));

        // Get the absolute path to the testing folder
        String testingFolderPath = testFile.getParent();

        // Create a FFmpeg handler
        FFmpegHandler handler = new FFmpegHandler("ffmpeg");
        String outputFilePath = handler.convertAudio(testFile, testingFolderPath + "test-converted.WAV");

        // Check the output file path, and ensure that the extension is no longer in capitals
        assertEquals(testingFolderPath + "test-converted.wav", outputFilePath);

        // Compute file hash
        byte[] buff = new byte[1024];
        int bytesCount;

        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(Path.of(testingFolderPath + "test-converted.wav"));
             DigestInputStream dis = new DigestInputStream(is, md)) {
            while ((bytesCount = dis.read(buff)) != -1) {
                md.update(buff, 0, bytesCount);
            }
        }
        byte[] digest = md.digest();
        String md5HashInHex = HexFormat.of().formatHex(digest);

        // Remove the file
        assertTrue(
                (new File(testingFolderPath + "test-converted.wav")).delete(),
                "Failed to delete the converted file."
        );

        // Compare the hash
        assertEquals(
                "d2abaeb87297ef6137616953829d8922",
                md5HashInHex,
                "Converted file hash incorrect."
        );
    }
}