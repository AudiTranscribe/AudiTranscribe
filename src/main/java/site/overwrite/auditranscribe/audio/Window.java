/*
 * Windows.java
 *
 * Created on 2022-03-11
 * Updated on 2022-03-11
 *
 * Description: Enum that contains windows.
 */

package site.overwrite.auditranscribe.audio;

import site.overwrite.auditranscribe.audio.windows.*;
import site.overwrite.auditranscribe.audio.windows.AbstractWindow;

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
