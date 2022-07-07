/*
 * DownloadFileHandler.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Methods that handles the downloading of files.
 */

package site.overwrite.auditranscribe.network;

import site.overwrite.auditranscribe.exceptions.network.FileSignatureMismatchException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.HashingUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Methods that handles the downloading of files.
 */
public final class DownloadFileHandler {
    private DownloadFileHandler() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that downloads a file from a URL.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @throws IOException If downloading the file fails.
     */
    public static void downloadFile(URL url, String outputFilePath) throws IOException {
        try (
                InputStream in = url.openStream();
                ReadableByteChannel rbc = Channels.newChannel(in);
                FileOutputStream fos = new FileOutputStream(outputFilePath)
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Method that downloads a file from a URL, and then checks the downloaded file's signature with
     * the correct signature.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param algorithm      Hashing algorithm to generate the signature.
     * @param correctHash    Correct signature.
     * @throws IOException                    If downloading the file fails.
     * @throws NoSuchAlgorithmException       If the specified algorithm for hashing could not be
     *                                        found on the system.
     * @throws FileSignatureMismatchException If the calculated file signature is not the same as
     *                                        the correct file signature.
     */
    public static void downloadFile(
            URL url, String outputFilePath, String algorithm, String correctHash
    ) throws IOException, NoSuchAlgorithmException, FileSignatureMismatchException {
        // Download the file first
        downloadFile(url, outputFilePath);

        // Calculate the hash of the file
        String calculatedHash = HashingUtils.getHash(new File(outputFilePath), algorithm);

        // Check if the hashes are equal
        if (!Objects.equals(correctHash, calculatedHash)) {
            // Delete the downloaded file
            IOMethods.deleteFile(outputFilePath);

            // Throw an exception
            throw new FileSignatureMismatchException(
                    "Calculated hash (" + calculatedHash + ") does not match correct hash (" + correctHash + ")."
            );
        }
    }

    /**
     * Method that attempts to download a file from a URL at most <code>maxRetryCount</code> times.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param maxAttempts    Maximum number of times to try and download the file before giving up.
     * @throws IOException If downloading the file fails.
     */
    public static void downloadFileWithRetry(URL url, String outputFilePath, int maxAttempts) throws IOException {
        for (int i = 0; i < maxAttempts; i++) {
            // Try downloading the file
            try {
                downloadFile(url, outputFilePath);
            } catch (IOException e) {
                continue;  // Try again
            }

            // Download successful, return
            return;
        }

        // If reached here, that means maximum number of tries was exceeded. Throw an IO exception
        throw new IOException("File download from '" + url + "' failed after " + maxAttempts + " attempts");
    }

    /**
     * Method that downloads a file from a URL, and then checks the downloaded file's signature with
     * the correct signature.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param algorithm      Hashing algorithm to generate the signature.
     * @param correctHash    Correct signature.
     * @param maxAttempts    Maximum number of times to try and download the file before giving up.
     * @throws IOException              If downloading the file fails.
     * @throws NoSuchAlgorithmException If the specified algorithm for hashing could not be found on
     *                                  the system.
     */
    public static void downloadFileWithRetry(
            URL url, String outputFilePath, String algorithm, String correctHash, int maxAttempts
    ) throws IOException, NoSuchAlgorithmException {
        for (int i = 0; i < maxAttempts; i++) {
            // Try downloading the file
            try {
                downloadFile(url, outputFilePath, algorithm, correctHash);
            } catch (IOException | FileSignatureMismatchException e) {
                continue;  // Try again
            }

            // Download successful, return
            return;
        }

        // If reached here, that means maximum number of tries was exceeded. Throw an IO exception
        throw new IOException("File download from '" + url + "' failed after " + maxAttempts + " attempts");
    }
}
