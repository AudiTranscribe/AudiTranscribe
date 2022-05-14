/*
 * NotePlayer.java
 *
 * Created on 2022-05-14
 * Updated on 2022-05-14
 *
 * Description: Class that handles the playing of notes.
 */

package site.overwrite.auditranscribe.audio.note_synthesis;

import javafx.application.Platform;
import site.overwrite.auditranscribe.utils.UnitConversion;

import javax.sound.midi.*;
import java.security.InvalidParameterException;
import java.util.Map;

/**
 * Class that handles the playing of notes.
 */
public class NotePlayer {
    // Constants
    public final static Map<String, Integer> INSTRUMENT_TO_MIDI_INTEGER = Map.ofEntries(
            Map.entry("PIANO", 0),
            Map.entry("XYLOPHONE", 13),
            Map.entry("VIOLIN", 40),
            Map.entry("TRUMPET", 56),
            Map.entry("FLUTE", 73),
            Map.entry("DRUMS", 114)
    );

    // Attributes
    final Synthesizer midiSynth;
    final Soundbank soundbank;
    final Instrument[] instruments;
    final MidiChannel[] midiChannels;

    final int channelNum;

    /**
     * Initialization method for a note player object.
     *
     * @param instrument Instrument to play.
     * @param channelNum MIDI channel number to play the note on.
     * @throws MidiUnavailableException If MIDI is unavailable on the current system.
     */
    public NotePlayer(String instrument, int channelNum) throws MidiUnavailableException {
        // Get the MIDI synthesizer and open the device
        midiSynth = MidiSystem.getSynthesizer();
        midiSynth.open();

        // Get the soundbank, instruments, and channels available to the MIDI synthesizer
        soundbank = midiSynth.getDefaultSoundbank();
        instruments = soundbank.getInstruments();
        midiChannels = midiSynth.getChannels();

        // Update the MIDI channel number attribute
        this.channelNum = channelNum;

        // Set that channel's instrument
        int instrumentNumber = INSTRUMENT_TO_MIDI_INTEGER.get(instrument);
        midiChannels[channelNum].programChange(instrumentNumber);
    }

    // Public methods

    /**
     * Method that makes the specified note play indefinitely, until told to stop.
     *
     * @param noteNumber Note number of the note to play.
     * @param velocity   So-called <em>volume</em> to play the note at. This value should be in the
     *                   range [0, 127].
     * @throws InvalidParameterException If the note number is invalid (i.e. cannot be played in
     *                                   MIDI).
     * @see #noteOff(int, int)
     */
    public void noteOn(int noteNumber, int velocity) throws InvalidParameterException {
        // Get the MIDI number of the note number
        int midiNumber = UnitConversion.noteNumberToMIDINumber(noteNumber);

        // Ensure that the MIDI number found is not -1
        if (midiNumber == -1) {
            throw new InvalidParameterException("MIDI number cannot be found for note number " + noteNumber);
        }

        // Turn on the note on that channel
        midiChannels[channelNum].noteOn(midiNumber, velocity);
    }

    /**
     * Method that makes the specified note stop playing.
     *
     * @param noteNumber Note number of the note to play.
     * @param velocity   So-called <em>volume</em> to stop the note at. This value should be in the
     *                   range [0, 127].
     * @throws InvalidParameterException If the note number is invalid (i.e. cannot be played in
     *                                   MIDI).
     * @see #noteOn(int, int)
     */
    public void noteOff(int noteNumber, int velocity) throws InvalidParameterException {
        // Get the MIDI number of the note number
        int midiNumber = UnitConversion.noteNumberToMIDINumber(noteNumber);

        // Ensure that the MIDI number found is not -1
        if (midiNumber == -1) {
            throw new InvalidParameterException("MIDI number cannot be found for note number " + noteNumber);
        }

        // Turn on the note on that channel
        midiChannels[channelNum].noteOff(midiNumber, velocity);
    }

    /**
     * Method that plays the specified note.
     *
     * @param noteNumber  Note number of the note to play.
     * @param onVelocity  So-called <em>volume</em> to play the note at. This value should be in the
     *                    range [0, 127].
     * @param offVelocity So-called <em>volume</em> to stop the note at. This value should be in the
     *                    range [0, 127].
     * @param onDuration  How long this note should be played, in <b>milliseconds</b>.
     * @param offDuration How long this note should be offed, in <b>milliseconds</b>.
     * @throws InvalidParameterException If the note number is invalid (i.e. cannot be played in
     *                                   MIDI).
     */
    public void playNoteForDuration(int noteNumber, int onVelocity, int offVelocity, long onDuration, long offDuration) throws InvalidParameterException {
        // Get the MIDI number of the note number
        int midiNumber = UnitConversion.noteNumberToMIDINumber(noteNumber);

        // Ensure that the MIDI number found is not -1
        if (midiNumber == -1) {
            throw new InvalidParameterException("MIDI number cannot be found for note number " + noteNumber);
        }

        Platform.runLater(() -> {
            // Turn on the note on that channel
            midiChannels[channelNum].noteOn(midiNumber, onVelocity);

            // Wait for the specified duration
            try {
                Thread.sleep(onDuration);
            } catch (InterruptedException ignored) {
            }

            // Turn off that note
            midiChannels[channelNum].noteOff(midiNumber, offVelocity);
            try {
                Thread.sleep(offDuration);
            } catch (InterruptedException ignored) {
            }
        });
    }
}
