/*
 * UnchangingDataPropertiesObject.java
 * Description: Data object that stores the unchanging data's properties.
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

package app.auditranscribe.io.audt_file.base.data_encapsulators;

/**
 * Data object that stores the unchanging data's properties.
 */
public abstract class UnchangingDataPropertiesObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 1;

    public static final int NUM_BYTES_NEEDED =
            4 +   // Section ID
                    4 +  // Number of skippable bytes
                    4;   // EOS delimiter

    // Attributes
    public int numSkippableBytes;
}
