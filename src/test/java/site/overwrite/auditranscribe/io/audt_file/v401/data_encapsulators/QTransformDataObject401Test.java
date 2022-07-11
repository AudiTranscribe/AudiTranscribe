/*
 * QTransformDataObject401Test.java
 *
 * Created on 2022-07-02
 * Updated on 2022-07-11
 *
 * Description: Test `QTransformDataObject401.java`.
 */

package site.overwrite.auditranscribe.io.audt_file.v401.data_encapsulators;

import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class QTransformDataObject401Test {
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
        QTransformDataObject one = new QTransformDataObject401(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        QTransformDataObject two = new QTransformDataObject401(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        // Tests
        assertEquals(31, one.numBytesNeeded());
        assertEquals(33, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        QTransformDataObject temp = new QTransformDataObject401(
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
            QTransformDataObject one = new QTransformDataObject401(
                    qTransformBytes[indices1[0]],
                    minMagnitudes[indices1[1]],
                    maxMagnitudes[indices1[2]]
            );

            for (int[] indices2 : indexProduct) {
                QTransformDataObject two = new QTransformDataObject401(
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
        QTransformDataObject one = new QTransformDataObject401(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        QTransformDataObject two = new QTransformDataObject401(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        // Tests
        assertEquals(-1506726541, one.hashCode());
        assertEquals(-1293707692, two.hashCode());
    }

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
        Triplet<Byte[], Double, Double> returned = QTransformDataObject.qTransformMagnitudesToByteData(
                magnitudes, null
        );

        // Tests
        assertArrayEquals(correctBytes, TypeConversionUtils.toByteArray(returned.getValue0()));
        assertEquals(correctMinMagnitude, returned.getValue1(), 1e-5);
        assertEquals(correctMaxMagnitude, returned.getValue2(), 1e-5);
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