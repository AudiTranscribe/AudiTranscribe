package app.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

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
}