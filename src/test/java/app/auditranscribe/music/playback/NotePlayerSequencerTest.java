package app.auditranscribe.music.playback;

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.TimeSignature;
import app.auditranscribe.utils.HashingUtils;
import app.auditranscribe.utils.UnitConversionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

class NotePlayerSequencerTest {
    static final boolean IS_TEST_ENABLED = false;

    // Test constants
    static double[] noteOnsetTimes = {
            0, 0.9, 1.2, 1.8, 2.4, 3.3, 3.6, 4.2
    };
    static double[] noteDurations = {
            0.9, 0.3, 0.6, 0.6, 0.9, 0.3, 0.6, 0.6
    };
    static String[] notes = {
            "E5", "D5", "F4", "G4", "C5", "B4", "E4", "F4"
    };
    static int[] noteNumbers = new int[notes.length];

    static NotePlayerSequencer notePlayerSequencer;

    @BeforeAll
    static void beforeAll() {
        // Convert notes to note numbers
        for (int i = 0; i < notes.length; i++) {
            noteNumbers[i] = UnitConversionUtils.noteToNoteNumber(notes[i]);
        }

        // Create a multi-note player object
        notePlayerSequencer = new NotePlayerSequencer(0);

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
            notePlayerSequencer.setNotesOnTrack(noteOnsetTimes, noteDurations, noteNumbers, false);
        }
    }

    // Tests
    @Test
    @DisabledIf("isTestDisabled")
    void playbackNotes() {
        if (notePlayerSequencer.isSequencerAvailable()) {
            // Play the notes
            notePlayerSequencer.play(0, false);

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

    @Test
    @DisabledIf("isTestDisabled")
    void exportToMIDI() throws IOException, NoSuchAlgorithmException {
        String filePath = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "test-files", "test-MIDI-file.midi"
        );

        try {
            if (notePlayerSequencer.isSequencerAvailable()) {
                notePlayerSequencer.exportToMIDI(TimeSignature.FOUR_FOUR, MusicKey.C_MAJOR, filePath);
            }

            Assertions.assertEquals(
                    "4aaf16de62f0cb63558e6544e8ffaed657c641d3",
                    HashingUtils.getHash(new File(filePath), "SHA1")
            );
        } finally {
            IOMethods.delete(filePath);
        }
    }

    // Helper methods
    boolean isTestDisabled() {
        return !IS_TEST_ENABLED;
    }
}