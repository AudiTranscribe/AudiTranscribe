/*
 * MIDIMessageDecoder.java
 *
 * Created on 2022-06-14
 * Updated on 2022-06-28
 *
 * Description: Class that contains methods to decode MIDI messages.
 */

package site.overwrite.auditranscribe.music.notes;

import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.midi.*;
import java.util.HexFormat;

/**
 * Class that contains methods to decode MIDI messages.
 */
public final class MIDIMessageDecoder {
    // Constants
    // (Following https://www.fourmilab.ch/webtools/midicsv/)
    public static final String NOTE_ON = "NOTE_ON_C";
    public static final String NOTE_OFF = "NOTE_OFF_C";
    public static final String PITCH_WHEEL_CHANGE = "PITCH_BEND_C";
    public static final String CONTROL_CHANGE = "CONTROL_C";
    public static final String PROGRAM_CHANGE = "PROGRAM_C";
    public static final String CHANNEL_AFTERTOUCH = "CHANNEL_AFTERTOUCH_C";
    public static final String POLYPHONIC_AFTERTOUCH = "POLY_AFTERTOUCH_C";

    public static final String SYSTEM_MESSAGE = "SYSTEM_MESSAGE_C";
    public static final String UNKNOWN_MESSAGE = "UNKNOWN MESSAGE";

    private static final String[] SYSTEM_MESSAGE_TEXT = {
            "System Exclusive",
            "MTC Quarter Frame: ", "Song Position: ", "Song Select: ",
            "Undefined", "Undefined", "Tune Request",
            "End of System Exclusive",
            "Timing Clock", "Undefined", "Start", "Continue", "Stop",
            "Undefined", "Active Sensing", "System Reset"
    };
    private static final String[] QUARTER_FRAME_MESSAGE_TEXT = {
            "Frame count LS: ", "Frame count MS: ",
            "Seconds count LS: ", "Seconds count MS: ",
            "Minutes count LS: ", "Minutes count MS: ",
            "Hours count LS: ", "Hours count MS: "
    };
    private static final String[] FRAME_TYPE_TEXT = {
            "24 fps", "25 fps", "30 fps (drop)", "30 fps (non-drop)"
    };
    private static final String[] MIDI_KEY_SIGNATURES = {
            "Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"
    };  // As explained in https://www.recordingblogs.com/wiki/midi-key-signature-meta-message

    private MIDIMessageDecoder() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that decodes a MIDI message and returns a string representation of the message.
     *
     * @param message The MIDI message to decode.
     * @return A string representation of the message.
     */
    public static String midiMessageToString(MidiMessage message) {
        if (message instanceof ShortMessage shortMessage) {
            return decodeMessage(shortMessage);
        } else if (message instanceof SysexMessage sysexMessage) {
            return decodeMessage(sysexMessage);
        } else if (message instanceof MetaMessage metaMessage) {
            return decodeMessage(metaMessage);
        } else {
            return UNKNOWN_MESSAGE;
        }
    }

    // Private methods

    /**
     * Method that decodes a MIDI short message and returns a string representation of it.
     *
     * @param message The MIDI short message to decode.
     * @return A string representation of the message.
     */
    private static String decodeMessage(ShortMessage message) {
        // Get the command of the short message
        String command = decodeCommand(message.getCommand());

        // Handle the different cases of the command
        String output = null;

        switch (command) {
            case NOTE_OFF -> output = NOTE_OFF + " " + getKeyName(message.getData1());

            case NOTE_ON -> output = NOTE_ON + " " + getKeyName(message.getData1());

            case POLYPHONIC_AFTERTOUCH -> output =
                    POLYPHONIC_AFTERTOUCH + " " + getKeyName(message.getData1()) +
                            " pressure: " + message.getData2();

            case CONTROL_CHANGE -> output =
                    CONTROL_CHANGE + " " + message.getData1() +
                            " value: " + message.getData2();

            case PROGRAM_CHANGE -> output = PROGRAM_CHANGE + " " + message.getData1();

            case CHANNEL_AFTERTOUCH -> output =
                    CHANNEL_AFTERTOUCH + " " +
                            getKeyName(message.getData1()) +
                            " pressure: " + message.getData2();

            case PITCH_WHEEL_CHANGE -> output =
                    PITCH_WHEEL_CHANGE + " " + get14bitValue(message.getData1(), message.getData2());

            case SYSTEM_MESSAGE -> {
                // Get the message channel
                int channel = message.getChannel();

                // Get the message type
                output = SYSTEM_MESSAGE_TEXT[channel];

                // Handle the different cases of the message channel
                switch (channel) {
                    case 1 -> {
                        int nQType = (message.getData1() & 0x70) >> 4;
                        int nQData = message.getData1() & 0x0F;
                        if (nQType == 7) {
                            nQData = nQData & 0x1;
                        }
                        output += QUARTER_FRAME_MESSAGE_TEXT[nQType] + nQData;
                        if (nQType == 7) {
                            int nFrameType = (message.getData1() & 0x06) >> 1;
                            output += ", frame type: "
                                    + FRAME_TYPE_TEXT[nFrameType];
                        }
                    }
                    case 2 -> output += get14bitValue(message.getData1(), message.getData2());
                    case 3 -> output += message.getData1();
                }
            }

            case UNKNOWN_MESSAGE -> output = UNKNOWN_MESSAGE +
                    ": Status is " + message.getStatus() +
                    ", byte 1 is " + message.getData1() +
                    ", byte 2 is " + message.getData2();
        }

        if (!command.equals(SYSTEM_MESSAGE)) {
            // Get the channel of the short message and add to the message
            output = "Channel " + (message.getChannel() + 1) + ": " + output;
        }

        // Return the string representation of the message
        return output;
    }

