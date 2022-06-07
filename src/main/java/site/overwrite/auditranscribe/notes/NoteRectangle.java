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
    // Constants
    private static final double BORDER_WIDTH = 3;  // In pixels
    private static final double EXTEND_REGIONS_WIDTH = 8;  // In pixels

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

    private final Region bordersRegion;    // Region for the borders
    private final Rectangle mainRectangle;  // Base rectangle to be used for the note
    private final Region resizeLeftRegion;  // Region that permits left-side resizing
    private final Region resizeRightRegion;  // Region that permits right-side resizing

    private final double rectangleWidth, rectangleHeight;

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
        bordersRegion = new Region();
        mainRectangle = new Rectangle();
        resizeLeftRegion = new Region();
        resizeRightRegion = new Region();

        // Update properties of the main rectangle
        mainRectangle.widthProperty().bind(bordersRegion.prefWidthProperty().subtract(BORDER_WIDTH));
        mainRectangle.heightProperty().bind(bordersRegion.prefHeightProperty().subtract(BORDER_WIDTH));

        // Update the nodes' style classes
        bordersRegion.getStyleClass().add("note-borders-region");
        mainRectangle.getStyleClass().add("note-main-rectangle");

        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;

        // Calculate the x-coordinate of the note rectangle and the width of the note rectangle
        rectangleWidth = duration * pixelsPerSecond;
        double xCoord = timeToPlaceRect * pixelsPerSecond;

        // Calculate y-coordinate and height to place the rectangle
        rectangleHeight = PlottingHelpers.getHeightDifference(spectrogramHeight, minNoteNum, maxNoteNum);
        double yCoord = PlottingHelpers.noteNumToHeight(noteNum, minNoteNum, maxNoteNum, spectrogramHeight) -
                rectangleHeight / 2;

        // Set the borders' region's attributes
        bordersRegion.setPrefWidth(rectangleWidth);
        bordersRegion.setPrefHeight(rectangleHeight);

        // Update properties of the resizing regions
        resizeLeftRegion.setTranslateX(-rectangleWidth / 2);
        resizeLeftRegion.prefHeightProperty().bind(bordersRegion.prefHeightProperty());
        resizeLeftRegion.setPrefWidth(EXTEND_REGIONS_WIDTH);
        resizeLeftRegion.setMaxWidth(EXTEND_REGIONS_WIDTH);

        resizeRightRegion.setTranslateX(rectangleWidth / 2);
        resizeRightRegion.prefHeightProperty().bind(bordersRegion.prefHeightProperty());
        resizeRightRegion.setPrefWidth(EXTEND_REGIONS_WIDTH);
        resizeRightRegion.setMaxWidth(EXTEND_REGIONS_WIDTH);

        // Update the stack pane
        this.getChildren().addAll(bordersRegion, mainRectangle, resizeLeftRegion, resizeRightRegion);
        this.setTranslateX(xCoord);
        this.setTranslateY(yCoord);

        // Set cursor handlers on hover
        mainRectangle.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && newValue) {
                this.setCursor(Cursor.OPEN_HAND);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        resizeLeftRegion.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && newValue) {
                this.setCursor(Cursor.W_RESIZE);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        resizeRightRegion.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && newValue) {
                this.setCursor(Cursor.E_RESIZE);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        // Set mouse events for the main rectangle
        EventHandler<ScrollEvent> cancelScroll = Event::consume;  // To handle disabling/enabling of scrolling

        mainRectangle.setOnMouseDragged(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Set cursor
                this.setCursor(Cursor.CLOSED_HAND);

                // Move the note rectangle horizontally if it is within range
                double newX = event.getSceneX() - initXDiff;

                if (newX >= 0 && newX + rectangleWidth <= spectrogramWidth) {
                    this.setTranslateX(event.getSceneX() - initXDiff);
                }

                // If the difference in Y coordinates are greater than the height of the note rectangle,
                // then move the note rectangle vertically
                double diffY = event.getSceneY() - initYEvent;
                int numIncrements = (int) (diffY / rectangleHeight);
                double newY = numIncrements * rectangleHeight + initYTrans;

                if (newY >= 0 && newY + rectangleHeight <= spectrogramHeight) {
                    this.setTranslateY(newY);
                }

                // Prevent default scrolling action
                event.consume();
            }
        });

        mainRectangle.setOnMousePressed(event -> {
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

        mainRectangle.setOnMouseReleased(event -> {
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
     * Change the note rectangle's width from the left (i.e. right-most side's position is
     * unchanged).
     *
     * @param durationOfNote The duration (in seconds) of the note.
     */
    public void changeWidthFromLeft(double durationOfNote) {
        // Calculate the pixels per second for the spectrogram, if not set already
        if (Double.isNaN(pixelsPerSecond)) pixelsPerSecond = spectrogramWidth / totalDuration;

        // Simultaneously update the x-coordinate and width of rectangle
        double xCoord = this.getTranslateX() - durationOfNote * pixelsPerSecond;
        double rectWidth = bordersRegion.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        setTranslateX(xCoord);
        bordersRegion.setPrefWidth(rectWidth);
    }

    /**
     * Change the note rectangle's width from the right (i.e. left-most side's position is
     * unchanged).
     *
     * @param durationOfNote The duration (in seconds) of the note.
     */
    public void changeWidthFromRight(double durationOfNote) {
        // Calculate the pixels per second for the spectrogram, if not set already
        if (Double.isNaN(pixelsPerSecond)) pixelsPerSecond = spectrogramWidth / totalDuration;

        // Update width of rectangle
        double rectWidth = bordersRegion.getWidth() + durationOfNote * pixelsPerSecond;

        // Update the rectangle's attributes
        bordersRegion.setPrefWidth(rectWidth);
    }

    /**
     * Method that plays the note that is defined by this note rectangle.
     */
    public void playNote() {
        notePlayer.playNoteForDuration(
                noteNum, onVelocity, offVelocity, (long) duration * 1000,
                (long) offDuration * 1000
        );
    }
}
