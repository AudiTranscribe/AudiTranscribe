/*
 * CompressionHandlersTest.java
 *
 * Created on 2022-05-04
 * Updated on 2022-07-09
 *
 * Description: Test `CompressionHandlers.java`.
 */

package site.overwrite.auditranscribe.io;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.utils.HashingUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompressionHandlersTest {
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
        byte[] result = CompressionHandlers.lz4Compress(HexFormat.of().parseHex(hexStr));

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
        byte[] result = CompressionHandlers.lz4Compress(HexFormat.of().parseHex(hexStr), new CustomTask<Void>() {
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
        byte[] result = CompressionHandlers.lz4Decompress(HexFormat.of().parseHex(lz4HexStr));

        // Check if they are the same
        assertEquals(HexFormat.of().formatHex(result), hexStr);
    }

    @Test
    @Order(2)
    void zipCompressFiles() {
        // Define some file paths to compress
        String baseDir = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH, "testing-files"
        );
        String filepath1 = IOMethods.joinPaths(baseDir, "text", "MyFile.txt");
        String filepath2 = IOMethods.joinPaths(baseDir, "misc", "testing-properties-file.properties");
        String filepath3 = IOMethods.joinPaths(baseDir, "audio", "README.txt");

        String nonexistent = "not-a-real-file-path";

        String outputPath = IOMethods.joinPaths(baseDir, "zip-file-1.zip");

        // Attempt compression
        assertDoesNotThrow(() -> CompressionHandlers.zipCompressFiles(outputPath, filepath1, filepath2, filepath3, nonexistent));

        // Attempt decompression
        assertDoesNotThrow(() -> CompressionHandlers.zipDecompress(IOMethods.joinPaths(baseDir, "zip-file-1-dir"), outputPath));

        // Check if there are still 3 items
        assertEquals(3, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "zip-file-1-dir")));

        // Delete files
        IOMethods.delete(IOMethods.joinPaths(baseDir, "zip-file-1-dir", "MyFile.txt"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "zip-file-1-dir", "testing-properties-file.properties"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "zip-file-1-dir", "README.txt"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "zip-file-1-dir"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "zip-file-1.zip"));
    }

    @Test
    @Order(3)
    void zipCompressDir() {
        // Define base directory path
        String baseDir = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH, "testing-files"
        );

        // Tests
        assertDoesNotThrow(() -> CompressionHandlers.zipCompressDir(
                IOMethods.joinPaths(baseDir, "zip-file-2.zip"),
                IOMethods.joinPaths(baseDir, "testing-directory")
        ));

        // Delete generated zip file
        IOMethods.delete(IOMethods.joinPaths(baseDir, "zip-file-2.zip"));
    }

    @Test
    @Order(1)
    void zipDecompress() throws NoSuchAlgorithmException, IOException {
        // Define base directory
        String baseDir = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH, "testing-files"
        );

        // Check if we have the correct ZIP file and that it is not damaged
        String correctHash = "d7714a0884d7bdc6a3bc55dddf29eb9f";
        if (!Objects.equals(HashingUtils.getHash(new File(IOMethods.joinPaths(baseDir, "misc", "zip-test.zip")), "MD5"), correctHash)) {
            MyLogger.log(
                    Level.WARNING,
                    "The 'zip-test.zip' file signature is incorrect, not conducting test.",
                    CompressionHandlersTest.class.getName()
            );
            return;
        }

        // Attempt unzip file
        CompressionHandlers.zipDecompress(
                IOMethods.joinPaths(baseDir, "misc", "zip-test-base-dir"),
                IOMethods.joinPaths(baseDir, "misc", "zip-test.zip")
        );

        // Update base directory
        baseDir = IOMethods.joinPaths(baseDir, "misc", "zip-test-base-dir", "zip-test");

        // Define file paths and hashes
        String[] filePaths = {
                IOMethods.joinPaths(baseDir, "file0.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "file1a.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "file1b.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "file1-1a.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "file1-1b.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "file1-1c.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "dir-1-1-1", "file1-1-1.txt"),
                IOMethods.joinPaths(baseDir, "dir1", "dir1-2", "file1-2.txt"),
                IOMethods.joinPaths(baseDir, "dir2", "file2.txt")
        };
        String[] hashes = {
                "56a3b0e65febddf6197b728f8f95e6cd",
                "31409284fb03e57c25e367ad112eee63",
                "07b35ac86b53bba58d601fa52f195873",
                "bf37dbcc99ac449f686c25994f415967",
                "0385846fb839dbc4d13bcd390a509e0c",
                "038fb3cb4904530aada54d9931c33d8c",
                "4f699db4ee872fa0dab5fc7dd18a693d",
                "e24666c07286fd451b7a0ac36ece3db1",
                "3d709e89c8ce201e3c928eb917989aef"
        };

        // Run tests
        assertEquals(3, IOMethods.numThingsInDir(baseDir));
        assertEquals(4, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "dir1")));
        assertEquals(5, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "dir1", "dir1-1")));
        assertEquals(1, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "dir-1-1-1")));
        assertEquals(0, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "dir-1-1-2")));
        assertEquals(1, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "dir1", "dir1-2")));
        assertEquals(1, IOMethods.numThingsInDir(IOMethods.joinPaths(baseDir, "dir2")));

        for (int i = 0; i < filePaths.length; i++) {
            // Check hash
            assertEquals(hashes[i], HashingUtils.getHash(new File(filePaths[i]), "MD5"));

            // Delete file
            IOMethods.delete(filePaths[i]);
        }

        // Delete directories
        IOMethods.delete(IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "dir-1-1-1"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "dir1", "dir1-1", "dir-1-1-2"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "dir1", "dir1-1"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "dir1", "dir1-2"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "dir1"));
        IOMethods.delete(IOMethods.joinPaths(baseDir, "dir2"));
        IOMethods.delete(baseDir);
        IOMethods.delete(IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH, "testing-files", "misc",
                "zip-test-base-dir"
        ));
    }
}