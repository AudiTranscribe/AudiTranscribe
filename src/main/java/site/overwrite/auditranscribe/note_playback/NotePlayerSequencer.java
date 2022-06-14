/*
 * NotePlayerSequencer.java
 *
 * Created on 2022-06-09
 * Updated on 2022-06-14
 *
 * Description: Class that handles the playing of notes as a MIDI sequence.
 */

package site.overwrite.auditranscribe.note_playback;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import site.overwrite.auditranscribe.exceptions.LengthException;
import site.overwrite.auditranscribe.exceptions.ValueException;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.midi.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that handles the playing of notes as a MIDI sequence.
 */
public class NotePlayerSequencer {
    // Constants
    public static int TICKS_PER_QUARTER = 100_000;

    // Attributes
    private Track track;  // Midi track
    private Sequence sequence;  // Midi sequence
    private final Sequencer sequencer;

    public int onVelocity;
    public int offVelocity;

    public int instrumentNum;

    public double bpm;

    private final Map<Triplet<Double, Double, Integer>, Pair<MidiEvent, MidiEvent>> allMIDIEventPairs = new HashMap<>();
    public boolean areNotesSet = false;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Initialization method for a multi-note player object.<br>
     * This object is able to handle MIDI data and play multiple notes.
     */
    public NotePlayerSequencer() {
        // Get default sequencer
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException("MIDI is unavailable on this system");
        }

        // Create the sequence and track to use
        sequence = null;
        track = null;

