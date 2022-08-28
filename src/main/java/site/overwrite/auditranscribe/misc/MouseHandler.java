/*
 * MouseHandler.java
 * Description: Handler that handles a mouse click event and a mouse drag event separately.
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