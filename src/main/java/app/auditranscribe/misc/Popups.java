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

package app.auditranscribe.misc;

import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
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
 * Class that handles the showing of popups and dialogs to the user.
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
     * @param owner   Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showInformationAlert(Window owner, String title, String content) {
        showGenericAlert(title, content, Alert.AlertType.INFORMATION, owner);
    }

    /**
     * Method that shows a warning alert.
     *
     * @param owner   Stage that owns the alert. Provide <code>null</code> if there is no owner.
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showWarningAlert(Window owner, String title, String content) {
        showGenericAlert(title, content, Alert.AlertType.WARNING, owner);
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
        setupDialog(alert, owner, title, headerText, contentText, currentTheme);

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
        setupDialog(alert, owner, "Error", headerText, contentText, currentTheme);

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
        setupDialog(dialog, owner, title, headerText, contentText, currentTheme);

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
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     * @param type    Type of the alert box.
     * @param owner   Stage that owns the alert. Provide <code>null</code> if there is no owner.
     */
    private static void showGenericAlert(String title, String content, Alert.AlertType type, Window owner) {
        Theme currentTheme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        Alert alert = new Alert(type);
        setupDialog(alert, owner, title, null, content, currentTheme);

        alert.showAndWait();
    }

    /**
     * Helper method that sets up a dialog.
     *
     * @param dialog       Dialog object to set up.
     * @param owner        Stage that owns the dialog. Provide <code>null</code> if there is no
     *                     owner.
     * @param title        Title to be shown on the dialog.
     * @param headerText   Header text of the dialog.
     * @param contentText  Main text on the dialog.
     * @param currentTheme Theme to apply to the dialog.
     */
    private static void setupDialog(
            Dialog<?> dialog, Window owner, String title, String headerText, String contentText,
            Theme currentTheme
    ) {
        dialog.getDialogPane().getStylesheets().add(
                IOMethods.getFileURL(IOMethods.joinPaths("views", "css", currentTheme.cssFile)).toExternalForm()
        );

        dialog.initStyle(StageStyle.UTILITY);
        if (owner != null) dialog.initOwner(owner);

        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
    }
}
