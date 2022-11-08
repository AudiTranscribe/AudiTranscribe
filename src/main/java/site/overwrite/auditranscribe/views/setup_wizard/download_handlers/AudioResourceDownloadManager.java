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

package site.overwrite.auditranscribe.views.setup_wizard.download_handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.network.APICallHandler;
import site.overwrite.auditranscribe.network.exceptions.APIServerException;

import java.io.*;
import java.net.URL;
import java.util.logging.Level;

/**
 * Class that handles the downloading of the audio resource.
 */
public class AudioResourceDownloadManager extends AbstractDownloadManager {
    /**
     * Initialization method for an <code>AudioResourceDownloadManager</code>.
     *
     * @param maxAttempts Number of attempts to try and download the audio resource before failing.
     */
    public AudioResourceDownloadManager(int maxAttempts) {
        super(maxAttempts);

        log(Level.FINE, "Starting audio resource download manager");

        // Update attributes
        downloadFileName = "Breakfast.wav";

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
                downloadURL = new URL(APICallHandler.API_SERVER_URL + data.downloadPage);

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

    // Overridden methods
    @Override
    public void processDownload(String downloadedResourcePath) {
        // No processing needed
    }

    // Helper classes
    public static class AudioResourceDownloadData {
        public String downloadPage;
        public String signaturePage;
    }
}
