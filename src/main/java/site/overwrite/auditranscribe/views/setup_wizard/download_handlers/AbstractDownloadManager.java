/*
 * AbstractDownloadManager.java
 * Description: Class that contains the main methods for downloading resources.
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

import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.exceptions.ValueException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.network.DownloadFileHandler;
import site.overwrite.auditranscribe.network.DownloadTask;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Class that contains the main methods for downloading resources.
 */
public abstract class AbstractDownloadManager extends ClassWithLogging {
    // Attributes
    private final int maxAttempts;

    protected URL downloadURL;
    protected String downloadFileName;
    protected String signature;

    /**
     * Initialization method for an <code>AbstractDownloadManager</code>.
     *
     * @param maxAttempts Number of attempts to try and download the resource before failing.
     */
    public AbstractDownloadManager(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    // Public methods

    /**
     * Method that downloads the resource.
     *
     * @param destFolder Folder that stores the resource.
     * @param task       A <code>DownloadTask</code> object to show the progress of the download.
     * @return The <b>absolute</b> path to the downloaded resource.
     * @throws IOException    If the downloading of the resource fails.
     * @throws ValueException If not all required attributes (i.e., <code>downloadURL</code>,
     *                        <code>downloadFileName</code> and <code>signature</code>) are set.
     */
    public String downloadResource(String destFolder, DownloadTask<?> task) throws IOException {
        // Ensure all required attributes are set
        if (downloadURL == null || downloadFileName == null || signature == null) {
            throw new ValueException("Not all required attributes set");
        }

        // Create all parent directories
        IOMethods.createFolder(destFolder);

        // Download the resource file
        try {
            log(Level.INFO, "Downloading resource from '" + downloadURL.toString() + "'.");
            DownloadFileHandler.downloadFileWithRetry(
                    downloadURL, IOMethods.joinPaths(destFolder, downloadFileName), task, "SHA256", signature,
                    maxAttempts
            );
        } catch (NoSuchAlgorithmException ignored) {
        }

        log(Level.INFO, "Resource download complete");

        return IOMethods.joinPaths(destFolder, downloadFileName);
    }

    /**
     * Method that processes the downloaded resource.
     *
     * @param downloadedResourcePath Path to the downloaded resource.
     */
    public abstract void processDownload(String downloadedResourcePath) throws IOException;
}
