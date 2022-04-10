/*
 * NoteStuffAdder.java
 *
 * Created on 2022-03-20
 * Updated on 2022-04-10
 *
 * Description: Class that adds the notes' stuff to the spectrogram area.
 */

package site.overwrite.auditranscribe.plotting;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import site.overwrite.auditranscribe.utils.UnitConversion;

/**
 * Class that adds the notes' stuff to the spectrogram area.
 */
public class NoteStuffAdder {
    // Attributes
    static final Font noteLabelFont = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 16);

    // Public methods

    /**
     * Method that adds the note labels to the note pane.
     *
     * @param notePane      Note pane.
     * @param height        (Final) height of the note pane.
     * @param minNoteNumber Minimum note number.
     * @param maxNoteNumber Maximum note number.
     */
    public static void addNoteLabels(Pane notePane, double height, int minNoteNumber, int maxNoteNumber) {
        // Get the width of the note pane
        double width = notePane.getPrefWidth();

        // Place the notes onto the pane
        for (int i = minNoteNumber; i <= maxNoteNumber; i++) {
            // Get the note's text
            String note = UnitConversion.noteNumberToNote(i);

            // Calculate the height to move the pointer to
            double placementHeight = PlottingHelpers.noteNumToHeight(i, minNoteNumber, maxNoteNumber, height);

            // Create the label
            Label noteLabel = new Label(note);
            noteLabel.setFont(noteLabelFont);
            noteLabel.setTextFill(Color.BLACK);

            // Make the label centred
            noteLabel.setPrefWidth(width);
            noteLabel.setAlignment(Pos.BASELINE_CENTER);  // Todo: check if positioning is correct

            // Add the label to the plane
            notePane.getChildren().add(noteLabel);

            // Position the label correctly
            noteLabel.setTranslateY(placementHeight);
        }
    }
}
