/*
 * WindowFunction.java
 *
 * Created on 2022-03-11
 * Updated on 2022-05-22
 *
 * Description: Enum that contains windowing functions.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.audio.window_functions.*;
import site.overwrite.auditranscribe.audio.window_functions.AbstractWindow;

/**
 * WindowFunction enum.<br>
 * Contains windowing functions to be used when windowing the audio signal.
 */
public enum WindowFunction {
    // Enum values
    ONES_WINDOW(new OnesWindow(), "Ones"),
    HANN_WINDOW(new HannWindow(), "Hann"),
    HAMMING_WINDOW(new HammingWindow(), "Hamming");

    // Attributes
    public final AbstractWindow window;
    private final String name;

    // Enum constructor
    WindowFunction(AbstractWindow window, String name) {
        this.window = window;
        this.name = name;
    }

    // Override methods
    @Override
    public String toString() {
        return name;
    }
}
