/*
 * Popups.java
 *
 * Created on 2022-05-26
 * Updated on 2022-06-04
 *
 * Description: Class that handles the popups and dialogs.
 */

package site.overwrite.auditranscribe.views.helpers;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Class that handles the popups and dialogs.
 */
public class Popups {
    // Public methods

    /**
     * Method that shows an information alert.
     *
     * @param title   Title of the alert box.
     * @param content Content of the information alert.
     */
    public static void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        alert.showAndWait();
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
    public static Optional<ButtonType> showMultiButtonAlert(String title, String headerText, String contentText, ButtonType... buttonTypes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getButtonTypes().setAll(buttonTypes);

        return alert.showAndWait();
    }

    /**
     * Method that shows a confirmation alert.<br>
     * This confirmation alert only has two buttons: an OK button and a Cancel button.
     *
     * @param title       Title of the confirmation alert.
     * @param headerText  Header text for the confirmation alert.
     * @param contentText Content of the confirmation alert.
     * @return A <code>ButtonType</code> value.
     */
    public static Optional<ButtonType> showConfirmationAlert(String title, String headerText, String contentText) {
        return showMultiButtonAlert(title, headerText, contentText, ButtonType.OK, ButtonType.CANCEL);
    }

    /**
     * Method that shows an exception alert.
     *
     * @param headerText  Header text for the exception alert.
     * @param contentText Content text for the exception alert.
     * @param e           Exception that occurred.
     */
    public static void showExceptionAlert(String headerText, String contentText, Exception e) {
        // Create a new error alert
        Alert alert = new Alert(Alert.AlertType.ERROR);

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
        e.printStackTrace(pw);

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
     * Method that shows a text input dialog.
     *
     * @param title        Title of the text input dialog.
     * @param headerText   Header text for the text input dialog.
     * @param contentText  Content text for the text input dialog.
     * @param defaultValue Default value for the text input dialog.
     * @return A <code>String</code> value.
     */
    public static Optional<String> showTextInputDialog(String title, String headerText, String contentText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);

        return dialog.showAndWait();
    }

    /**
     * Method that shows a choice dialog.
     *
     * @param title       Title of the choice dialog.
     * @param headerText  Header text for the choice dialog.
     * @param contentText Content text for the choice dialog.
     * @param choices     Choices for the choice dialog.
     * @return A <code>String</code> value.
     */
    public static Optional<String> showChoiceDialog(String title, String headerText, String contentText, String... choices) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices[0], choices);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);

        return dialog.showAndWait();
    }
}
