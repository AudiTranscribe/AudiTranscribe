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

package app.auditranscribe.fxml.views.setup_wizard.download_handlers;

import app.auditranscribe.io.CompressionHandlers;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.network.DownloadTask;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.util.logging.Level;

/**
 * Class that handles the downloading, unpackaging, and installation of FFmpeg.
 */
public class FFmpegDownloadManager extends AbstractDownloadManager {
    // Attributes
    private final OSType os;

    public String destFolder;
    public String ffmpegFolder;
    public String outputFolder;

    public String ffmpegBinPath;

    /**
     * Initialization method for a <code>FFmpegDownloadManager</code>.
     *
     * @param maxAttempts Number of attempts to try and download the FFmpeg zip file before failing.
     */
    public FFmpegDownloadManager(int maxAttempts) {
        super(maxAttempts);

        log(Level.FINE, "Starting FFmpeg download manager");

        // Update attributes
        os = OSMethods.getOS();
        downloadFileName = "ffmpeg.zip";

        // Linux and others do not have automatic FFmpeg installation
        if (os == OSType.LINUX || os == OSType.OTHER) {
            downloadDriveID = null;
            signatureDriveID = null;
            ffmpegFolder = null;
        } else {
            try {
                defineAttributes(os.toString().toLowerCase());
            } catch (IOException e) {
                logException(e);

                downloadDriveID = null;
                signatureDriveID = null;
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
     * @return A string, representing the <b>absolute</b> path to the FFmpeg zip file.
     * @throws IOException If the downloading of the FFmpeg zip fails.
     */
    public String downloadResource(String destFolder, DownloadTask<?> task) throws IOException {
        // Update attributes
        this.destFolder = destFolder;
        outputFolder = IOMethods.joinPaths(destFolder, ffmpegFolder);

        // Handle download proper
        return super.downloadResource(destFolder, task);
    }

    // Private methods

    /**
     * Define all the attributes of this filter.
     *
     * @param osName Name of the OS.
     * @throws IOException         If the data file path is incorrect, or if the file signature URL
     *                             is incorrect.
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
        InputStream inputStream = IOMethods.readAsInputStream(dataFilePath);

        // Check if the input stream is null or not
        if (inputStream == null) {
            throw new IOException("Cannot find the FFmpeg download data file '" + dataFilePath + "'.");
        }

        // Try loading the FFmpeg download data
        try (Reader reader = new InputStreamReader(inputStream)) {
            FFmpegDownloadData data = gson.fromJson(reader, FFmpegDownloadData.class);

            downloadDriveID = data.downloadDriveID;
            signatureDriveID = data.signatureDriveID;
            ffmpegFolder = data.outputFolder;
        } catch (JsonSyntaxException e) {
            throw new IOException(e);
        }
    }

    // Overridden methods
    @Override
    public void processDownload(String downloadedResourcePath) throws IOException {
        // Unzip the downloaded file
        log(Level.INFO, "Unzipping FFmpeg");
        CompressionHandlers.zipDecompress(outputFolder, downloadedResourcePath);

        // Define a variable to store the FFmpeg binary path
        if (os == OSType.MAC) {
            // Set executable status if needed
            if (!new File(IOMethods.joinPaths(outputFolder, "ffmpeg")).setExecutable(true)) {
                IOException e = new IOException(
                        "Failed to set executable status for '" + IOMethods.joinPaths(outputFolder, "ffmpeg") + "'."
                );
                logException(e);
                throw e;
            } else {
                log(Level.INFO, "Set executable status for FFmpeg");
                ffmpegBinPath = IOMethods.joinPaths(outputFolder, "ffmpeg");
            }
        } else if (os == OSType.WINDOWS) {
            ffmpegBinPath = IOMethods.joinPaths(outputFolder, "ffmpeg.exe");
        } else {
            throw new IOException("Unrecognised OS");
        }

        IOMethods.delete(IOMethods.joinPaths(destFolder, "ffmpeg.zip"));

        log(Level.INFO, "FFmpeg installation complete");
    }

    // Helper classes
    public static class FFmpegDownloadData {
        public String downloadDriveID;
        public String signatureDriveID;
        public String outputFolder;
    }
}
