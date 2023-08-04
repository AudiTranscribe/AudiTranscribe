/*
 * MusicNotesDataObject0x000C00.java
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

package app.auditranscribe.io.audt_file.v0x000C00.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;

/**
 * Data object that stores the music notes' data.
 */
public class MusicNotesDataObject0x000C00 extends MusicNotesDataObject {
    /**
     * Initialization method for the music notes data object.
     */
    public MusicNotesDataObject0x000C00() {
        this.timesToPlaceRectangles = null;
        this.noteDurations = null;
        this.noteNums = null;
    }

    // Public methods
    @Override
    public int numBytesNeeded() {
        return 0;
    }
}
