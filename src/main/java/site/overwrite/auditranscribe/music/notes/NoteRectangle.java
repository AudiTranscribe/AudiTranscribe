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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.music.exceptions.NoteRectangleCollisionException;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;
import site.overwrite.auditranscribe.utils.MiscUtils;
import site.overwrite.auditranscribe.utils.MusicUtils;

import java.util.*;
import java.util.logging.Level;

public class NoteRectangle extends StackPane {
    // Constants
    private static final double BORDER_WIDTH = 3;  // In pixels
    private static final double RESIZING_REGIONS_WIDTH = 8;  // In pixels

    // Static attributes
    public static Map<String, NoteRectangle> allNoteRectangles = new HashMap<>();

    public static List<ObservableList<NoteRectangle>> noteRectanglesByNoteNumber = new ArrayList<>();
    public static List<SortedList<NoteRectangle>> sortedNoteRectanglesByNoteNumber;

    private static final Stack<Triple<String, UndoOrRedoAction, Double[]>> undoStack = new Stack<>();
    private static final Stack<Triple<String, UndoOrRedoAction, Double[]>> redoStack = new Stack<>();

    public static AnchorPane spectrogramPaneAnchor;
    public static double spectrogramWidth;
    public static double spectrogramHeight;
    public static int minNoteNum;
    public static int maxNoteNum;
    public static double totalDuration;

    public static boolean isPaused = true;
    public static boolean canEdit = false;
    public static boolean isEditing = false;
    public static boolean canUndoOrRedo = true;

    private static boolean hasEditedNoteRectangles = false;

    // Instance attributes
    private String uuid;

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
    private double initYEvent;  // Initial y-coordinate of the event

    private double initWidth;  // Initial width of the note
    private int initNoteNum;  // Initial note number

    private NoteRectangle leftBoundingRectangle;
    private NoteRectangle rightBoundingRectangle;

    private boolean isDragging;

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
        // Determine a UUID for the note rectangle
        this.uuid = MiscUtils.generateUUID((long) MiscUtils.getUnixTimestamp());

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

        // Define nodes
        this.bordersRegion = new Region();

        Rectangle mainRectangle = new Rectangle();  // Base rectangle to be used for the note
        Region resizeLeftRegion = new Region();  // Region that permits left-side resizing
        Region resizeRightRegion = new Region();  // Region that permits right-side resizing

        // Update properties of the main rectangle
        mainRectangle.widthProperty().bind(this.bordersRegion.prefWidthProperty().subtract(BORDER_WIDTH));
        mainRectangle.heightProperty().bind(this.bordersRegion.prefHeightProperty().subtract(BORDER_WIDTH));

        // Update the nodes' style classes
        this.bordersRegion.getStyleClass().add("note-borders-region");

        mainRectangle.getStyleClass().add("note-main-rectangle");

        resizeLeftRegion.getStyleClass().add("note-resizing-region");
        resizeRightRegion.getStyleClass().add("note-resizing-region");

        // Calculate the x-coordinate of the note rectangle
        double xCoord = timeToPlaceRect * pixelsPerSecond;

        // Calculate the y-coordinate of the note rectangle
        double yCoord = PlottingHelpers.noteNumToHeight(noteNum, minNoteNum, maxNoteNum, spectrogramHeight) -
                this.rectangleHeight / 2;

        // Check for collision
        CollisionLocation colLoc = checkCollision(xCoord, initialRectangleWidth, noteNum, VerticalMovement.NONE);

        if (colLoc != CollisionLocation.NONE) {
            MyLogger.log(
                    Level.FINE,
                    "Note rectangle collision detected (" + colLoc + "); not placing note",
                    this.getClass().toString()
            );
            throw new NoteRectangleCollisionException(
                    "Note rectangle collision detected (" + colLoc + "); not placing note"
            );
        }

        // Set the borders' region's attributes
        this.bordersRegion.setPrefWidth(initialRectangleWidth);
        this.bordersRegion.setPrefHeight(this.rectangleHeight);

