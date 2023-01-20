/*
 * MusicNotesDataObject0x00080001Test.java
 * Description: Test `MusicNotesDataObject0x00080001.java`.
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

package app.auditranscribe.io.audt_file.v0x00080001.data_encapsulators;

import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Test;
import app.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MusicNotesDataObject0x00080001Test {
    // Attributes
    double[] timesToPlaceRectangles1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    double[] timesToPlaceRectangles2 = {1.2, 3.4, 5.6, 7.8, 9};

    double[] noteDurations1 = {0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.5, 0.5, 0.75, 1};
    double[] noteDurations2 = {1, 1, 0.25, 0.125, 0.5};

    int[] noteNums1 = {60, 61, 62, 63, 64, 65, 66, 67, 68, 69};
    int[] noteNums2 = {100, 90, 80, 70, 60};

    // Tests
    @Test
    void numBytesNeeded() {
        // Define the two music notes data objects to test number of bytes needed
        MusicNotesDataObject one = new MusicNotesDataObject0x00080001(
                timesToPlaceRectangles1, noteDurations1, noteNums1
        );
        MusicNotesDataObject two = new MusicNotesDataObject0x00080001(
                timesToPlaceRectangles2, noteDurations2, noteNums2
        );

        // Tests
        assertEquals(220, one.numBytesNeeded());
        assertEquals(120, two.numBytesNeeded());
    }

    @Test
    void testEquals() {
        // Define temporary data object for testing the initial checks
        MusicNotesDataObject temp = new MusicNotesDataObject0x00080001(
                timesToPlaceRectangles1, noteDurations1, noteNums1
        );

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(temp, temp);
        assertNotEquals(temp, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(temp, otherTypedVar);  // Not redundant to test the equality method

        // Define arrays to pick the data attributes from
        double[][] timesToPlaceRectangles = {timesToPlaceRectangles1, timesToPlaceRectangles2};
        double[][] noteDurations = {noteDurations1, noteDurations2};
        int[][] noteNums = {noteNums1, noteNums2};

        // Generate product of indices
        int[][] indexProduct = MathUtils.selfProduct(2, 3);  // 3 data attributes
        for (int[] indices1 : indexProduct) {
            MusicNotesDataObject one = new MusicNotesDataObject0x00080001(
                    timesToPlaceRectangles[indices1[0]],
                    noteDurations[indices1[1]],
                    noteNums[indices1[2]]
            );

            for (int[] indices2 : indexProduct) {
                MusicNotesDataObject two = new MusicNotesDataObject0x00080001(
                        timesToPlaceRectangles[indices2[0]],
                        noteDurations[indices2[1]],
                        noteNums[indices2[2]]
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
        // Define the two music notes data objects to test hash code generation
        MusicNotesDataObject one = new MusicNotesDataObject0x00080001(
                timesToPlaceRectangles1, noteDurations1, noteNums1
        );
        MusicNotesDataObject two = new MusicNotesDataObject0x00080001(
                timesToPlaceRectangles2, noteDurations2, noteNums2
        );

        // Tests
        assertEquals(143846438, one.hashCode());
        assertEquals(-1004171889, two.hashCode());
    }
}