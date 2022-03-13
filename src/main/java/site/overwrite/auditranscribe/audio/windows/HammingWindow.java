/*
 * HammingWindow.java
 *
 * Created on 2022-03-11
 * Updated on 2022-03-12
 *
 * Description: Class that encapsulates the Hamming window function.
 */

package site.overwrite.auditranscribe.audio.windows;

/**
 * Hamming window.
 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">This
 * article</a> about the Hamming window function.
 */
public class HammingWindow extends GeneralCosineWindow {
    public HammingWindow() {
        bandwidth = 1.3629455320350348;
        aCoefficients = new double[] {0.54, 0.46};
    }
}
