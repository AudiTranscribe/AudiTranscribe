/*
 * ValueException.java
 *
 * Created on 2022-05-25
 * Updated on 2022-05-25
 *
 * Description: Exception to mark when an operation or function receives an argument that has the
 *              right type but an inappropriate value, and the situation is not described by a more
 *              precise exception such as `IndexOutOfBoundsException`.
 */

package site.overwrite.auditranscribe.exceptions;

/**
 * Exception to mark when an operation or function receives an argument that has the right type but
 * an inappropriate value, and the situation is not described by a more precise exception such as
 * <code>IndexOutOfBoundsException</code>.<br>
 * This is the Java equivalent to Python's
 * <a href="https://docs.python.org/3/library/exceptions.html#ValueError"><code>ValueError</code>
 * </a>.
 */
public class ValueException extends RuntimeException {
    public ValueException() {
    }

    public ValueException(String message) {
        super(message);
    }

    public ValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueException(Throwable cause) {
        super(cause);
    }

    public ValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
