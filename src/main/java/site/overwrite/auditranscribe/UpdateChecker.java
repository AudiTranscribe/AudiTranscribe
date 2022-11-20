/*
 * UpdateChecker.java
 * Description: Class that assists with the checking of updates from the API server.
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

package site.overwrite.auditranscribe;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.misc.Version;
import site.overwrite.auditranscribe.utils.GUIUtils;
import site.overwrite.auditranscribe.utils.MiscUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Class that assists with the checking of updates from the API server.
 */
@ExcludeFromGeneratedCoverageReport
public class UpdateChecker extends ClassWithLogging {
    // Constants
    public static int CONNECTION_TIMEOUT = 1500;  // In milliseconds; duration to wait for connecting to server
    public static int READ_TIMEOUT = 2500;

    public static String AUDITRANSCRIBE_REPO = "AudiTranscribe/AudiTranscribe";

    // Public methods

    /**
     * Method that checks if the current version of AudiTranscribe is the most recent version.
     *
     * @param currentVersion Current version of AudiTranscribe. Does <b>not</b> include a preceding
     *                       <code>v</code> in front of the semantic version.
     */
    public static void checkForUpdates(String currentVersion) {
        // Determine if we need to check for updates
        int currentTime = (int) MiscUtils.getUnixTimestamp();
        int lastChecked = DataFiles.PERSISTENT_DATA_FILE.data.lastCheckedForUpdates;
        int interval = DataFiles.SETTINGS_DATA_FILE.data.checkForUpdateInterval;
        if (currentTime - lastChecked <= interval * 3600) {  // 3600 s = 1 h
            return;
        }

        // Check if there is any new updates available
        Pair<Boolean, String> response = checkIfHaveNewVersion(currentVersion);
        boolean isLatest = response.value0();
        String newVersionTag = response.value1();

        if (!isLatest) {
            log(Level.INFO, "New version available: " + newVersionTag, UpdateChecker.class.getName());

            // Convert the new version tag into a semver
            String semver = newVersionTag.substring(1);

            // Show an alert telling the user that a new version is available
            ButtonType seeNewUpdate = new ButtonType("See New Update");
            ButtonType remindLater = new ButtonType("Remind Me Later");
            ButtonType ignore = new ButtonType("Ignore For Now", ButtonBar.ButtonData.CANCEL_CLOSE);

            Optional<ButtonType> selectedButton = Popups.showMultiButtonAlert(
                    null,
                    "New Version Available",
                    "New Version Available: " + semver,
                    "A new version for AudiTranscribe is available. Do you want to see it?",
                    seeNewUpdate, remindLater, ignore
            );

            if (selectedButton.isPresent()) {
                if (selectedButton.get() == seeNewUpdate) {
                    // Send user to the new release page
                    String urlString = "https://auditranscribe.app/release?tag=" + newVersionTag;
                    GUIUtils.openURLInBrowser(urlString);
                }
            }
        } else {
            log(Level.INFO, "AudiTranscribe is up to date", UpdateChecker.class.getName());
        }

        // Supress alert for a set duration
        DataFiles.PERSISTENT_DATA_FILE.data.lastCheckedForUpdates = currentTime;
        DataFiles.PERSISTENT_DATA_FILE.saveFile();
    }

    // Private methods

    /**
     * Helper method that retrieves the GitHub tag array.
     *
     * @return JSON array of the tags' data.
     * @throws IOException If something went wrong when processing the URL or when opening a
     *                     connection to the GitHub API server.
     */
    private static JsonArray getRawTagArray() throws IOException {
        // Form the URL
        URL url = new URL("https://api.github.com/repos/" + AUDITRANSCRIBE_REPO + "/tags");

        // Send request to GitHub server for all the version tags
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String output;

        try {
            // Set the request method for the connection
            con.setRequestMethod("GET");

            // Set timeouts
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);

            // Get the output from the connection
            StringBuilder content = new StringBuilder();
            Reader streamReader;

            if (con.getResponseCode() > 299) {
                streamReader = new InputStreamReader(con.getErrorStream());
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }

            BufferedReader in = new BufferedReader(streamReader);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();

            output = content.toString();
        } finally {
            // Must remember to disconnect
            con.disconnect();
        }

        // Parse the content as JSON data
        return JsonParser.parseString(output).getAsJsonArray();
    }

    /**
     * Helper method that returns all the version tags.
     *
     * @return All the version tags.
     */
    private static Version[] getVersions() {
        Version[] tags;
        try {
            JsonArray tagArray = getRawTagArray();

            int numTags = tagArray.size();
            tags = new Version[numTags];
            for (int i = 0; i < numTags; i++) {
                tags[i] = new Version(
                        tagArray.get(i).getAsJsonObject().getAsJsonPrimitive("name")
                                .getAsString().substring(1)
                );
            }
        } catch (IOException e) {
            logException(e);
            tags = new Version[0];
        }
        return tags;
    }

    /**
     * Helper method that checks if there are any new versions for AudiTranscribe.
     *
     * @param currentVersionString Current version of AudiTranscribe.
     * @return A pair. First element is a boolean, describing whether the current version is latest.
     * Second element is the tag of a newer version, or <code>null</code> if current version is
     * already latest.
     */
    private static Pair<Boolean, String> checkIfHaveNewVersion(String currentVersionString) {
        // Parse current version as a version object as well
        Version currentVersion = new Version(currentVersionString);

        // Get all version tags
        Version[] versions = getVersions();

        // Find the latest version among all the versions
        Version latestVersion = currentVersion;
        for (Version otherVersion : versions) {
            if (latestVersion.compareTo(otherVersion) < 0) {
                latestVersion = otherVersion;
            }
        }

        // If current version is not latest, return newer version as well
        if (currentVersion != latestVersion) {
            return new Pair<>(false, "v" + latestVersion.version);
        } else {
            return new Pair<>(true, null);  // Is latest, so do not return any version
        }
    }
}
