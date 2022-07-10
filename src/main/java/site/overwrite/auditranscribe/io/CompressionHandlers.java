/*
 * CompressionHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-07-10
 *
 * Description: Class that handles compression/decompression operations.
 */

package site.overwrite.auditranscribe.io;

import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import site.overwrite.auditranscribe.misc.CustomTask;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    // Public methods

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
     * Method that returns an LZ4 compressed version of the bytes array.
     *
     * @param bytes Bytes array to be LZ4 compressed.
     * @param task  The <code>CustomTask</code> object that is handling the generation. Pass in
     *              <code>null</code> if no such task is being used.
     * @return Compressed bytes.
     * @throws IOException If something went wrong when compressing the bytes.
     */
    public static byte[] lz4Compress(byte[] bytes, CustomTask<?> task) throws IOException {
        // Create a byte buffer
        byte[] buf = new byte[BUFFER_SIZE];

        // Count the number of passes that are needed for the LZ4 compression to complete
        int numPasses = (int) Math.ceil((double) bytes.length / BUFFER_SIZE);

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
        byte[] buf = new byte[BUFFER_SIZE];

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
            File newFile = zipMakeFile(outputDir, zipEntry);

            // Determine what was just created
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !IOMethods.createFolder(newFile.getAbsolutePath())) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // Fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !IOMethods.createFolder(parent.getAbsolutePath())) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // Define output stream for the created file
                FileOutputStream fos = new FileOutputStream(newFile);

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
            File[] children = file.listFiles();

            if (children != null) {
                for (File childFile : children) {
                    zipCompressHelper(childFile, IOMethods.joinPaths(filePath, childFile.getName()), zos);
                }
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

    /**
     * Helper method that creates a file/folder based on the zip entry received.
     *
     * @param destinationDir <b>Absolute</b> path to the destination directory to create the
     *                       file/folder in.
     * @param zipEntry       The <code>ZipEntry</code> object.
     * @return A new <code>File</code> object, representing the file that was created.
     * @throws IOException If the file creation process encounters an error.
     */
    private static File zipMakeFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
