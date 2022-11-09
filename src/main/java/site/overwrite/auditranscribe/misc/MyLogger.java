/*
 * MyLogger.java
 * Description: Class that handles the logging functionalities of AudiTranscribe.
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

import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.utils.MiscUtils;
import site.overwrite.auditranscribe.utils.TestingUtils;

import java.io.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that handles the logging functionalities of AudiTranscribe.
 */
@ExcludeFromGeneratedCoverageReport
public final class MyLogger {
    // Constants
    private static final long MAX_LOG_FILE_SIZE = 5_000_000;  // In bytes

    // Static attributes
    public static String currentLogName;
    public static String loggingFolder;

    private static Logger logger;

    private MyLogger() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Log a message, with no arguments.<br>
     * If the logger is currently enabled for the given message level then the given message is
     * forwarded to all the registered output <code>Handler</code> objects.
     *
     * @param level            One of the message level identifiers, e.g., <code>SEVERE</code>.
     * @param msg              Message to log.
     * @param callingClassName Name of the class that called this method.
     */
    public static void log(Level level, String msg, String callingClassName) {
        // Convert calling class name to just the class
        String[] split = callingClassName.split("\\.");
        String classStr = split[split.length - 1];

        // Log the message
        getLogger().log(level, String.format("(%1$-30s) %2$s", classStr, msg));
    }

    /**
     * Method that logs an exception to file/console.
     *
     * @param e Exception to log.
     */
    public static void logException(Exception e) {
        // Get the stack trace message
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        // Log the error
        getLogger().log(Level.SEVERE, sw.toString());
    }

    /**
     * Method that helps clear old logs from the logs' folder.
     *
     * @param persistenceInDays Number of days to keep any log.
     */
    public static void clearOldLogs(int persistenceInDays) {
        // Set logging folder path
        setLoggingFolder();

        // Obtain all files in the logging folder
        File[] files = new File(loggingFolder).listFiles();

        // Get current time
        int currTime = (int) (MiscUtils.getUnixTimestamp());

        // Process all files
        if (files != null) {
            for (File file : files) {
                // Check if the file is in the correct format
                Pattern pattern = Pattern.compile("Log-(?<timestamp>\\d+)\\.log");
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.find()) {  // Is in correct format
                    // Get the timestamp portion
                    int timestamp = Integer.parseInt(matcher.group("timestamp"));  // Should not fail

                    // Compare the current time with the timestamp of the log
                    int timeDelta = currTime - timestamp;  // In seconds
                    double daysDelta = timeDelta / 86400.;  // 86400 seconds in a day

                    // Check if the days delta exceeds the persistence value
                    if (daysDelta > persistenceInDays) {
                        // Delete old log
                        IOMethods.delete(file);

                        // Log it
                        MyLogger.log(
                                Level.FINE,
                                "Deleted old log '" + file.getName() + "' (Older than " +
                                        persistenceInDays + " days)",
                                MyLogger.class.getName()
                        );
                    }
                }
            }
        }
    }

    // Private methods

    /**
     * Helper method that retrieves the <code>Logger</code> instance.
     *
     * @return The <code>Logger</code> instance.
     */
    private static Logger getLogger() {
        if (MyLogger.logger == null) {
            try {
                // Get initialization time
                int initTime = (int) (MiscUtils.getUnixTimestamp());

                // Set logging folder path
                setLoggingFolder();

                // Try to read logging config from the logging properties file
                try (InputStream is = IOMethods.getInputStream("conf/logging.properties")) {
                    LogManager.getLogManager().readConfiguration(is);
                }

                // Set up logger
                logger = Logger.getLogger("AudiTranscribe");

                // Create file handler if not running a test
                if (!TestingUtils.isRunningTest()) {
                    currentLogName = "Log-" + initTime + ".log";
                    FileHandler fileHandler = new FileHandler(
                            IOMethods.joinPaths(loggingFolder, currentLogName),
                            MAX_LOG_FILE_SIZE,
                            1,
                            true
                    );

                    // Update attributes of the file handler
                    fileHandler.setLevel(Level.FINE);
                    fileHandler.setFormatter(new SimpleFormatter());

                    // Add the file handler to the logger
                    logger.addHandler(fileHandler);
                }

                // Make logger use parent handlers
                logger.setUseParentHandlers(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return logger;
    }

    /**
     * Helper method that sets the logging folder path.
     */
    private static void setLoggingFolder() {
        if (loggingFolder == null) {
            // Determine the path
            if (new File(IOConstants.APP_DATA_FOLDER_PATH).exists()) {
                loggingFolder = IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH, "logs");
            } else {
                loggingFolder = IOConstants.USER_HOME_PATH;
            }

            // Create if not exists
            IOMethods.createFolder(loggingFolder);
        }
    }
}
