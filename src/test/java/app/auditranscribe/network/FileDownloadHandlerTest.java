package app.auditranscribe.network;

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.network.exceptions.FileSignatureMismatchException;
import app.auditranscribe.utils.HashingUtils;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileDownloadHandlerTest {
    @Test
    @Order(1)
    void downloadFile() throws IOException, NoSuchAlgorithmException {
        // Specify the output file path
        String outputFilePath = IOMethods.joinPaths(IOConstants.ROOT_ABSOLUTE_PATH, "test-file-1.txt");

        try {
            // Download the file
            FileDownloadHandler.downloadFile(
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
            assertDoesNotThrow(() -> FileDownloadHandler.downloadFile(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "3f74f90488683c4c88e93eb27eaf9d8b3c9bf1ce"
            ));
            assertThrowsExactly(FileSignatureMismatchException.class, () -> FileDownloadHandler.downloadFile(
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
            IOMethods.delete(outputFilePath);
        }
    }

    @Test
    @Order(1)
    void downloadFileWithRetry() throws IOException, NoSuchAlgorithmException {
        // Define a constant for the maximum number of tries
        int maxNumTries = 3;

        // Specify the output file path
        String outputFilePath = IOMethods.joinPaths(IOConstants.ROOT_ABSOLUTE_PATH, "test-file-2.txt");

        try {
            // Download the file
            FileDownloadHandler.downloadFileWithRetry(
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

            // This should throw an exception
            assertThrowsExactly(IOException.class, () ->
                    FileDownloadHandler.downloadFileWithRetry(
                            new URL("https://no-file-here.com/not-a-file.txt"),
                            outputFilePath,
                            maxNumTries
                    ));

            // Test the method that downloads the file and checks the signature at the same time
            assertDoesNotThrow(() -> FileDownloadHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "3f74f90488683c4c88e93eb27eaf9d8b3c9bf1ce",
                    maxNumTries
            ));
            assertThrowsExactly(IOException.class, () -> FileDownloadHandler.downloadFileWithRetry(
                    new URL("https://no-file-here.com/not-a-file.txt"),
                    outputFilePath,
                    "SHA1",
                    "Not a hash",
                    maxNumTries
            ));
            assertThrowsExactly(IOException.class, () -> FileDownloadHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    "SHA1",
                    "this-is-a-fake-hash",
                    maxNumTries
            ));
        } finally {
            // Delete the test file
            IOMethods.delete(outputFilePath);
        }
    }

    @Test
    @Order(2)
    void downloadFile_withTask() throws IOException, NoSuchAlgorithmException {
        // Start JavaFX toolkit
        new JFXPanel();

        // Define the task
        DownloadTask<Void> task = new DownloadTask<>() {
            @Override
            protected Void call() {
                return null;
            }
        };

        // Check that the downloaded amount is currently 0
        assertEquals(0, task.getDownloadedAmount());
        assertEquals(0, task.downloadedAmountProperty().get());

        // Specify the output file path
        String outputFilePath = IOMethods.joinPaths(IOConstants.ROOT_ABSOLUTE_PATH, "test-file-3.txt");

        try {
            // Download the file
            FileDownloadHandler.downloadFile(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    task,
                    4
            );

            // Check the hash manually
            assertEquals(
                    "aa458767a7305cfb2ad50bd017c35e6e",
                    HashingUtils.getHash(new File(outputFilePath), "MD5")
            );

            // Check the value of the download file size
            assertEquals(248, task.getDownloadFileSize());

            // Test the updating of the progress
            task.updateProgress(-1, -1);

            // Test the method that downloads the file and checks the signature at the same time
            assertDoesNotThrow(() -> FileDownloadHandler.downloadFile(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    task,
                    "SHA1",
                    "3f74f90488683c4c88e93eb27eaf9d8b3c9bf1ce"
            ));
        } finally {
            // Delete the test file
            IOMethods.delete(outputFilePath);
        }
    }

    @Test
    @Order(2)
    void downloadFileWithRetry_withTask() throws IOException, NoSuchAlgorithmException {
        // Start JavaFX toolkit
        new JFXPanel();

        // Define the task
        DownloadTask<Void> task = new DownloadTask<>() {
            @Override
            protected Void call() {
                return null;
            }
        };

        // Define a constant for the maximum number of tries
        int maxNumTries = 3;

        // Specify the output file path
        String outputFilePath = IOMethods.joinPaths(IOConstants.ROOT_ABSOLUTE_PATH, "test-file-4.txt");

        try {
            // Download the file
            FileDownloadHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    maxNumTries,
                    task
            );

            // Check the hash manually
            assertEquals(
                    "aa458767a7305cfb2ad50bd017c35e6e",
                    HashingUtils.getHash(new File(outputFilePath), "MD5")
            );

            // Test the method that downloads the file and checks the signature at the same time
            assertDoesNotThrow(() -> FileDownloadHandler.downloadFileWithRetry(
                    new URL(
                            "https://raw.githubusercontent.com/AudiTranscribe/AudiTranscribe/" +
                                    "90ba622e09c867250c24b3a2e437e888b2740027/Feature%20Plan.txt"
                    ),
                    outputFilePath,
                    task,
                    "SHA1",
                    "3f74f90488683c4c88e93eb27eaf9d8b3c9bf1ce",
                    maxNumTries
            ));
        } finally {
            // Delete the test file
            IOMethods.delete(outputFilePath);
        }
    }
}