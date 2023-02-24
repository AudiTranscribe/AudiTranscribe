package app.auditranscribe.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteUnitTest {
    @Test
    void numericValueToNoteUnit() {
        assertEquals(NoteUnit.HALF_NOTE, NoteUnit.numericValueToNoteUnit(2));
        assertEquals(NoteUnit.QUARTER_NOTE, NoteUnit.numericValueToNoteUnit(4));
        assertEquals(NoteUnit.THIRTY_SECOND_NOTE, NoteUnit.numericValueToNoteUnit(32));

        assertNull(NoteUnit.numericValueToNoteUnit(5));
    }

    @Test
    void testToString() {
        assertEquals("Quarter Note", NoteUnit.QUARTER_NOTE.toString());
        assertEquals("Thirty-second Note", NoteUnit.THIRTY_SECOND_NOTE.toString());
    }
}