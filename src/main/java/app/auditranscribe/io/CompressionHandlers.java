/*
 * CompressionHandlers.java
 * Description: Class that handles compression/decompression operations.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.io;

import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;

import java.io.*;
import java.util.zip.*;

/**
 * Class that handles compression/decompression operations.
 */
public final class CompressionHandlers {
    // Constants
    public static final int BUFFER_SIZE = 2048;  // In bytes
    public static final int VERSION_NUMBER = 1;

    private CompressionHandlers() {
        // Private constructor to signal this is a utility class
    }

    // LZ4 compression

    /**
     * Method that returns an LZ4 compressed version of the bytes array.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @param task  The <code>CustomTask</code> object that is handling the compression. Pass in
     *              <code>null</code> if no such task is being used.
     * @return Compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] lz4Compress(byte[] bytes, CustomTask<?> task) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int numPasses = (int) Math.ceil((double) bytes.length / BUFFER_SIZE);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);  // Takes bytes from the input bytes array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BlockLZ4CompressorOutputStream compressionStream = new BlockLZ4CompressorOutputStream(outputStream);

        return handleStreamCompression(inputStream, outputStream, compressionStream, buffer, numPasses, task);
    }

    /**
     * Method that returns an LZ4 compressed version of the bytes array.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @return Compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] lz4Compress(byte[] bytes) throws IOException {
        return lz4Compress(bytes, null);
    }

    /**
     * Method that returns an LZ4 compressed version of the bytes array.<br>
     * Fails silently, so no exception is thrown.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @param task  The <code>CustomTask</code> object that is handling the compression. Pass in
     *              <code>null</code> if no such task is being used.
     * @return Compressed bytes. Returns <code>null</code> if an error occurred.
     */
    @ExcludeFromGeneratedCoverageReport
    public static byte[] lz4CompressFailSilently(byte[] bytes, CustomTask<?> task) {
        try {
            return lz4Compress(bytes, task);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Method that returns an LZ4 compressed version of the bytes array.<br>
     * Fails silently, so no exception is thrown.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @return Compressed bytes. Returns <code>null</code> if an error occurred.
     */
    @ExcludeFromGeneratedCoverageReport
    public static byte[] lz4CompressFailSilently(byte[] bytes) {
        return lz4CompressFailSilently(bytes, null);
    }

    /**
     * Method that decompresses the input LZ4 bytes and returns the decompressed bytes.
     *
     * @param compressed LZ4 compressed bytes that are to be decompressed.
     * @return Decompressed bytes.
     * @throws IOException If something went wrong when decompressing the bytes.
     */
    public static byte[] lz4Decompress(byte[] compressed) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);  // Take bytes from input byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        BlockLZ4CompressorInputStream decompressionStream = new BlockLZ4CompressorInputStream(inputStream);

        return handleStreamDecompression(inputStream, outputStream, decompressionStream, buffer);
    }

    /**
     * Method that decompresses the input LZ4 bytes and returns the decompressed bytes.<br>
     * Fails silently, so no exception is thrown.
     *
     * @param compressed LZ4 compressed bytes that are to be decompressed.
     * @return Decompressed bytes. Returns <code>null</code> if an error occurred.
     */
    @ExcludeFromGeneratedCoverageReport
    public static byte[] lz4DecompressFailSilently(byte[] compressed) {
        try {
            return lz4Decompress(compressed);
        } catch (IOException e) {
            return null;
        }
    }

    // DEFLATE compression

    /**
     * Method that returns a DEFLATE compressed version of the bytes array.
     *
     * @param bytes Bytes to be compressed using DEFLATE.
     * @param task  The <code>CustomTask</code> object that is handling the compression. Pass in
     *              <code>null</code> if no such task is being used.
     * @return Compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] deflateCompress(byte[] bytes, CustomTask<?> task) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int numPasses = (int) Math.ceil((double) bytes.length / BUFFER_SIZE);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);  // Takes bytes from the input bytes array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        DeflaterOutputStream out = new DeflaterOutputStream(outputStream);

        return handleStreamCompression(inputStream, outputStream, out, buffer, numPasses, task);
    }

    /**
     * Method that returns a DEFLATE compressed version of the bytes array.
     *
     * @param bytes Bytes to be compressed using DEFLATE.
     * @return Compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] deflateCompress(byte[] bytes) throws IOException {
        return deflateCompress(bytes, null);
    }

    /**
     * Method that returns a DEFLATE compressed version of the bytes array.<br>
     * Fails silently, so no exception is thrown.
     *
     * @param bytes Bytes to be compressed using DEFLATE.
     * @param task  The <code>CustomTask</code> object that is handling the compression. Pass in
     *              <code>null</code> if no such task is being used.
     * @return Compressed bytes. Returns <code>null</code> if an error occurred.
     */
    @ExcludeFromGeneratedCoverageReport
    public static byte[] deflateCompressFailSilently(byte[] bytes, CustomTask<?> task) {
        try {
            return deflateCompress(bytes, task);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Method that returns a DEFLATE compressed version of the bytes array.<br>
     * Fails silently, so no exception is thrown.
     *
     * @param bytes Bytes to be compressed using DEFLATE.
     * @return Compressed bytes. Returns <code>null</code> if an error occurred.
     */
    @ExcludeFromGeneratedCoverageReport
    public static byte[] deflateCompressFailSilently(byte[] bytes) {
        return deflateCompressFailSilently(bytes, null);
    }

    /**
     * Method that decompresses the DEFLATE compressed bytes and returns the decompressed bytes.
     *
     * @param compressed DEFLATE compressed bytes that are to be decompressed.
     * @return Decompressed bytes.
     * @throws IOException If something went wrong when decompressing the bytes.
     */
    public static byte[] deflateDecompress(byte[] compressed) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);  // Take bytes from input byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InflaterInputStream decompressionStream = new InflaterInputStream(inputStream);

        return handleStreamDecompression(inputStream, outputStream, decompressionStream, buffer);
    }

