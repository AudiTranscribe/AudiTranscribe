/*
 * Theme.java
 *
 * Created on 2022-05-28
 * Updated on 2022-05-29
 *
 * Description: Theme enum.
 */

package site.overwrite.auditranscribe.misc;

public enum Theme {
    // Enum values
    LIGHT_MODE("Light Mode", "light-mode"),
    DARK_MODE("Dark Mode", "dark-mode");

    // Enum attributes
    private final String name;
    public final String shortName;
    public final String cssFile;

    // Enum constructor
    Theme(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
        this.cssFile = shortName + ".css";
    }

    // Override methods
    @Override
    public String toString() {
        return name;
    }
}
