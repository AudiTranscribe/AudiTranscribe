/*
 * TimeIt.java
 * Description: Helper class that times how long a section of code takes to run.
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

package app.auditranscribe.misc;

import java.util.logging.Level;

/**
 * Helper class that times how long a section of code takes to run.
 */
@ExcludeFromGeneratedCoverageReport
public final class TimeIt {
    // Attributes
    private static long startTime;
    private static long endTime;

    private static long pauseStart;
    private static long pauseDuration;

    private TimeIt() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Start timing the code.
     */
    public static void start() {
        startTime = System.nanoTime();
    }

    /**
     * Stop timing the code.<br>
     * Will display the time taken.
     */
    public static void end() {
        endTime = System.nanoTime();
        countTime();
    }

    /**
     * Pause timing.
     */
    public static void pause() {
        pauseStart = System.nanoTime();
    }

    /**
     * Resume timing.
     */
    public static void resume() {
        long pauseEnd = System.nanoTime();
        pauseDuration += pauseEnd - pauseStart;
    }

    // Private methods

    /**
     * Helper method that counts how long the function took to execute.
     */
    private static void countTime() {
        // Calculate duration
        long duration = endTime - startTime - pauseDuration;

        // Determine unit to use for display
        String unit;
        double timeTaken;

        if (duration < 1e3) {  // Within nanosecond
            unit = "ns";
            timeTaken = duration;
        } else if (duration < 1e6) {  // Within microsecond
            unit = "µs";
            timeTaken = duration / 1e3;
        } else if (duration < 1e9) {  // Within millisecond
            unit = "ms";
            timeTaken = duration / 1e6;
        } else {
            unit = "s";
            timeTaken = duration / 1e9;
        }

        CustomLogger.log(Level.FINE, "Took " + timeTaken + " " + unit, "TimeIt");
    }
}
