/*
 * SpectrogramStuffHandler.java
 *
 * Created on 2022-03-20
 * Updated on 2022-04-23
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
public class SpectrogramStuffHandler {
    // Attributes
    // Todo: move to `Spectrogram` class?
    static final Font NOTE_LABEL_FONT = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 16);

    static final double NOTE_LINE_WIDTH = 2;
    static final double BEAT_LINE_WIDTH = 5;  // This will be the same as the bar line width

    static final Color NOTE_LINE_COLOUR = new Color(1, 1, 1, 0.5);  // Todo: work with themes
    static final Color BEAT_LINE_COLOUR = new Color(1, 1, 1, 0.5);  // Todo: work with themes
    static final Color BAR_LINE_COLOR = new Color(0, 1, 0, 0.5);  // Todo: work with themes

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
            noteLabel.setFont(NOTE_LABEL_FONT);
            noteLabel.setTextFill(Color.BLACK);

            // Position the label correctly
            noteLabel.setTranslateY(placementHeight - 0.675 * NOTE_LABEL_FONT.getSize());  // Magical 0.675 constant

            // Make the label centred
            noteLabel.setPrefWidth(width);
            noteLabel.setAlignment(Pos.TOP_CENTER);

            // Add the label to the plane
            notePane.getChildren().add(noteLabel);
        }
    }

    /**
     * Method that adds the notes lines to the spectrogram pane.
     *
     * @param spectrogramPane Spectrogram pane.
     * @param height          Height of the spectrogram pane.
     * @param minNoteNumber   Lowest permitted note number.
     * @param maxNoteNumber   Highest permitted note number.
     */
    public static void addNoteLines(Pane spectrogramPane, double height, int minNoteNumber, int maxNoteNumber) {
        // Get the width of the note pane
        double width = spectrogramPane.getPrefWidth();

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
            noteLine.setStroke(NOTE_LINE_COLOUR);
            noteLine.setStrokeWidth(NOTE_LINE_WIDTH);

            // Add the line to the spectrogram
            spectrogramPane.getChildren().add(noteLine);
        }
    }

    /**
     * Method that returns the beat lines to be drawn on the canvas.
     *
     * @param bpm         Number of beats per minute.
     * @param beatsPerBar Number of beats per bar.
     * @param pxPerSecond Number of pixels dedicated per second.
     * @param height      Spectrogram pane height.
     * @param duration    Duration of the audio.
     * @param offset      Number of seconds to wait before the actual audio <em>starts</em>.
     * @param zoomScaleX  Zoom scaling for the X direction.
     * @return Array of <code>Line</code> objects, representing the lines to be added.
     */
    public static Line[] getBeatLines(
            int bpm, int beatsPerBar, int pxPerSecond, double height, double duration, double offset,
            double zoomScaleX
    ) {
        // Calculate the number of beats and the number of seconds per beat
        double spb = secondsPerBeat(bpm);  // SPB = seconds per beat
        int numBeats = (int) Math.ceil(bpm / 60. * duration);

        // Generate lines for every beat
        Line[] lines = new Line[numBeats + 1];

        for (int beatNum = 0; beatNum <= numBeats; beatNum++) {
            // Generate the beat line
            Line beatLine = generateBeatLine(beatNum, beatsPerBar, pxPerSecond, height, zoomScaleX, spb, offset);

            // Add line to array
            lines[beatNum] = beatLine;
        }

        // Return the generated lines
        return lines;
    }

    /**
     * Method that adds the beat lines onto the spectrogram pane.
     *
     * @param spectrogramPane Spectrogram pane.
     * @param lines           Beat lines to add.
     */
    public static void addBeatLines(Pane spectrogramPane, Line[] lines) {
        spectrogramPane.getChildren().addAll(lines);
    }

    /**
     * Method that updates the existing beat lines, and adds/removes as necessary.
     *
     * @param spectrogramPane Spectrogram pane.
     * @param lines           Original beat lines.
     * @param duration        Duration of the audio.
     * @param oldBPM          Old value for the BPM.
     * @param newBPM          New value for the BPM.
     * @param oldOffset       Old offset value.
     * @param newOffset       New offset value.
     * @param height          Spectrogram pane height.
     * @param beatsPerBar     Number of beats per bar.
     * @param pxPerSecond     Number of pixels dedicated per second.
     * @param zoomScaleX      Zoom scaling for the X direction.
     * @return Array of <code>Line</code> objects, representing the <b>new</b> beat lines that are
     * shown.
     */
    // Todo: handle change in offset
    // Todo: handle change in beats per bar
    public static Line[] updateBeatLines(
            Pane spectrogramPane, Line[] lines, double duration, int oldBPM, int newBPM, double oldOffset,
            double newOffset, double height, int beatsPerBar, int pxPerSecond, double zoomScaleX
    ) {
        // Return prematurely if the olds equal the news
        if (oldBPM == newBPM && oldOffset == newOffset) return lines;  // Nothing to update

        // Calculate the new seconds per beat (SPB)
        double newSPB = secondsPerBeat(newBPM);

        // Calculate the difference between the number of beats needed
        int oldNumBeats = lines.length - 1;
        int newNumBeats = (int) Math.ceil(newBPM / 60. * (duration + Math.abs(newOffset)));
        int deltaNumBeats = newNumBeats - oldNumBeats;

        // Create a new array with the new beat lines
        Line[] newLines = new Line[newNumBeats + 1];

        if (deltaNumBeats > 0) {  // Need to add beats
            // Array copy old beat lines into the new lines
            System.arraycopy(lines, 0, newLines, 0, oldNumBeats + 1);
        } else {  // Need to remove beats
            // Limit the number of lines that are copied
            System.arraycopy(lines, 0, newLines, 0, newNumBeats + 1);
        }

        // Update existing lines
        int numCopiedBeatLines = Math.min(oldNumBeats, newNumBeats) + 1;

        for (int beatNum = 0; beatNum < numCopiedBeatLines; beatNum++) {
            // Calculate position to place the line
            double pos = (newOffset + beatNum * newSPB) * pxPerSecond * zoomScaleX;

            // Update line position
            newLines[beatNum].setStartX(pos);
            newLines[beatNum].setEndX(pos);

            // Try and add again
            try {
                spectrogramPane.getChildren().add(newLines[beatNum]);
            } catch (Exception ignored) {}

        }

        // Add more lines if needed
        if (deltaNumBeats > 0) {
            for (int beatNum = numCopiedBeatLines; beatNum <= newNumBeats; beatNum++) {
                // Generate the beat line
                Line beatLine = generateBeatLine(
                        beatNum, beatsPerBar, pxPerSecond, height, zoomScaleX, newSPB, newOffset
                );

                // Add line to array
                newLines[beatNum] = beatLine;
                spectrogramPane.getChildren().add(beatLine);
            }

        // Remove old lines if needed
        } else {
            for (int beatNum = newNumBeats; beatNum <= oldNumBeats; beatNum++) {
                spectrogramPane.getChildren().remove(lines[beatNum]);
            }
        }

        // Return the new lines
        return newLines;
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

    /**
     * Calculates the number of seconds needed to finish one beat.
     *
     * @param bpm Beats per minute.
     * @return Seconds per beat.
     */
    static double secondsPerBeat(int bpm) {
        return 1. / (bpm / 60.);  // BPM / 60 = Beats per second, so 1 / Beats Per Second = Seconds per Beat
    }

    /**
     * Helper method that generates a beat line.
     *
     * @param beatNum     Beat number.
     * @param beatsPerBar Number of beats per bar.
     * @param pxPerSecond Number of pixels dedicated per second.
     * @param height      Spectrogram height.
     * @param zoomScaleX  Zoom scaling for the X direction.
     * @param spb         Seconds per beat.
     * @param offset      Number of seconds to wait before the actual audio <em>starts</em>.
     * @return A <code>Line</code> object representing the beat line.
     */
    static Line generateBeatLine(
            int beatNum, int beatsPerBar, int pxPerSecond, double height, double zoomScaleX, double spb, double offset
    ) {
        // Calculate position to place the line
        double pos = (offset + beatNum * spb) * pxPerSecond * zoomScaleX;

        // Create the line
        Line beatLine = new Line(pos, 0, pos, height);

        // Format the line
        beatLine.setFill(null);
        beatLine.setStroke(beatNum % beatsPerBar != 0 ? BEAT_LINE_COLOUR : BAR_LINE_COLOR);
        beatLine.setStrokeWidth(BEAT_LINE_WIDTH);

        // Return the line
        return beatLine;
    }
}