    /**
     * Method that decodes a MIDI System Exclusive (Sysex) message and returns a string
     * representation of it.
     *
     * @param message The MIDI Sysex message to decode.
     * @return A string representation of the message.
     */
    private static String decodeMessage(SysexMessage message) {
        // Get the byte array of the message and convert to a hexadecimal string
        String hexMessage = bytesToHex(message.getMessage());

        // Get the status of the message
        int status = message.getStatus();

        // Form the string representation of the message
        String output = null;

        if (status == SysexMessage.SYSTEM_EXCLUSIVE) {
            output = "Sysex message: F0" + hexMessage;
        } else if (status == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            output = "Continued Sysex message F7" + hexMessage;
        }

        // Return the string representation of the message
        return output;
    }

    /**
     * Helper method that decodes a MIDI meta message and returns a string representation of it.
     *
     * @param message The MIDI meta message to decode.
     * @return A string representation of the meta message.
     */
    private static String decodeMessage(MetaMessage message) {
        // Get the byte data of the message
        byte[] msgData = message.getData();

        // Interpret the meta message
        String output;

        switch (message.getType()) {
            case 0x00 -> output = "Sequence Number: " + (((msgData[0] & 0xFF) << 8) | (msgData[1] & 0xFF));
            case 0x01 -> output = "Text Event: " + new String(msgData);
            case 0x02 -> output = "Copyright Notice: " + new String(msgData);
            case 0x03 -> output = "Track Name: " + new String(msgData);
            case 0x04 -> output = "Instrument Name: " + new String(msgData);
            case 0x05 -> output = "Lyric: " + new String(msgData);
            case 0x06 -> output = "Marker: " + new String(msgData);
            case 0x07 -> output = "Cue Point: " + new String(msgData);
            case 0x20 -> output = "Channel Prefix: " + (msgData[0] & 0xFF);
            case 0x2F -> output = "End of Track";
            case 0x51 -> {
                // Get the tempo in µs per beat
                int tempo = ((msgData[0] & 0xFF) << 16) | ((msgData[1] & 0xFF) << 8) | (msgData[2] & 0xFF);

                // Convert the tempo to beats per minute, correct to 2 decimal places
                float bpm = MathUtils.round(convertTempoToBPM(tempo), 2);

                // Set the string message
                output = "Tempo: " + bpm + " bpm";
            }
            case 0x54 -> output =
                    "SMTPE Offset: " +
                            (msgData[0] & 0xFF) + ":" +
                            (msgData[1] & 0xFF) + ":" +
                            (msgData[2] & 0xFF) + "." +
                            (msgData[3] & 0xFF) + "." +
                            (msgData[4] & 0xFF);
            case 0x58 -> output =
                    "Time Signature: " + (msgData[0] & 0xFF) +
                            "/" + (1 << (msgData[1] & 0xFF)) +
                            ", MIDI clocks per metronome tick: " + (msgData[2] & 0xFF) +
                            ", 1/32 per 24 MIDI clocks: " + (msgData[3] & 0xFF);
            case 0x59 -> {
                // Determine the key of the track
                String key = MIDI_KEY_SIGNATURES[msgData[0] + 7];

                // Determine the mode of the track
                String mode = (msgData[1] == 1) ? "Minor" : "Major";

                // Set the string message
                output = "Key Signature: " + key + " " + mode;
            }
            case 0x7F -> output = "Sequencer-Specific Meta Event: " + bytesToHex(msgData);
            default -> output = "Unknown Meta event: " + bytesToHex(msgData);
        }
        return output;
    }

    /**
     * Helper method that decodes the MIDI command byte code into a readable string.
     *
     * @param command The MIDI command in integer representation.
     * @return The readable string representation of the command.
     * @see <a href="https://www.hobbytronics.co.uk/datasheets/9_MIDI_code.pdf">MIDI code</a>
     * explanation.
     */
    private static String decodeCommand(int command) {
        return switch (command) {
            case 0x80 -> NOTE_OFF;
            case 0x90 -> NOTE_ON;
            case 0xa0 -> POLYPHONIC_AFTERTOUCH;
            case 0xb0 -> CONTROL_CHANGE;
            case 0xc0 -> PROGRAM_CHANGE;
            case 0xd0 -> CHANNEL_AFTERTOUCH;
            case 0xe0 -> PITCH_WHEEL_CHANGE;
            case 0xF0 -> SYSTEM_MESSAGE;
            default -> UNKNOWN_MESSAGE;
        };
    }

    /**
     * Helper method that gets the note string from the MIDI number.
     *
     * @param midiNum The MIDI number of the note.
     * @return The note string.
     */
    private static String getKeyName(int midiNum) {
        return UnitConversionUtils.midiNumberToNote(midiNum, false);
    }

    /**
     * Helper method that gets the 14 bit value from the two 7 bit integer values.
     *
     * @param nLowerPart  The lower 7 bit byte.
     * @param nHigherPart The higher 7 bit byte.
     * @return The 14 bit value.
     */
    private static int get14bitValue(int nLowerPart, int nHigherPart) {
        return (nLowerPart & 0x7F) | ((nHigherPart & 0x7F) << 7);
    }

    /**
     * Helper method that gets the hexadecimal representation of a byte array.
     *
     * @param bytes The byte array.
     * @return The hexadecimal representation of the byte array.
     */
    private static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Helper method that converts tempo in µs per beat to tempo in beats per minute.
     *
     * @param microsecondsPerBeat The tempo in µs per beat.
     * @return The tempo in beats per minute.
     */
    private static float convertTempoToBPM(float microsecondsPerBeat) {
        if (microsecondsPerBeat <= 0) {
            return 600_000_000f;
        }
        return 60_000_000f / microsecondsPerBeat;
    }
}
