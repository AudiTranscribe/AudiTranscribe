/*
 * LZ4Test.java
 *
 * Created on 2022-05-04
 * Updated on 2022-07-02
 *
 * Description: Test `LZ4.java`.
 */

package site.overwrite.auditranscribe.io;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.misc.CustomTask;

import java.io.IOException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class LZ4Test {
    @Test
    void lz4Compress() throws IOException {
        // Define the hex string of the bytes to compress
        String hexStr = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef" +
                "f0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcde" +
                "ef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd" +
                "def0123456789abcdef0123456789abcdef0123456789abcdef0123456789abc";

        // Define the hex string of the supposed resulting bytes
        String lz4HexStr = "8f0123456789abcdef0800058ff0123456789abcde0800051fef41000c1fde410000c056789abcdef0123456789abc";

        // Get the result of the compression
        byte[] result = LZ4.lz4Compress(HexFormat.of().parseHex(hexStr));

        // Check if they are the same
        assertEquals(HexFormat.of().formatHex(result), lz4HexStr);
    }

    @Test
    void lz4CompressWithFakeTask() throws IOException {
        // Start JavaFX toolkit
        new JFXPanel();

        // Define the hex string of the bytes to compress
        String hexStr = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef" +
                "f0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcde" +
                "ef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd" +
                "def0123456789abcdef0123456789abcdef0123456789abcdef0123456789abc";

        // Define the hex string of the supposed resulting bytes
        String lz4HexStr = "8f0123456789abcdef0800058ff0123456789abcde0800051fef41000c1fde410000c056789abcdef0123456789abc";

        // Get the result of the compression
        byte[] result = LZ4.lz4Compress(HexFormat.of().parseHex(hexStr), new CustomTask<Void>() {
            @Override
            protected Void call() {
                return null;
            }
        });

        // Check if they are the same
        assertEquals(HexFormat.of().formatHex(result), lz4HexStr);
    }

    @Test
    void lz4Decompress() throws IOException {
        // Define the hex string of the LZ4 bytes
        String lz4HexStr = "8f0123456789abcdef0800058ff0123456789abcde0800051fef41000c1fde410000c056789abcdef0123456789abc";

        // Define the hex string of the original bytes
        String hexStr = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef" +
                "f0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcde" +
                "ef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd" +
                "def0123456789abcdef0123456789abcdef0123456789abcdef0123456789abc";

        // Get the result of the decompression
        byte[] result = LZ4.lz4Decompress(HexFormat.of().parseHex(lz4HexStr));

        // Check if they are the same
        assertEquals(HexFormat.of().formatHex(result), hexStr);
    }
}