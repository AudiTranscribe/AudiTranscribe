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

import com.google.gson.JsonObject;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.network.APICallHandler;
import site.overwrite.auditranscribe.network.exceptions.APIServerException;
import site.overwrite.auditranscribe.utils.GUIUtils;
import site.overwrite.auditranscribe.utils.MiscUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Class that assists with the checking of updates from the API server.
 */
@ExcludeFromGeneratedCoverageReport
public class UpdateChecker extends ClassWithLogging {
    // Public methods
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
     * Helper method that checks if there are any new versions for AudiTranscribe.
     *
     * @param currentVersion Current version of AudiTranscribe.
     * @return A pair. First element is a boolean, describing whether the current version is latest.
     * Second element is the tag of a newer version, or <code>null</code> if current version is
     * already latest.
     */
    private static Pair<Boolean, String> checkIfHaveNewVersion(String currentVersion) {
        // Make a call to the API server checking if the current version is latest
        HashMap<String, String> params = new HashMap<>();
        params.put("current-version", "v" + currentVersion);

        JsonObject response;
        try {
            response = APICallHandler.sendAPIGetRequest("check-if-have-new-version", params, 5000);
        } catch (APIServerException e) {
            // Return a value that says that the current version is latest
            log(
                    Level.WARNING,
                    "Error for API request on checking new version: timed out or connection refused",
                    UpdateChecker.class.getName()
            );
            return new Pair<>(true, null);
        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }

        // Parse the response and return
        boolean isLatest = response.get("is_latest").getAsBoolean();
        if (isLatest) {
            return new Pair<>(true, null);
        } else {
            return new Pair<>(false, response.get("newer_tag").getAsString());
        }
    }
}
