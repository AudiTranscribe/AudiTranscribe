/*
 * ValueException.java
 * Description: Exception to mark when an invalid value is encountered.
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

package app.auditranscribe.generic.exceptions;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Exception to mark when an operation or function receives an argument that has the right type but
 * an inappropriate value, and the situation is not described by a more precise exception such as
 * <code>IndexOutOfBoundsException</code>.<br>
 * This is the Java equivalent to Python's
 * <a href="https://docs.python.org/3/library/exceptions.html#ValueError"><code>ValueError</code>
 * </a>.
 */
@ExcludeFromGeneratedCoverageReport
public class ValueException extends RuntimeException {
    public ValueException(String message) {
        super(message);
    }
}
