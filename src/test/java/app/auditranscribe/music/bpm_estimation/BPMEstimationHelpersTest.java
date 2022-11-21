/*
 * BPMEstimationHelpersTest.java
 * Description: Test `BPMEstimationHelpers.java`.
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

package app.auditranscribe.music.bpm_estimation;

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