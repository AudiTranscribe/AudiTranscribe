/*
 * AudioDataObject0x00080001Test.java
 * Description: Test `AudioDataObject0x00080001.java`.
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

package site.overwrite.auditranscribe.io.audt_file.v0x00080001.data_encapsulators;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.utils.MathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AudioDataObject0x00080001Test {
    // Attributes
    byte[] compressedOriginalMP3Bytes1 = new byte[]{(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a};
    byte[] compressedOriginalMP3Bytes2 = new byte[]{(byte) 0xab, (byte) 0xcd, (byte) 0xef};

    byte[] compressedSlowedMP3Bytes1 = new byte[]{
            (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55,
            (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xaa
    };
    byte[] compressedSlowedMP3Bytes2 = new byte[]{
            (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98, (byte) 0x76, (byte) 0x54
    };

    double sampleRate1 = 44100;
    double sampleRate2 = 22050;

    int totalDurationInMS1 = 1234;
    int totalDurationInMS2 = 5678;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two audio data objects to test the number of bytes needed
        AudioDataObject one = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes1, compressedSlowedMP3Bytes1, sampleRate1, totalDurationInMS1
        );
        AudioDataObject two = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes2, compressedSlowedMP3Bytes2, sampleRate2, totalDurationInMS2
        );

        // Tests
        assertEquals(43, one.numBytesNeeded());
        assertEquals(37, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        AudioDataObject temp = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes1, compressedSlowedMP3Bytes1, sampleRate1, totalDurationInMS1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        byte[][] compressedOriginalMP3BytesArrays = {compressedOriginalMP3Bytes1, compressedOriginalMP3Bytes2};
        byte[][] compressedSlowedMP3BytesArrays = {compressedSlowedMP3Bytes1, compressedSlowedMP3Bytes2};
        double[] sampleRates = {sampleRate1, sampleRate2};
        int[] totalDurationInMSs = {totalDurationInMS1, totalDurationInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 4);  // 4 data attributes
        for (int[] indices1 : indexProduct) {
            AudioDataObject one = new AudioDataObject0x00080001(
                    compressedOriginalMP3BytesArrays[indices1[0]],
                    compressedSlowedMP3BytesArrays[indices1[1]],
                    sampleRates[indices1[2]],
                    totalDurationInMSs[indices1[3]]
            );

            for (int[] indices2 : indexProduct) {
                AudioDataObject two = new AudioDataObject0x00080001(
                        compressedOriginalMP3BytesArrays[indices2[0]],
                        compressedSlowedMP3BytesArrays[indices2[1]],
                        sampleRates[indices2[2]],
                        totalDurationInMSs[indices2[3]]
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
        AudioDataObject one = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes1, compressedSlowedMP3Bytes1, sampleRate1, totalDurationInMS1
        );
        AudioDataObject two = new AudioDataObject0x00080001(
                compressedOriginalMP3Bytes2, compressedSlowedMP3Bytes2, sampleRate2, totalDurationInMS2
        );

        // Tests
        assertEquals(1116214964, one.hashCode());
        assertEquals(12559934, two.hashCode());
    }
}