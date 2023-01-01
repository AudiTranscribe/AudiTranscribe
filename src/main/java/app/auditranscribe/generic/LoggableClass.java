/*
 * LoggableClass.java
 * Description: An abstract class that supports logging.
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

package app.auditranscribe.generic;

import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

import java.util.logging.Level;

/**
 * An abstract class that supports logging.
 */
@ExcludeFromGeneratedCoverageReport
public abstract class LoggableClass {
    // Public methods

    /**
     * Logs a specific message at the specified level.
     *
     * @param level         A message level identifier (e.g. <code>INFO</code>,
     *                      <code>SEVERE</code>).
     * @param msg           The message to log.
     * @param qualifiedName The fully qualified name of the class that called this method.
     */
    public static void log(Level level, String msg, String qualifiedName) {
        CustomLogger.log(level, msg, qualifiedName);
    }

    /**
     * Logs a specific message at the specified level.
     *
     * @param level A message level identifier (e.g. <code>INFO</code>,
     *              <code>SEVERE</code>).
     * @param msg   The message to log.
     */
    public void log(Level level, String msg) {
        CustomLogger.log(level, msg, this.getClass().getName());
    }

    /**
     * Method that logs an exception.
     *
     * @param e Exception to log.
     */
    public static void logException(Exception e) {
        CustomLogger.logException(e);
    }
}
