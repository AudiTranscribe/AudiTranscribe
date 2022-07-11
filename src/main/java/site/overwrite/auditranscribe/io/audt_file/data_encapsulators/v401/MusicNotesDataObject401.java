/*
 * MusicNotesDataObject401.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the music notes' data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators.v401;

import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.MusicNotesDataObject;

/**
 * Data object that stores the music notes' data.
 */
public class MusicNotesDataObject401 extends MusicNotesDataObject {
    /**
     * Initialization method for the music notes data object.
     *
     * @param timesToPlaceRectangles The times to place the note rectangles.
     * @param noteDurations          The duration of each note rectangle.
     * @param noteNums               The note number of each note rectangle.
     */
    public MusicNotesDataObject401(double[] timesToPlaceRectangles, double[] noteDurations, int[] noteNums) {
        this.timesToPlaceRectangles = timesToPlaceRectangles;
        this.noteDurations = noteDurations;
        this.noteNums = noteNums;
    }
}
