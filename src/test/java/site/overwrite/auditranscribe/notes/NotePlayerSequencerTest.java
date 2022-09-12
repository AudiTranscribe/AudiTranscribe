/*
 * NotePlayerSequencerTest.java
 * Description: Test the note player sequencer class.
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

package site.overwrite.auditranscribe.notes;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.music.notes.MIDIInstrument;
import site.overwrite.auditranscribe.music.notes.NotePlayerSequencer;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

class NotePlayerSequencerTest {
    @Disabled
    @Test
    void testNotePlayerSequencer() {
        // Define arrays
        double[] noteOnsetTimes = {
                0, 0.9, 1.2, 1.8, 2.4, 3.3, 3.6, 4.2
        };
        double[] noteDurations = {
                0.9, 0.3, 0.6, 0.6, 0.9, 0.3, 0.6, 0.6
        };
        String[] notes = {
                "E5", "D5", "F4", "G4", "C5", "B4", "E4", "F4"
        };

        // Convert notes to note numbers
        int[] noteNumbers = new int[notes.length];
        for (int i = 0; i < notes.length; i++) {
            noteNumbers[i] = UnitConversionUtils.noteToNoteNumber(notes[i]);
        }

        // Create a multi-note player object
        NotePlayerSequencer notePlayerSequencer = new NotePlayerSequencer();

        // Check if the sequencer is available
        if (notePlayerSequencer.isSequencerAvailable()) {
            // Set velocities
            notePlayerSequencer.setOnVelocity(94);
            notePlayerSequencer.setOffVelocity(64);

            // Set BPM
            notePlayerSequencer.setBPM(100);

            // Set instrument
            notePlayerSequencer.setInstrument(MIDIInstrument.FLUTE);

            // Set the notes
            notePlayerSequencer.setNotesOnTrack(noteOnsetTimes, noteDurations, noteNumbers);

            // Play the notes
            notePlayerSequencer.play(0);

            while (true) {
                // Exit the program when sequencer has stopped playing
                if (!notePlayerSequencer.getSequencer().isRunning()) {
                    notePlayerSequencer.stop();
                    break;
                }
            }
        } else {
            System.out.println("Sequencer is not available, skipping test.");
        }
    }
}