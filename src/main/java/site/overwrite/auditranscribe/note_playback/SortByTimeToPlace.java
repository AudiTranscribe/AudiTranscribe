/*
 * SortByTimeToPlace.java
 *
 * Created on 2022-06-09
 * Updated on 2022-06-10
 *
 * Description: Comparator class that compares two notes by their time to place.
 */

package site.overwrite.auditranscribe.note_playback;

import java.util.Comparator;

/**
 * Comparator class that compares two notes by their time to place.
 */
public class SortByTimeToPlace implements Comparator<NoteRectangle> {
    @Override
    public int compare(NoteRectangle o1, NoteRectangle o2) {
        return Double.compare(o1.noteOnsetTime.getValue(), o2.noteOnsetTime.getValue());
    }
}
