/*
 * CustomLogger.java
 * Description: A custom logger for AudiTranscribe.
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

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.utils.MiscUtils;
import app.auditranscribe.utils.TestingUtils;

import java.io.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom logger for AudiTranscribe.
 */
@ExcludeFromGeneratedCoverageReport
public class CustomLogger {
    // Constants
    private static final long MAX_LOG_FILE_SIZE = 5_000_000;  // In bytes

    // Static attributes
    public static String currentLogName;
    public static String logsFolder;

    private static Logger logger;

    private CustomLogger() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Log a message.<br>
     * If the logger is currently enabled for the given message level, then the provided message is
     * forwarded to all the registered output <code>Handler</code> objects.
     *
     * @param level         A message level identifier (e.g. <code>INFO</code>,
     *                      <code>SEVERE</code>).
     * @param msg           The message to log.
     * @param qualifiedName The fully qualified name of the class that called this method.
     */
    public static void log(Level level, String msg, String qualifiedName) {
        // Convert fully qualified name to just the class name
        String[] split = qualifiedName.split("\\.");
        String classStr = split[split.length - 1];

        // Log the message
        getLogger().log(level, String.format("(%1$-30s) %2$s", classStr, msg));
    }

    /**
     * Log an exception.<br>
     * Will use the <code>SEVERE</code> level to log the message.
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
     * @param persistenceInDays Number of days to keep a log file.
     */
    public static void clearOldLogs(int persistenceInDays) {
        // Set logs' folder path
        setLogsFolder();

        // Obtain all files in the logs folder
        File[] files = new File(logsFolder).listFiles();

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
                    int timeDelta = currTime - timestamp;   // This is in seconds...
                    double daysDelta = timeDelta / 86400.;  // ...so we divide by 86400 (number of seconds in a day)

                    // Check if the days delta exceeds the persistence value
                    if (daysDelta > persistenceInDays) {
                        // Delete old log
                        IOMethods.delete(file);

                        // Record that the log was deleted
                        CustomLogger.log(
                                Level.FINE,
                                "Deleted old log '" + file.getName() + "' (Older than " +
                                        persistenceInDays + " days)",
                                CustomLogger.class.getName()
                        );
                    }
                }
            }
        }
    }

    // Private methods

    /**
     * Helper method that retrieves a <code>Logger</code> instance.
     *
     * @return The <code>Logger</code> instance.
     */
    private static Logger getLogger() {
        if (CustomLogger.logger == null) {
            try {
                // Get initialization time
                int initTime = (int) (MiscUtils.getUnixTimestamp());

                // Set logging folder path
                setLogsFolder();

                // Try to read logging config from the logging properties file
                try (InputStream is = IOMethods.getInputStream("conf/logging.properties")) {
                    LogManager.getLogManager().readConfiguration(is);
                }

                // Set up logger
                logger = Logger.getLogger("AudiTranscribe");

                // Determine which handler to add
                if (!TestingUtils.isRunningTest()) {
                    // Create file handler if not running a test
                    currentLogName = "Log-" + initTime + ".log";
                    FileHandler fileHandler = new FileHandler(
                            IOMethods.joinPaths(logsFolder, currentLogName),
                            MAX_LOG_FILE_SIZE,
                            1,
                            true
                    );

                    // Update attributes of the file handler
                    fileHandler.setLevel(Level.FINE);
                    fileHandler.setFormatter(new SimpleFormatter());

                    // Add the file handler to the logger
                    logger.addHandler(fileHandler);
                } else {
                    // Use a console handler with level set to `FINE`
                    ConsoleHandler consoleHandler = new ConsoleHandler();

                    consoleHandler.setLevel(Level.FINE);
                    consoleHandler.setFormatter(new SimpleFormatter());

                    logger.addHandler(consoleHandler);
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
     * Helper method that sets the logs' folder path.
     */
    private static void setLogsFolder() {
        if (logsFolder == null) {
            if (new File(IOConstants.APP_DATA_FOLDER_PATH).exists()) {
                logsFolder = IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH, "logs");
            } else {
                logsFolder = IOConstants.USER_HOME_PATH;
            }

            IOMethods.createFolder(logsFolder);  // Creates the path if it does not exist
        }
    }
}
