/*
 * MusicUtils.java
 * Description: Music utilities.
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

package app.auditranscribe.utils;

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.music.MusicKey;

import java.util.HashSet;

/**
 * Music utilities.
 */
public final class MusicUtils {
    private MusicUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that converts all <code>#</code> to <code>♯</code> and all <code>b</code> to
     * <code>♭</code>.
     *
     * @param string The string to fancify.
     * @return The fancified string.
     */
    public static String fancifyMusicString(String string) {
        return string.replace('#', '♯').replace('b', '♭');
    }

}
