/*
 * NoteQuantizationUnit.java
 * Description: Enum that contains the possible note quantization units.
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

package site.overwrite.auditranscribe.music.notes;

/**
 * Enum that contains the possible note quantization units.
 */
public enum NoteQuantizationUnit {
    // Enum values
    THIRTY_SECOND_NOTE("Thirty-Second Note", 32),
    SIXTEENTH_NOTE("Sixteenth Note", 16),
    EIGHTH_NOTE("Eighth Note", 8),
    QUARTER_NOTE("Quarter Note", 4);

    // Attribute
    public final String name;
    public final int numericValue;

    // Enum constructor
    NoteQuantizationUnit(String name, int numericValue) {
        this.name = name;
        this.numericValue = numericValue;
    }

    // Overridden methods
    @Override
    public String toString() {
        return name;
    }
}
