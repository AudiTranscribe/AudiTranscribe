/*
 * FileDownloadHandler.java
 * Description: Handles the downloading of files.
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

package app.auditranscribe.network;

import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.utils.HashingUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Handles the downloading of files.
 */
public final class FileDownloadHandler extends LoggableClass {
    // Constants
    public static int FILE_DOWNLOAD_BUFFER_SIZE = 4096;  // In bytes

    private FileDownloadHandler() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that gets the file size of a file located at the specified URL.
     *
     * @param url URL that has the file.
     * @throws IOException If the URL is not correct.
     * @implNote Assumes that the file size is less than 2,147,483,647 (~2GB) bytes.
     */
    public static int getFileSize(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        return urlConnection.getContentLength();
    }

    /**
     * Method that downloads a file from a URL.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @throws IOException If downloading the file fails.
     */
    public static void downloadFile(URL url, String outputFilePath) throws IOException {
        downloadFile(url, outputFilePath, null);
    }

    /**
     * Method that downloads a file from a URL.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param task           A <code>DownloadTask</code> object to show the progress of the download.
     * @throws IOException If downloading the file fails.
     */
    public static void downloadFile(URL url, String outputFilePath, DownloadTask<?> task) throws IOException {
        downloadFile(url, outputFilePath, task, FILE_DOWNLOAD_BUFFER_SIZE);
    }

    /**
     * Method that downloads a file from a URL.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param task           A <code>DownloadTask</code> object to show the progress of the download.
     * @param buffSize       Buffer size for the download process.
     * @throws IOException If downloading the file fails.
     */
    public static void downloadFile(
            URL url, String outputFilePath, DownloadTask<?> task, int buffSize
    ) throws IOException {
        // Get file size
        int fileSize = getFileSize(url);

        // If a task is provided, update its download file size
        if (task != null) task.setDownloadFileSize(fileSize);

        // Determine the number of passes needed to download the full data
        int numPasses = (int) Math.ceil((double) fileSize / buffSize);

        // Define a buffer to handle the incoming bytes
        byte[] buf = new byte[buffSize];

        // Download the file
        int len;
        int currPass = 0;  // Current pass number

        try (InputStream in = url.openStream(); FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            while ((len = in.read(buf)) > 0) {
                fos.write(buf, 0, len);
                if (task != null) {
                    task.updateProgress(++currPass, numPasses);
                }
            }
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
     * @throws IOException                If downloading the file fails.
     * @throws NoSuchAlgorithmException   If the specified algorithm for hashing could not be found
     *                                    on the system.
     * @throws SignatureMismatchException If the calculated file signature is not the same as the
     *                                    correct file signature.
     */
    public static void downloadFile(
            URL url, String outputFilePath, String algorithm, String correctHash
    ) throws IOException, NoSuchAlgorithmException, SignatureMismatchException {
        downloadFile(url, outputFilePath, null, algorithm, correctHash);
    }

    /**
     * Method that downloads a file from a URL, and then checks the downloaded file's signature with
     * the correct signature.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param task           A <code>DownloadTask</code> object to show the progress of the download.
     * @param algorithm      Hashing algorithm to generate the signature.
     * @param correctHash    Correct signature.
     * @throws IOException                If downloading the file fails.
     * @throws NoSuchAlgorithmException   If the specified algorithm for hashing could not be found
     *                                    on the system.
     * @throws SignatureMismatchException If the calculated file signature is not the same as the
     *                                    correct file signature.
     */
    public static void downloadFile(
            URL url, String outputFilePath, DownloadTask<?> task, String algorithm, String correctHash
    ) throws IOException, NoSuchAlgorithmException, SignatureMismatchException {
        // Download the file first
        downloadFile(url, outputFilePath, task);

        // Calculate the hash of the file
        String calculatedHash = HashingUtils.getHash(new File(outputFilePath), algorithm);

        // Check if the hashes are equal
        if (!Objects.equals(correctHash, calculatedHash)) {
            // Delete the downloaded file
            IOMethods.delete(outputFilePath);

            // Throw an exception
            throw new SignatureMismatchException(
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
        downloadFileWithRetry(url, outputFilePath, maxAttempts, null);
    }

    /**
     * Method that attempts to download a file from a URL at most <code>maxRetryCount</code> times.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param maxAttempts    Maximum number of times to try and download the file before giving up.
     * @param task           A <code>DownloadTask</code> object to show the progress of the download.
     * @throws IOException If downloading the file fails.
     */
    public static void downloadFileWithRetry(
            URL url, String outputFilePath, int maxAttempts, DownloadTask<?> task
    ) throws IOException {
        for (int i = 0; i < maxAttempts; i++) {
            // Try downloading the file
            try {
                downloadFile(url, outputFilePath, task);
            } catch (IOException e) {
                log(
                        Level.WARNING,
                        "Failed to download file, trying again (attempt " + (i + 1) + " of " + maxAttempts + ")",
                        FileDownloadHandler.class.getName()
                );
                continue;  // Try again
            }

            // Download successful, return
            return;
        }

        // If reached here, that means maximum number of tries was exceeded. Throw an IO exception
        log(
                Level.WARNING,
                "Failed to download file '" + url.toString() + "' after " + maxAttempts + " attempts",
                FileDownloadHandler.class.getName()
        );
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
        downloadFileWithRetry(url, outputFilePath, null, algorithm, correctHash, maxAttempts);
    }

    /**
     * Method that downloads a file from a URL, and then checks the downloaded file's signature with
     * the correct signature.
     *
     * @param url            URL to download the file from.
     * @param outputFilePath <b>Absolute</b> file path to the output file.
     * @param task           A <code>DownloadTask</code> object to show the progress of the download.
     * @param algorithm      Hashing algorithm to generate the signature.
     * @param correctHash    Correct signature.
     * @param maxAttempts    Maximum number of times to try and download the file before giving up.
     * @throws IOException              If downloading the file fails.
     * @throws NoSuchAlgorithmException If the specified algorithm for hashing could not be found on
     *                                  the system.
     */
    public static void downloadFileWithRetry(
            URL url, String outputFilePath, DownloadTask<?> task, String algorithm, String correctHash, int maxAttempts
    ) throws IOException, NoSuchAlgorithmException {
        for (int i = 0; i < maxAttempts; i++) {
            // Try downloading the file
            try {
                downloadFile(url, outputFilePath, task, algorithm, correctHash);
            } catch (IOException e) {
                log(
                        Level.WARNING,
                        "File download failed, trying again (attempt " + (i + 1) + " of " + maxAttempts + ")",
                        FileDownloadHandler.class.getName()
                );
                continue;  // Try again
            } catch (SignatureMismatchException e) {
                log(
                        Level.WARNING,
                        e.getMessage() + " Trying again (attempt " + (i + 1) + " of " + maxAttempts + ")",
                        FileDownloadHandler.class.getName()
                );
                continue;
            }

            // Download successful, return
            return;
        }

        // If reached here, that means maximum number of tries was exceeded. Throw an IO exception
        log(
                Level.WARNING,
                "Failed to download file '" + url.toString() + "' after " + maxAttempts + " attempts",
                FileDownloadHandler.class.getName()
        );
        throw new IOException("File download from '" + url + "' failed after " + maxAttempts + " attempts");
    }

    // Exceptions

    /**
     * Exception to mark when the calculated file hash does not match the expected file hash.
     */
    @ExcludeFromGeneratedCoverageReport
    public static class SignatureMismatchException extends Exception {
        public SignatureMismatchException(String message) {
            super(message);
        }
    }
}
