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

import static org.junit.jupiter.api.Assertions.*;

class AudioConverterTest {
    @Test
    void convertAudio() throws IOException {
        // Get a testing MP3 file
        File testFile = new File(IOMethods.getAbsoluteFilePath("testing-audio-files/A440.mp3"));

        // Create a converter
        // Todo: make this dependent on operating system
        AudioConverter converter = new AudioConverter("ffmpeg");

        converter.convertAudio(testFile, ".wav");
    }
}