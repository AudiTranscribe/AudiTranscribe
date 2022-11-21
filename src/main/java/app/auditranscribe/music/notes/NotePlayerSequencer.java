/*
 * NotePlayerSequencer.java
 * Description: Class that handles the playing of notes as a MIDI sequence.
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

import app.auditranscribe.generic.ClassWithLogging;
import app.auditranscribe.generic.exceptions.LengthException;
import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.music.TimeSignature;
import app.auditranscribe.utils.MathUtils;
import app.auditranscribe.utils.MusicUtils;
import app.auditranscribe.utils.UnitConversionUtils;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Class that handles the playing of notes as a MIDI sequence.
 */
public class NotePlayerSequencer extends ClassWithLogging {
    // Constants
    public static int TICKS_PER_QUARTER = 10_000;  // Number of ticks per quarter note
    public static int MIDI_FILE_TYPE = 1;  // See https://tinyurl.com/2m9tcvzb for MIDI file types

    // Attributes
    private Track track;
    private Sequence sequence;
    private final Sequencer sequencer;

    public int onVelocity;
    public int offVelocity;

    public int instrumentNum;

    public double bpm;
    public double notePlayingDelayOffset;

    private final Map<Triple<Double, Double, Integer>, Pair<MidiEvent, MidiEvent>> allMIDIEventPairs = new HashMap<>();
    public boolean areNotesSet = false;

