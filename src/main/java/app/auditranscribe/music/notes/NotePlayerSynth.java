/*
 * NotePlayerSynth.java
 * Description: Class that handles the playing of notes using the synthesizer.
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

import app.auditranscribe.generic.exceptions.ValueException;
import app.auditranscribe.utils.UnitConversionUtils;

import javax.sound.midi.*;
import java.util.HashSet;

/**
 * Class that handles the playing of notes using the synthesizer.
 */
public class NotePlayerSynth {
    // Attributes
    final Synthesizer midiSynth;
    final Soundbank soundbank;
    final Instrument[] instruments;
    final MidiChannel[] midiChannels;
    final int channelNum;

    HashSet<Integer> onNotes = new HashSet<>();  // Keeps track of the notes that are currently on

    /**
     * Initialization method for a note player object.
     *
     * @param instrument Instrument to play.
     * @param channelNum MIDI channel number to play the note on.
     */
    public NotePlayerSynth(MIDIInstrument instrument, int channelNum) throws MidiUnavailableException {
        // Get the MIDI synthesizer and open the device
        try {
            midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();
        } catch (MidiUnavailableException e) {
            throw new MidiUnavailableException("MIDI is unavailable on this system");
        }

        // Get the soundbank, instruments, and channels available to the MIDI synthesizer
        soundbank = midiSynth.getDefaultSoundbank();
        instruments = soundbank.getInstruments();
        midiChannels = midiSynth.getChannels();

        // Update the MIDI channel number attribute
        this.channelNum = channelNum;

        // Set that channel's instrument
        int instrumentNumber = instrument.midiNumber;
        midiChannels[channelNum].programChange(instrumentNumber);
    }

    // Public methods

    /**
     * Method that silences the note playing channel.
     */
    public void silenceChannel() {
        midiChannels[channelNum].allNotesOff();
        onNotes.clear();
    }

    /**
     * Method that makes the specified note play indefinitely, until told to stop.
     *
     * @param noteNumber   Note number of the note to play.
     * @param velocity     So-called <em>volume</em> to play the note at. This value should be in
     *                     the range [0, 127].
     * @param failSilently Whether the program should not throw errors.
     * @throws ValueException If the note number is invalid (i.e. cannot be played in MIDI) and
     *                        <code>failSilently</code> is <code>false</code>.
     * @see #noteOff(int, int)
     */
    public void noteOn(int noteNumber, int velocity, boolean failSilently) {
        // Get the MIDI number of the note number
        int midiNumber = UnitConversionUtils.noteNumberToMIDINumber(noteNumber);

        // Ensure that the MIDI number found is not -1
        if (midiNumber == -1 && !failSilently) {
            throw new ValueException("MIDI number cannot be found for note number " + noteNumber);
        }

        // Turn on the note on that channel
        if (!onNotes.contains(midiNumber)) {
            midiChannels[channelNum].noteOn(midiNumber, velocity);
            onNotes.add(midiNumber);
        }
    }

    /**
     * Method that makes the specified note play indefinitely, until told to stop.<br>
     * Invalid notes will not play.
     *
     * @param noteNumber Note number of the note to play.
     * @param velocity   So-called <em>volume</em> to play the note at. This value should be in the
     *                   range [0, 127].
     * @see #noteOff(int, int)
     */
    public void noteOn(int noteNumber, int velocity) {
        noteOn(noteNumber, velocity, true);
    }

    /**
     * Method that makes the specified note stop playing.
     *
     * @param noteNumber   Note number of the note to play.
     * @param velocity     So-called <em>volume</em> to stop the note at. This value should be in
     *                     the range [0, 127].
     * @param failSilently Whether the program should not throw errors.
     * @throws ValueException If the note number is invalid (i.e. cannot be played in MIDI) and
     *                        <code>failSilently</code> is <code>false</code>.
     * @see #noteOn(int, int)
     */
    public void noteOff(int noteNumber, int velocity, boolean failSilently) {
        // Get the MIDI number of the note number
        int midiNumber = UnitConversionUtils.noteNumberToMIDINumber(noteNumber);

        // Ensure that the MIDI number found is not -1
        if (midiNumber == -1 && !failSilently) {
            throw new ValueException("MIDI number cannot be found for note number " + noteNumber);
        }

        // Turn on the note on that channel
        if (onNotes.contains(midiNumber)) {
            midiChannels[channelNum].noteOff(midiNumber, velocity);
            onNotes.remove(midiNumber);
        }
    }

    /**
     * Method that makes the specified note stop playing.<br>
     * Invalid notes will not play.
     *
     * @param noteNumber Note number of the note to play.
     * @param velocity   So-called <em>volume</em> to stop the note at. This value should be in the
     *                   range [0, 127].
     * @see #noteOn(int, int)
     */
    public void noteOff(int noteNumber, int velocity) {
        noteOff(noteNumber, velocity, true);
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
     * @throws ValueException If the note number is invalid (i.e. cannot be played in MIDI).
     */
    public void playNoteForDuration(
            int noteNumber, int onVelocity, int offVelocity, long onDuration, long offDuration
    ) {
        // Get the MIDI number of the note number
        int midiNumber = UnitConversionUtils.noteNumberToMIDINumber(noteNumber);

        // Ensure that the MIDI number found is not -1
        if (midiNumber == -1) {
            throw new ValueException("MIDI number cannot be found for note number " + noteNumber);
        }

        // Play the sound
        Thread playSoundThread = new Thread(() -> {
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
        playSoundThread.start();
    }
}
