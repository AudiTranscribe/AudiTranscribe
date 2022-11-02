/*
 * TimeSignature.java
 * Description: Enum that contains all the supported time signatures of AudiTranscribe.
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
 * Enum that contains all the supported time signatures of AudiTranscribe.
 */
public enum TimeSignature {
    // Enum values
    TWO_TWO(2, NoteUnit.HALF_NOTE),
    THREE_TWO(3, NoteUnit.HALF_NOTE),
    FOUR_TWO(4, NoteUnit.HALF_NOTE),

    TWO_FOUR(2, NoteUnit.QUARTER_NOTE),
    THREE_FOUR(3, NoteUnit.QUARTER_NOTE),
    FOUR_FOUR(4, NoteUnit.QUARTER_NOTE),
    FIVE_FOUR(5, NoteUnit.QUARTER_NOTE),
    SIX_FOUR(6, NoteUnit.QUARTER_NOTE),

    THREE_EIGHT(3, NoteUnit.EIGHTH_NOTE),
    SIX_EIGHT(6, NoteUnit.EIGHTH_NOTE),
    SEVEN_EIGHT(7, NoteUnit.EIGHTH_NOTE),
    NINE_EIGHT(9, NoteUnit.EIGHTH_NOTE),
    TWELVE_EIGHT(12, NoteUnit.EIGHTH_NOTE);

    // Attributes
    public final int beatsPerBar;
    public final NoteUnit denominator;

    // Enum constructor
    TimeSignature(int beatsPerBar, NoteUnit denominator) {
        this.beatsPerBar = beatsPerBar;
        this.denominator = denominator;
    }

    // Overriden methods

    @Override
    public String toString() {
        return displayText();
    }

    // Public methods

    /**
     * Obtains the display text for the time signature.
     *
     * @return The display text to display.
     */
    public String displayText() {
        return beatsPerBar + "/" + denominator.numericValue;
    }

    /**
     * Method that obtains the correct <code>TimeSignature</code> value from the display text.
     *
     * @param displayText Display text of the time signature.
     * @return The <code>TimeSignature</code> value.<br>
     * Returns <code>null</code> if not found.
     */
    public static TimeSignature displayTextToTimeSignature(String displayText) {
        // Get beats per bar and denominator value
        String[] split = displayText.split("/");
        int beatsPerBar = Integer.parseInt(split[0]);
        NoteUnit denominator = NoteUnit.numericValueToNoteUnit(Integer.parseInt(split[1]));

        // Get matching time signature
        for (TimeSignature timeSignature : TimeSignature.values()) {
            if (timeSignature.beatsPerBar == beatsPerBar && timeSignature.denominator == denominator) {
                return timeSignature;
            }
        }

        return null;
    }
}
