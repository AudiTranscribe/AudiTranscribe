/*
 * Window.java
 *
 * Created on 2022-03-11
 * Updated on 2022-03-15
 *
 * Description: Enum that contains windowing functions.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.audio.windows.*;
import site.overwrite.auditranscribe.audio.windows.AbstractWindow;

/**
 * Window enum.<br>
 * Contains windowing functions to be used when windowing the audio signal.
 */
public enum Window {
    // Enum values
    ONES_WINDOW(new OnesWindow()),
    HANN_WINDOW(new HannWindow()),
    HAMMING_WINDOW(new HammingWindow());

    // Attributes
    public final AbstractWindow window;

    // Constructor
    Window(AbstractWindow window) {
        this.window = window;
    }
}
