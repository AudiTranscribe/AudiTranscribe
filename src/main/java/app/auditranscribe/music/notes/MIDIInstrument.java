/*
 * MIDIInstrument.java
 * Description: Enum that contains the MIDI values of the instruments.
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

package app.auditranscribe.music.notes;

/**
 * Enum that contains the MIDI values of the instruments.
 */
public enum MIDIInstrument {
    // Enum values
    PIANO(0),
    XYLOPHONE(13),
    VIOLIN(40),
    TRUMPET(56),
    FLUTE(73);

    // Attributes
    public final int midiNumber;

    // Enum constructor
    MIDIInstrument(int midiNumber) {
        this.midiNumber = midiNumber;
    }
}
