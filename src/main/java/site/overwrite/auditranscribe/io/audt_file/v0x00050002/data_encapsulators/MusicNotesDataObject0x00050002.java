/*
 * MusicNotesDataObject0x00050002.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the music notes' data.
 */

package site.overwrite.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;

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
}
