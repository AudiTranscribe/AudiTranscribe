/*
 * Popups.java
 * Description: Class that handles the showing of popups and dialogs to the user.
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

package site.overwrite.auditranscribe.misc;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Class that handles the showing of popups and dialogs to the user.
 */
public final class Popups {
    private Popups() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that shows an information alert.
     *
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showInformationAlert(String title, String content) {
        showGenericAlert(title, content, Alert.AlertType.INFORMATION);
    }

    /**
     * Method that shows a warning alert.
     *
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showWarningAlert(String title, String content) {
        showGenericAlert(title, content, Alert.AlertType.WARNING);
    }

    /**
     * Method that shows a confirmation alert.
     *
     * @param title       Title of the confirmation alert.
     * @param headerText  Header text for the confirmation alert.
     * @param contentText Content of the confirmation alert.
     * @param buttonTypes Button types for the confirmation alert.
     * @return A <code>ButtonType</code> value.
     */
    public static Optional<ButtonType> showMultiButtonAlert(
            String title, String headerText, String contentText, ButtonType... buttonTypes
    ) {
        // Get active theme
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Create the alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(
                IOMethods.getFileURL(IOMethods.joinPaths("views", "css", currentTheme.cssFile)).toExternalForm()
        );
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getButtonTypes().setAll(buttonTypes);

        return alert.showAndWait();
    }

    /**
     * Method that shows an exception alert.
     *
     * @param headerText  Header text for the exception alert.
     * @param contentText Content text for the exception alert.
     * @param throwable   <code>Throwable</code> object for the exception that occurred.
     */
    public static void showExceptionAlert(String headerText, String contentText, Throwable throwable) {
        // Get active theme
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Create a new error alert
        Alert alert = new Alert(Alert.AlertType.ERROR);

        // Set the theme on the alert
        alert.getDialogPane().getStylesheets().add(
                IOMethods.getFileURL(IOMethods.joinPaths("views", "css", currentTheme.cssFile)).toExternalForm()
        );

        // Set the alert style to `UTILITY` so that it can be shown during fullscreen
        alert.initStyle(StageStyle.UTILITY);

        // Set texts
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Set exception texts
        Label label = new Label("The exception stacktrace was:");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        // Show the alert
        alert.showAndWait();
    }

    // Private methods

    /**
     * Helper method that shows a generic alert.
     *
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     * @param type    Type of the alert box.
     */
    private static void showGenericAlert(String title, String content, Alert.AlertType type) {
        // Get active theme
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Create the alert
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add(
                IOMethods.getFileURL(IOMethods.joinPaths("views", "css", currentTheme.cssFile)).toExternalForm()
        );
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
