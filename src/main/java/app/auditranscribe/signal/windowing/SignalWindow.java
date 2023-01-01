/*
 * SignalWindow.java
 * Description: Enum that contains signal windowing functions.
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

package app.auditranscribe.signal.windowing;

import app.auditranscribe.signal.windowing.window_functions.*;

/**
 * Enum that contains signal windowing functions.
 */
public enum SignalWindow {
    // Enum values
    ONES_WINDOW(new OnesWindow(), "Ones"),
    HANN_WINDOW(new HannWindow(), "Hann"),
    HAMMING_WINDOW(new HammingWindow(), "Hamming");

    // Attributes
    public final AbstractWindow window;
    private final String name;

    // Enum constructor
    SignalWindow(AbstractWindow window, String name) {
        this.window = window;
        this.name = name;
    }

    // Override methods
    @Override
    public String toString() {
        return name;
    }
}
