/*
 * MusicNotesDataObject0x00070001.java
 * Description: Data object that stores the music notes' data.
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

package site.overwrite.auditranscribe.io.audt_file.v0x00070001.data_encapsulators;

import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;

import java.util.Arrays;

/**
 * Data object that stores the music notes' data.
 */
public class MusicNotesDataObject0x00070001 extends MusicNotesDataObject {
    /**
     * Initialization method for the music notes data object.
     *
     * @param timesToPlaceRectangles The times to place the note rectangles.
     * @param noteDurations          The duration of each note rectangle.
     * @param noteNums               The note number of each note rectangle.
     */
    public MusicNotesDataObject0x00070001(double[] timesToPlaceRectangles, double[] noteDurations, int[] noteNums) {
        this.timesToPlaceRectangles = timesToPlaceRectangles;
        this.noteDurations = noteDurations;
        this.noteNums = noteNums;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicNotesDataObject0x00070001 that = (MusicNotesDataObject0x00070001) o;
        return (
                Arrays.equals(timesToPlaceRectangles, that.timesToPlaceRectangles) &&
                        Arrays.equals(noteDurations, that.noteDurations) &&
                        Arrays.equals(noteNums, that.noteNums)
        );
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(timesToPlaceRectangles);
        result = 31 * result + Arrays.hashCode(noteDurations);
        result = 31 * result + Arrays.hashCode(noteNums);
        return result;
    }

    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + 8 * timesToPlaceRectangles.length) +  // +4 for the integer telling how many notes there are
                (4 + 8 * noteDurations.length) +
                (4 + 4 * noteNums.length) +
                4;  // EOS delimiter
    }
}
