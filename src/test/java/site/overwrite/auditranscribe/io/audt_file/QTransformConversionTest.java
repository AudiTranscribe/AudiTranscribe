/*
 * QTransformConversionTest.java
 * Description: Tests the Q-Transform conversion methods inside the `QTransformDataObject` class.
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

package site.overwrite.auditranscribe.io.audt_file;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QTransformConversionTest {
    @Test
    void qTransformMagnitudesToByteData() throws IOException {
        // Define sample Q-transform magnitudes
        double[][] magnitudes = {
                {0.1, -0.2, 0.3},
                {4, -5, 6},
                {7, -8, 9}
        };

        // Define correct return values
        byte[] correctBytes = {
                64, 0, 0, 0, 3, 4, 0, -16, 21, -7, -7, -7, -6, -11, 117, 117, 117, -4, -4, -4,
                -3, 52, -76, -76, -76, -83, 45, 45, 45, 82, -46, -46, -46, 97, -31, -31, -31, -128, 0, 0,
                0, 127, -1, -1, -1
        };
        double correctMinMagnitude = -8;
        double correctMaxMagnitude = 9;

        // Run the method
        Triple<Byte[], Double, Double> returned = QTransformDataObject.qTransformMagnitudesToByteData(
                magnitudes, null
        );

        // Tests
        assertArrayEquals(correctBytes, TypeConversionUtils.toByteArray(returned.value0()));
        assertEquals(correctMinMagnitude, returned.value1(), 1e-5);
        assertEquals(correctMaxMagnitude, returned.value2(), 1e-5);
    }

    @Test
    void byteDataToQTransformMagnitudes() throws IOException {
        // Define sample byte data and the min and max magnitudes
        byte[] bytes = {
                64, 0, 0, 0, 3, 4, 0, -16, 21, -7, -7, -7, -6, -11, 117, 117, 117, -4, -4, -4,
                -3, 52, -76, -76, -76, -83, 45, 45, 45, 82, -46, -46, -46, 97, -31, -31, -31, -128, 0, 0,
                0, 127, -1, -1, -1
        };
        double minMagnitude = -8;
        double maxMagnitude = 9;

        // Define correct magnitude array
        double[][] correctMagnitudes = {
                {0.1, -0.2, 0.3},
                {4, -5, 6},
                {7, -8, 9}
        };

        // Run the method
        double[][] magnitudes = QTransformDataObject.byteDataToQTransformMagnitudes(bytes, minMagnitude, maxMagnitude);

        // Check lengths
        assertEquals(correctMagnitudes.length, magnitudes.length);
        assertEquals(correctMagnitudes[0].length, magnitudes[0].length);

        // Check values
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(correctMagnitudes[i][j], magnitudes[i][j], 1e-5);
            }
        }
    }
}
