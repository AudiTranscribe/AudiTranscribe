/*
 * HannWindow.java
 *
 * Created on 2022-03-11
 * Updated on 2022-05-14
 *
 * Description: Class that encapsulates the Hann window function.
 */

package site.overwrite.auditranscribe.audio.window_functions;

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
