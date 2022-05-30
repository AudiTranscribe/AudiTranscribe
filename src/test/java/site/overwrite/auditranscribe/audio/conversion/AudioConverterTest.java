/*
 * AudioConverterTest.java
 *
 * Created on 2022-05-06
 * Updated on 2022-05-30
 *
 * Description: Test `AudioConverter.java`.
 */

package site.overwrite.auditranscribe.audio.conversion;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class AudioConverterTest {
    @Test
    void convertAudio() throws IOException, NoSuchAlgorithmException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("testing-audio-files/A440.mp3"));

        // Create a converter
        // Todo: make this dependent on operating system
        AudioConverter converter = new AudioConverter("ffmpeg");
        converter.convertAudio(testFile, "test-converted.wAV");

        // Compute file hash
        byte[] buff = new byte[1024];
        int bytesCount;

        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(Paths.get("test-converted.wav"));
             DigestInputStream dis = new DigestInputStream(is, md))
        {
            while ((bytesCount = dis.read(buff)) != -1) {
                md.update(buff, 0, bytesCount);
            }
        }
        byte[] digest = md.digest();
        String md5HashInHex = HexFormat.of().formatHex(digest);

        // Remove the file
        assertTrue((new File("test-converted.wav")).delete(), "Failed to delete the converted file.");

        // Compare the hash
        assertEquals("d2abaeb87297ef6137616953829d8922", md5HashInHex, "Converted file hash incorrect.");
    }
}