    /**
     * Method that decompresses the DEFLATE compressed bytes and returns the decompressed bytes.<br>
     * Fails silently, so no exception is thrown.
     *
     * @param compressed DEFLATE compressed bytes that are to be decompressed.
     * @return Decompressed bytes. Returns <code>null</code> if an error occurred.
     */
    @ExcludeFromGeneratedCoverageReport
    public static byte[] deflateDecompressFailSilently(byte[] compressed) {
        try {
            return deflateDecompress(compressed);
        } catch (IOException e) {
            return null;
        }
    }

    // ZIP compression

    /**
     * Method that zips a set of files into a single zip file.
     *
     * @param outputPath <b>Absolute</b> path to the output zip file.
     * @param filePaths  <b>Absolute</b> paths to the files to be zipped.
     * @throws IOException If something went wrong during the zipping operation.
     */
    public static void zipCompressFiles(String outputPath, String... filePaths) throws IOException {
        // Define output streams
        FileOutputStream fos = new FileOutputStream(outputPath);
        ZipOutputStream zos = new ZipOutputStream(fos);

        // Iterate through the input files
        for (String filePath : filePaths) {
            zipCompressHelper(filePath != null ? new File(filePath) : null, filePath, zos);
        }

        // Close output streams
        zos.close();
        fos.close();
    }

    /**
     * Method that zips a directory into a single zip file.
     *
     * @param outputPath <b>Absolute</b> path to the output zip file.
     * @param inputDir   <b>Absolute</b> path to the input directory.
     * @throws IOException If something went wrong during the zipping operation.
     */
    public static void zipCompressDir(String outputPath, String inputDir) throws IOException {
        // Define output streams
        FileOutputStream fos = new FileOutputStream(outputPath);
        ZipOutputStream zos = new ZipOutputStream(fos);

        // Define a file object to wrap around the input directory
        File fileToZip = new File(inputDir);

        // Zip the directory
        zipCompressHelper(fileToZip, inputDir, zos);

        // Close the output streams
        zos.close();
        fos.close();
    }

    /**
     * Method that decompresses a zip file into a specified directory.
     *
     * @param outputDirPath <b>Absolute</b> path to the directory, where we will decompress a zip
     *                      file into.
     * @param inputZipPath  <b>Absolute</b> path to the input zip file.
     * @throws IOException If something went wrong during the unzipping operation.
     */
    public static void zipDecompress(String outputDirPath, String inputZipPath) throws IOException {
        // Create the output directory path
        IOMethods.createFolder(outputDirPath);

        // Wrap the output directory path as a file object
        File outputDir = new File(outputDirPath);

        // Create a byte buffer
        byte[] buf = new byte[BUFFER_SIZE];

        // Create a `ZipInputStream` object
        ZipInputStream zis = new ZipInputStream(new FileInputStream(inputZipPath));

        // Get the next zip entry from the `ZipInputStream` object
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            // Create a new file based on the zip entry
            File newFileObj = getFileObjectFromZipEntry(outputDir, zipEntry);

            // Check if the new file object can be created
            /*
            The file cannot be created if:
            - The file object's parent directory does not exist, and cannot be created; or
            - The file object itself represents a directory, and cannot be created.
             */
            File folder = newFileObj;
            if (!zipEntry.isDirectory()) {  // Is not a directory entry
                folder = newFileObj.getParentFile();  // Get parent file object to see if it is a folder
            }

            if (!folder.exists()) {  // Doesn't exist
                if (!IOMethods.createFolder(folder)) {  // Failed to create folder
                    throw new IOException("Failed to create directory " + newFileObj);
                }
            }

            // Fill in file contents
            if (!zipEntry.isDirectory()) {
                // Define output stream for the created file
                FileOutputStream fos = new FileOutputStream(newFileObj);

                // Write file content to the created file
                int len;
                while ((len = zis.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }

                // Close the output stream
                fos.close();
            }

            // Get the next entry in the `ZipInputStream`
            zipEntry = zis.getNextEntry();
        }

        // Close the `ZipInputStream`
        zis.closeEntry();
        zis.close();
    }

