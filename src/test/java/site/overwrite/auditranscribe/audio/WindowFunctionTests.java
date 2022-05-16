/*
 * WindowFunctionTests.java
 *
 * Created on 2022-04-15
 * Updated on 2022-05-14
 *
 * Description: Test the windowing functions.
 */

package site.overwrite.auditranscribe.audio;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.audio.window_functions.AbstractWindow;
import site.overwrite.auditranscribe.audio.window_functions.HammingWindow;
import site.overwrite.auditranscribe.audio.window_functions.HannWindow;
import site.overwrite.auditranscribe.audio.window_functions.OnesWindow;

import static org.junit.jupiter.api.Assertions.*;

class WindowFunctionTests {
    @Test
    void onesWindow() {
        // Define window object
        AbstractWindow window = new OnesWindow();

        // Get the windows
        double[] windowArr1 = window.generateWindow(12, false);
        double[] windowArr2 = window.generateWindow(15, false);
        double[] windowArr3 = window.generateWindow(12, true);
        double[] windowArr4 = window.generateWindow(15, true);

        double[] windowArr5 = window.generateWindow(1, false);
        double[] windowArr6 = window.generateWindow(1, true);
        double[] windowArr7 = window.generateWindow(2, false);
        double[] windowArr8 = window.generateWindow(2, true);
        double[] windowArr9 = window.generateWindow(3, false);
        double[] windowArr10 = window.generateWindow(3, true);

        // Define the correct window arrays
        double[] correctWindowArr1 = {1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1.};
        double[] correctWindowArr2 = {1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1.};
        double[] correctWindowArr3 = {1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1.};
        double[] correctWindowArr4 = {1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1., 1.};

        double[] correctWindowArr5 = {1.};
        double[] correctWindowArr6 = {1.};
        double[] correctWindowArr7 = {1., 1.};
        double[] correctWindowArr8 = {1., 1.};
        double[] correctWindowArr9 = {1., 1., 1.};
        double[] correctWindowArr10 = {1., 1., 1.};

        // Assertions
        assertArrayEquals(correctWindowArr1, windowArr1);
        assertArrayEquals(correctWindowArr2, windowArr2);
        assertArrayEquals(correctWindowArr3, windowArr3);
        assertArrayEquals(correctWindowArr4, windowArr4);
        assertArrayEquals(correctWindowArr5, windowArr5);
        assertArrayEquals(correctWindowArr6, windowArr6);
        assertArrayEquals(correctWindowArr7, windowArr7);
        assertArrayEquals(correctWindowArr8, windowArr8);
        assertArrayEquals(correctWindowArr9, windowArr9);
        assertArrayEquals(correctWindowArr10, windowArr10);
    }

    @Test
    void hammingWindow() {
        // Define window object
        AbstractWindow window = new HammingWindow();

        // Get the windows
        double[] windowArr1 = window.generateWindow(12, false);
        double[] windowArr2 = window.generateWindow(15, false);
        double[] windowArr3 = window.generateWindow(12, true);
        double[] windowArr4 = window.generateWindow(15, true);

        double[] windowArr5 = window.generateWindow(1, false);
        double[] windowArr6 = window.generateWindow(1, true);
        double[] windowArr7 = window.generateWindow(2, false);
        double[] windowArr8 = window.generateWindow(2, true);
        double[] windowArr9 = window.generateWindow(3, false);
        double[] windowArr10 = window.generateWindow(3, true);

        // Define the correct window arrays
        double[] correctWindowArr1 = {0.08000000000000007, 0.14162831425915828, 0.30999999999999994, 0.54, 0.77, 0.9383716857408417, 1.0, 0.9383716857408417, 0.7700000000000002, 0.54, 0.3100000000000003, 0.14162831425915834};
        double[] correctWindowArr2 = {0.08000000000000007, 0.11976908948440373, 0.23219992107492526, 0.3978521825875243, 0.5880830931031207, 0.77, 0.9121478174124759, 0.9899478963375505, 0.9899478963375505, 0.9121478174124759, 0.7700000000000002, 0.5880830931031209, 0.3978521825875243, 0.23219992107492543, 0.11976908948440379};
        double[] correctWindowArr3 = {0.08000000000000007, 0.15302337489765683, 0.34890909401913234, 0.6054648256057111, 0.8412359376148312, 0.9813667678626689, 0.9813667678626689, 0.8412359376148312, 0.6054648256057111, 0.3489090940191324, 0.15302337489765666, 0.08000000000000007};
        double[] correctWindowArr4 = {0.08000000000000007, 0.12555432076488732, 0.25319469114498266, 0.43764037038009546, 0.6423596296199047, 0.8268053088550175, 0.9544456792351128, 1.0, 0.9544456792351128, 0.8268053088550175, 0.6423596296199047, 0.43764037038009546, 0.25319469114498266, 0.12555432076488732, 0.08000000000000007};

        double[] correctWindowArr5 = {1.};
        double[] correctWindowArr6 = {1.};
        double[] correctWindowArr7 = {0.08, 1.};
        double[] correctWindowArr8 = {0.08, 0.08};
        double[] correctWindowArr9 = {0.08, 0.77, 0.77};
        double[] correctWindowArr10 = {0.08, 1., 0.08};

        // Assertions
        assertArrayEquals(correctWindowArr1, windowArr1, 1e-10);
        assertArrayEquals(correctWindowArr2, windowArr2, 1e-10);
        assertArrayEquals(correctWindowArr3, windowArr3, 1e-10);
        assertArrayEquals(correctWindowArr4, windowArr4, 1e-10);
        assertArrayEquals(correctWindowArr5, windowArr5, 1e-10);
        assertArrayEquals(correctWindowArr6, windowArr6, 1e-10);
        assertArrayEquals(correctWindowArr7, windowArr7, 1e-10);
        assertArrayEquals(correctWindowArr8, windowArr8, 1e-10);
        assertArrayEquals(correctWindowArr9, windowArr9, 1e-10);
        assertArrayEquals(correctWindowArr10, windowArr10, 1e-10);
    }

