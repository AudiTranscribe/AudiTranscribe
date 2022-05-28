/*
 * Theme.java
 *
 * Created on 2022-05-28
 * Updated on 2022-05-28
 *
 * Description: Theme enum.
 */

package site.overwrite.auditranscribe.misc;

public enum Theme {
    // Enum values
    LIGHT_MODE("Light Mode", "light-mode.css"),
    DARK_MODE("Dark Mode", "dark-mode.css");

    // Enum attributes
    public final String cssFile;
    private final String name;

    // Enum constructor
    Theme(String name, String cssFile) {
        this.name = name;
        this.cssFile = cssFile;
    }

    // Override methods
    @Override
    public String toString() {
        return name;
    }
}
