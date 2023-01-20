/*
 * AudioDataObject0x00070001Test.java
 * Description: Test `AudioDataObject0x00070001.java`.
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

package app.auditranscribe.io.audt_file.v0x00070001.data_encapsulators;

import org.junit.jupiter.api.Test;
import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.utils.MathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AudioDataObject0x00070001Test {
    // Attributes
    byte[] compressedMP3Bytes1 = new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a};
    byte[] compressedMP3Bytes2 = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};

    double sampleRate1 = 44100;
    double sampleRate2 = 22050;

    int totalDurationInMS1 = 1234;
    int totalDurationInMS2 = 5678;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two audio data objects to test the number of bytes needed
        AudioDataObject one = new AudioDataObject0x00070001(
                compressedMP3Bytes1, sampleRate1, totalDurationInMS1
        );
        AudioDataObject two = new AudioDataObject0x00070001(
                compressedMP3Bytes2, sampleRate2, totalDurationInMS2
        );

        // Tests
        assertEquals(29, one.numBytesNeeded());
        assertEquals(27, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        AudioDataObject temp = new AudioDataObject0x00070001(
                compressedMP3Bytes1, sampleRate1, totalDurationInMS1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        byte[][] compressedMP3BytesArrays = {compressedMP3Bytes1, compressedMP3Bytes2};
        double[] sampleRates = {sampleRate1, sampleRate2};
        int[] totalDurationInMSs = {totalDurationInMS1, totalDurationInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 3);  // 3 data attributes
        for (int[] indices1 : indexProduct) {
            AudioDataObject one = new AudioDataObject0x00070001(
                    compressedMP3BytesArrays[indices1[0]],
                    sampleRates[indices1[1]],
                    totalDurationInMSs[indices1[2]]
            );

            for (int[] indices2 : indexProduct) {
                AudioDataObject two = new AudioDataObject0x00070001(
                        compressedMP3BytesArrays[indices2[0]],
                        sampleRates[indices2[1]],
                        totalDurationInMSs[indices2[2]]
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
        // Define the two audio data objects to test the hash code method
        AudioDataObject one = new AudioDataObject0x00070001(
                compressedMP3Bytes1, sampleRate1, totalDurationInMS1
        );
        AudioDataObject two = new AudioDataObject0x00070001(
                compressedMP3Bytes2, sampleRate2, totalDurationInMS2
        );

        // Tests
        assertEquals(-1603148222, one.hashCode());
        assertEquals(1637333885, two.hashCode());
    }
}