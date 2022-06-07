/*
 * NoteRectangle.java
 *
 * Created on 2022-06-07
 * Updated on 2022-06-07
 *
 * Description: A `StackPane` object that is used to denote a note in the transcription view.
 */

package site.overwrite.auditranscribe.notes;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;
import site.overwrite.auditranscribe.utils.MathUtils;

public class NoteRectangle extends StackPane {
    // Attributes
    private static double spectrogramWidth;
    private static double spectrogramHeight;
    private static int minNoteNum;
    private static int maxNoteNum;
    private static double totalDuration;

    private static NotePlayer notePlayer;
    private static int onVelocity;
    private static int offVelocity;
    private static double offDuration;  // In seconds

    private final double duration;
    private final int noteNum;

    private final Rectangle rectangle;  // Base rectangle to be used for the note

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
        this.rectangle = new Rectangle();
        // Label to show how long this note will play for, in seconds
        Label durationLabel = new Label();

        // Make the text label's width and height follow the note rectangle's width and height
        durationLabel.prefWidthProperty().bind(rectangle.widthProperty());
        durationLabel.prefHeightProperty().bind(rectangle.heightProperty());

        // Update their style classes
        this.rectangle.getStyleClass().add("note-rectangle");
        durationLabel.getStyleClass().add("note-duration-label");

        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;

        // Calculate the x-coordinate of the note rectangle and the width of the note rectangle
        double xCoord = timeToPlaceRect * pixelsPerSecond;
        double rectWidth = duration * pixelsPerSecond;

        // Determine y-coordinate and height to place the rectangle
        double yCoord = PlottingHelpers.noteNumToHeight(noteNum, minNoteNum, maxNoteNum, spectrogramHeight);
        double rectHeight = PlottingHelpers.getHeightDifference(spectrogramHeight, minNoteNum, maxNoteNum);

        // Now set the rectangle's attributes
        this.rectangle.setX(xCoord);
        this.rectangle.setY(yCoord);
        this.rectangle.setWidth(rectWidth);
        this.rectangle.setHeight(rectHeight);

        // Update the duration label
        durationLabel.setText(MathUtils.round(duration, 2) + "s");
    }

    // Getter/Setter methods

    public static double getSpectrogramWidth() {
        return spectrogramWidth;
    }

    public static void setSpectrogramWidth(double spectrogramWidth) {
        NoteRectangle.spectrogramWidth = spectrogramWidth;
    }

    public static double getSpectrogramHeight() {
        return spectrogramHeight;
    }

    public static void setSpectrogramHeight(double spectrogramHeight) {
        NoteRectangle.spectrogramHeight = spectrogramHeight;
    }

    public static int getMinNoteNum() {
        return minNoteNum;
    }

    public static void setMinNoteNum(int minNoteNum) {
        NoteRectangle.minNoteNum = minNoteNum;
    }

    public static int getMaxNoteNum() {
        return maxNoteNum;
    }

    public static void setMaxNoteNum(int maxNoteNum) {
        NoteRectangle.maxNoteNum = maxNoteNum;
    }

    public static double getTotalDuration() {
        return totalDuration;
    }

    public static void setTotalDuration(double totalDuration) {
        NoteRectangle.totalDuration = totalDuration;
    }

    public static NotePlayer getNotePlayer() {
        return notePlayer;
    }

    public static void setNotePlayer(NotePlayer notePlayer) {
        NoteRectangle.notePlayer = notePlayer;
    }

    public static int getOnVelocity() {
        return onVelocity;
    }

    public static void setOnVelocity(int onVelocity) {
        NoteRectangle.onVelocity = onVelocity;
    }

    public static int getOffVelocity() {
        return offVelocity;
    }

    public static void setOffVelocity(int offVelocity) {
        NoteRectangle.offVelocity = offVelocity;
    }

    public static double getOffDuration() {
        return offDuration;
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
        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;

        // Simultaneously update the x-coordinate and width of rectangle
        double xCoord = rectangle.getX() - durationOfNote * pixelsPerSecond;
        double rectWidth = rectangle.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        this.rectangle.setX(xCoord);
        this.rectangle.setWidth(rectWidth);
    }

    /**
     * Shorten the note rectangle from the right (i.e. left-most side's position is unchanged).
     *
     * @param durationOfNote The duration (in seconds) of the note.
     */
    public void extendWidthFromRight(double durationOfNote) {
        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;

        // Update width of rectangle
        double rectWidth = rectangle.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        this.rectangle.setWidth(rectWidth);
    }

    public void playRectangle() {
        notePlayer.playNoteForDuration(
                this.noteNum, onVelocity, offVelocity, (long) duration * 1000,
                (long) offDuration * 1000
        );
    }
}
