/*
 * FFmpegDownloadManager.java
 * Description: Class that handles the downloading, unpackaging, and installation of FFmpeg.
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

package site.overwrite.auditranscribe.setup_wizard.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.network.DownloadFileHandler;
import site.overwrite.auditranscribe.setup_wizard.helpers.data_encapsulators.FFmpegDownloadData;
import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Class that handles the downloading, unpackaging, and installation of FFmpeg.
 */
public class FFmpegDownloadManager {
    // Attributes
    private final int maxAttempts;
    private final OSType os;

    private URL downloadURL;
    private String signature;
    private String ffmpegFolder;
    private boolean needSetExecutable;

    /**
     * Initialization method for a <code>FFmpegDownloadManager</code>.
     *
     * @param maxAttempts Number of attempts to try and download the FFmpeg zip file before failing.
     */
    public FFmpegDownloadManager(int maxAttempts) {
        // Update attributes
        this.maxAttempts = maxAttempts;
        this.os = OSMethods.getOS();

        // Linux is currently not supported
        if (os == OSType.LINUX) {
            downloadURL = null;
            signature = null;
            ffmpegFolder = null;
        } else {
            try {
                defineAttributes(os.toString().toLowerCase());
            } catch (IOException e) {
                downloadURL = null;
                signature = null;
                ffmpegFolder = null;
            }
        }
    }

    // Public methods

    /**
     * Method that downloads the FFmpeg stuff.
     *
     * @param destFolder <b>Absolute</b> path to the folder to place the FFmpeg folder within.
     * @param task       A <code>CustomTask</code> object to show the progress of the download.
     * @return A string, representing the path to the FFmpeg binary.
     * @throws IOException If the downloading and unzipping of the FFmpeg binary fails.
     */
    public String downloadFFmpeg(String destFolder, CustomTask<?> task) throws IOException {
        // Define the output folder
        String outputFolder = IOMethods.joinPaths(destFolder, ffmpegFolder);

        // Create all parent directories
        IOMethods.createFolder(outputFolder);

        // Download the FFmpeg zip file
        try {
            MyLogger.log(
                    Level.INFO,
                    "Downloading FFmpeg for " + os + " from '" + downloadURL.toString() + "'.",
                    FFmpegDownloadManager.class.getName()
            );
            DownloadFileHandler.downloadFileWithRetry(
                    downloadURL, IOMethods.joinPaths(destFolder, "ffmpeg.zip"), task, "SHA256", signature,
                    maxAttempts
            );
        } catch (NoSuchAlgorithmException ignored) {
        }

        // Unzip the downloaded file
        MyLogger.log(Level.INFO, "Unzipping FFmpeg", FFmpegDownloadManager.class.getName());
        CompressionHandlers.zipDecompress(outputFolder, IOMethods.joinPaths(destFolder, "ffmpeg.zip"));

        // Define a variable to store the FFmpeg binary path
        String ffmpegBinPath = null;

        // Set executable status if needed
        if (needSetExecutable) {
            if (os == OSType.MAC) {
                if (!new File(IOMethods.joinPaths(outputFolder, "ffmpeg")).setExecutable(true)) {
                    MyLogger.log(
                            Level.SEVERE,
                            "Failed to set executable status for FFmpeg",
                            FFmpegDownloadManager.class.getName()
                    );
                    throw new IOException(
                            "Failed to set executable status for '" +
                                    IOMethods.joinPaths(outputFolder, "ffmpeg") + "'."
                    );
                } else {
                    MyLogger.log(
                            Level.INFO,
                            "Set executable status for FFmpeg",
                            FFmpegDownloadManager.class.getName()
                    );
                    ffmpegBinPath = IOMethods.joinPaths(outputFolder, "ffmpeg");
                }
            } else {
                throw new IOException("Unrecognised OS");
            }
        }

        // Delete the downloaded zip file
        IOMethods.delete(IOMethods.joinPaths(destFolder, "ffmpeg.zip"));

        MyLogger.log(
                Level.INFO,
                "FFmpeg installation complete",
                FFmpegDownloadManager.class.getName()
        );

        // Return the FFmpeg binary path
        return ffmpegBinPath;
    }

    // Private methods

    /**
     * Define all the attributes of this filter.
     *
     * @param osName Name of the OS.
     * @throws IOException         If the data file path is incorrect.
     * @throws JsonSyntaxException If the syntax of the filter file is incorrect.
     */
    private void defineAttributes(String osName) throws IOException {
        // Convert the OS name to lowercase
        osName = osName.toLowerCase();

        // Define the data file path
        String dataFilePath = IOMethods.joinPaths(
                "setup-wizard-files", "ffmpeg-data", "ffmpeg-data-" + osName + ".json"
        );

        // Create the GSON loader object
        Gson gson = new Gson();

        // Attempt to get the input stream
        InputStream inputStream = IOMethods.getInputStream(dataFilePath);

        // Check if the input stream is null or not
        if (inputStream == null) {
            throw new IOException("Cannot find the FFmpeg download data file '" + dataFilePath + "'.");
        }

        try (Reader reader = new InputStreamReader(inputStream)) {
            // Try loading the filter data
            FFmpegDownloadData data = gson.fromJson(reader, FFmpegDownloadData.class);

            // Set attributes
            downloadURL = new URL(data.url);
            signature = data.signature;
            ffmpegFolder = data.outputFolder;
            needSetExecutable = data.needSetExecutable;

        } catch (JsonSyntaxException e) {
            throw new IOException(e);
        }
    }
}
