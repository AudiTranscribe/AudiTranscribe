/*
 * UnchangingDataPropertiesObject0x00050002Test.java
 *
 * Created on 2022-07-02
 * Updated on 2022-07-11
 *
 * Description: Test `UnchangingDataPropertiesObject0x00050002.java`.
 */

package site.overwrite.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.UnchangingDataPropertiesObject;
import site.overwrite.auditranscribe.utils.MathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UnchangingDataPropertiesObject0x00050002Test {
    // Attributes
    int numSkippableBytes1 = 1234;
    int numSkippableBytes2 = 56789;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two objects to test number of bytes needed
        UnchangingDataPropertiesObject one = new UnchangingDataPropertiesObject0x00050002(numSkippableBytes1);
        UnchangingDataPropertiesObject two = new UnchangingDataPropertiesObject0x00050002(numSkippableBytes2);

        // Tests
        assertEquals(12, one.numBytesNeeded());
        assertEquals(12, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        UnchangingDataPropertiesObject temp = new UnchangingDataPropertiesObject0x00050002(numSkippableBytes1);

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
            UnchangingDataPropertiesObject one = new UnchangingDataPropertiesObject0x00050002(
                    numSkippableBytes[indices1[0]]
            );

            for (int[] indices2 : indexProduct) {
                UnchangingDataPropertiesObject two = new UnchangingDataPropertiesObject0x00050002(
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

    @Test
    void testHashCode() {
        // Define the two objects to test hash code calculation
        UnchangingDataPropertiesObject one = new UnchangingDataPropertiesObject0x00050002(numSkippableBytes1);
        UnchangingDataPropertiesObject two = new UnchangingDataPropertiesObject0x00050002(numSkippableBytes2);

        // Tests
        assertEquals(1265, one.hashCode());
        assertEquals(56820, two.hashCode());
    }
}