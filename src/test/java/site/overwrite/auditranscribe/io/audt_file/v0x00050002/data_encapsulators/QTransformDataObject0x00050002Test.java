/*
 * QTransformDataObject0x00050002Test.java
 * Description: Test `QTransformDataObject0x00050002.java`.
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

package site.overwrite.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.utils.MathUtils;

import static org.junit.jupiter.api.Assertions.*;

class QTransformDataObject0x00050002Test {
    // Attributes
    byte[] qTransformBytes1 = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};  // Not real data; do NOT decompress
    byte[] qTransformBytes2 = new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a};

    double minMagnitude1 = 123.4;
    double minMagnitude2 = 456.7;

    double maxMagnitude1 = 567.89;
    double maxMagnitude2 = 1234.5;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two Q-Transform data objects to test number of bytes needed
        QTransformDataObject one = new QTransformDataObject0x00050002(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        QTransformDataObject two = new QTransformDataObject0x00050002(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        // Tests
        assertEquals(31, one.numBytesNeeded());
        assertEquals(33, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        QTransformDataObject temp = new QTransformDataObject0x00050002(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        byte[][] qTransformBytes = {qTransformBytes1, qTransformBytes2};
        double[] minMagnitudes = {minMagnitude1, minMagnitude2};
        double[] maxMagnitudes = {maxMagnitude1, maxMagnitude2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 3);  // 3 data attributes
        for (int[] indices1 : indexProduct) {
            QTransformDataObject one = new QTransformDataObject0x00050002(
                    qTransformBytes[indices1[0]],
                    minMagnitudes[indices1[1]],
                    maxMagnitudes[indices1[2]]
            );

            for (int[] indices2 : indexProduct) {
                QTransformDataObject two = new QTransformDataObject0x00050002(
                        qTransformBytes[indices2[0]],
                        minMagnitudes[indices2[1]],
                        maxMagnitudes[indices2[2]]
                );

                // Check equality
                if (indices1 == indices2) {
                    assertEquals(one, two);
                    assertEquals(two, one);
                } else {
                    assertNotEquals(one, two);
                    assertNotEquals(two, one);
                }
            }
        }
    }

    @Test
    void testHashCode() {
        // Define the two Q-Transform data objects to test hash code computation
        QTransformDataObject one = new QTransformDataObject0x00050002(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        QTransformDataObject two = new QTransformDataObject0x00050002(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        // Tests
        assertEquals(-1506726541, one.hashCode());
        assertEquals(-1293707692, two.hashCode());
    }
}