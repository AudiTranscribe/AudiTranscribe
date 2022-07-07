/*
 * DownloadFileHandlerTest.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Test `DownloadFileHandler.java`.
 */

package site.overwrite.auditranscribe.network;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.network.FileSignatureMismatchException;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.HashingUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class DownloadFileHandlerTest {
    @Test
    void downloadFile() throws IOException, NoSuchAlgorithmException {
        // Specify the output file path
        String outputFilePath = IOMethods.joinPaths(IOConstants.ROOT_ABSOLUTE_PATH, "test-file-1.txt");

        try {
            // Download the file
            DownloadFileHandler.downloadFile(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath
            );

            // Check the hash manually
            assertEquals(
                    "aa458767a7305cfb2ad50bd017c35e6e",
                    HashingUtils.getHash(new File(outputFilePath), "MD5")
            );

            // Test the method that downloads the file and checks the signature at the same time
            assertDoesNotThrow(() -> DownloadFileHandler.downloadFile(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "3f74f90488683c4c88e93eb27eaf9d8b3c9bf1ce"
            ));
            assertThrowsExactly(FileSignatureMismatchException.class, () -> DownloadFileHandler.downloadFile(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "Not a hash"
            ));
        } finally {
            // Delete the test file
            IOMethods.deleteFile(outputFilePath);
        }
    }

    @Test
    void downloadFileWithRetry() throws IOException, NoSuchAlgorithmException {
        // Define a constant for the maximum number of tries
        int maxNumTries = 5;

        // Specify the output file path
        String outputFilePath = IOMethods.joinPaths(IOConstants.ROOT_ABSOLUTE_PATH, "test-file-2.txt");

        try {
            // Download the file
            DownloadFileHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    maxNumTries
            );

            // Check the hash manually
            assertEquals(
                    "aa458767a7305cfb2ad50bd017c35e6e",
                    HashingUtils.getHash(new File(outputFilePath), "MD5")
            );

            // Test the method that downloads the file and checks the signature at the same time
            assertDoesNotThrow(() -> DownloadFileHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "3f74f90488683c4c88e93eb27eaf9d8b3c9bf1ce",
                    maxNumTries
            ));
            assertThrowsExactly(IOException.class, () -> DownloadFileHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "Not a hash",
                    maxNumTries
            ));
        } finally {
            // Delete the test file
            IOMethods.deleteFile(outputFilePath);
        }
    }
}