/*
 * MusicKeyTest.java
 * Description: Test `MusicKey.java`.
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

package app.auditranscribe.music;

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