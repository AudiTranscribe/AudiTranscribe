/*
 * BPMEstimationHelpersTest.java
 *
 * Created on 2022-05-31
 * Updated on 2022-05-31
 *
 * Description: Test `BPMEstimationHelpers.java`.
 */

package site.overwrite.auditranscribe.bpm_estimation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BPMEstimationHelpersTest {

    @Test
    void tempoFrequencies() {
        // Define expected arrays
        double[] expected1 = {
                Double.POSITIVE_INFINITY, 2583.984375, 1291.9921875, 861.328125,
                645.99609375, 516.796875, 430.6640625, 369.140625
        };
        double[] expected2 = {
                Double.POSITIVE_INFINITY, 645.99609375, 322.99804688, 215.33203125,
                161.49902344, 129.19921875, 107.66601562, 92.28515625,
                80.74951172, 71.77734375, 64.59960938, 58.72691761
        };

        // Run tests
        assertArrayEquals(expected1, BPMEstimationHelpers.tempoFrequencies(8, 512, 22050), 1e-5);
        assertArrayEquals(expected2, BPMEstimationHelpers.tempoFrequencies(12, 4096, 44100), 1e-5);
    }
}