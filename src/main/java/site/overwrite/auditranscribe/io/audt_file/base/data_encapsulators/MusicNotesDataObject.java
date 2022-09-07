/*
 * MusicNotesDataObject.java
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

package site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators;

import java.util.Arrays;

/**
 * Data object that stores the music notes' data.
 */
public abstract class MusicNotesDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 5;

    // Attributes
    public double[] timesToPlaceRectangles;
    public double[] noteDurations;
    public int[] noteNums;

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicNotesDataObject that = (MusicNotesDataObject) o;
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
}
