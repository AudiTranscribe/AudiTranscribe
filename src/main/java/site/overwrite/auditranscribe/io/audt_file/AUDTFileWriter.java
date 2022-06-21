/*
 * AUDTFileWriter.java
 *
 * Created on 2022-05-01
 * Updated on 2022-06-21
 *
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file.
 */

package site.overwrite.auditranscribe.io.audt_file;

import site.overwrite.auditranscribe.io.IOConverters;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AUDTFileWriter {
    // Attributes
    public final String filepath;

    private final List<Byte> bytes = new ArrayList<>();
    private final int numBytesToSkip;

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter(String filepath, int numBytesToSkip) {
        // Update attributes
        this.filepath = filepath;
        this.numBytesToSkip = numBytesToSkip;

        // Write the header section if no bytes are to be skipped
        if (numBytesToSkip == 0) writeHeaderSection();
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter(String filepath) {
        // Update attributes
        this.filepath = filepath;
        this.numBytesToSkip = 0;

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
        // Write the EOF delimiter at the end of the file
        writeEOFDelimiter();

        // Now convert the `Byte` list into a `byte` array
        int numBytes = bytes.size();  // We shouldn't have more than 2,147,483,647 bytes (i.e. ~2GB)
        byte[] byteArray = new byte[numBytes];

        for (int i = 0; i < numBytes; i++) {
            byteArray[i] = bytes.get(i);
        }

        // Define the file path
        File myFile = new File(filepath);

        // Check if we need to skip bytes
        if (numBytesToSkip == 0) {
            // Write the byte array to the beginning of the file
            Files.write(myFile.toPath(), byteArray);
        } else {
            // Define a random access file so that we can seek to a specific position
            try (RandomAccessFile raf = new RandomAccessFile(myFile, "rw")) {  // Automatically closes file
                // Seek to the correct position
                raf.seek(numBytesToSkip);

                // Write the byte array to the correct position
                raf.write(byteArray);
            }
        }
    }

    /**
     * Method that writes the unchanging data properties to file.
     *
     * @param unchangingDataProperties Data object that contains the unchanging data's properties.
     */
    public void writeUnchangingDataProperties(UnchangingDataPropertiesObject unchangingDataProperties) {
        writeSectionID(UnchangingDataPropertiesObject.SECTION_ID);
        writeInteger(unchangingDataProperties.numSkippableBytes);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the Q-Transform data to file.
     *
     * @param qTransformDataObj Data object that holds all the Q-Transform data.
     */
    public void writeQTransformData(QTransformDataObject qTransformDataObj) {
        writeSectionID(QTransformDataObject.SECTION_ID);
        writeDouble(qTransformDataObj.minMagnitude);
        writeDouble(qTransformDataObj.maxMagnitude);
        writeByteArray(qTransformDataObj.qTransformBytes);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the audio data to file.
     *
     * @param audioDataObj Data object that holds all the audio data.
     */
    public void writeAudioData(AudioDataObject audioDataObj) {
        writeSectionID(AudioDataObject.SECTION_ID);
        writeByteArray(audioDataObj.compressedMP3Bytes);
        writeDouble(audioDataObj.sampleRate);
        writeInteger(audioDataObj.totalDurationInMS);
        writeString(audioDataObj.audioFileName);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the GUI data to file.
     *
     * @param guiDataObj Data object that holds all the GUI data.
     */
    public void writeGUIData(GUIDataObject guiDataObj) {
        writeSectionID(GUIDataObject.SECTION_ID);
        writeInteger(guiDataObj.musicKeyIndex);
        writeInteger(guiDataObj.timeSignatureIndex);
        writeDouble(guiDataObj.bpm);
        writeDouble(guiDataObj.offsetSeconds);
        writeDouble(guiDataObj.playbackVolume);
        writeInteger(guiDataObj.currTimeInMS);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the music notes data to file.
     *
     * @param musicNotesDataObj Data object that holds all the music notes data.
     * @throws IOException If something went wrong when LZ4 compressing.
     */
    public void writeMusicNotesData(MusicNotesDataObject musicNotesDataObj) throws IOException {
        writeSectionID(MusicNotesDataObject.SECTION_ID);
        write1DDoubleArray(musicNotesDataObj.timesToPlaceRectangles);
        write1DDoubleArray(musicNotesDataObj.noteDurations);
        write1DIntegerArray(musicNotesDataObj.noteNums);
        writeEOSDelimiter();
    }

    // Private methods

    /**
     * Helper method that writes the header section.
     */
    private void writeHeaderSection() {
        // Write the file header
        AUDTFileHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_FILE_HEADER);

        // Write version numbers
        byte[] fileVersionBytes = IOConverters.intToBytes(AUDTFileConstants.FILE_VERSION_NUMBER);
        byte[] lz4VersionBytes = IOConverters.intToBytes(AUDTFileConstants.LZ4_VERSION_NUMBER);

        AUDTFileHelpers.addBytesIntoBytesList(bytes, fileVersionBytes);
        AUDTFileHelpers.addBytesIntoBytesList(bytes, lz4VersionBytes);

        // Write the end-of-section delimiter
        writeEOSDelimiter();
    }

    /**
     * Helper method that writes an integer to the byte list.
     *
     * @param integer Integer to write.
     */
    private void writeInteger(int integer) {
        // Convert the integer into its bytes
        byte[] byteArray = IOConverters.intToBytes(integer);

        // Write to the byte list
        AUDTFileHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a double to the byte list.
     *
     * @param dbl Double to write.
     */
    private void writeDouble(double dbl) {
        // Convert the double into its bytes
        byte[] byteArray = IOConverters.doubleToBytes(dbl);

        // Write to the byte list
        AUDTFileHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a string to the byte list.
     *
     * @param str String to write.
     */
    private void writeString(String str) {
        // Convert the string into its bytes
        byte[] byteArray = IOConverters.stringToBytes(str);

        // Get the number of bytes needed to store the string
        int numBytes = byteArray.length;

        // Write to the byte list
        writeInteger(numBytes);
        AUDTFileHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a byte array to the byte list.
     *
     * @param array Byte array to write.
     */
    private void writeByteArray(byte[] array) {
        writeInteger(array.length);  // Write number of bytes present in the array
        AUDTFileHelpers.addBytesIntoBytesList(bytes, array);
    }

    /**
     * Helper method that writes an 1D integer array into the byte list.
     *
     * @param array 1D array of integers.
     */
    private void write1DIntegerArray(int[] array) throws IOException {
        // Convert the 1D array into its bytes
        byte[] byteArray = IOConverters.oneDimensionalIntegerArrayToBytes(array);

        // Compress the byte array
        byte[] compressedBytes = LZ4.lz4Compress(byteArray);

        // Get the number of compressed bytes
        int numCompressedBytes = compressedBytes.length;

        // Write to the byte list
        writeInteger(numCompressedBytes);
        AUDTFileHelpers.addBytesIntoBytesList(bytes, compressedBytes);
    }

    /**
     * Helper method that writes an 1D double array into the byte list.
     *
     * @param array 1D array of doubles.
     */
    private void write1DDoubleArray(double[] array) throws IOException {
        // Convert the 1D array into its bytes
        byte[] byteArray = IOConverters.oneDimensionalDoubleArrayToBytes(array);

        // Compress the byte array
        byte[] compressedBytes = LZ4.lz4Compress(byteArray);

        // Get the number of compressed bytes
        int numCompressedBytes = compressedBytes.length;

        // Write to the byte list
        writeInteger(numCompressedBytes);
        AUDTFileHelpers.addBytesIntoBytesList(bytes, compressedBytes);
    }

    /**
     * Helper method that writes the section ID to the byte list.
     *
     * @param sectionID Section ID to write.
     */
    private void writeSectionID(int sectionID) {
        // This is just a special case of writing an integer
        writeInteger(sectionID);
    }

    /**
     * Helper method that writes the end-of-section delimiter.
     */
    private void writeEOSDelimiter() {
        AUDTFileHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_SECTION_DELIMITER);
    }

    /**
     * Helper method that writes the end-of-file delimiter.
     */
    private void writeEOFDelimiter() {
        AUDTFileHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_END_OF_FILE_DELIMITER);
    }
}
