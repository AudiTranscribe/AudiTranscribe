/*
 * NoteRectangle.java
 * Description: A `StackPane` object that is used to denote a note in the transcription view.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.music.notes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import site.overwrite.auditranscribe.exceptions.notes.NoteRectangleCollisionException;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.misc.tuples.Pair;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public class NoteRectangle extends StackPane {
    // Constants
    private static final double BORDER_WIDTH = 3;  // In pixels
    private static final double RESIZING_REGIONS_WIDTH = 8;  // In pixels

    // Static attributes
    public static List<NoteRectangle> allNoteRectangles = new ArrayList<>();

    public static List<ObservableList<NoteRectangle>> noteRectanglesByNoteNumber = new ArrayList<>();
    public static List<SortedList<NoteRectangle>> sortedNoteRectanglesByNoteNumber;

    public static double spectrogramWidth;
    public static double spectrogramHeight;
    public static int minNoteNum;
    public static int maxNoteNum;
    public static double totalDuration;

    public static boolean isPaused = true;
    public static boolean canEdit = false;
    public static boolean isEditing = false;

    // Instance attributes
    public int noteNum;
    private boolean isRemoved = false;

    public final DoubleProperty noteOnsetTime;
    public final DoubleProperty noteDuration;

    private final DoubleProperty rectangleWidth;
    private final double rectangleHeight;  // The rectangle's height is fixed

    private final Region bordersRegion;  // Region that shows the borders of the note rectangle

    // Helper attributes
    // (Note: "initial" refers to initial value before *resizing*, not when the object is created)
    private double initXTrans; // Initial x-translation of the note
    private double initXDiff;  // Initial difference between the mouse's x-coordinate and the note's x-coordinate

    private double initYTrans;  // Initial y-translation of the note
    private double initYEvent;  // Initial difference between the mouse's y-coordinate and the note's y-coordinate

    private double initWidth;  // Initial width of the note
    private int initNoteNum;  // Initial note number

    private NoteRectangle leftBoundingRectangle;
    private NoteRectangle rightBoundingRectangle;

    /**
     * Initialization method for a <code>NoteRectangle</code> object.<br>
     * Expects all required static values to be set.
     *
     * @param timeToPlaceRect The time (in seconds) at which the <b>start</b> of the note should be
     *                        placed.
     * @param noteDuration    The duration (in seconds) of the note.
     * @param noteNum         The note number of the note.
     * @throws NoteRectangleCollisionException If the creation of this note rectangle would cause a
     *                                         collision with another note rectangle.
     */
    public NoteRectangle(
            double timeToPlaceRect, double noteDuration, int noteNum
    ) throws NoteRectangleCollisionException {
        // Calculate the pixels per second for the spectrogram
        double pixelsPerSecond = spectrogramWidth / totalDuration;
        double secondsPerPixel = totalDuration / spectrogramWidth;

        // Calculate the initial width of the rectangle
        double initialRectangleWidth = noteDuration * pixelsPerSecond;

        // Update non-property attributes
        this.noteNum = noteNum;
        this.rectangleHeight = PlottingHelpers.getHeightDifference(spectrogramHeight, minNoteNum, maxNoteNum);

        // Define and bind properties
        this.noteOnsetTime = new SimpleDoubleProperty();
        this.noteDuration = new SimpleDoubleProperty();
        this.rectangleWidth = new SimpleDoubleProperty();

        this.noteOnsetTime.bind(this.translateXProperty().multiply(secondsPerPixel));
        this.noteDuration.bind(this.widthProperty().multiply(secondsPerPixel));
        this.rectangleWidth.bind(this.widthProperty());

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

        resizeLeftRegion.getStyleClass().add("note-resizing-region");
        resizeRightRegion.getStyleClass().add("note-resizing-region");

        // Calculate the x-coordinate of the note rectangle
        double xCoord = timeToPlaceRect * pixelsPerSecond;

        // Calculate the y-coordinate of the note rectangle
        double yCoord = PlottingHelpers.noteNumToHeight(noteNum, minNoteNum, maxNoteNum, spectrogramHeight) -
                rectangleHeight / 2;

        // Check for collision
        if (checkCollision(xCoord, initialRectangleWidth, noteNum, VerticalMovement.NONE) != CollisionLocation.NONE) {
            MyLogger.log(
                    Level.FINE,
                    "Note rectangle collision detected; not placing note",
                    this.getClass().toString()
            );
            throw new NoteRectangleCollisionException("Note rectangle collision detected; not placing note");
        }

        // Set the borders' region's attributes
        bordersRegion.setPrefWidth(initialRectangleWidth);
        bordersRegion.setPrefHeight(rectangleHeight);

        // Update properties of the resizing regions
        resizeLeftRegion.translateXProperty().bind(bordersRegion.widthProperty().divide(-2));
        resizeLeftRegion.prefHeightProperty().bind(bordersRegion.prefHeightProperty());
        resizeLeftRegion.setPrefWidth(RESIZING_REGIONS_WIDTH);
        resizeLeftRegion.setMaxWidth(RESIZING_REGIONS_WIDTH);

        resizeRightRegion.translateXProperty().bind(bordersRegion.widthProperty().divide(2));
        resizeRightRegion.prefHeightProperty().bind(bordersRegion.prefHeightProperty());
        resizeRightRegion.setPrefWidth(RESIZING_REGIONS_WIDTH);
        resizeRightRegion.setMaxWidth(RESIZING_REGIONS_WIDTH);

        // Update the stack pane
        this.getChildren().addAll(bordersRegion, mainRectangle, resizeLeftRegion, resizeRightRegion);
        this.setTranslateX(xCoord);
        this.setTranslateY(yCoord);

        // Set cursor handlers on hover
        mainRectangle.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && isPaused && newValue) {
                this.setCursor(Cursor.OPEN_HAND);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        resizeLeftRegion.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && isPaused && newValue) {
                this.setCursor(Cursor.W_RESIZE);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        resizeRightRegion.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && isPaused && newValue) {
                this.setCursor(Cursor.E_RESIZE);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        // Define scroll event disabler
        EventHandler<ScrollEvent> cancelScroll = Event::consume;  // To be used so that we can remove this handler

        // Set mouse events for the main rectangle
        mainRectangle.setOnMousePressed(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Remove rectangle from the note rectangle by number lists
                noteRectanglesByNoteNumber.get(this.noteNum).remove(this);

                // Determine if the secondary mouse button was pressed
                if (event.isSecondaryButtonDown()) {
                    // Remove the note rectangle from the parent pane
                    ((Pane) this.getParent()).getChildren().remove(this);

                    // Remove the note rectangle from the list of note rectangles
                    allNoteRectangles.remove(this);

                    // Update the `isRemoved` flag
                    isRemoved = true;
                } else {
                    // Set initial values
                    initXTrans = this.getTranslateX();
                    initXDiff = event.getSceneX() - initXTrans;

                    initYTrans = this.getTranslateY();
                    initYEvent = event.getSceneY();

                    initNoteNum = this.noteNum;

                    // Disable scrolling
                    this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                    // Prevent default action
                    event.consume();
                }

                // Update the `isEditing` flag
                isEditing = true;
            }
        });

        mainRectangle.setOnMouseDragged(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Set cursor
                this.setCursor(Cursor.CLOSED_HAND);

                // Calculate new X position
                double newX = event.getSceneX() - initXDiff;

                // Calculate number of rectangles' heights to shift the note and calculate new note number
                double diffY = event.getSceneY() - initYEvent;
                int numIncrements = (int) (diffY / rectangleHeight);
                int changeInNoteNumber = -numIncrements;  // Higher Y -> Lower on screen => need to negate
                double newY = numIncrements * rectangleHeight + initYTrans;
                int newNoteNum = initNoteNum + changeInNoteNumber;

                // Determine vertical movement from where the rectangle is CURRENTLY at
                VerticalMovement verticalMovement;
                if (newNoteNum == this.noteNum) {
                    verticalMovement = VerticalMovement.NONE;
                } else if (newNoteNum > this.noteNum) {
                    verticalMovement = VerticalMovement.UP;
                } else {
                    verticalMovement = VerticalMovement.DOWN;
                }

                // Check for collision
                CollisionLocation collisionLoc = checkCollision(
                        newX, getRectangleWidth(), newNoteNum, verticalMovement
                );

                if (collisionLoc == CollisionLocation.LEFT) {  // Collided with left rectangle
                    // Move current rectangle to the right edge of the left rectangle
                    this.setTranslateX(leftBoundingRectangle.getEndX());
                } else if (collisionLoc == CollisionLocation.RIGHT) {  // Collided with right rectangle
                    // Move current rectangle to the left edge of the right rectangle
                    this.setTranslateX(rightBoundingRectangle.getStartX() - getRectangleWidth());
                } else {
                    // Permit horizontal movement if within range
                    if (newX >= 0 && newX + getRectangleWidth() <= spectrogramWidth) {
                        this.setTranslateX(newX);
                    }
                }

                if (collisionLoc != CollisionLocation.UP && collisionLoc != CollisionLocation.DOWN) {
                    // Permit vertical movement if within range
                    if (newY >= 0 && newY + rectangleHeight <= spectrogramHeight) {
                        this.setTranslateY(newY);
                        this.noteNum = newNoteNum;
                    }
                }

                // Prevent default scrolling action
                event.consume();
            }
        });

        mainRectangle.setOnMouseReleased(event -> {
            // Remove the scroll cancelling effect (if it still exists)
            if (this.getParent() != null) {
                this.getParent().removeEventHandler(ScrollEvent.ANY, cancelScroll);
            }

            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Revert cursor
                this.setCursor(Cursor.OPEN_HAND);

                // Run only if the note rectangle was not removed
                if (!isRemoved) {
                    // Update the note rectangles' list
                    noteRectanglesByNoteNumber.get(this.noteNum).add(this);

                    // Unset the bounding rectangles
                    setBoundingRectangles(null, null);
                }

                // Update the `isEditing` flag
                isEditing = false;
            }

            MyLogger.log(
                    Level.FINE,
                    "Moved rectangle to " + getNoteOnsetTime() + " seconds with note number " + this.noteNum,
                    this.getClass().toString());
        });

        // Set mouse events for the resizing regions
        resizeLeftRegion.setOnMousePressed(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Remove rectangle from the note rectangle by number lists
                noteRectanglesByNoteNumber.get(this.noteNum).remove(this);

                // Set initial values
                initXTrans = this.getTranslateX();
                initXDiff = event.getSceneX() - this.getTranslateX();
                initWidth = bordersRegion.getPrefWidth();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Update the `isEditing` flag
                isEditing = true;

                // Prevent default action
                event.consume();
            }
        });

        resizeLeftRegion.setOnMouseDragged(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Get the new X position
                double newX = event.getSceneX() - initXDiff;

                // Calculate new width of the rectangle
                double newWidth = initWidth + (initXTrans - newX);

                // Check if collision will occur
                CollisionLocation collisionLoc = checkCollision(newX, newWidth, this.noteNum, VerticalMovement.NONE);
                if (collisionLoc == CollisionLocation.NONE) {
                    // If the new width is at least the resizing regions' width then resize
                    if (newWidth >= RESIZING_REGIONS_WIDTH) {
                        this.setTranslateX(newX);
                        bordersRegion.setPrefWidth(newWidth);
                    }
                }

                // Prevent default action
                event.consume();
            }
        });

        resizeLeftRegion.setOnMouseReleased(event -> {
            // Remove the scroll cancelling effect (if it still exists)
            if (this.getParent() != null) {
                this.getParent().removeEventHandler(ScrollEvent.ANY, cancelScroll);
            }

            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Run only if the note rectangle was not removed
                if (!isRemoved) {
                    // Add rectangle back into the note rectangle by number lists
                    noteRectanglesByNoteNumber.get(this.noteNum).add(this);

                    // Unset the bounding rectangles
                    setBoundingRectangles(null, null);
                }

                // Update the `isEditing` flag
                isEditing = false;
            }
        });

        resizeRightRegion.setOnMousePressed(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Remove rectangle from the note rectangle by number lists
                noteRectanglesByNoteNumber.get(this.noteNum).remove(this);

                // Set initial values
                initXTrans = this.getTranslateX();
                initXDiff = event.getSceneX() - this.getTranslateX();
                initWidth = bordersRegion.getPrefWidth();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Update the `isEditing` flag
                isEditing = true;

                // Prevent default action
                event.consume();
            }
        });

        resizeRightRegion.setOnMouseDragged(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Get the new X position
                double newX = event.getSceneX() - initXDiff;

                // Update the width of the note rectangle
                double newWidth = initWidth + (newX - initXTrans);

                // Check if collision will occur
                CollisionLocation collisionLoc = checkCollision(newX, newWidth, this.noteNum, VerticalMovement.NONE);
                if (collisionLoc == CollisionLocation.NONE) {
                    // If the new width is at least the resizing regions' width then resize
                    if (newWidth >= RESIZING_REGIONS_WIDTH) {
                        bordersRegion.setPrefWidth(newWidth);
                    }
                }

                // Prevent default action
                event.consume();
            }
        });

        resizeRightRegion.setOnMouseReleased(event -> {
            // Remove the scroll cancelling effect (if it still exists)
            if (this.getParent() != null) {
                this.getParent().removeEventHandler(ScrollEvent.ANY, cancelScroll);
            }

            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Run only if the note rectangle was not removed
                if (!isRemoved) {
                    // Add rectangle back into the note rectangle by number lists
                    noteRectanglesByNoteNumber.get(this.noteNum).add(this);

                    // Unset the bounding rectangles
                    setBoundingRectangles(null, null);
                }

                // Update the `isEditing` flag
                isEditing = false;
            }
        });

        // Add this new note rectangle into the note rectangles' lists
        allNoteRectangles.add(this);
        noteRectanglesByNoteNumber.get(noteNum).add(this);
    }

    // Getter/Setter methods
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

    public static void setCanEdit(boolean canEdit) {
        NoteRectangle.canEdit = canEdit;
    }

    public static void setIsPaused(boolean isPaused) {
        NoteRectangle.isPaused = isPaused;
    }

    public double getNoteOnsetTime() {
        return noteOnsetTime.get();
    }

    public double getNoteDuration() {
        return noteDuration.get();
    }

    public double getRectangleWidth() {
        return rectangleWidth.get();
    }

    public double getStartX() {
        return this.getTranslateX();
    }

    public double getEndX() {
        return this.getStartX() + this.getRectangleWidth();
    }

    // Public methods

    /**
     * Defines the note rectangles by note number lists.
     *
     * @param numNoteNumbers Number of note numbers. Assumes the lowest note number is 0.
     */
    public static void defineNoteRectanglesByNoteNumberLists(int numNoteNumbers) {
        // Define lists of note rectangles by note numbers
        NoteRectangle.noteRectanglesByNoteNumber = new ArrayList<>(numNoteNumbers);
        NoteRectangle.sortedNoteRectanglesByNoteNumber = new ArrayList<>(numNoteNumbers);

        // Update the contents of the lists
        for (int i = 0; i < numNoteNumbers; i++) {
            NoteRectangle.noteRectanglesByNoteNumber.add(FXCollections.observableArrayList());
            NoteRectangle.sortedNoteRectanglesByNoteNumber.add(
                    new SortedList<>(NoteRectangle.noteRectanglesByNoteNumber.get(i), new SortByTimeToPlace())
            );
        }
    }

    // Private methods

    /**
     * Helper method that sets the bounding rectangles.
     */
    private void setBoundingRectangles(NoteRectangle leftBoundingRectangle, NoteRectangle rightBoundingRectangle) {
        this.leftBoundingRectangle = leftBoundingRectangle;
        this.rightBoundingRectangle = rightBoundingRectangle;
    }

    /**
     * Helper method that checks if the proposed X position, width, and note number will cause a
     * collision with another note rectangle.
     *
     * @param xPos             X position.
     * @param rectangleWidth   Width of the rectangle.
     * @param noteNumber       Note number.
     * @param verticalMovement Vertical movement value.
     * @return A <code>CollisionLocation</code> value, describing the <em>direction</em> of the collision, or
     * <code>NONE</code> if there was no collision.
     */
    private CollisionLocation checkCollision(
            double xPos, double rectangleWidth, int noteNumber, VerticalMovement verticalMovement
    ) {
        // Determine bounding rectangles
        NoteRectangle leftBounder;
        NoteRectangle rightBounder;
        if ((leftBoundingRectangle == null && rightBoundingRectangle == null) ||
                (leftBoundingRectangle != null && xPos + rectangleWidth < leftBoundingRectangle.getStartX()) ||
                (rightBoundingRectangle != null && xPos > rightBoundingRectangle.getEndX()) ||
                verticalMovement != VerticalMovement.NONE) {
            // Recalculate the bounding rectangles and use the new ones
            Pair<NoteRectangle, NoteRectangle> rectangles = getLeftAndRightRectangles(xPos, noteNumber);
            leftBounder = rectangles.value0();
            rightBounder = rectangles.value1();
        } else {
            // No changes made; use the old ones
            leftBounder = leftBoundingRectangle;
            rightBounder = rightBoundingRectangle;
        }

        // Handle edge cases
        if (leftBounder == null && rightBounder == null) {
            // No rectangles present at all (other than itself) => no collision
            setBoundingRectangles(null, null);
            return CollisionLocation.NONE;

        } else if (leftBounder == null) {  // No left rectangle
            // If the end of this rectangle is before the start of the right rectangle, then there is no collision
            if (xPos + rectangleWidth >= rightBounder.getStartX()) {
                if (verticalMovement == VerticalMovement.NONE) {  // No vertical movement
                    return CollisionLocation.RIGHT;  // Collides with the rectangle on the right
                } else {
                    if (verticalMovement == VerticalMovement.UP) return CollisionLocation.UP;
                    return CollisionLocation.DOWN;
                }
            } else {
                setBoundingRectangles(null, rightBounder);
                return CollisionLocation.NONE;
            }

        } else if (rightBounder == null) {  // No right rectangle
            // If the start of this rectangle is after the end of the left rectangle, then there is no collision
            if (xPos <= leftBounder.getEndX()) {
                if (verticalMovement == VerticalMovement.NONE) {  // No vertical movement
                    return CollisionLocation.LEFT;  // Collides with the rectangle on the left
                } else {
                    if (verticalMovement == VerticalMovement.UP) return CollisionLocation.UP;
                    return CollisionLocation.DOWN;
                }
            } else {
                setBoundingRectangles(leftBounder, null);
                return CollisionLocation.NONE;
            }

        } else {
            // Check if start and end of this rectangle lies between the left and right rectangles. If so, there is no
            // collision; otherwise there is a collision.
            CollisionLocation tempCollisionLoc = CollisionLocation.NONE;

            if (leftBounder.getEndX() >= xPos) {
                tempCollisionLoc = CollisionLocation.LEFT;
            } else if (xPos + rectangleWidth >= rightBounder.getStartX()) {
                tempCollisionLoc = CollisionLocation.RIGHT;
            }

            if (tempCollisionLoc == CollisionLocation.NONE) {
                setBoundingRectangles(leftBounder, rightBounder);
                return CollisionLocation.NONE;
            }

            // Otherwise, there was a collision; check if there was vertical movement
            if (verticalMovement == VerticalMovement.UP) return CollisionLocation.UP;
            if (verticalMovement == VerticalMovement.DOWN) return CollisionLocation.DOWN;
            return tempCollisionLoc;
        }
    }

    /**
     * Helper method that gets the left and right bounding rectangles.
     *
     * @param xPos       X position.
     * @param noteNumber Note number.
     * @return A <code>Pair</code>, with the first value being the left bounding rectangle and the
     * second value being the right bounding rectangle. If no bounding rectangle exists, the
     * appropriate value will be <code>null</code>.
     */
    private Pair<NoteRectangle, NoteRectangle> getLeftAndRightRectangles(double xPos, int noteNumber) {
        // Get relevant note rectangles to check
        SortedList<NoteRectangle> relevantRectangles = sortedNoteRectanglesByNoteNumber.get(noteNumber);

        // Get number of relevant rectangles
        int numRelevantRectangles = relevantRectangles.size();

        // Find left and right bounding rectangle indices
        int leftIndex = getLeftSideRectangleIndex(numRelevantRectangles, xPos, relevantRectangles);
        int rightIndex = leftIndex + 1;

        // Get the appropriate rectangles
        NoteRectangle left, right;
        if (leftIndex == -1) {
            left = null;
        } else {
            left = relevantRectangles.get(leftIndex);
        }

        if (rightIndex == numRelevantRectangles) {
            right = null;
        } else {
            right = relevantRectangles.get(rightIndex);
        }

        // Return as a pair
        return new Pair<>(left, right);
    }

    /**
     * Helper method that gets the index of the rectangle that is on the LEFT of the new X position
     * on the row corresponding to the new note number.
     *
     * @param numRelevantRectangles Number of relevant rectangles.
     * @param xPos                  X position.
     * @param relevantRectangles    List of relevant rectangles. This must be a sorted list.
     * @return Index of the rectangle that is on the LEFT of the new X position.
     */
    private int getLeftSideRectangleIndex(
            int numRelevantRectangles, double xPos, SortedList<NoteRectangle> relevantRectangles
    ) {
        // Perform trivial checks
        if (numRelevantRectangles == 0) {
            // No rectangles
            return -1;
        } else if (xPos <= relevantRectangles.get(0).getStartX()) {
            // All rectangles' starting X more than or equal to new X position => no left side rectangle
            return -1;
        } else if (xPos > relevantRectangles.get(numRelevantRectangles - 1).getEndX()) {
            // All rectangles' ending X less than new X position => left side rectangle is last rectangle
            return numRelevantRectangles - 1;
        }

        // Find the last rectangle in the sorted list where its starting X is less than the new X position
        // (Use binary search on the sorted list)
        int left = 0;
        int right = numRelevantRectangles - 1;
        int middle;

        // Perform iterative binary search
        while (left < right) {
            // Calculate middle
            middle = (left + right) / 2;

            // Get middle rectangle's starting X position
            double middleStartingX = relevantRectangles.get(middle).getStartX();

            // Compare 'middle' value with the target value
            if (middleStartingX < xPos) {
                left = middle + 1;
            } else if (middleStartingX == xPos) {
                return middle - 1;  // `middle` is insertion point; we want *left* element's index
            } else {
                right = middle;
            }
        }

        // Return left pointer
        return left - 1;  // `left` is insertion point; we want *left* element's index
    }

    // Overwritten methods
    @Override
    public String toString() {
        return "NoteRectangle{" +
                "noteNum=" + noteNum +
                ", noteOnsetTime=" + getNoteOnsetTime() +
                ", noteDuration=" + getNoteDuration() +
                '}';
    }

    // Helper classes
    static class SortByTimeToPlace implements Comparator<NoteRectangle> {
        @Override
        public int compare(NoteRectangle o1, NoteRectangle o2) {
            return Double.compare(o1.noteOnsetTime.getValue(), o2.noteOnsetTime.getValue());
        }
    }

    enum CollisionLocation {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NONE
    }

    enum VerticalMovement {
        UP,
        DOWN,
        NONE
    }
}
