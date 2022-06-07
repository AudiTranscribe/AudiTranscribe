/*
 * NoteRectangle.java
 *
 * Created on 2022-06-07
 * Updated on 2022-06-07
 *
 * Description: A `StackPane` object that is used to denote a note in the transcription view.
 */

package site.overwrite.auditranscribe.notes;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

public class NoteRectangle extends StackPane {
    // Attributes
    public static double spectrogramWidth;
    public static double spectrogramHeight;
    public static int minNoteNum;
    public static int maxNoteNum;
    public static double totalDuration;

    public static NotePlayer notePlayer;
    public static int onVelocity;
    public static int offVelocity;
    public static double offDuration;  // In seconds

    public static double pixelsPerSecond = Double.NaN;

    private final double duration;
    private final int noteNum;

    private final Region region;  // Base region to be used for the note

    /**
     * Initialization method for a <code>NoteRectangle</code> object.
     *
     * @param timeToPlaceRect The time (in seconds) at which the <b>start</b> of the note should be
     *                        placed.
     * @param duration        The duration (in seconds) of the note.
     * @param noteNum         The note number of the note.
     */
    public NoteRectangle(double timeToPlaceRect, double duration, int noteNum) {
        // Update attributes
        this.duration = duration;
        this.noteNum = noteNum;

        // Define the nodes
        this.region = new Region();  // Region to show where the note rectangle is
        Label noteLabel = new Label();  // Label to show what note this is

        // Make the text label's width and height follow the note rectangle's width and height
        noteLabel.prefWidthProperty().bind(region.widthProperty().subtract(3));
        noteLabel.prefHeightProperty().bind(region.heightProperty());
        noteLabel.setPadding(new Insets(0, 0, 0, 3));  // 3px left padding

        // Update the nodes' style classes
        this.region.getStyleClass().add("note-rectangle");
        noteLabel.getStyleClass().add("note-label");

        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;

        // Calculate the x-coordinate of the note rectangle and the width of the note rectangle
        double xCoord = timeToPlaceRect * pixelsPerSecond;
        double rectWidth = duration * pixelsPerSecond;

        // Calculate y-coordinate and height to place the rectangle
        double yCoord = PlottingHelpers.noteNumToHeight(noteNum, minNoteNum, maxNoteNum, spectrogramHeight) -
                PlottingHelpers.getHeightDifference(spectrogramHeight, minNoteNum, maxNoteNum) / 2;
        double rectHeight = PlottingHelpers.getHeightDifference(spectrogramHeight, minNoteNum, maxNoteNum);

        // Now set the region's attributes
        this.region.setPrefWidth(rectWidth);
        this.region.setPrefHeight(rectHeight);

        // Update the duration label
        noteLabel.setText(UnitConversionUtils.noteNumberToNote(noteNum, true));

        // Update the stack pane
        this.getChildren().addAll(this.region, noteLabel);
        this.setTranslateX(xCoord);
        this.setTranslateY(yCoord);
    }

    // Setter methods
    public static void setSpectrogramWidth(double spectrogramWidth) {
        NoteRectangle.spectrogramWidth = spectrogramWidth;
    }

    public static void setSpectrogramHeight(double spectrogramHeight) {
        NoteRectangle.spectrogramHeight = spectrogramHeight;
    }

    public static void setMinNoteNum(int minNoteNum) {
        NoteRectangle.minNoteNum = minNoteNum;
    }

    public static void setMaxNoteNum(int maxNoteNum) {
        NoteRectangle.maxNoteNum = maxNoteNum;
    }

    public static void setTotalDuration(double totalDuration) {
        NoteRectangle.totalDuration = totalDuration;
    }

    public static void setNotePlayer(NotePlayer notePlayer) {
        NoteRectangle.notePlayer = notePlayer;
    }

    public static void setOnVelocity(int onVelocity) {
        NoteRectangle.onVelocity = onVelocity;
    }

    public static void setOffVelocity(int offVelocity) {
        NoteRectangle.offVelocity = offVelocity;
    }

    public static void setOffDuration(double offDuration) {
        NoteRectangle.offDuration = offDuration;
    }

    // Public methods

    /**
     * Lengthen the note rectangle from the left (i.e. right-most side's position is unchanged).
     *
     * @param durationOfNote The duration (in seconds) of the note.
     */
    public void extendWidthFromLeft(double durationOfNote) {
        // Calculate the pixels per second for the spectrogram, if not set already
        if (Double.isNaN(pixelsPerSecond)) pixelsPerSecond = spectrogramWidth / totalDuration;

        // Simultaneously update the x-coordinate and width of rectangle
        double xCoord = this.getTranslateX() - durationOfNote * pixelsPerSecond;
        double rectWidth = region.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        this.setTranslateX(xCoord);
        this.region.setPrefWidth(rectWidth);
    }

    /**
     * Shorten the note rectangle from the right (i.e. left-most side's position is unchanged).
     *
     * @param durationOfNote The duration (in seconds) of the note.
     */
    public void extendWidthFromRight(double durationOfNote) {
        // Calculate the pixels per second for the spectrogram, if not set already
        if (Double.isNaN(pixelsPerSecond)) pixelsPerSecond = spectrogramWidth / totalDuration;

        // Update width of rectangle
        double rectWidth = region.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        this.region.setPrefWidth(rectWidth);
    }

    /**
     * Method that plays the note that is defined by this note rectangle.
     */
    public void playNote() {
        notePlayer.playNoteForDuration(
                this.noteNum, onVelocity, offVelocity, (long) duration * 1000,
                (long) offDuration * 1000
        );
    }
}
