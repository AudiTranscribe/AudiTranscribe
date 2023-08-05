/*
 * AUDTFileConstants.java
 * Description: Constants that are needed when processing AudiTranscribe files.
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

package app.auditranscribe.io.audt_file;

/**
 * Constants that are needed when processing AudiTranscribe files.
 */
public final class AUDTFileConstants {
    // Constants
    public static final byte[] AUDT_MAGIC_CONSTANT = {
            (byte) 0xad, (byte) 0x75, (byte) 0xc1, (byte) 0xbe
    };

    public static final byte[] AUDT_FILE_HEADING = {
            (byte) 0x41, (byte) 0x55, (byte) 0x44, (byte) 0x49,
            (byte) 0x54, (byte) 0x52, (byte) 0x41, (byte) 0x4e,
            (byte) 0x53, (byte) 0x43, (byte) 0x52, (byte) 0x49,
            (byte) 0x42, (byte) 0x45, (byte) 0x0a, (byte) 0x0a
    };
    public static final byte[] AUDT_SECTION_DELIMITER = {
            (byte) 0xe0, (byte) 0x5e, (byte) 0x05, (byte) 0xe5
    };
    public static final byte[] AUDT_END_OF_FILE_DELIMITER = {
            (byte) 0xe0, (byte) 0xfe, (byte) 0x0f, (byte) 0xef,
            (byte) 0xe0, (byte) 0xfe, (byte) 0x0f, (byte) 0xef
    };

    public static final int FILE_VERSION_NUMBER = 0x000C0001;  // File version 0.12.0, revision 1 -> 00 0C 00 01

    private AUDTFileConstants() {
        // Private constructor to signal this is a utility class
    }
}
