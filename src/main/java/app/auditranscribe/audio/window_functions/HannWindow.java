/*
 * HannWindow.java
 * Description: Class that encapsulates the Hann window function.
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
 * Hann window.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This
 * article</a> about the Hann window function.
 */
public class HannWindow extends GeneralCosineWindow {
    public HannWindow() {
        bandwidth = 1.50018310546875;
        aCoefficients = new double[]{0.5, 0.5};
    }
}
