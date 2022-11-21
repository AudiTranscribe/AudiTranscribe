/*
 * OnesWindow.java
 * Description: Class that encapsulates a window that only contains ones.
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

package app.auditranscribe.audio.window_functions;

/**
 * Ones window.<br>
 * Basically a window that only contains ones.
 */
public class OnesWindow extends AbstractWindow {
    public OnesWindow() {
        bandwidth = 1.;
    }

    /**
     * Method that generates the window value for a specific index <code>k</code>.
     *
     * @param k      Index of the window. <b>Note that this index is 1-indexed</b>.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>k</code>.
     */
    @Override
    double windowFunc(int k, int length) {
        return 1;
    }
}
