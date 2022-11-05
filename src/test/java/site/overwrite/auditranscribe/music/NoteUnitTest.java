/*
 * NoteUnitTest.java
 * Description: Test `NoteUnit.java`
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

package site.overwrite.auditranscribe.music;

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
        assertEquals("Thirty-Second Note", NoteUnit.THIRTY_SECOND_NOTE.toString());
    }
}