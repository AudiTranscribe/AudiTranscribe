/*
 * ValidationUtils.java
 *
 * Created on 2022-04-23
 * Updated on 2022-04-23
 *
 * Description: Utilities to assist with validating data.
 */

package site.overwrite.auditranscribe.utils;

import java.util.Objects;

/**
 * Utilities to assist with validating data.
 */
public class ValidationUtils {
    // Public methods

    /**
     * Validates that the entered string is non-empty.
     * @param string    The string to check.
     * @return A boolean: <code>true</code> is the string is non-empty, and <code>false</code>
     * otherwise.
     */
    public static boolean isEmpty(String string) {
        return Objects.equals(string, "");
    }

    /**
     * Validates that the entered integer is non-zero.
     * @param number    Number to check.
     * @return A boolean: <code>true</code> is the integer is non-zero, and <code>false</code>
     * otherwise.
     */
    public static boolean isNonZero(int number) {
        return isNonZero((double) number);
    }

    /**
     * Validates that the entered float is non-zero.
     * @param number    Number to check.
     * @return A boolean: <code>true</code> is the float is non-zero, and <code>false</code>
     * otherwise.
     */
    public static boolean isNonZero(float number) {
        return isNonZero((double) number);
    }

    /**
     * Validates that the entered double is non-zero.
     * @param number    Number to check.
     * @return A boolean: <code>true</code> is the double is non-zero, and <code>false</code>
     * otherwise.
     */
    public static boolean isNonZero(double number) {
        return number != 0;
    }

    /**
     * Validates that the entered integer is non-negative.
     * @param number    Number to check.
     * @return A boolean: <code>true</code> is the integer is non-negative, and <code>false</code>
     * otherwise.
     */
    public static boolean isNonNegative(int number) {
        return isNonNegative((double) number);
    }

    /**
     * Validates that the entered float is non-negative.
     * @param number    Number to check.
     * @return A boolean: <code>true</code> is the float is non-negative, and <code>false</code>
     * otherwise.
     */
    public static boolean isNonNegative(float number) {
        return isNonNegative((double) number);
    }

    /**
     * Validates that the entered double is non-negative.
     * @param number    Number to check.
     * @return A boolean: <code>true</code> is the double is non-negative, and <code>false</code>
     * otherwise.
     */
    public static boolean isNonNegative(double number) {
        return number >= 0;
    }

    /**
     * Validates whether the entered string is an integer.
     * @param string    String to check.
     * @return A boolean: <code>true</code> is the string is an integer, and <code>false</code>
     * otherwise.
     */
    public static boolean isStringInteger(String string) {
        // Try and parse
        try {
            Integer.parseInt(string);
        } catch (Exception e) {  // Failed to parse
            return false;
        }

        // If reached here then parsed successfully
        return true;
    }

    /**
     * Validates whether the entered string is a float
     * @param string    String to check.
     * @return A boolean: <code>true</code> is the string is a float, and <code>false</code>
     * otherwise.
     */
    public static boolean isStringFloat(String string) {
        // Try and parse
        try {
            Float.parseFloat(string);
        } catch (Exception e) {  // Failed to parse
            return false;
        }

        // If reached here then parsed successfully
        return true;
    }

    /**
     * Validates whether the entered string is a double
     * @param string    String to check.
     * @return A boolean: <code>true</code> is the string is a double, and <code>false</code>
     * otherwise.
     */
    public static boolean isStringDouble(String string) {
        // Try and parse
        try {
            Double.parseDouble(string);
        } catch (Exception e) {  // Failed to parse
            return false;
        }

        // If reached here then parsed successfully
        return true;
    }

    /**
     * Validates whether the integer <code>num</code> lies within the <b>closed</b> interval
     * [<code>min</code>, <code>max</code>].
     * @param num   Number to check.
     * @param min   Lower bound.
     * @param max   Upper bound.
     * @return A boolean: <code>true</code> if <code>num</code> is inside the interval, and
     * <code>false</code> otherwise.
     */
    public static boolean isInRange(int num, int min, int max) {
        return min <= num && num <= max;
    }

    /**
     * Validates whether the double <code>num</code> lies within the <b>closed</b> interval 
     * [<code>min</code>, <code>max</code>].
     * @param num   Number to check.
     * @param min   Lower bound.
     * @param max   Upper bound.
     * @return A boolean: <code>true</code> if <code>num</code> is inside the interval, and
     * <code>false</code> otherwise.
     */
    public static boolean isInRange(double num, double min, double max) {
        return min <= num && num <= max;
    }
}
