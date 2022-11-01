/*
 * TimeSignatureTest.java
 * Description: Test `TimeSignature.java`
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

class TimeSignatureTest {

    @Test
    void testToString() {
        assertEquals("4/4", TimeSignature.FOUR_FOUR.toString());
        assertEquals("6/8", TimeSignature.SIX_EIGHT.toString());
    }

    @Test
    void displayText() {
        assertEquals("4/4", TimeSignature.FOUR_FOUR.displayText());
        assertEquals("6/8", TimeSignature.SIX_EIGHT.displayText());
    }

    @Test
    void displayTextToTimeSignature() {
        assertEquals(TimeSignature.FIVE_FOUR, TimeSignature.displayTextToTimeSignature("5/4"));
        assertEquals(TimeSignature.TWO_FOUR, TimeSignature.displayTextToTimeSignature("2/4"));
        assertEquals(TimeSignature.TWELVE_EIGHT, TimeSignature.displayTextToTimeSignature("12/8"));

        assertNull(TimeSignature.displayTextToTimeSignature("13/2"));
        assertNull(TimeSignature.displayTextToTimeSignature("8/7"));
        assertNull(TimeSignature.displayTextToTimeSignature("12/34"));
    }
}