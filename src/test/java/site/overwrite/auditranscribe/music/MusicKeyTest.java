/*
 * MusicKeyTest.java
 *
 * Created on 2022-08-04
 * Updated on 2022-08-04
 *
 * Description: Test `MusicKey.java`.
 */

package site.overwrite.auditranscribe.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MusicKeyTest {
    @Test
    void getMusicKey() {
        // The full suite of tests will be conducted in `MusicUtilsTest`, not here
        assertEquals(MusicKey.A_MAJOR, MusicKey.getMusicKey("A Major"));
        assertEquals(MusicKey.A_SHARP_MINOR, MusicKey.getMusicKey("A# Minor"));
        assertEquals(MusicKey.A_FLAT_MAJOR, MusicKey.getMusicKey("Ab Major"));
    }

    @Test
    void testToString() {
        assertEquals("A Major", MusicKey.A_MAJOR.toString());
        assertEquals("A♯ Minor", MusicKey.A_SHARP_MINOR.toString());
        assertEquals("A♭ Major", MusicKey.A_FLAT_MAJOR.toString());
    }
}