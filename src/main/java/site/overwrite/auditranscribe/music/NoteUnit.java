/*
 * NoteUnit.java
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

package site.overwrite.auditranscribe.music;

/**
 * Enum that contains the possible note quantization units.
 */
public enum NoteUnit {
    // Enum values
    HALF_NOTE("Half Note", 2),
    QUARTER_NOTE("Quarter Note", 4),
    EIGHTH_NOTE("Eighth Note", 8),
    SIXTEENTH_NOTE("Sixteenth Note", 16),
    THIRTY_SECOND_NOTE("Thirty-Second Note", 32);

    // Attribute
    public final String name;
    public final int numericValue;

    // Enum constructor
    NoteUnit(String name, int numericValue) {
        this.name = name;
        this.numericValue = numericValue;
    }

    // Public methods

    /**
     * Method that retrieves the correct <code>NoteUnit</code> value given the numeric value of the note.
     *
     * @param numericValue Numeric value of the note.
     * @return The <code>NoteUnit</code> value.<br>
     * Returns <code>null</code> if not found.
     */
    public static NoteUnit numericValueToNoteUnit(int numericValue) {
        for (NoteUnit noteUnit : NoteUnit.values()) if (numericValue == noteUnit.numericValue) return noteUnit;
        return null;
    }

    // Overridden methods
    @Override
    public String toString() {
        return name;
    }
}
