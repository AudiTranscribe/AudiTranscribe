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

    public static boolean canEdit = false;

    private final double duration;
    private final int noteNum;

    private final double rectangleWidth, rectangleHeight;

    private final Region bordersRegion;  // Region that shows the borders of the note rectangle

    // Helper attributes
    // (Note: "initial" refers to initial value before *resizing*, not when the object is created)
    private double initXTrans; // Initial x-translation of the note
    private double initXDiff;  // Initial difference between the mouse's x-coordinate and the note's x-coordinate

    private double initYTrans;  // Initial y-translation of the note
    private double initYEvent;  // Initial difference between the mouse's y-coordinate and the note's y-coordinate

    private double initWidth;  // Initial width of the note before resizing

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
        Rectangle mainRectangle = new Rectangle();  // Base rectangle to be used for the note
        Region resizeLeftRegion = new Region();  // Region that permits left-side resizing
        Region resizeRightRegion = new Region();  // Region that permits right-side resizing

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
        resizeLeftRegion.translateXProperty().bind(bordersRegion.widthProperty().divide(-2));
        resizeLeftRegion.prefHeightProperty().bind(bordersRegion.prefHeightProperty());
        resizeLeftRegion.setPrefWidth(EXTEND_REGIONS_WIDTH);
        resizeLeftRegion.setMaxWidth(EXTEND_REGIONS_WIDTH);

        resizeRightRegion.translateXProperty().bind(bordersRegion.widthProperty().divide(2));
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

        // Define scroll event disabler
        EventHandler<ScrollEvent> cancelScroll = Event::consume;  // To be used so that we can remove this handler

        // Set mouse events for the main rectangle
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

        // Set mouse events for the resizing regions
        resizeLeftRegion.setOnMouseDragged(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Get the new X position
                double newX = event.getSceneX() - initXDiff;

                // Update the width of the note rectangle
                double newWidth = initWidth + (initXTrans - newX);

                if (newWidth >= EXTEND_REGIONS_WIDTH) {
                    this.setTranslateX(newX);
                    bordersRegion.setPrefWidth(newWidth);
                }

                // Prevent default action
                event.consume();
            }
        });

        resizeLeftRegion.setOnMousePressed(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Set initial values
                initXTrans = this.getTranslateX();
                initXDiff = event.getSceneX() - this.getTranslateX();
                initWidth = bordersRegion.getPrefWidth();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Prevent default action
                event.consume();
            }
        });

        resizeLeftRegion.setOnMouseReleased(event -> {
            // Remove the scroll cancelling effect
            this.getParent().removeEventHandler(ScrollEvent.ANY, cancelScroll);
        });

        resizeRightRegion.setOnMouseDragged(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Get the new X position
                double newX = event.getSceneX() - initXDiff;

                // Update the width of the note rectangle
                double newWidth = initWidth + (newX - initXTrans);

                if (newWidth >= EXTEND_REGIONS_WIDTH) {
                    bordersRegion.setPrefWidth(newWidth);
                }

                // Prevent default action
                event.consume();
            }
        });

        resizeRightRegion.setOnMousePressed(event -> {
            // Check if editing is enabled
            if (canEdit) {
                // Set initial values
                initXTrans = this.getTranslateX();
                initXDiff = event.getSceneX() - this.getTranslateX();
                initWidth = bordersRegion.getPrefWidth();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Prevent default action
                event.consume();
            }
        });

        resizeRightRegion.setOnMouseReleased(event -> {
            // Remove the scroll cancelling effect
            this.getParent().removeEventHandler(ScrollEvent.ANY, cancelScroll);
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
     * Method that plays the note that is defined by this note rectangle.
     */
    public void playNote() {
        notePlayer.playNoteForDuration(
                noteNum, onVelocity, offVelocity, (long) duration * 1000,
                (long) offDuration * 1000
        );
    }
}
