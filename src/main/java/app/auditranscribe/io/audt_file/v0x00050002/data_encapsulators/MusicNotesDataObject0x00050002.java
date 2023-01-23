/*
 * MusicNotesDataObject0x00050002.java
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

package app.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;

/**
 * Data object that stores the music notes' data.
 */
public class MusicNotesDataObject0x00050002 extends MusicNotesDataObject {
    /**
     * Initialization method for the music notes data object.
     *
     * @param timesToPlaceRectangles The times to place the note rectangles.
     * @param noteDurations          The duration of each note rectangle.
     * @param noteNums               The note number of each note rectangle.
     */
    public MusicNotesDataObject0x00050002(double[] timesToPlaceRectangles, double[] noteDurations, int[] noteNums) {
        this.timesToPlaceRectangles = timesToPlaceRectangles;
        this.noteDurations = noteDurations;
        this.noteNums = noteNums;
    }

    // Public methods
    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + 8 * timesToPlaceRectangles.length) +  // +4 for the integer telling how many notes there are
                (4 + 8 * noteDurations.length) +
                (4 + 4 * noteNums.length) +
                4;  // EOS delimiter
    }
}
