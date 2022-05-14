/*
 * WindowFunction.java
 *
 * Created on 2022-03-11
 * Updated on 2022-05-14
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
    ONES_WINDOW(new OnesWindow()),
    HANN_WINDOW(new HannWindow()),
    HAMMING_WINDOW(new HammingWindow());

    // Attributes
    public final AbstractWindow window;

    // Constructor
    WindowFunction(AbstractWindow window) {
        this.window = window;
    }
}