        try {
            sequence = new Sequence(Sequence.PPQ, TICKS_PER_QUARTER, 1);
            track = sequence.createTrack();

        } catch (InvalidMidiDataException ignored) {
        }
    }

    // Getter/Setter methods

    public Sequencer getSequencer() {
        return sequencer;
    }

    public void setOnVelocity(int onVelocity) {
        this.onVelocity = onVelocity;
    }

    public void setOffVelocity(int offVelocity) {
        this.offVelocity = offVelocity;
    }

    public void setBPM(double bpm) {
        this.bpm = bpm;
    }

    public void setInstrument(MIDIInstrument instrument) {
        this.instrumentNum = instrument.midiNumber;
    }

    // Public methods

    /**
     * Method that sets the current time of the MIDI sequencer playback.
     *
     * @param currTime Time to set the sequencer to, <b>in seconds</b>.
     */
    public void setCurrTime(double currTime) {
        sequencer.setMicrosecondPosition((long) (currTime * 1e6));
        logger.log(Level.FINE, "Current time set to " + currTime);
    }

    /**
     * Method that sets notes on the track.
     *
     * @param noteOnsetTimes The onset times of the notes to set.
     * @param durations      The durations of the notes to set.
     * @param noteNums       The note numbers of the notes to set.
     */
    public void setNotesOnTrack(double[] noteOnsetTimes, double[] durations, int[] noteNums) {
        // Check that the three arrays are of the same length
        if (noteOnsetTimes.length != durations.length || noteOnsetTimes.length != noteNums.length) {
            throw new LengthException("The three arrays must be of the same length");
        }

        // Clear the existing MIDI events
        clearNotesOnTrack();

        // Add new MIDI events
        int n = noteOnsetTimes.length;
        for (int i = 0; i < n; i++) {
            addNote(noteOnsetTimes[i], durations[i], noteNums[i]);
        }

        // Update the `areNotesSet` flag
        areNotesSet = true;
        logger.log(Level.FINE, "Notes set on track");
    }

    /**
     * Method that clears all the notes that are currently present on the track.
     */
    public void clearNotesOnTrack() {
        Set<Triplet<Double, Double, Integer>> keys = new HashSet<>(allMIDIEventPairs.keySet());  // Make copy

        for (Triplet<Double, Double, Integer> key : keys) {
            removeNote(key.getValue0(), key.getValue1(), key.getValue2());
        }
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        logger.log(Level.FINE, "Notes cleared from track");
    }

    public void play() {
        // Set tempo
        sequencer.setTempoInBPM((float) bpm);

        // Set instrument
        setInstrumentOfNotePlayer(instrumentNum);

        // Set sequencer's sequence
        try {
            sequencer.setSequence(sequence);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        // Start playback
        sequencer.start();
        logger.log(Level.FINE, "Note sequence playback started");
    }

    public void stop() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            logger.log(Level.FINE, "Note sequence playback stopped");
        } else {
            logger.log(Level.FINE, "Note sequence playback is not running, not stopping");
        }
    }

    public void close() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            sequencer.close();
            logger.log(Level.FINE, "Note sequence playback closed");
        } else {
            logger.log(Level.FINE, "Note sequence playback is not running, not closing");
        }
    }

    // Private methods

    /**
     * Helper method that adds a note to the MIDI sequence.
     *
     * @param noteOnsetTime Time at which the note should start, <b>in seconds</b>.
     * @param duration      Time the note should last, <b>in seconds</b>.
     * @param noteNum       Note number to play.
     * @throws ValueException If the velocities and BPM have not been set yet.
     */
    private void addNote(double noteOnsetTime, double duration, int noteNum) {
        // Check if the velocities and BPM have been set
        if (onVelocity == 0 || offVelocity == 0 || bpm == 0) {
            throw new ValueException("Velocities and BPM must be set before adding notes");
        }

        // Convert the note number to the MIDI number
        int midiNum = UnitConversionUtils.noteNumberToMIDINumber(noteNum);

        // Define the "on" message of the note
        ShortMessage onMessage = new ShortMessage();
        try {
            onMessage.setMessage(ShortMessage.NOTE_ON, 0, midiNum, onVelocity);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        // Define the "off" message of the note
        ShortMessage offMessage = new ShortMessage();
        try {
            offMessage.setMessage(ShortMessage.NOTE_OFF, 0, midiNum, offVelocity);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        // Convert the timings to beats
        long noteOnsetTimeInTicks = (long) (noteOnsetTime * (bpm / 60) * TICKS_PER_QUARTER);
        long durationInTicks = (long) (duration * (bpm / 60) * TICKS_PER_QUARTER);

        // Pass these messages into their own MIDI events
        MidiEvent onEvent = new MidiEvent(onMessage, noteOnsetTimeInTicks);
        MidiEvent offEvent = new MidiEvent(offMessage, noteOnsetTimeInTicks + durationInTicks);

        // Add the events to the track and to the set of all MIDI events
        track.add(onEvent);
        track.add(offEvent);
        allMIDIEventPairs.put(new Triplet<>(noteOnsetTime, duration, noteNum), new Pair<>(onEvent, offEvent));
    }

    /**
     * Helper method that removes a note from the MIDI sequence.
     *
     * @param noteOnsetTime Time at which the note would start, <b>in seconds</b>.
     * @param duration      Time the note would last, <b>in seconds</b>.
     * @param noteNum       Note number that would play.
     */
    private void removeNote(double noteOnsetTime, double duration, int noteNum) {
        // Get the MIDI event pair that corresponds to this
        Pair<MidiEvent, MidiEvent> eventPair = allMIDIEventPairs.remove(new Triplet<>(noteOnsetTime, duration, noteNum));
        if (eventPair == null) return;

        MidiEvent onEvent = eventPair.getValue0();
        MidiEvent offEvent = eventPair.getValue1();

        // Remove the events from the track
        track.remove(onEvent);
        track.remove(offEvent);
    }

    /**
     * Helper method that sets the instrument of the note player.
     *
     * @param instrumentNum The instrument number to set. <b>Must be an instrument that is present
     *                      on Bank 0</b>.
     */
    // Fixme: changing instrument before playback is a little buggy; instrument does not change for first note but
    //        subsequent notes' playback has changed
    private void setInstrumentOfNotePlayer(int instrumentNum) {
        try {
            // Define messages to set the instrument
            ShortMessage bankSelect = new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 0, instrumentNum >> 7);
            ShortMessage bankSelectLSB = new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 32, instrumentNum & 0x7F);
            ShortMessage changeInstrumentMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, 0, instrumentNum, 0);

            // Add to track
            track.add(new MidiEvent(bankSelect, 0));
            track.add(new MidiEvent(bankSelectLSB, 0));
            track.add(new MidiEvent(changeInstrumentMessage, 0));

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

    }
}
