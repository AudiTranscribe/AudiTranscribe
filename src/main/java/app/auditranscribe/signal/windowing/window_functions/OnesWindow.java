/*
 * OnesWindow.java
 * Description: A signal window that only contains ones.
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

package app.auditranscribe.signal.windowing.window_functions;

/**
 * A signal window that only contains ones.
 */
public class OnesWindow extends AbstractWindow {
    public OnesWindow() {
        bandwidth = 1.;  // Taken from http://librosa.org/doc/main/_modules/librosa/filters.html
    }

    // Package-protected methods
    @Override
    double windowFunc(int k, int length) {
        return 1;
    }
}
