/*
 * Popups.java
 * Description: Class that handles the showing of popups and dialogs.
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

package app.auditranscribe.fxml;

import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Class that handles the showing of popups and dialogs.
 */
@ExcludeFromGeneratedCoverageReport
public final class Popups {
    private Popups() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that shows an information alert.
     *
     * @param owner      Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param stageStyle Style of the stage to show.
     * @param title      Title of the alert box.
     * @param content    Content of the information alert.
     */
    public static void showInformationAlert(Window owner, StageStyle stageStyle, String title, String content) {
        showGenericAlert(Alert.AlertType.INFORMATION, owner, stageStyle, title, content);
    }

    /**
     * Method that shows an information alert.
     *
     * @param owner   Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showInformationAlert(Window owner, String title, String content) {
        showInformationAlert(owner, StageStyle.UTILITY, title, content);
    }

    /**
     * Method that shows a warning alert.
     *
     * @param owner   Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showWarningAlert(Window owner, String title, String content) {
        showGenericAlert(Alert.AlertType.WARNING, owner, StageStyle.UTILITY, title, content);
    }

    /**
     * Method that shows a confirmation alert.
     *
     * @param owner       Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param title       Title of the confirmation alert.
     * @param headerText  Header text for the confirmation alert.
     * @param contentText Content of the confirmation alert.
     * @param buttonTypes Button types for the confirmation alert.
     * @return A <code>ButtonType</code> value.
     */
    public static Optional<ButtonType> showMultiButtonAlert(
            Window owner, String title, String headerText, String contentText, ButtonType... buttonTypes
    ) {
        // Get active theme
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Create the alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupDialog(alert, owner, StageStyle.UTILITY, currentTheme, title, headerText, contentText);

        alert.getButtonTypes().setAll(buttonTypes);

        return alert.showAndWait();
    }

    /**
     * Method that shows an exception alert.
     *
     * @param owner       Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param headerText  Header text for the exception alert.
     * @param contentText Content text for the exception alert.
     * @param throwable   <code>Throwable</code> object for the exception that occurred.
     */
    public static void showExceptionAlert(
            Window owner, String headerText, String contentText, Throwable throwable
    ) {
        // Get active theme
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Create the alert
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupDialog(alert, owner, StageStyle.UTILITY, currentTheme, "Error", headerText, contentText);

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

    /**
     * Method that shows a text input dialog with the specified header and content texts.
     *
     * @param owner       Stage that owns the dialog. Provide <code>null</code> if there is no owner.
     * @param title       Title of the text input dialog.
     * @param headerText  Header text for the dialog.
     * @param contentText Content text for the dialog.
     * @return The value that was entered in the text input dialog.
     */
    public static Optional<String> showTextInputDialog(
            Window owner, String title, String headerText, String contentText, String defaultText
    ) {
        // Get active theme
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Set up text input dialog
        TextInputDialog dialog = new TextInputDialog();
        setupDialog(dialog, owner, StageStyle.UTILITY, currentTheme, title, headerText, contentText);

        // Get the nodes on the dialog
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        TextField inputField = dialog.getEditor();
        inputField.setText(defaultText);

        // Disable the OK button if the input field is empty
        BooleanBinding isInvalid = Bindings.createBooleanBinding(
                () -> inputField.getText().length() == 0,  // Invalid if the length is 0
                inputField.textProperty()
        );
        okButton.disableProperty().bind(isInvalid);

        // Wait for user's response
        return dialog.showAndWait();
    }

    // Private methods

    /**
     * Helper method that shows a generic alert.
     *
     * @param type       Type of the alert box.
     * @param owner      Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param stageStyle Style of the stage to show.
     * @param title      Title of the alert box.
     * @param content    Content of the information alert.
     */
    private static void showGenericAlert(
            Alert.AlertType type, Window owner, StageStyle stageStyle, String title, String content
    ) {
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        Alert alert = new Alert(type);
        setupDialog(alert, owner, stageStyle, currentTheme, title, null, content);

        alert.showAndWait();
    }

    /**
     * Helper method that sets up a dialog.
     *
     * @param dialog       Dialog object to set up.
     * @param owner        Stage that owns the dialog. Provide <code>null</code> if there is no
     *                     owner.
     * @param stageStyle   Style of the stage to show.
     * @param currentTheme Theme to apply to the dialog.
     * @param title        Title to be shown on the dialog.
     * @param headerText   Header text of the dialog.
     * @param contentText  Main text on the dialog.
     */
    private static void setupDialog(
            Dialog<?> dialog, Window owner, StageStyle stageStyle, Theme currentTheme, String title,
            String headerText, String contentText
    ) {
        dialog.getDialogPane().getStylesheets().addAll(
                IOMethods.getFileURL(IOMethods.joinPaths("fxml", "css", "base.css")).toExternalForm(),
                IOMethods.getFileURL(IOMethods.joinPaths("fxml", "css", "views", "popups.css")).toExternalForm(),
                IOMethods.getFileURL(IOMethods.joinPaths("fxml", "css", "theme", currentTheme.cssFile)).toExternalForm()
        );

        dialog.initStyle(stageStyle);
        if (owner != null) dialog.initOwner(owner);

        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
    }
}
