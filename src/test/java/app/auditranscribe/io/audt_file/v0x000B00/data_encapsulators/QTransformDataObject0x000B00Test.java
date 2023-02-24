package app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators;

import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.TypeConversionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QTransformDataObject0x000B00Test {
    static byte[] qTransformBytes1 = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};  // Not real data; do NOT decompress
    static byte[] qTransformBytes2 = new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a};

    static double minMagnitude1 = 123.4;
    static double minMagnitude2 = 456.7;

    static double maxMagnitude1 = 567.89;
    static double maxMagnitude2 = 1234.5;

    @Test
    void numBytesNeeded() {
        // Define the two Q-Transform data objects to test number of bytes needed
        QTransformDataObject one = new QTransformDataObject0x000B00(
                qTransformBytes1, minMagnitude1, maxMagnitude1
        );
        QTransformDataObject two = new QTransformDataObject0x000B00(
                qTransformBytes2, minMagnitude2, maxMagnitude2
        );

        // Tests
        assertEquals(31, one.numBytesNeeded());
        assertEquals(33, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        QTransformDataObject temp = new QTransformDataObject0x000B00(
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
            QTransformDataObject one = new QTransformDataObject0x000B00(
                    qTransformBytes[indices1[0]],
                    minMagnitudes[indices1[1]],
                    maxMagnitudes[indices1[2]]
            );

            for (int[] indices2 : indexProduct) {
                QTransformDataObject two = new QTransformDataObject0x000B00(
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
    void magnitudesToByteData() {
        // Define sample Q-transform magnitudes
        double[][] magnitudes = {
                {0.1, -0.2, 0.3},
                {4, -5, 6},
                {7, -8, 9}
        };

        // Define correct return values
        byte[] correctBytes = {
                120, -100, 99, 96, 96, 96, 102, 0, -30, -97, 63, 127, -2, -6, 90, 90,
                90, -6, -25, -49, -97, -65, 38, 91, -74, 108, 89, -85, -85, -85, 27, 116,
                -23, -46, -91, -60, -121, 15, 31, 54, 0, 21, -44, -1, -1, -1, 31, 0,
                -44, 113, 23, 125
        };
        double correctMinMagnitude = -8;
        double correctMaxMagnitude = 9;

        // Run the method
        Triple<Byte[], Double, Double> returned = QTransformDataObject0x000B00.magnitudesToByteData(
                magnitudes, null
        );

        // Tests
        assertArrayEquals(correctBytes, TypeConversionUtils.toByteArray(returned.value0()));
        assertEquals(correctMinMagnitude, returned.value1(), 1e-5);
        assertEquals(correctMaxMagnitude, returned.value2(), 1e-5);
    }

    @Test
    void byteDataToMagnitudes() {
        // Define sample byte data and the min and max magnitudes
        byte[] bytes = {
                120, -100, 99, 96, 96, 96, 102, 0, -30, -97, 63, 127, -2, -6, 90, 90,
                90, -6, -25, -49, -97, -65, 38, 91, -74, 108, 89, -85, -85, -85, 27, 116,
                -23, -46, -91, -60, -121, 15, 31, 54, 0, 21, -44, -1, -1, -1, 31, 0,
                -44, 113, 23, 125
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
        double[][] magnitudes = QTransformDataObject0x000B00.byteDataToMagnitudes(bytes, minMagnitude, maxMagnitude);

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