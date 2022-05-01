/*
 * AUDTFileWriter.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-01
 *
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file.
 */

package site.overwrite.auditranscribe.io.file_handers;

import site.overwrite.auditranscribe.io.IOConverters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AUDTFileWriter {
    // Attributes
    public final String filename;
    private final List<Byte> bytes = new ArrayList<>();

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param filename File name of the AUDT file (excluding extension).
     */
    public AUDTFileWriter(String filename) {
        // Update attributes
        this.filename = filename;

        // Write the header section
        writeHeaderSection();
    }

    // Public methods

    /**
     * Method that writes the bytes generated to file.
     *
     * @throws IOException If something went wrong when writing to file.
     */
    public void writeBytesToFile() throws IOException {
        // Now convert the `Byte` list into a `byte` array
        int numBytes = bytes.size();  // We shouldn't have more than 2,147,483,647 bytes (i.e. ~2GB)
        byte[] byteArray = new byte[numBytes];

        for (int i = 0; i < numBytes; i++) {
            byteArray[i] = bytes.get(i);
        }

        // Write the byte array to file
        Files.write(new File(filename + ".audt").toPath(), byteArray);
    }

    /**
     * Method that writes the GUI data (<b>section number 3</b>) to file.
     *
     * @param musicKeyIndex      The index of the music key in the dropdown menu shown in the
     *                           application.
     * @param timeSignatureIndex The index of the time signature in the dropdown menu shown in the application.
     * @param bpm                Number of beats per minute.
     * @param offsetSeconds      Number of seconds offset from the start of the audio.
     * @param playbackVolume     Volume to play back at.
     * @param audioFileName      Name of the audio file.
     * @param totalDurationInMS  Total duration of the audio in <b>milliseconds</b>.
     * @param currTimeInMS       Current playback time of the audio in <b>milliseconds</b>.
     */
    public void writeGUIData(
            int musicKeyIndex, int timeSignatureIndex, double bpm, double offsetSeconds, double playbackVolume,
            String audioFileName, int totalDurationInMS, int currTimeInMS
    ) {
        writeSectionID(3);
        writeInteger(musicKeyIndex);
        writeInteger(timeSignatureIndex);
        writeDouble(bpm);
        writeDouble(offsetSeconds);
        writeDouble(playbackVolume);
        writeString(audioFileName);
        writeInteger(totalDurationInMS);
        writeInteger(currTimeInMS);
        writeSectionDelimiter();
    }

    // Private methods

    /**
     * Helper method that writes the header section.
     */
    private void writeHeaderSection() {
        // Write the file header
        FileHandlersHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_FILE_HEADER);

        // Write version numbers
        byte[] fileVersionBytes = IOConverters.intToBytes(AUDTFileConstants.FILE_VERSION_NUMBER);
        byte[] lz4VersionBytes = IOConverters.intToBytes(AUDTFileConstants.LZ4_VERSION_NUMBER);

        FileHandlersHelpers.addBytesIntoBytesList(bytes, fileVersionBytes);
        FileHandlersHelpers.addBytesIntoBytesList(bytes, lz4VersionBytes);

        // Write the section delimiter
        writeSectionDelimiter();
    }

    /**
     * Helper method that writes the section delimiter.
     */
    private void writeSectionDelimiter() {
        bytes.add((byte) 0xe0);
        bytes.add((byte) 0x5e);
        bytes.add((byte) 0x05);
        bytes.add((byte) 0xe5);
    }

    /**
     * Helper method that writes an integer to the byte list.
     */
    private void writeInteger(int integer) {
        // Convert the integer into its bytes
        byte[] byteArray = IOConverters.intToBytes(integer);

        // Write to the byte list
        FileHandlersHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a double to the byte list.
     */
    private void writeDouble(double dbl) {
        // Convert the double into its bytes
        byte[] byteArray = IOConverters.doubleToBytes(dbl);

        // Write to the byte list
        FileHandlersHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a string to the byte list.
     */
    private void writeString(String str) {
        // Convert the string into its bytes
        byte[] byteArray = IOConverters.stringToBytes(str);

        // Get the number of bytes needed to store the string
        int numBytes = byteArray.length;

        // Write to the byte list
        writeInteger(numBytes);
        FileHandlersHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes the section ID to the byte list.
     */
    private void writeSectionID(int sectionID) {
        // This is just a special case of writing an integer
        writeInteger(sectionID);
    }
}
