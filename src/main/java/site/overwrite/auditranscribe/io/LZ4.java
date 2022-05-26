/*
 * LZ4.java
 *
 * Created on 2022-05-04
 * Updated on 2022-05-26
 *
 * Description: Class that encapsulates LZ4 compression/decompression on byte arrays.
 */

package site.overwrite.auditranscribe.io;

import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import site.overwrite.auditranscribe.CustomTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class that encapsulates LZ4 compression/decompression on byte arrays.
 */
public class LZ4 {
    // Constants
    public static final int LZ4_BYTE_BUFFER_SIZE = 2048;

    public static final int LZ4_VERSION_NUMBER = 1;

    // Public methods

    /**
     * Method that returns an LZ4 compressed version of the bytes array.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @return LZ4 compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] lz4Compress(byte[] bytes) throws IOException {
        return lz4Compress(bytes, null);
    }

    /**
     * Method that returns an LZ4 compressed version of the bytes array.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @param task  The <code>CustomTask</code> object that is handling the generation. Pass in
     *              <code>null</code> if no such task is being used.
     * @return LZ4 compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] lz4Compress(byte[] bytes, CustomTask<?> task) throws IOException {
        // Create a byte buffer
        byte[] buf = new byte[LZ4_BYTE_BUFFER_SIZE];

        // Count the number of passes that are needed for the LZ4 compression to complete
        int numPasses = (int) Math.ceil((double) bytes.length / LZ4_BYTE_BUFFER_SIZE);

        // Define needed byte streams
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);  // Takes bytes from the input bytes array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Define the LZ4 compressor stream
        BlockLZ4CompressorOutputStream out = new BlockLZ4CompressorOutputStream(outputStream);

        // Pass bytes from the input stream through the LZ4 compression stream
        int len;
        int currPass = 0;  // Current pass number

        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
            if (task != null) task.updateProgress(++currPass, numPasses);
        }

        // Close streams
        inputStream.close();
        outputStream.close();
        out.close();

        // Get the resulting byte array
        return outputStream.toByteArray();
    }

    /**
     * Method that decompressed the input LZ4 bytes and returns the decompressed bytes.
     *
     * @param lz4Bytes LZ4 compressed bytes that are to be decompressed.
     * @return Decompressed bytes.
     * @throws IOException If something went wrong when decompressing the bytes.
     */
    public static byte[] lz4Decompress(byte[] lz4Bytes) throws IOException {
        // Create a byte buffer
        byte[] buf = new byte[LZ4_BYTE_BUFFER_SIZE];

        // Define needed byte streams
        ByteArrayInputStream inputStream = new ByteArrayInputStream(lz4Bytes);  // Takes bytes from the input bytes array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Define the LZ4 decompressor stream
        BlockLZ4CompressorInputStream in = new BlockLZ4CompressorInputStream(inputStream);

        // Pass bytes from the input stream through the LZ4 decompression stream
        int len;
        while ((len = in.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }

        // Close streams
        in.close();
        inputStream.close();
        outputStream.close();

        // Get the resulting byte array
        return outputStream.toByteArray();
    }
}
