/*
 * HammingWindow.java
 * Description: Class that encapsulates the Hamming window function.
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

package site.overwrite.auditranscribe.audio.window_functions;

/**
 * Hamming window.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This
 * article</a> about the Hamming window function.
 */
public class HammingWindow extends GeneralCosineWindow {
    public HammingWindow() {
        bandwidth = 1.3629455320350348;
        aCoefficients = new double[]{0.54, 0.46};
    }
}
