/*
 * FormatException.java
 * Description: Exception to mark when a string does not have the correct format.
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
 * Exception to mark when a string does not have the correct format.
 */
@ExcludeFromGeneratedCoverageReport
public class FormatException extends RuntimeException {
    public FormatException(String message) {
        super(message);
    }
}