    @Test
    void hannWindow() {
        // Define window object
        AbstractWindow window = new HannWindow();

        // Get the windows
        double[] windowArr1 = window.generateWindow(12, false);
        double[] windowArr2 = window.generateWindow(15, false);
        double[] windowArr3 = window.generateWindow(12, true);
        double[] windowArr4 = window.generateWindow(15, true);

        double[] windowArr5 = window.generateWindow(1, false);
        double[] windowArr6 = window.generateWindow(1, true);
        double[] windowArr7 = window.generateWindow(2, false);
        double[] windowArr8 = window.generateWindow(2, true);
        double[] windowArr9 = window.generateWindow(3, false);
        double[] windowArr10 = window.generateWindow(3, true);

        // Define the correct window arrays
        double[] correctWindowArr1 = {0.0, 0.06698729810778065, 0.2499999999999999, 0.5, 0.75, 0.9330127018922192, 1.0, 0.9330127018922194, 0.7500000000000002, 0.5, 0.2500000000000003, 0.06698729810778076};
        double[] correctWindowArr2 = {0.0, 0.04322727117869962, 0.16543469682057088, 0.34549150281252633, 0.5522642316338268, 0.75, 0.9045084971874737, 0.9890738003669028, 0.9890738003669028, 0.9045084971874737, 0.7500000000000002, 0.5522642316338271, 0.34549150281252633, 0.16543469682057105, 0.04322727117869968};
        double[] correctWindowArr3 = {0.0, 0.07937323358440951, 0.29229249349905684, 0.5711574191366425, 0.8274303669726426, 0.9797464868072487, 0.9797464868072487, 0.8274303669726426, 0.5711574191366425, 0.29229249349905695, 0.07937323358440934, 0.0};
        double[] correctWindowArr4 = {0.0, 0.04951556604879048, 0.18825509907063326, 0.38873953302184283, 0.6112604669781572, 0.8117449009293668, 0.9504844339512095, 1.0, 0.9504844339512095, 0.8117449009293668, 0.6112604669781572, 0.38873953302184283, 0.18825509907063326, 0.04951556604879048, 0.0};

        double[] correctWindowArr5 = {1.};
        double[] correctWindowArr6 = {1.};
        double[] correctWindowArr7 = {0., 1.};
        double[] correctWindowArr8 = {0., 0.};
        double[] correctWindowArr9 = {0., 0.75, 0.75};
        double[] correctWindowArr10 = {0., 1., 0.};

        // Assertions
        assertArrayEquals(correctWindowArr1, windowArr1, 1e-10);
        assertArrayEquals(correctWindowArr2, windowArr2, 1e-10);
        assertArrayEquals(correctWindowArr3, windowArr3, 1e-10);
        assertArrayEquals(correctWindowArr4, windowArr4, 1e-10);
        assertArrayEquals(correctWindowArr5, windowArr5, 1e-10);
        assertArrayEquals(correctWindowArr6, windowArr6, 1e-10);
        assertArrayEquals(correctWindowArr7, windowArr7, 1e-10);
        assertArrayEquals(correctWindowArr8, windowArr8, 1e-10);
        assertArrayEquals(correctWindowArr9, windowArr9, 1e-10);
        assertArrayEquals(correctWindowArr10, windowArr10, 1e-10);
    }
}