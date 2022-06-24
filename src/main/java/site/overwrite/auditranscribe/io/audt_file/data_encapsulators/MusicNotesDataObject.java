/*
 * MusicNotesDataObject.java
 *
 * Created on 2022-06-08
 * Updated on 2022-06-21
 *
 * Description: Data object that stores the music notes' data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import java.util.Arrays;

/**
 * Data object that stores the music notes' data.
 */
public class MusicNotesDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 5;

    // Attributes
    public double[] timesToPlaceRectangles;
    public double[] noteDurations;
    public int[] noteNums;

    /**
     * Initialization method for the music notes data object.
     *
     * @param timesToPlaceRectangles The times to place the note rectangles.
     * @param noteDurations          The duration of each note rectangle.
     * @param noteNums               The note number of each note rectangle.
     */
    public MusicNotesDataObject(double[] timesToPlaceRectangles, double[] noteDurations, int[] noteNums) {
        this.timesToPlaceRectangles = timesToPlaceRectangles;
        this.noteDurations = noteDurations;
        this.noteNums = noteNums;
    }

    // Overwritten methods
    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + timesToPlaceRectangles.length) +  // +4 for the integer telling how many notes there are
                (4 + noteDurations.length) +
                (4 + noteNums.length) +
                4;  // EOS delimiter
    }

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
