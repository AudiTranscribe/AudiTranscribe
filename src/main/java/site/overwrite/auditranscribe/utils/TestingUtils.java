/*
 * TestingUtils.java
 *
 * Created on 2022-07-08
 * Updated on 2022-07-08
 *
 * Description: Testing utility methods.
 */

package site.overwrite.auditranscribe.utils;

/**
 * Testing utility methods.
 */
public final class TestingUtils {
    // Attributes
    private static Boolean isRunningTest = null;

    private TestingUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that determines if a test is running or not.
     *
     * @return A boolean, that determines whether a JUnit test is running or not.
     */
    public static boolean isRunningTest() {
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