        // Update properties of the resizing regions
        resizeLeftRegion.translateXProperty().bind(this.bordersRegion.widthProperty().multiply(-0.5));
        resizeLeftRegion.prefHeightProperty().bind(this.bordersRegion.prefHeightProperty());
        resizeLeftRegion.setPrefWidth(RESIZING_REGIONS_WIDTH);
        resizeLeftRegion.setMaxWidth(RESIZING_REGIONS_WIDTH);

        resizeRightRegion.translateXProperty().bind(this.bordersRegion.widthProperty().multiply(0.5));
        resizeRightRegion.prefHeightProperty().bind(this.bordersRegion.prefHeightProperty());
        resizeRightRegion.setPrefWidth(RESIZING_REGIONS_WIDTH);
        resizeRightRegion.setMaxWidth(RESIZING_REGIONS_WIDTH);

        // Update the stack pane
        this.getChildren().addAll(this.bordersRegion, mainRectangle, resizeLeftRegion, resizeRightRegion);
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
            } else if (!this.isDragging) {
                this.setCursor(Cursor.DEFAULT);
            }
        });

        resizeRightRegion.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (canEdit && isPaused && newValue) {
                this.setCursor(Cursor.E_RESIZE);
            } else if (!this.isDragging) {
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
                    allNoteRectangles.remove(this.uuid, this);

                    // Add create action to the undo
                    addToStack(undoStack, this, UndoOrRedoAction.CREATE);  // When undoing, create rectangle

                    // Update flags
                    this.isRemoved = true;
                } else {
                    // Set initial values
                    this.initXTrans = this.getTranslateX();
                    this.initXDiff = event.getSceneX() - this.initXTrans;

                    this.initYTrans = this.getTranslateY();
                    this.initYEvent = event.getSceneY();

                    this.initNoteNum = this.noteNum;

                    // Disable scrolling
                    this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                    // Add transform action to the `undo` stack
                    addToStack(undoStack, this, UndoOrRedoAction.TRANSFORM);
                }

                // Purge redo history
                redoStack.clear();

                // Update flags
                isEditing = true;
                canUndoOrRedo = false;
                hasEditedNoteRectangles = true;

                // Prevent default action
                event.consume();
            }
        });

        mainRectangle.setOnMouseDragged(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Set cursor
                this.setCursor(Cursor.CLOSED_HAND);

                // Calculate new X position
                double newX = event.getSceneX() - this.initXDiff;

                // Calculate number of rectangles' heights to shift the note and calculate new note number
                double diffY = event.getSceneY() - this.initYEvent;
                int numIncrements = (int) (diffY / this.rectangleHeight);
                int changeInNoteNumber = -numIncrements;  // Higher Y -> Lower on screen => need to negate
                double newY = numIncrements * this.rectangleHeight + this.initYTrans;
                int newNoteNum = this.initNoteNum + changeInNoteNumber;

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
                    if (this.leftBoundingRectangle != null) {
                        this.setTranslateX(this.leftBoundingRectangle.getEndX());
                    }
                } else if (collisionLoc == CollisionLocation.RIGHT) {  // Collided with right rectangle
                    // Move current rectangle to the left edge of the right rectangle
                    if (this.rightBoundingRectangle != null) {
                        this.setTranslateX(this.rightBoundingRectangle.getStartX() - getRectangleWidth());
                    }
                } else {
                    // Permit horizontal movement if within range
                    if (newX >= 0 && newX + getRectangleWidth() <= spectrogramWidth) {
                        this.setTranslateX(newX);
                    }
                }

                if (collisionLoc != CollisionLocation.UP && collisionLoc != CollisionLocation.DOWN) {
                    // Permit vertical movement if within range
                    if (newY >= 0 && newY + this.rectangleHeight <= spectrogramHeight) {
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
                if (!this.isRemoved) {
                    // Update the note rectangles' list
                    noteRectanglesByNoteNumber.get(this.noteNum).add(this);

                    // Unset the bounding rectangles
                    setBoundingRectangles(null, null);
                }

                // Update flags
                isEditing = false;
                canUndoOrRedo = true;
            }

            MyLogger.log(
                    Level.FINE,
                    "Moved rectangle to " + getNoteOnsetTime() + " seconds with note number " + this.noteNum,
                    this.getClass().toString()
            );
        });

        // Set mouse events for the resizing regions
        resizeLeftRegion.setOnMousePressed(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Add current state to the undo stack and purge redo history
                addToStack(undoStack, this, UndoOrRedoAction.TRANSFORM);
                redoStack.clear();

                // Remove rectangle from the note rectangle by number lists
                noteRectanglesByNoteNumber.get(this.noteNum).remove(this);

                // Set initial values
                this.initXTrans = this.getTranslateX();
                this.initXDiff = event.getSceneX() - this.getTranslateX();
                this.initWidth = this.bordersRegion.getPrefWidth();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Update cursor
                this.setCursor(Cursor.W_RESIZE);

                // Update flags
                isEditing = true;
                hasEditedNoteRectangles = true;

                this.isDragging = true;

                // Prevent default action
                event.consume();
            }
        });

        resizeLeftRegion.setOnMouseDragged(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Get the new X position
                double newX = event.getSceneX() - this.initXDiff;

                // Calculate new width of the rectangle
                double newWidth = this.initWidth + (this.initXTrans - newX);

                // Check if collision will occur
                CollisionLocation collisionLoc = checkCollision(newX, newWidth, this.noteNum, VerticalMovement.NONE);
                if (collisionLoc == CollisionLocation.NONE) {
                    // If the new width is at least the resizing regions' width then resize
                    if (newWidth >= RESIZING_REGIONS_WIDTH) {
                        this.setTranslateX(newX);
                        this.bordersRegion.setPrefWidth(newWidth);
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
                if (!this.isRemoved) {
                    // Add rectangle back into the note rectangle by number lists
                    noteRectanglesByNoteNumber.get(this.noteNum).add(this);

                    // Unset the bounding rectangles
                    setBoundingRectangles(null, null);
                }

                // Reset cursor back to default if cursor is no longer on the resizing region
                if (!resizeLeftRegion.isHover()) this.setCursor(Cursor.DEFAULT);

                // Update flags
                isEditing = false;

                this.isDragging = false;
            }
        });

        resizeRightRegion.setOnMousePressed(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Add current state to the undo stack and purge redo history
                addToStack(undoStack, this, UndoOrRedoAction.TRANSFORM);
                redoStack.clear();

                // Remove rectangle from the note rectangle by number lists
                noteRectanglesByNoteNumber.get(this.noteNum).remove(this);

                // Set initial values
                this.initXTrans = this.getTranslateX();
                this.initXDiff = event.getSceneX() - this.getTranslateX();
                this.initWidth = this.bordersRegion.getPrefWidth();

                // Disable scrolling
                this.getParent().addEventHandler(ScrollEvent.ANY, cancelScroll);

                // Update cursor
                this.setCursor(Cursor.E_RESIZE);

                // Update flags
                isEditing = true;
                hasEditedNoteRectangles = true;

                this.isDragging = true;

                // Prevent default action
                event.consume();
            }
        });

        resizeRightRegion.setOnMouseDragged(event -> {
            // Check if editing is permitted
            if (canEdit && isPaused) {
                // Get the new X position
                double newX = event.getSceneX() - this.initXDiff;

                // Update the width of the note rectangle
                double newWidth = this.initWidth + (newX - this.initXTrans);

                // Check if collision will occur
                CollisionLocation collisionLoc = checkCollision(newX, newWidth, this.noteNum, VerticalMovement.NONE);
                if (collisionLoc == CollisionLocation.NONE) {
                    // If the new width is at least the resizing regions' width then resize
                    if (newWidth >= RESIZING_REGIONS_WIDTH) {
                        this.bordersRegion.setPrefWidth(newWidth);
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
                if (!this.isRemoved) {
                    // Add rectangle back into the note rectangle by number lists
                    noteRectanglesByNoteNumber.get(this.noteNum).add(this);

                    // Unset the bounding rectangles
                    setBoundingRectangles(null, null);
                }

                // Reset cursor back to default if cursor is no longer on the resizing region
                if (!resizeRightRegion.isHover()) this.setCursor(Cursor.DEFAULT);

                // Update flags
                isEditing = false;

                this.isDragging = false;
            }
        });

        // Add this new note rectangle into the note rectangles' lists
        allNoteRectangles.put(this.uuid, this);
        noteRectanglesByNoteNumber.get(noteNum).add(this);

        // Mark that the note rectangles were edited
        hasEditedNoteRectangles = true;

        // Add delete action to the undo stack and purge redi stack
        addToStack(undoStack, this, UndoOrRedoAction.DELETE);  // When undoing, delete rectangle
        redoStack.clear();
    }

    // Getter/Setter methods

    public static void setSpectrogramPaneAnchor(AnchorPane spectrogramPaneAnchor) {
        NoteRectangle.spectrogramPaneAnchor = spectrogramPaneAnchor;
    }

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

    public static boolean getHasEditedNoteRectangles() {
        return hasEditedNoteRectangles;
    }

    public static void setHasEditedNoteRectangles(boolean hasEditedNoteRectangles) {
        NoteRectangle.hasEditedNoteRectangles = hasEditedNoteRectangles;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public double getNoteOnsetTime() {
        return this.noteOnsetTime.get();
    }

    public double getNoteDuration() {
        return this.noteDuration.get();
    }

    public double getRectangleWidth() {
        return this.rectangleWidth.get();
    }

    public double getStartX() {
        return this.getTranslateX();
    }

    public double getEndX() {
        return this.getStartX() + this.getRectangleWidth();
    }

    // Public methods

    /**
     * Clears both the undo and redo stacks.
     */
    public static void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }

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

        // Make sure that `hasEditedNoteRectangles` is `false` (because user did not edit the notes)
        hasEditedNoteRectangles = false;
    }

    /**
     * Method that quantizes the notes.
     *
     * @param bpm           Beats per minute.
     * @param offset        Offset value.
     * @param timeSignature Time signature string.
     */
    public static void quantizeNotes(double bpm, double offset, String timeSignature) {
        // Only allow quantization if the playback is paused
        if (isPaused) {
            double pixelsPerSecond = spectrogramWidth / totalDuration;

            // Determine the note 'unit' we are working with
            int noteUnit = MusicUtils.parseTimeSignature(timeSignature).value1();  // What "one beat" represents

            // Get the number of seconds per beat
            double spb = 1. / bpm * 60.;  // spb = seconds per beat

            // Determine resolution of the quantization
            NoteQuantizationUnit quantizationUnit =
                    NoteQuantizationUnit.values()[DataFiles.SETTINGS_DATA_FILE.data.noteQuantizationUnitEnumOrdinal];
            int divisionFactor = quantizationUnit.numericValue / noteUnit;
            double resolution = spb / divisionFactor;

            // Process each note rectangle
            for (NoteRectangle rectangle : allNoteRectangles.values()) {
                // Get the onset time and duration
                double onsetTime = rectangle.getNoteOnsetTime();
                double duration = rectangle.getNoteDuration();

                // Compute the number of resolution 'units' for the offset and the duration
                int numOffsetResolutions = (int) Math.round((onsetTime - offset) / resolution);
                int numDurationResolutions = (int) Math.round(duration / resolution);

                // Quantize both onset time and duration
                onsetTime = numOffsetResolutions * resolution + offset;
                duration = numDurationResolutions * resolution;

                // Update rectangle's position and width
                rectangle.setTranslateX(onsetTime * pixelsPerSecond);
                rectangle.bordersRegion.setPrefWidth(duration * pixelsPerSecond);
            }
        }
    }

    /**
     * Method that performs the specified <code>editAction</code>, such as undoing or redoing.
     *
     * @param editAction Action to perform.
     */
    // Fixme: fix undo/redo bugs for all three actions
    public static void editAction(EditAction editAction) {
        // If editing is disabled or if undo or redo is disabled, do nothing
        if (!canEdit || !canUndoOrRedo) return;

        // Determine the stacks to act upon
        Stack<Triple<String, UndoOrRedoAction, Double[]>> primaryStack, secondaryStack;

        if (editAction == EditAction.UNDO) {
            primaryStack = undoStack;
            secondaryStack = redoStack;
        } else {
            primaryStack = redoStack;
            secondaryStack = undoStack;
        }

        // If the primary stack if empty
        if (primaryStack.empty()) return;

        // Get the latest undo action from primary stack
        Triple<String, UndoOrRedoAction, Double[]> latestAction = primaryStack.pop();
        String uuid = latestAction.value0();
        UndoOrRedoAction action = latestAction.value1();
        Double[] data = latestAction.value2();

        // Determine 'inverse' action to perform when doing the reverse operation
        UndoOrRedoAction invAction = UndoOrRedoAction.TRANSFORM;  // By default assume transform
        if (action == UndoOrRedoAction.CREATE) invAction = UndoOrRedoAction.DELETE;
        else if (action == UndoOrRedoAction.DELETE) invAction = UndoOrRedoAction.CREATE;

        // Get relevant rectangle
        NoteRectangle relevantRect = allNoteRectangles.get(uuid);

        // Add inverse action to secondary stack
        if (relevantRect == null) {
            addToStack(secondaryStack, uuid, invAction, data);
        } else {
            addToStack(secondaryStack, relevantRect, invAction);
        }

        // Perform the action
        handleAction(uuid, action, data);
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
     * @param rectWidth        Width of the rectangle.
     * @param noteNumber       Note number.
     * @param verticalMovement Vertical movement value.
     * @return A <code>CollisionLocation</code> value, describing the <em>direction</em> of the collision, or
     * <code>NONE</code> if there was no collision.
     */
    private CollisionLocation checkCollision(
            double xPos, double rectWidth, int noteNumber, VerticalMovement verticalMovement
    ) {
        // Determine bounding rectangles
        NoteRectangle leftBounder;
        NoteRectangle rightBounder;
        if ((this.leftBoundingRectangle == null && this.rightBoundingRectangle == null) ||
                (this.leftBoundingRectangle != null && xPos + rectWidth < this.leftBoundingRectangle.getStartX()) ||
                (this.rightBoundingRectangle != null && xPos > this.rightBoundingRectangle.getEndX()) ||
                verticalMovement != VerticalMovement.NONE) {
            // Recalculate the bounding rectangles and use the new ones
            Pair<NoteRectangle, NoteRectangle> rectangles = getLeftAndRightRectangles(xPos, noteNumber);
            leftBounder = rectangles.value0();
            rightBounder = rectangles.value1();
        } else {
            // No changes made; use the old ones
            leftBounder = this.leftBoundingRectangle;
            rightBounder = this.rightBoundingRectangle;
        }

        // Handle edge cases
        if (leftBounder == null && rightBounder == null) {
            // No rectangles present at all (other than itself) => no collision
            setBoundingRectangles(null, null);
            return CollisionLocation.NONE;

        } else if (leftBounder == null) {  // No left rectangle
            // If the end of this rectangle is before the start of the right rectangle, then there is no collision
            if (xPos + rectWidth >= rightBounder.getStartX()) {
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
            } else if (xPos + rectWidth >= rightBounder.getStartX()) {
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

    /**
     * Helper method that saves a rectangle's state to the specified stack.
     *
     * @param stack  Stack to save the state to.
     * @param rect   Rectangle to save the state of.
     * @param action Action to take.
     */
    private static void addToStack(
            Stack<Triple<String, UndoOrRedoAction, Double[]>> stack, NoteRectangle rect,
            UndoOrRedoAction action
    ) {
        addToStack(stack, rect.uuid, action, getDataForStack(rect, action));
    }

    /**
     * Helper method that saves a state to the specified stack.
     *
     * @param stack  Stack to save the state to.
     * @param uuid   UUID of the rectangle.
     * @param action Action to take.
     * @param data   Data of the action.
     */
    private static void addToStack(
            Stack<Triple<String, UndoOrRedoAction, Double[]>> stack, String uuid, UndoOrRedoAction action,
            Double[] data
    ) {
        stack.add(new Triple<>(uuid, action, data));
    }

    /**
     * Helper method that retrieves the needed data to preform the action provided.
     *
     * @param rect   Rectangle to act on.
     * @param action Action to perform.
     * @return Data needed to perform the action.
     */
    private static Double[] getDataForStack(NoteRectangle rect, UndoOrRedoAction action) {
        return switch (action) {
            case TRANSFORM -> new Double[]{rect.getTranslateX(), rect.getTranslateY(), rect.getWidth()};
            case CREATE -> new Double[]{rect.getNoteOnsetTime(), rect.getNoteDuration(), (double) rect.noteNum};
            case DELETE -> new Double[0];
        };
    }

    /**
     * Helper method that handles the action to be performed.
     *
     * @param uuid   UUID of the rectangle.
     * @param action Action to take.
     * @param data   Data needed to perform the action.
     */
    private static void handleAction(String uuid, UndoOrRedoAction action, Double[] data) {
        if (action == UndoOrRedoAction.TRANSFORM) {
            // Get relevant rectangle
            NoteRectangle rect = allNoteRectangles.get(uuid);

            // Update transform attributes
            rect.setTranslateX(data[0]);
            rect.setTranslateY(data[1]);
            rect.setWidth(data[2]);
        } else if (action == UndoOrRedoAction.CREATE) {
            try {
                // Create a new rectangle
                NoteRectangle rect = new NoteRectangle(data[0], data[1], data[2].intValue());
                spectrogramPaneAnchor.getChildren().add(rect);
                rect.setUUID(uuid);
            } catch (NoteRectangleCollisionException ignored) {
            }
        } else {
            // Get the relevant rectangle
            NoteRectangle rect = allNoteRectangles.get(uuid);

            // Remove rectangle from the note rectangle by number lists
            noteRectanglesByNoteNumber.get(rect.noteNum).remove(rect);

            // Remove the note rectangle from the parent pane
            ((Pane) rect.getParent()).getChildren().remove(rect);

            // Remove the note rectangle from the list of note rectangles
            allNoteRectangles.remove(uuid, rect);

            // Update flags
            rect.isRemoved = true;
        }

        hasEditedNoteRectangles = true;
    }

    // Overwritten methods
    @Override
    public String toString() {
        return "NoteRectangle(" +
                "uuid=" + this.uuid +
                ", noteNum=" + this.noteNum +
                ", noteOnsetTime=" + getNoteOnsetTime() +
                ", noteDuration=" + getNoteDuration() +
                ")";
    }

    // Helper classes/enums
    static class SortByTimeToPlace implements Comparator<NoteRectangle> {
        @Override
        public int compare(NoteRectangle o1, NoteRectangle o2) {
            return Double.compare(o1.noteOnsetTime.getValue(), o2.noteOnsetTime.getValue());
        }
    }

    enum CollisionLocation {LEFT, RIGHT, UP, DOWN, NONE}

    enum VerticalMovement {UP, DOWN, NONE}

    enum UndoOrRedoAction {TRANSFORM, CREATE, DELETE}

    public enum EditAction {UNDO, REDO}
}
