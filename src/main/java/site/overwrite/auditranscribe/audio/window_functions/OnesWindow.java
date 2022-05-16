/*
 * OnesWindow.java
 *
 * Created on 2022-03-11
 * Updated on 2022-05-14
 *
 * Description: Class that encapsulates the Ones window function.
 */

package site.overwrite.auditranscribe.audio.window_functions;

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
     * @param k      Index of the window. Note that this is 1-indexed.
     * @param length Total length of the window.
     * @return Double representing the window value at index <code>k</code>.
     */
    @Override
    double windowFunc(int k, int length) {
        return 1;
    }
}
