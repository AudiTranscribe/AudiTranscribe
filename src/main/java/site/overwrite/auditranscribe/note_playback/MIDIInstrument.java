/*
 * MIDIInstrument.java
 *
 * Created on 2022-06-10
 * Updated on 2022-06-10
 *
 * Description: Enum that contains the MIDI values of the instruments.
 */

package site.overwrite.auditranscribe.note_playback;

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
