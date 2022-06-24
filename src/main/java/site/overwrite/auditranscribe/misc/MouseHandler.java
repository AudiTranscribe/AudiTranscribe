/*
 * MouseHandler.java
 *
 * Created on 2022-06-07
 * Updated on 2022-06-21
 *
 * Description: Handler that handles a mouse click event and a mouse drag event separately.
 */

package site.overwrite.auditranscribe.misc;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Handler that handles a mouse click event and a mouse drag event separately.
 */
public class MouseHandler implements EventHandler<MouseEvent> {
    // Attributes
    private final EventHandler<MouseEvent> onDraggedEventHandler;
    private final EventHandler<MouseEvent> onClickedEventHandler;
    private boolean dragging = false;

    /**
     * Initialization method for a <code>MouseHandler</code> object.
     * @param onDraggedEventHandler Event handler to be called when the mouse is dragged.
     * @param onClickedEventHandler Event handler to be called when the mouse is clicked.
     */
    public MouseHandler(
            EventHandler<MouseEvent> onDraggedEventHandler, EventHandler<MouseEvent> onClickedEventHandler
    ) {
        this.onDraggedEventHandler = onDraggedEventHandler;
        this.onClickedEventHandler = onClickedEventHandler;
    }

    // Overwritten methods
    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            dragging = false;
        } else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
            dragging = true;
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (dragging) onDraggedEventHandler.handle(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (!dragging) onClickedEventHandler.handle(event);
        }
    }
}