    // Private methods

    /**
     * Helper method that handles the compression via streams.
     *
     * @param inputStream       Input stream of the bytes.
     * @param outputStream      Output stream of the bytes.
     * @param compressionStream Intermediate stream that actually handles the compression.
     * @param buffer            Byte buffer for storing compressed bytes.
     * @param numPasses         Number of passes needed to complete the full compression.
     * @param task              The <code>CustomTask</code> object that is handling the compression.
     *                          Pass in <code>null</code> if no such task is being used.
     * @return Compressed bytes.
     * @throws IOException If something went wrong during the compression.
     */
    private static byte[] handleStreamCompression(
            ByteArrayInputStream inputStream, ByteArrayOutputStream outputStream,
            OutputStream compressionStream, byte[] buffer, int numPasses, CustomTask<?> task
    ) throws IOException {
        int len;
        int currPass = 0;

        while ((len = inputStream.read(buffer)) > 0) {
            compressionStream.write(buffer, 0, len);
            if (task != null) task.updateProgress(++currPass, numPasses);
        }

        inputStream.close();
        outputStream.close();
        compressionStream.close();

        return outputStream.toByteArray();
    }

    /**
     * Helper method that handles the decompression via streams.
     *
     * @param inputStream         Input stream of the bytes.
     * @param outputStream        Output stream of the bytes.
     * @param decompressionStream Intermediate stream that actually handles the decompression.
     * @param buffer              Byte buffer for storing decompressed bytes.
     * @return Decompressed bytes.
     * @throws IOException If something went wrong during the decompression.
     */
    private static byte[] handleStreamDecompression(
            ByteArrayInputStream inputStream, ByteArrayOutputStream outputStream,
            InputStream decompressionStream, byte[] buffer
    ) throws IOException {
        int len;
        while ((len = decompressionStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }

        decompressionStream.close();
        inputStream.close();
        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * Helper method that gets a <code>File</code> object based on the zip entry received.
     *
     * @param destinationDir <b>Absolute</b> path to the destination directory that the file/folder
     *                       is in.
     * @param zipEntry       The <code>ZipEntry</code> object.
     * @return A new <code>File</code> object, representing the file/folder.
     * @throws IOException If the zip entry points to a file that is <b>outside</b> the target
     *                     directory (which usually indicates a path traversal attempt such as
     *                     <a href="https://security.snyk.io/research/zip-slip-vulnerability">
     *                     ZipSlip</a>).
     */
    private static File getFileObjectFromZipEntry(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Helper method that helps zip compress a file/folder recursively.
     *
     * @param file     File object, representing the file/folder to zip.
     * @param filePath <b>Absolute</b> path to the file/folder.
     * @param zos      <code>ZipOutputStream</code> object.
     * @throws IOException If something went wrong during the zipping operation.
     */
    private static void zipCompressHelper(File file, String filePath, ZipOutputStream zos) throws IOException {
        // Make sure the file exists
        if (!IOMethods.isSomethingAt(filePath)) return;

        // If the file is hidden, ignore
        if (file.isHidden()) return;

        // Handle the case where it is a directory
        if (file.isDirectory()) {
            // Determine the entry value
            String entryValue = filePath.endsWith("/") ? filePath : filePath + "/";

            // Put the entry into the `ZipOutputStream`
            zos.putNextEntry(new ZipEntry(entryValue));
            zos.closeEntry();

            // Add the children
            File[] children = file.listFiles();  // Since `file` represents a directory, it will never be `null`

            //noinspection ConstantConditions
            for (File childFile : children) {
                zipCompressHelper(childFile, IOMethods.joinPaths(filePath, childFile.getName()), zos);
            }
        } else {  // Otherwise, it is a file
            // Create a byte buffer
            byte[] buf = new byte[BUFFER_SIZE];

            // Define the file input stream
            FileInputStream fis = new FileInputStream(file);

            // Add an entry to the Zip file
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            // Write the file's bytes to the `ZipOutputStream`
            int len;

            while ((len = fis.read(buf)) >= 0) {
                zos.write(buf, 0, len);
            }
            fis.close();
        }
    }
}
