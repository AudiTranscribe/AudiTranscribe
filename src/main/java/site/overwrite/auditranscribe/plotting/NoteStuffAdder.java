/*
 * NoteStuffAdder.java
 *
 * Created on 2022-03-20
 * Updated on 2022-04-16
 *
 * Description: Class that adds the notes' stuff to the spectrogram area.
 */

package site.overwrite.auditranscribe.plotting;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import site.overwrite.auditranscribe.utils.UnitConversion;

/**
 * Class that adds the notes' stuff to the spectrogram area.
 */
public class NoteStuffAdder {
    // Attributes
    // Todo: move to `Spectrogram` class?
    static final Font noteLabelFont = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 16);

    static final double noteLineWidth = 2;
    static final Color noteLineColour = new Color(1, 1, 1, 0.5);

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

            // Position the label correctly
            noteLabel.setTranslateY(placementHeight - 0.675 * noteLabelFont.getSize());  // Magical 0.675 constant

            // Make the label centred
            noteLabel.setPrefWidth(width);
            noteLabel.setAlignment(Pos.TOP_CENTER);

            // Add the label to the plane
            notePane.getChildren().add(noteLabel);
        }
    }

    /**
     * Method that adds the notes lines to the spectrogram pane.
     */
    public static void addNoteLines(Pane spectrogramPaneAnchor, double height, int minNoteNumber, int maxNoteNumber) {
        // Get the width of the note pane
        double width = spectrogramPaneAnchor.getPrefWidth();

        // Place the notes onto the pane
        for (int i = minNoteNumber; i <= maxNoteNumber; i++) {
            // Calculate the height to move the pointer to
            double placementHeight = PlottingHelpers.noteNumToHeight(i, minNoteNumber, maxNoteNumber, height);
            placementHeight += getHeightDifference(height, minNoteNumber, maxNoteNumber) / 2;

            // Create the line
            Line noteLine = new Line(0, placementHeight, width, placementHeight);

            // Set the line dashed format
            if (i % 12 != 0) {  // Not a C note
                noteLine.getStrokeDashArray().addAll(10d, 6d);  // Todo: make this a constant
            } else {  // A C note
                noteLine.getStrokeDashArray().addAll(1d);
            }

            // Format the line
            noteLine.setFill(null);
            noteLine.setStroke(noteLineColour);
            noteLine.setStrokeWidth(noteLineWidth);

            // Add the line to the spectrogram
            spectrogramPaneAnchor.getChildren().add(noteLine);
        }
    }

    // Private methods

    /**
     * Calculates the difference in height between two consecutive notes.
     *
     * @param height     Height of the pane.
     * @param minNoteNum Smallest note number.
     * @param maxNoteNum Largest note number.
     * @return Difference in height between two consecutive notes.
     */
    static double getHeightDifference(double height, double minNoteNum, double maxNoteNum) {
        return height / (maxNoteNum - minNoteNum);
    }
}
