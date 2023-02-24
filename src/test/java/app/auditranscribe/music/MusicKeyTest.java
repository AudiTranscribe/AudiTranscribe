package app.auditranscribe.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicKeyTest {
    @Test
    void getMusicKey() {
        assertEquals(MusicKey.C_MAJOR, MusicKey.getMusicKey("C Major"));
        assertNull(MusicKey.getMusicKey("Fake key"));

        assertEquals(MusicKey.B_MAJOR, MusicKey.getMusicKey((short) 14));
        assertNull(MusicKey.getMusicKey((short) -1));
    }

    @Test
    void testToString() {
        assertEquals("C Major", MusicKey.C_MAJOR.toString());
        assertEquals("G♯ Minor", MusicKey.G_SHARP_MINOR.toString());
        assertEquals("E♭ Major", MusicKey.E_FLAT_MAJOR.toString());
    }
}