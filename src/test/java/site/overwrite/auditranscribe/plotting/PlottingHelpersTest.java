/*
 * PlottingHelpersTest.java
 *
 * Created on 2022-04-09
 * Updated on 2022-04-09
 *
 * Description: Test `PlottingHelpers.java`.
 */

package site.overwrite.auditranscribe.plotting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlottingHelpersTest {

    @Test
    void freqToHeight() {
        assertEquals(334.914, PlottingHelpers.freqToHeight(123, 10, 20000, 500), 0.001);
        assertEquals(153.6, PlottingHelpers.freqToHeight(4096,32,32768,512), 0.001);
        assertEquals(59.392, PlottingHelpers.freqToHeight(12345, 1, 22050, 1024), 0.001);
    }

    @Test
    void noteNumToHeight() {
        assertEquals(48.739, PlottingHelpers.noteNumToHeight(61, 0, 119, 100), 0.001);
        assertEquals(400, PlottingHelpers.noteNumToHeight(2, 0, 10, 500), 0.001);
        assertEquals(208.333, PlottingHelpers.noteNumToHeight(100, 5, 125, 1000), 0.001);
    }
}