    /**
     * Initialization method for a multi-note player object.<br>
     * This object is able to handle MIDI data and play multiple notes.<br>
     * Ensure that code checks if the sequencer is available before attempting to play the sequence
     * of MIDI notes.
     */
    public NotePlayerSequencer(double notePlayingDelayOffset) {
        // Get MIDI sequencer
        Sequencer tempSequencer;  // So that we may assign null later

        try {
            tempSequencer = MidiSystem.getSequencer();
            tempSequencer.open();
        } catch (MidiUnavailableException e) {
            tempSequencer = null;
        }

        sequencer = tempSequencer;

        // Create the sequence and track to use
        sequence = null;
        track = null;

        try {
            sequence = new Sequence(Sequence.PPQ, TICKS_PER_QUARTER, 1);
            track = sequence.createTrack();

        } catch (InvalidMidiDataException ignored) {
        }

        // Update other attributes
        this.notePlayingDelayOffset = notePlayingDelayOffset;
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

    /**
     * Method that sets the current time of the MIDI sequencer playback.
     *
     * @param currTime Time to set the sequencer to, <b>in seconds</b>.
     */
    public void setCurrTime(double currTime) {
        sequencer.setMicrosecondPosition((long) (currTime * 1e6));
        log(Level.FINE, "Set note sequencer current time to " + sequencer.getMicrosecondPosition() + " µs");
    }

    // Public methods

    /**
     * Method that checks if the sequencer is available.
     *
     * @return A boolean, <code>true</code> if the sequencer is available, <code>false</code>
     * otherwise.
     */
    public boolean isSequencerAvailable() {
        return sequencer != null;
    }

    /**
     * Method that sets notes on the track.
     *
     * @param noteOnsetTimes The onset times of the notes to set.
     * @param durations      The durations of the notes to set.
     * @param noteNums       The note numbers of the notes to set.
     * @param isSlowed       Whether the playback of the notes should be slowed.
     */
    public void setNotesOnTrack(
            double[] noteOnsetTimes, double[] durations, int[] noteNums, boolean isSlowed
    ) {
        // Check that the three arrays are of the same length
        int noteOnsetTimesLength = noteOnsetTimes.length;
        int durationsLength = durations.length;
        int noteNumsLength = noteNums.length;

        if (!(noteOnsetTimesLength == durationsLength && durationsLength == noteNumsLength)) {
            throw new LengthException("The three arrays must be of the same length");
        }

        // Clear the existing MIDI events
        clearNotesOnTrack();

        // Add new MIDI events
        for (int i = 0; i < noteOnsetTimesLength; i++) {
            // Determine the actual time to set
            double onsetTime = noteOnsetTimes[i];
            double duration = durations[i];

            if (isSlowed) {
                onsetTime *= 2;
                duration *= 2;
            }

            addNote(onsetTime, duration, noteNums[i]);
        }

        // Update the `areNotesSet` flag
        areNotesSet = true;
        log(Level.FINE, "Set notes on track");
    }

    /**
     * Method that clears all the notes that are currently present on the track.
     */
    public void clearNotesOnTrack() {
        Set<Triple<Double, Double, Integer>> keys = new HashSet<>(allMIDIEventPairs.keySet());  // Make copy

        for (Triple<Double, Double, Integer> key : keys) {
            removeNote(key.value0(), key.value1(), key.value2());
        }
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        log(Level.FINE, "Cleared notes from track");
    }

    /**
     * Method that writes the MIDI sequence to a MIDI file.
     *
     * @param timeSignature  Time signature of the MIDI sequence.
     * @param key            Music key of the piece.
     * @param outputFilePath <b>Absolute</b> path to the output MIDI file.
     * @throws IOException If an IO exception occurs.
     */
    public void exportToMIDI(TimeSignature timeSignature, String key, String outputFilePath) throws IOException {
        setTimeSignatureOfNotePlayer(timeSignature);
        setKeySignatureOfNotePlayer(key);
        setTempoOfNotePlayer((float) bpm);
        setInstrumentOfNotePlayer(instrumentNum);

        // Write to file
        MidiSystem.write(sequence, MIDI_FILE_TYPE, new File(outputFilePath));
    }

    /**
     * Start playback of the MIDI sequence.
     *
     * @param currTime The time to start playback at, <b>in seconds</b>.
     * @param isSlowed Whether the playback of the notes should be slowed.
     */
    public void play(double currTime, boolean isSlowed) {
        // Check if there is a sequencer to use in the first place
        if (sequencer == null) {
            log(Level.INFO, "No sequencer to use, so not playing");
            return;
        }

        // Set tempo
        setTempoOfNotePlayer((float) bpm);

        // Set instrument
        setInstrumentOfNotePlayer(instrumentNum);

        // Set sequencer's sequence
        try {
            sequencer.setSequence(sequence);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }

        // Set current time
        setCurrTime((isSlowed ? currTime * 2 : currTime) + notePlayingDelayOffset);

        // Start playback
        sequencer.start();
        log(Level.FINE, "Note sequencer playback started");
    }

    /**
     * Stop playback of the MIDI sequence.
     */
    public void stop() {
        // Check if there is a sequencer to use in the first place
        if (sequencer == null) {
            log(Level.INFO, "No sequencer to use; not stopping");
            return;
        }

        // Attempt to stop the sequencer
        try {
            sequencer.stop();
            log(Level.FINE, "Note sequencer playback stopped");
        } catch (IllegalStateException e) {
            log(Level.FINE, "Note sequencer playback is not running; not stopping");
        }
    }

    /**
     * Close the note sequencer.
     */
    public void close() {
        // Check if there is a sequencer to use in the first place
        if (sequencer == null) {
            log(Level.INFO, "No sequencer to use; not closing");
            return;
        }

        // First stop the playback
        stop();

        // Then close sequencer
        sequencer.close();
        log(Level.FINE, "Note sequencer playback closed");
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
        allMIDIEventPairs.put(new Triple<>(noteOnsetTime, duration, noteNum), new Pair<>(onEvent, offEvent));
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
        Pair<MidiEvent, MidiEvent> eventPair = allMIDIEventPairs.remove(new Triple<>(noteOnsetTime, duration, noteNum));
        if (eventPair == null) return;

        MidiEvent onEvent = eventPair.value0();
        MidiEvent offEvent = eventPair.value1();

        // Remove the events from the track
        track.remove(onEvent);
        track.remove(offEvent);
    }

    /**
     * Helper method that sets the time signature of the note player (and the track).<br>
     * The time signature set is <em>assumed</em> to make the metronome click once every 24 MIDI
     * clocks, and that there are eight 32nd notes per beat.<br>
     * This method also assumes that the <code>denominator</code> is a perfect power of 2.
     *
     * @param timeSignature Time signature to set.
     * @throws ValueException If: <ul>
     *                        <li>
     *                        Either the <code>numerator</code> or <code>denominator</code> does not
     *                        lie in the interval [0, 255].
     *                        </li>
     *                        <li>
     *                        The <code>denominator</code> is not a power of 2.
     *                        </li>
     *                        </ul>
     */
    private void setTimeSignatureOfNotePlayer(TimeSignature timeSignature) {
        // Create the time signature byte array
        byte[] timeSignatureByteArray = {
                (byte) timeSignature.beatsPerBar,
                (byte) ((int) MathUtils.log2(timeSignature.denominator.numericValue)),
                0x18,  // Metronome click once every 24 = 0x18 MIDI clocks
                0x08   // Eight 32nd notes per beat
        };

        // Create the meta message
        MetaMessage metaMessage = new MetaMessage();
        try {
            metaMessage.setMessage(0x58, timeSignatureByteArray, 4);  // 0x58 is time signature message
        } catch (InvalidMidiDataException ignored) {
        }

        // Add to track
        track.add(new MidiEvent(metaMessage, 0));
    }

    /**
     * Helper method that sets the key signature of the note player (and the track).
     *
     * @param key The key to set.
     */
    private void setKeySignatureOfNotePlayer(String key) {
        // Get the tonic and mode of the provided key
        Pair<String, String> tonicAndMode = MusicUtils.parseKeySignature(key);
        String modePart = tonicAndMode.value1();

        // Get the numeric value of the key
        int numericValue = MusicUtils.getNumericValueOfKey(key);

        // Create the key signature byte array
        byte[] keySignatureByteArray = {
                (byte) numericValue,
                (byte) (Objects.equals(modePart, "Major") ? 0x00 : 0x01)
        };

        // Create the meta message
        MetaMessage metaMessage = new MetaMessage();
        try {
            metaMessage.setMessage(0x59, keySignatureByteArray, 2);  // 0x59 is key signature message
        } catch (InvalidMidiDataException ignored) {
        }

        // Add to track
        track.add(new MidiEvent(metaMessage, 0));
    }

    /**
     * Helper method that sets the BPM of the note player (and the track).
     *
     * @param bpm Beats per minute, as a <b>float</b> (and not a double).
     */
    private void setTempoOfNotePlayer(float bpm) {
        // Get the number of microseconds per beat
        long microsecondsPerBeat = (long) (6e7 / bpm);  // 6e7 microseconds per minute

        // Create the tempo byte array
        byte[] tempoByteArray = new byte[]{0, 0, 0};

        for (int i = 0; i < 3; i++) {
            // Calculate bit shift amount
            int bitShift = (3 - (i + 1)) * 8;

            // Compute byte to place in array
            tempoByteArray[i] = (byte) (microsecondsPerBeat >> bitShift);
        }

        // Create the meta message
        MetaMessage metaMessage = new MetaMessage();
        try {
            metaMessage.setMessage(0x51, tempoByteArray, 3);  // 0x51 is tempo message
        } catch (InvalidMidiDataException ignored) {
        }

        // Add to track
        track.add(new MidiEvent(metaMessage, 0));

        // Set sequencer BPM
        sequencer.setTempoInBPM(bpm);
    }

    /**
     * Helper method that sets the instrument of the note player.
     *
     * @param instrumentNum The instrument number to set.<br>
     *                      <b>Must be an instrument that is present on Bank 0</b>.
     */
    // Fixme: Setting instrument type is buggy; although MIDI exports for instrument is correct, playback using the
    //        changed instrument does not change first note's instrument, but changes subsequent notes
    private void setInstrumentOfNotePlayer(int instrumentNum) {
        try {
            // Define messages to set the instrument
            ShortMessage bankSelect = new ShortMessage(
                    ShortMessage.CONTROL_CHANGE, 0, 0, instrumentNum >> 7
            );
            ShortMessage bankSelectLSB = new ShortMessage(
                    ShortMessage.CONTROL_CHANGE, 0, 32, instrumentNum & 0x7F
            );
            ShortMessage changeInstrumentMessage = new ShortMessage(
                    ShortMessage.PROGRAM_CHANGE, 0, instrumentNum, 0
            );

            // Add to track
            track.add(new MidiEvent(bankSelect, 0));
            track.add(new MidiEvent(bankSelectLSB, 0));
            track.add(new MidiEvent(changeInstrumentMessage, 0));

        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }
}
