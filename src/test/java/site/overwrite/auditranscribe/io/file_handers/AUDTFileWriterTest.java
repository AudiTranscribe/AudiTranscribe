/*
 * AUDTFileWriterTest.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-01
 *
 * Description: Test `AUDTFileWriter.java`.
 */

package site.overwrite.auditranscribe.io.file_handers;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AUDTFileWriterTest {
    @Test
    void fileWriterTest() {
        // Create a filewriter object
        AUDTFileWriter fileWriter = new AUDTFileWriter("test-Melancholy");

        // Test writing some data
        fileWriter.writeGUIData(11, 9, 123.45, 0.01, 0.55, "Melancholy.wav", 120000, 9000);

        // Write the bytes to file
        try {
            fileWriter.writeBytesToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}