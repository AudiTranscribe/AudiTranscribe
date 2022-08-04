/*
 * NotePlayerSequencer.java
 *
 * Created on 2022-06-09
 * Updated on 2022-07-17
 *
 * Description: Class that handles the playing of notes as a MIDI sequence.
 */

package site.overwrite.auditranscribe.music.notes;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import site.overwrite.auditranscribe.exceptions.generic.LengthException;
import site.overwrite.auditranscribe.exceptions.generic.ValueException;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.MusicUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Class that handles the playing of notes as a MIDI sequence.
 */
public class NotePlayerSequencer {
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

    private final Map<Triplet<Double, Double, Integer>, Pair<MidiEvent, MidiEvent>> allMIDIEventPairs = new HashMap<>();
    public boolean areNotesSet = false;

    /**
     * Initialization method for a multi-note player object.<br>
     * This object is able to handle MIDI data and play multiple notes.<br>
     * Ensure that code checks if the sequencer is available before attempting to play the sequence
     * of MIDI notes.
     */
    public NotePlayerSequencer() {
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
        MyLogger.log(
                Level.FINE,
                "Set note sequencer current time to " + sequencer.getMicrosecondPosition() + " µs",
                this.getClass().toString());
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
        MyLogger.log(Level.FINE, "Notes set on track", this.getClass().toString());
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
        MyLogger.log(Level.FINE, "Notes cleared from track", this.getClass().toString());
    }

    /**
     * Method that returns strings that represent the events that are currently on the track.
     *
     * @return A string that represents the events on the track.
     */
    public String[] getEventsOnTrack() {
        // Get all MIDI events
        MidiEvent[] allEvents = getMidiEventsFromTrack();

        // Convert each event into a string
        String[] strings = new String[allEvents.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = midiEventToHumanReadableString(allEvents[i]);
        }

        return strings;
    }

    /**
     * Method that neatly organizes the events on the track as a string.
     */
    public String eventsOnTrackToString() {
        StringBuilder sb = new StringBuilder();
        for (String s : getEventsOnTrack()) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    /**
     * Method that writes the MIDI sequence to a MIDI file.
     *
     * @param timeSignature  Time signature string (e.g. <code>6/8</code>, <code>3/4</code>).
     * @param key            Music key of the piece.
     * @param outputFilePath <b>Absolute</b> path to the output MIDI file.
     * @throws IOException If an IO exception occurs.
     */
    public void exportToMIDI(String timeSignature, String key, String outputFilePath) throws IOException {
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
     */
    public void play(double currTime) {
        // Check if there is a sequencer to use in the first place
        if (sequencer == null) {
            MyLogger.log(Level.INFO, "No sequencer to use, so not playing", this.getClass().toString());
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
        setCurrTime(currTime);

        // Start playback
        sequencer.start();
        MyLogger.log(Level.FINE, "Note sequencer playback started", this.getClass().toString());
    }

    /**
     * Stop playback of the MIDI sequence.
     */
    public void stop() {
        // Check if there is a sequencer to use in the first place
        if (sequencer == null) {
            MyLogger.log(Level.INFO, "No sequencer to use, so not stopping", this.getClass().toString());
            return;
        }

        // Attempt to stop the sequencer
        try {
            sequencer.stop();
            MyLogger.log(Level.FINE, "Note sequencer playback stopped", this.getClass().toString());
        } catch (IllegalStateException e) {
            MyLogger.log(Level.FINE, "Note sequencer playback is not running, not stopping", this.getClass().toString());
        }
    }

    /**
     * Close the note sequencer.
     */
    public void close() {
        // Check if there is a sequencer to use in the first place
        if (sequencer == null) {
            MyLogger.log(Level.INFO, "No sequencer to use, so not closing", this.getClass().toString());
            return;
        }

        // First stop the playback
        stop();

        // Then close sequencer
        sequencer.close();
        MyLogger.log(Level.FINE, "Note sequencer playback closed", this.getClass().toString());
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
     * Helper method that sets the time signature of the note player (and the track).<br>
     * The time signature set is <em>assumed</em> to make the metronome click once every 24 MIDI
     * clocks, and that there are eight 32nd notes per beat.<br>
     * This method also assumes that the <code>denominator</code> is a perfect power of 2.
     *
     * @param timeSignature Time signature string (e.g. <code>6/8</code>, <code>3/4</code>).
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
    private void setTimeSignatureOfNotePlayer(String timeSignature) {
        // Parse the time signature
        Pair<Integer, Integer> numeratorAndDenominator = MusicUtils.parseTimeSignature(timeSignature);
        int numerator = numeratorAndDenominator.getValue0();
        int denominator = numeratorAndDenominator.getValue1();

        // Check if the numerator and denominator values are valid
        if (!(numerator >= 0 && numerator <= 255 && denominator >= 0 && denominator <= 255))
            throw new ValueException("Numerator and denominator must be in the interval [0, 255]");

        // Check if the denominator is a power of 2
        if (!MathUtils.isPowerOf2(denominator)) throw new ValueException("Denominator must be a power of 2");

        // Create the time signature byte array
        byte[] timeSignatureByteArray = {
                (byte) numerator,
                (byte) ((int) MathUtils.log2(denominator)),
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
        // Get the key and mode of the provided key
        Pair<String, String> keyAndMode = MusicUtils.parseKeySignature(key);
        String modePart = keyAndMode.getValue1();

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
     * @param instrumentNum The instrument number to set. <b>Must be an instrument that is present
     *                      on Bank 0</b>.
     */
    // Fixme: changing instrument before playback is a little buggy; instrument does not change for first note but
    //        subsequent notes' playback has changed
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

    /**
     * Helper method that gets the MIDI events from the track.
     *
     * @return An array of MIDI events.
     */
    private MidiEvent[] getMidiEventsFromTrack() {
        // Get size of the track
        int n = track.size();

        // Create array of `MidiEvents` and return
        MidiEvent[] midiEvents = new MidiEvent[n];
        for (int i = 0; i < n; i++) {
            midiEvents[i] = track.get(i);
        }
        return midiEvents;
    }

    /**
     * Helper method that converts a MIDI event into a human-readable string.
     *
     * @param midiEvent The MIDI event to convert.
     * @return A string representation of the MIDI event.
     */
    private String midiEventToHumanReadableString(MidiEvent midiEvent) {
        // Get the MIDI message and tick from the `midiEvent`
        MidiMessage midiMessage = midiEvent.getMessage();
        long tick = midiEvent.getTick();

        // Convert the MIDI message to a human-readable string
        String messageString = MIDIMessageDecoder.midiMessageToString(midiMessage);

        // Return the string
        return messageString + " at " + tick + " µs";
    }
}
