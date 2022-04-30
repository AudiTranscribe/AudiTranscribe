/*
 * PlottingStuffHandler.java
 *
 * Created on 2022-03-20
 * Updated on 2022-04-30
 *
 * Description: Class that adds the notes' stuff to the spectrogram area.
 */

package site.overwrite.auditranscribe.plotting;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import site.overwrite.auditranscribe.utils.MiscUtils;
import site.overwrite.auditranscribe.utils.UnitConversion;

import java.util.HashSet;

/**
 * Class that adds the notes' stuff to the spectrogram area.
 */
public class PlottingStuffHandler {
    // Attributes
    // Todo: move to `Spectrogram` class?
    static final Font LABEL_FONT = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 16);
    static final Color LABEL_COLOUR = new Color(0, 0, 0, 1);  // Todo: work with themes

    static final double NOTE_LINE_WIDTH = 2;
    static final double BEAT_LINE_WIDTH = 5;  // This will be the same as the bar line width
    static final double BAR_NUMBER_ELLIPSE_THICKNESS = 1.25;
    static final double BAR_NUMBER_ELLIPSE_RADIUS_Y = 16;

    static final Color NOTE_LINE_COLOUR = new Color(1, 1, 1, 0.5);  // Todo: work with themes
    static final Color BEAT_LINE_COLOUR = new Color(1, 1, 1, 0.5);  // Todo: work with themes
    static final Color BAR_LINE_COLOUR = new Color(0, 1, 0, 0.5);  // Todo: work with themes
    static final Color BAR_NUMBER_ELLIPSE_COLOUR = new Color(0, 0, 0, 1);  // Todo: work with themes

    // Public methods

    /**
     * Method that adds the note labels to the note pane.
     *
     * @param notePane      Note pane.
     * @param noteLabels    Note label array.
     * @param musicKey      Key for the music piece.
     * @param height        (Final) height of the note pane.
     * @param minNoteNumber Minimum note number.
     * @param maxNoteNumber Maximum note number.
     * @param fancySharps   Whether <em>fancier sharps</em> (i.e. ♯ instead of #) should be used for
     *                      the note labels.
     */
    public static Label[] addNoteLabels(Pane notePane, Label[] noteLabels, String musicKey, double height, int minNoteNumber, int maxNoteNumber, boolean fancySharps) {
        // Get the width of the note pane
        double width = notePane.getPrefWidth();

        // Get the note offsets that are in the key
        HashSet<Integer> noteOffsets = MiscUtils.getNotesInKey(musicKey);

        // Check if there are existing note labels
        if (noteLabels != null) {
            // Remove the note labels from the pane
            notePane.getChildren().removeAll(noteLabels);
        }

        // Update the note labels
        Label[] newNoteLabels = new Label[maxNoteNumber - minNoteNumber + 1];

        for (int i = minNoteNumber; i <= maxNoteNumber; i++) {
            // Get the note's text
            String note = UnitConversion.noteNumberToNote(i, fancySharps);

            // Calculate the height to move the pointer to
            double placementHeight = PlottingHelpers.noteNumToHeight(i, minNoteNumber, maxNoteNumber, height);

            // Create the label
            Label noteLabel = new Label(note);
            noteLabel.setFont(LABEL_FONT);
            noteLabel.setTextFill(Color.BLACK);

            // Check if this note is one of the notes in the key
            if (noteOffsets.contains(i % 12)) {
                noteLabel.setUnderline(true);
            }

            // Position the label correctly
            noteLabel.setTranslateY(placementHeight - 0.675 * LABEL_FONT.getSize());  // Magical 0.675 constant

            // Make the label centred
            noteLabel.setPrefWidth(width);
            noteLabel.setAlignment(Pos.TOP_CENTER);

            // Add the label to the new note label array
            newNoteLabels[i - minNoteNumber] = noteLabel;
        }

        // Place the notes onto the pane
        notePane.getChildren().addAll(newNoteLabels);

        // Return the new note labels
        return newNoteLabels;
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
     * @param oldBeatsPerBar  Old number of beats per bar.
     * @param newBeatsPerBar  New number of beats per bar.
     * @param pxPerSecond     Number of pixels dedicated per second.
     * @param zoomScaleX      Zoom scaling for the X direction.
     * @return Array of <code>Line</code> objects, representing the <b>new</b> beat lines that are
     * shown.
     */
    public static Line[] updateBeatLines(
            Pane spectrogramPane, Line[] lines, double duration, int oldBPM, int newBPM, double oldOffset,
            double newOffset, double height, int oldBeatsPerBar, int newBeatsPerBar, int pxPerSecond, double zoomScaleX
    ) {
        // Return prematurely if the olds equal the news (b/c nothing to update)
        if (oldBPM == newBPM && oldOffset == newOffset && oldBeatsPerBar == newBeatsPerBar) return lines;

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

            // Determine new line colour
            newLines[beatNum].setStroke(beatNum % newBeatsPerBar != 0 ? BEAT_LINE_COLOUR : BAR_LINE_COLOUR);

            // Try and add again
            try {
                spectrogramPane.getChildren().add(newLines[beatNum]);
            } catch (Exception ignored) {
            }
        }

        // Add/remove lines if needed
        if (deltaNumBeats > 0) {
            for (int beatNum = numCopiedBeatLines; beatNum <= newNumBeats; beatNum++) {
                // Generate the beat line
                Line beatLine = generateBeatLine(
                        beatNum, newBeatsPerBar, pxPerSecond, height, zoomScaleX, newSPB, newOffset
                );

                // Add line to array
                newLines[beatNum] = beatLine;
                spectrogramPane.getChildren().add(beatLine);
            }

        } else {
            for (int beatNum = newNumBeats; beatNum <= oldNumBeats; beatNum++) {
                spectrogramPane.getChildren().remove(lines[beatNum]);
            }
        }

        // Return the new lines
        return newLines;
    }

    /**
     * Method that returns the ellipses to be drawn on the canvas.<br>
     * Note that this returns an array of <code>StackPanes</code> instead of <code>Ellipse</code>s.
     * This is because the beat number text is superimposed on the ellipse, and a
     * <code>StackPane</code> is needed to handle this properly.
     *
     * @param bpm         Number of beats per minute.
     * @param beatsPerBar Number of beats per bar.
     * @param pxPerSecond Number of pixels dedicated per second.
     * @param height      Spectrogram pane height.
     * @param duration    Duration of the audio.
     * @param offset      Number of seconds to wait before the actual audio <em>starts</em>.
     * @param zoomScaleX  Zoom scaling for the X direction.
     * @return Array of <code>StackPane</code>s, representing the generated ellipses.
     */
    public static StackPane[] getBarNumberEllipses(
            int bpm, int beatsPerBar, int pxPerSecond, double height, double duration, double offset, double zoomScaleX
    ) {
        // Calculate the number of bars
        double spb = secondsPerBeat(bpm);

        int numBeats = (int) Math.ceil(bpm / 60. * duration);
        int numBars = (int) Math.floor((double) numBeats / beatsPerBar) + 1;

        // Generate lines for every beat
        StackPane[] stackPanes = new StackPane[numBars + 1];

        for (int barNum = 0; barNum <= numBars; barNum++) {
            // Generate the ellipse
            StackPane stackPane = generateEllipse(barNum, beatsPerBar, pxPerSecond, height, zoomScaleX, spb, offset);

            // Add the stack pane to the array of all stack panes
            stackPanes[barNum] = stackPane;
        }

        // Return the generated stack panes
        return stackPanes;
    }

    /**
     * Method that adds the bar number ellipses onto the bar number pane.
     *
     * @param barNumberPane Bar number pane.
     * @param ellipses      Ellipses (as <code>StackPane</code> objects) to add.
     */
    public static void addBarNumberEllipses(Pane barNumberPane, StackPane[] ellipses) {
        barNumberPane.getChildren().addAll(ellipses);
    }

    /**
     * Method that updates the existing ellipses, and adds/removes as necessary.
     *
     * @param barNumberPane  Bar number pane.
     * @param ellipses       Original bar number ellipses.
     * @param duration       Duration of the audio.
     * @param oldBPM         Old value for the BPM.
     * @param newBPM         New value for the BPM.
     * @param oldOffset      Old offset value.
     * @param newOffset      New offset value.
     * @param height         Spectrogram pane height.
     * @param oldBeatsPerBar Old number of beats per bar.
     * @param newBeatsPerBar New number of beats per bar.
     * @param pxPerSecond    Number of pixels dedicated per second.
     * @param zoomScaleX     Zoom scaling for the X direction.
     * @return Array of <code>Line</code> objects, representing the <b>new</b> beat lines that are
     * shown.
     */
    public static StackPane[] updateBarNumberEllipses(
            Pane barNumberPane, StackPane[] ellipses, double duration, int oldBPM, int newBPM, double oldOffset,
            double newOffset, double height, int oldBeatsPerBar, int newBeatsPerBar, int pxPerSecond, double zoomScaleX
    ) {
        // Return prematurely if the olds equal the news (b/c nothing to update)
        if (oldBPM == newBPM && oldOffset == newOffset && oldBeatsPerBar == newBeatsPerBar) return ellipses;

        // Calculate the new seconds per beat (SPB)
        double newSPB = secondsPerBeat(newBPM);

        // Calculate the difference between the number of beats needed
        int oldNumBars = ellipses.length - 1;

        double newAbsOffset = Math.abs(newOffset);
        int newNumBars = (int) Math.floor(Math.ceil(newBPM / 60. * (duration + newAbsOffset)) / newBeatsPerBar) + 1;

        int deltaNumBars = newNumBars - oldNumBars;

        // Create a new array with the new ellipses
        StackPane[] newEllipses = new StackPane[newNumBars + 1];

        if (deltaNumBars > 0) {  // Need to add ellipses
            // Array copy old beat lines into the new lines
            System.arraycopy(ellipses, 0, newEllipses, 0, oldNumBars + 1);
        } else {  // Need to remove ellipses
            // Limit the number of lines that are copied
            System.arraycopy(ellipses, 0, newEllipses, 0, newNumBars + 1);
        }

        // Update existing ellipses
        int numCopiedEllipses = Math.min(oldNumBars, newNumBars) + 1;

        for (int barNum = 0; barNum < numCopiedEllipses; barNum++) {
            // Calculate position to place the ellipse
            double pos = (newOffset + barNum * newSPB * newBeatsPerBar) * pxPerSecond * zoomScaleX;

            // Update ellipse position
            newEllipses[barNum].setTranslateX(pos - BAR_NUMBER_ELLIPSE_RADIUS_Y * zoomScaleX);

            // Try and add again
            try {
                barNumberPane.getChildren().add(newEllipses[barNum]);
            } catch (Exception ignored) {
            }
        }

        // Add/remove ellipses if needed
        if (deltaNumBars > 0) {
            for (int barNum = numCopiedEllipses; barNum <= newNumBars; barNum++) {
                // Generate the ellipse
                StackPane stackPane = generateEllipse(
                        barNum, newBeatsPerBar, pxPerSecond, height, zoomScaleX, newSPB, newOffset
                );

                // Add ellipse to array
                newEllipses[barNum] = stackPane;
                barNumberPane.getChildren().add(stackPane);
            }

        } else {
            for (int barNum = newNumBars; barNum <= oldNumBars; barNum++) {
                barNumberPane.getChildren().remove(ellipses[barNum]);
            }
        }

        // Return the new generated ellipses
        return newEllipses;
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
        beatLine.setStroke(beatNum % beatsPerBar != 0 ? BEAT_LINE_COLOUR : BAR_LINE_COLOUR);
        beatLine.setStrokeWidth(BEAT_LINE_WIDTH);

        // Return the line
        return beatLine;
    }

    /**
     * Helper method that generates an ellipse.<br>
     * Note that this generates a <code>StackPane</code> instead of an <code>Ellipse</code> to
     * support adding text on the ellipse.
     *
     * @param barNum      Bar number.
     * @param beatsPerBar Number of beats per bar.
     * @param pxPerSecond Number of pixels dedicated per second.
     * @param height      Spectrogram height.
     * @param zoomScaleX  Zoom scaling for the X direction.
     * @param spb         Seconds per beat.
     * @param offset      Number of seconds to wait before the actual audio <em>starts</em>.
     * @return A <code>StackPane</code> object representing the ellipse.
     */
    static StackPane generateEllipse(
            int barNum, int beatsPerBar, int pxPerSecond, double height, double zoomScaleX, double spb, double offset
    ) {
        // Calculate position to place the ellipse
        double pos = (offset + barNum * spb * beatsPerBar) * pxPerSecond * zoomScaleX;

        // Create the ellipse
        Ellipse ellipse = new Ellipse(BAR_NUMBER_ELLIPSE_RADIUS_Y * zoomScaleX, BAR_NUMBER_ELLIPSE_RADIUS_Y);

        // Format the ellipse
        ellipse.setFill(Color.TRANSPARENT);
        ellipse.setStroke(BAR_NUMBER_ELLIPSE_COLOUR);
        ellipse.setStrokeType(StrokeType.CENTERED);
        ellipse.setStrokeWidth(BAR_NUMBER_ELLIPSE_THICKNESS);

        // Create the bar number text that will go inside the ellipse
        Text barNumText = new Text(Integer.toString(barNum));
        barNumText.setFont(LABEL_FONT);
        barNumText.setFill(LABEL_COLOUR);
        barNumText.setBoundsType(TextBoundsType.VISUAL);

        // Create the `StackPane` that will contain both these things
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(ellipse, barNumText);

        // Move the `StackPane` correctly
        stackPane.setTranslateX(pos - BAR_NUMBER_ELLIPSE_RADIUS_Y * zoomScaleX);
        stackPane.setTranslateY(height / 2 - BAR_NUMBER_ELLIPSE_RADIUS_Y);

        // Return the generated ellipse
        return stackPane;
    }
}
