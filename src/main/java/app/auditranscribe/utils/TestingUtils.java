/*
 * TestingUtils.java
 * Description: Testing utility methods.
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

package app.auditranscribe.utils;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Testing utility methods.
 */
@ExcludeFromGeneratedCoverageReport
public final class TestingUtils {
    // Attributes
    private static Boolean isRunningTest = null;

    private TestingUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that determines if a JUnit test is running.
     *
     * @return Boolean that determines whether a JUnit test is running.
     */
    public static boolean isRunningTest() {
        // If needed, update the flag
        if (isRunningTest == null) {
            // Try to check if the stack trace contains a JUnit test
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (element.getClassName().startsWith("org.junit.")) {
                    isRunningTest = true;
                    break;
                }
            }

            // If the boolean was not updated, then it is not running the test
            if (isRunningTest == null) isRunningTest = false;
        }

        return isRunningTest;
    }
}
