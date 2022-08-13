/*
 * CheckForUpdatesViewHelper.java
 *
 * Created on 2022-08-13
 * Updated on 2022-08-13
 *
 * Description: Helper class that handles the checking of updates.
 */

package site.overwrite.auditranscribe.main_views.helpers;

import com.google.gson.JsonObject;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.javatuples.Pair;
import site.overwrite.auditranscribe.exceptions.network.APIServerException;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.network.APICallHandler;
import site.overwrite.auditranscribe.network.RequestMethod;
import site.overwrite.auditranscribe.utils.GUIUtils;
import site.overwrite.auditranscribe.utils.MiscUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;

public class CheckForUpdatesViewHelper {
    // Public methods
    public static void checkForUpdates(String currentVersion) {
        // Determine if we need to check for updates
        int currentTime = (int) MiscUtils.getUnixTimestamp();
        int lastChecked = DataFiles.PERSISTENT_DATA_FILE.data.lastCheckedForUpdates;
        if (currentTime - lastChecked <= DataFiles.SETTINGS_DATA_FILE.data.checkForUpdateInterval * 86400) return;

        // Check if there is any new updates available
        Pair<Boolean, String> response = checkIfHaveNewVersion(currentVersion);
        boolean isLatest = response.getValue0();
        String newVersionTag = response.getValue1();

        if (!isLatest) {
            MyLogger.log(
                    Level.INFO,
                    "New version available: " + newVersionTag,
                    CheckForUpdatesViewHelper.class.getName()
            );

            // Convert the new version tag into a semver
            String semver = newVersionTag.substring(1);

            // Show an alert telling the user that a new version is available
            ButtonType seeNewUpdate = new ButtonType("See New Update");
            ButtonType remindLater = new ButtonType("Remind Me Later");
            ButtonType ignore = new ButtonType("Ignore For Now", ButtonBar.ButtonData.CANCEL_CLOSE);

            Optional<ButtonType> selectedButton = Popups.showMultiButtonAlert(
                    "New Version Available",
                    "New Version Available: " + semver,
                    "A new version for AudiTranscribe is available. Do you want to see it?",
                    seeNewUpdate, remindLater, ignore
            );

            if (selectedButton.isPresent()) {
                if (selectedButton.get() == seeNewUpdate) {
                    // Send user to the new release page
                    // Todo: use own website
                    String urlString = "https://github.com/AudiTranscribe/AudiTranscribe/releases/tag/" + newVersionTag;
                    GUIUtils.openURLInBrowser(urlString);

                } else if (selectedButton.get() == remindLater) {
                    // Supress alert for a set duration
                    DataFiles.PERSISTENT_DATA_FILE.data.lastCheckedForUpdates = currentTime;
                    DataFiles.PERSISTENT_DATA_FILE.saveFile();
                }
            }
        } else {
            MyLogger.log(
                    Level.FINE,
                    "AudiTranscribe is up to date",
                    CheckForUpdatesViewHelper.class.getName()
            );
        }
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
            response = APICallHandler.sendAPIRequest("check-if-have-new-version", RequestMethod.GET, params);
        } catch (APIServerException e) {
            // Return a value that says that the current version is latest
            MyLogger.log(
                    Level.WARNING,
                    "Error for API request on checking new version: timed out or connection refused",
                    CheckForUpdatesViewHelper.class.getName()
            );
            return new Pair<>(true, null);
        } catch (IOException e) {
            MyLogger.logException(e);
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
