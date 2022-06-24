/*
 * NotePlayerSequencerTest.java
 *
 * Created on 2022-06-09
 * Updated on 2022-06-19
 *
 * Description: Test `NotePlayerSequencer.java`.
 */

package site.overwrite.auditranscribe.notes;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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