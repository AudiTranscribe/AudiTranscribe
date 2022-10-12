/*
 * AudioResourceDownloadManager.java
 * Description: Class that handles the downloading of the audio resource.
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
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.network.APICallHandler;
import site.overwrite.auditranscribe.network.DownloadFileHandler;
import site.overwrite.auditranscribe.network.exceptions.APIServerException;
import site.overwrite.auditranscribe.setup_wizard.helpers.data_encapsulators.AudioResourceDownloadData;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Class that handles the downloading of the audio resource.
 */
public class AudioResourceDownloadManager extends ClassWithLogging {
    // Attributes
    private final int maxAttempts;

    private URL downloadURL;
    private String signature;

    /**
     * Initialization method for an <code>AudioResourceDownloadManager</code>.
     *
     * @param maxAttempts Number of attempts to try and download the audio resource before failing.
     */
    public AudioResourceDownloadManager(int maxAttempts) {
        log(Level.FINE, "Starting audio resource download manager");

        // Update attributes
        this.maxAttempts = maxAttempts;

        try {
            // Define the data file path
            String dataFilePath = IOMethods.joinPaths(
                    "setup-wizard-files", "audio-resource-data", "audio-resource-data.json"
            );

            // Create the GSON loader object
            Gson gson = new Gson();

            // Attempt to get the input stream
            InputStream inputStream = IOMethods.getInputStream(dataFilePath);

            // Check if the input stream is null or not
            if (inputStream == null) {
                throw new IOException("Cannot find the audio resource data file '" + dataFilePath + "'.");
            }

            try (Reader reader = new InputStreamReader(inputStream)) {
                // Try loading the filter data
                AudioResourceDownloadData data = gson.fromJson(reader, AudioResourceDownloadData.class);

                // Set attributes
                downloadURL = new URL(data.url);

                // Get signature from signature page
                JsonObject returnedData = APICallHandler.sendAPIGetRequest(data.signaturePage);
                signature = returnedData.get("signature").getAsString();

            } catch (JsonSyntaxException | APIServerException e) {
                throw new IOException(e);
            }
        } catch (IOException e) {
            // Note that an exception has occurred
            logException(e);

            // Make all the attributes `null`
            downloadURL = null;
            signature = null;
        }
    }

    // Public methods

    /**
     * Method that downloads the audio resource.
     *
     * @param destFolder Folder that stores the audio resource.
     * @param task       A <code>CustomTask</code> object to show the progress of the download.
     * @return The <b>absolute</b> path to the downloaded audio resource.
     * @throws IOException If the downloading of the audio resource fails.
     */
    public String downloadAudioResource(String destFolder, CustomTask<?> task) throws IOException {
        // Create all parent directories
        IOMethods.createFolder(destFolder);

        // Download the audio resource file
        try {
            log(Level.INFO, "Downloading audio resource from '" + downloadURL.toString() + "'.");
            DownloadFileHandler.downloadFileWithRetry(
                    downloadURL, IOMethods.joinPaths(destFolder, "Breakfast.wav"), task, "SHA256", signature,
                    maxAttempts
            );
        } catch (NoSuchAlgorithmException ignored) {
        }

        log(Level.INFO, "Audio resource download complete");

        return IOMethods.joinPaths(destFolder, "Breakfast.wav");
    }
}
