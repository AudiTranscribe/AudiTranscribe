/*
 * NoteRectangleCollisionException.java
 *
 * Created on 2022-06-17
 * Updated on 2022-06-23
 *
 * Description: Exception that is raised if there is a collision between note rectangles during the
 *              creation of a new note rectangle.
 */

package site.overwrite.auditranscribe.exceptions.notes;

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
