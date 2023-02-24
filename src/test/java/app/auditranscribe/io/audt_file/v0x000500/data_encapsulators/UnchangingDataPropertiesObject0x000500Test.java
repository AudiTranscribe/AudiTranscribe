package app.auditranscribe.io.audt_file.v0x000500.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.UnchangingDataPropertiesObject;
import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnchangingDataPropertiesObject0x000500Test {
    static int numSkippableBytes1 = 1234;
    static int numSkippableBytes2 = 56789;

    @Test
    void numBytesNeeded() {
        // Define the two objects to test number of bytes needed
        UnchangingDataPropertiesObject one = new UnchangingDataPropertiesObject0x000500(numSkippableBytes1);
        UnchangingDataPropertiesObject two = new UnchangingDataPropertiesObject0x000500(numSkippableBytes2);

        // Tests
        assertEquals(12, one.numBytesNeeded());
        assertEquals(12, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        UnchangingDataPropertiesObject temp = new UnchangingDataPropertiesObject0x000500(numSkippableBytes1);

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        int[] numSkippableBytes = {numSkippableBytes1, numSkippableBytes2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 1);  // 1 data attribute
        for (int[] indices1 : indexProduct) {
            UnchangingDataPropertiesObject one = new UnchangingDataPropertiesObject0x000500(
                    numSkippableBytes[indices1[0]]
            );

            for (int[] indices2 : indexProduct) {
                UnchangingDataPropertiesObject two = new UnchangingDataPropertiesObject0x000500(
                        numSkippableBytes[indices2[0]]
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