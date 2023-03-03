package app.auditranscribe.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicUtilsTest {
    @Test
    void fancifyMusicString() {
        assertEquals("C Major", MusicUtils.fancifyMusicString("C Major"));
        assertEquals("C♯ Minor", MusicUtils.fancifyMusicString("C# Minor"));
        assertEquals("C♭ Major", MusicUtils.fancifyMusicString("Cb Major"));
        assertEquals("C♯ Minor", MusicUtils.fancifyMusicString("C♯ Minor"));
        assertEquals("C♭ Major", MusicUtils.fancifyMusicString("C♭ Major"));
    }
}