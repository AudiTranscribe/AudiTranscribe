package app.auditranscribe.io.audt_file.v0x000C00.data_encapsulators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicNotesDataObject0x000C00Test {
    @Test
    void checkAttributes() {
        MusicNotesDataObject0x000C00 musicNotesObject = new MusicNotesDataObject0x000C00();
        assertNull(musicNotesObject.timesToPlaceRectangles);
        assertNull(musicNotesObject.noteDurations);
        assertNull(musicNotesObject.noteNums);
    }

    @Test
    void numBytesNeeded() {
        assertEquals(new MusicNotesDataObject0x000C00().numBytesNeeded(), 0);
    }
}
