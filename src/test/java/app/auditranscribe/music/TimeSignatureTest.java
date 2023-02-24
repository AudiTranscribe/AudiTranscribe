package app.auditranscribe.music;

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