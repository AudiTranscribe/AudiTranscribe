/*
 * GUIDataObject0X00050002Test.java
 * Description: Test `GUIDataObject0x00050002.java`.
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
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.utils.MathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GUIDataObject0X00050002Test {
    // Attributes
    int musicKeyIndex1 = 1;
    int musicKeyIndex2 = 23;

    int timeSignatureIndex1 = 12;
    int timeSignatureIndex2 = 3;

    double bpm1 = 123.456;
    double bpm2 = 78.9;

    double offsetSeconds1 = 1.23;
    double offsetSeconds2 = -4.56;

    double playbackVolume1 = 0.1234;
    double playbackVolume2 = 0.56789;

    int currTimeInMS1 = 123456789;
    int currTimeInMS2 = 987654321;

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two GUI data objects to test number of bytes needed
        GUIDataObject one = new GUIDataObject0x00050002(
                musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );
        GUIDataObject two = new GUIDataObject0x00050002(
                musicKeyIndex2, timeSignatureIndex2, bpm2, offsetSeconds2, playbackVolume2, currTimeInMS2
        );

        // Tests
        assertEquals(44, one.numBytesNeeded());
        assertEquals(44, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        GUIDataObject temp = new GUIDataObject0x00050002(
                musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        int[] musicKeyIndices = {musicKeyIndex1, musicKeyIndex2};
        int[] timeSignatureIndices = {timeSignatureIndex1, timeSignatureIndex2};
        double[] bpms = {bpm1, bpm2};
        double[] offsetSeconds = {offsetSeconds1, offsetSeconds2};
        double[] playbackVolumes = {playbackVolume1, playbackVolume2};
        int[] currTimeInMSs = {currTimeInMS1, currTimeInMS2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 6);  // 6 data attributes
        for (int[] indices1 : indexProduct) {
            GUIDataObject one = new GUIDataObject0x00050002(
                    musicKeyIndices[indices1[0]],
                    timeSignatureIndices[indices1[1]],
                    bpms[indices1[2]],
                    offsetSeconds[indices1[3]],
                    playbackVolumes[indices1[4]],
                    currTimeInMSs[indices1[5]]
            );

            for (int[] indices2 : indexProduct) {
                GUIDataObject two = new GUIDataObject0x00050002(
                        musicKeyIndices[indices2[0]],
                        timeSignatureIndices[indices2[1]],
                        bpms[indices2[2]],
                        offsetSeconds[indices2[3]],
                        playbackVolumes[indices2[4]],
                        currTimeInMSs[indices2[5]]
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
        // Define the two GUI data objects to test hash code
        GUIDataObject one = new GUIDataObject0x00050002(
                musicKeyIndex1, timeSignatureIndex1, bpm1, offsetSeconds1, playbackVolume1, currTimeInMS1
        );
        GUIDataObject two = new GUIDataObject0x00050002(
                musicKeyIndex2, timeSignatureIndex2, bpm2, offsetSeconds2, playbackVolume2, currTimeInMS2
        );

        // Tests
        assertEquals(184433900, one.hashCode());
        assertEquals(668235807, two.hashCode());
    }
}