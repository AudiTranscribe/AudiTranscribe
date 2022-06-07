/*
 * NoteRectangle.java
 *
 * Created on 2022-06-07
 * Updated on 2022-06-07
 *
 * Description: A `StackPane` object that is used to denote a note in the transcription view.
 */

package site.overwrite.auditranscribe.notes;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;

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
    public static boolean canEdit = false;

    private final double duration;
    private final int noteNum;

    private double initXDiff;  // Initial difference between the mouse's x-coordinate and the note's x-coordinate
    private double initYTrans;  // Initial translation of the note's y-coordinate
    private double initYEvent;  // Initial difference between the mouse's y-coordinate and the note's y-coordinate

    private final Rectangle mainRectangle;  // Base rectangle to be used for the note
    private final Region resizingRegion;    // Region that permits resizing

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
        this.resizingRegion = new Region();
        this.mainRectangle = new Rectangle();

        // Update properties of the main rectangle
        this.mainRectangle.widthProperty().bind(this.resizingRegion.prefWidthProperty().subtract(5));
        this.mainRectangle.heightProperty().bind(this.resizingRegion.prefHeightProperty().subtract(5));

        // Update the nodes' style classes
        this.resizingRegion.getStyleClass().add("note-resizing-region");
        this.mainRectangle.getStyleClass().add("note-main-rectangle");

        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;

        // Calculate the x-coordinate of the note rectangle and the width of the note rectangle
        double rectWidth = duration * pixelsPerSecond;
        double xCoord = timeToPlaceRect * pixelsPerSecond;

        // Calculate y-coordinate and height to place the rectangle
        double rectHeight = PlottingHelpers.getHeightDifference(spectrogramHeight, minNoteNum, maxNoteNum);
        double yCoord = PlottingHelpers.noteNumToHeight(noteNum, minNoteNum, maxNoteNum, spectrogramHeight) -
                rectHeight / 2;

        // Now set the region's attributes
        this.resizingRegion.setPrefWidth(rectWidth);
        this.resizingRegion.setPrefHeight(rectHeight);

        // Update the stack pane
        this.getChildren().addAll(this.resizingRegion, this.mainRectangle);
        this.setTranslateX(xCoord);
        this.setTranslateY(yCoord);

        // Set cursor handlers on hover
        this.mainRectangle.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && newValue) this.setCursor(Cursor.OPEN_HAND);
        });

        // Set mouse events for the main rectangle
        EventHandler<ScrollEvent> cancelScroll = Event::consume;

        // Todo: disable dragging off the edge of the spectrogram (i.e. exceeding x = 0 and max x as well as y = 0 and max y)
        this.mainRectangle.setOnMouseDragged(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Set cursor
                this.setCursor(Cursor.CLOSED_HAND);

                // Move the note rectangle horizontally
                this.setTranslateX(event.getSceneX() - initXDiff);

                // If the difference in Y coordinates are greater than the height of the note rectangle,
                // then move the note rectangle vertically
                double diffY = event.getSceneY() - initYEvent;
                int numIncrements = (int) (diffY / rectHeight);
                this.setTranslateY(numIncrements * rectHeight + initYTrans);

                // Prevent default scrolling action
                event.consume();
            }
        });

        this.mainRectangle.setOnMousePressed(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Set initial values
                initXDiff = event.getSceneX() - this.getTranslateX();

                initYTrans = this.getTranslateY();
                initYEvent = event.getSceneY();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Prevent default action
                event.consume();
            }
        });

        this.mainRectangle.setOnMouseReleased(event -> {
            // Remove the scroll cancelling effect
            this.getParent().removeEventHandler(ScrollEvent.ANY, cancelScroll);

            // Revert cursor
            if (canEdit) this.setCursor(Cursor.OPEN_HAND);
        });
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

    public static void setCanEdit(boolean canEdit) {
        NoteRectangle.canEdit = canEdit;
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
        double rectWidth = resizingRegion.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        this.setTranslateX(xCoord);
        this.resizingRegion.setPrefWidth(rectWidth);
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
        double rectWidth = resizingRegion.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        this.resizingRegion.setPrefWidth(rectWidth);
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
