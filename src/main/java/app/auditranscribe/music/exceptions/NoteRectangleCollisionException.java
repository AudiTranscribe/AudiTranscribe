/*
 * NoteRectangleCollisionException.java
 * Description: Exception that is raised if there is a collision between note rectangles during the
 *              creation of a new note rectangle.
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

package app.auditranscribe.music.exceptions;

/**
 * Exception that is raised if there is a collision between note rectangles during the creation of a
 * new note rectangle.
 */
public class NoteRectangleCollisionException extends Exception {
    public NoteRectangleCollisionException() {
        super();
    }

    public NoteRectangleCollisionException(String message) {
        super(message);
    }

    public NoteRectangleCollisionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoteRectangleCollisionException(Throwable cause) {
        super(cause);
    }
}
