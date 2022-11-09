/*
 * ClassWithLogging.java
 * Description: A generic AudiTranscribe class with logging capabilities.
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

package site.overwrite.auditranscribe.generic;

import site.overwrite.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import site.overwrite.auditranscribe.misc.MyLogger;

import java.util.logging.Level;

/**
 * A generic AudiTranscribe class with logging capabilities.
 */
@ExcludeFromGeneratedCoverageReport
public abstract class ClassWithLogging {
    // Public methods

    /**
     * Method that logs a specific message with the specified level.
     *
     * @param level     One of the message level identifiers, e.g., <code>SEVERE</code>.
     * @param msg       Message to log.
     * @param className Name of the class that called this method.
     */
    public static void log(Level level, String msg, String className) {
        MyLogger.log(level, msg, className);
    }

    /**
     * Method that logs a specific message with the specified level.
     *
     * @param level One of the message level identifiers, e.g., <code>SEVERE</code>.
     * @param msg   Message to log.
     */
    public void log(Level level, String msg) {
        MyLogger.log(level, msg, this.getClass().getName());
    }

    /**
     * Method that logs an exception to file/console.
     *
     * @param e Exception to log.
     */
    public static void logException(Exception e) {
        MyLogger.logException(e);
    }
}
