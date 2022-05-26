/*
 * AUDTFileWriter.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-26
 *
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file.
 */

package site.overwrite.auditranscribe.io.audt_file.file_handers;

import site.overwrite.auditranscribe.CustomTask;
import site.overwrite.auditranscribe.io.IOConverters;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AUDTFileWriter {
    // Attributes
    public final String filepath;
    private final List<Byte> bytes = new ArrayList<>();

    private final CustomTask<?> task;

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter(String filepath) {
        // Update attributes
        this.filepath = filepath;
        this.task = null;

        // Write the header section
        writeHeaderSection();
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     * @param task     The <code>CustomTask</code> object that is handling the generation. Pass in
     *                 <code>null</code> if no such task is being used.
     */
    public AUDTFileWriter(String filepath, CustomTask<?> task) {
        // Update attributes
        this.filepath = filepath;
        this.task = task;

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

        // Write the byte array to file
        Files.write(new File(filepath).toPath(), byteArray);
    }

    /**
     * Method that writes the Q-Transform data (<b>section number 1</b>) to file.
     *
     * @param qTransformDataObj Data object that holds all the Q-Transform data.
     */
    public void writeQTransformData(QTransformDataObject qTransformDataObj) throws IOException {
        writeSectionID(1);
        writeDouble(qTransformDataObj.minMagnitude);
        writeDouble(qTransformDataObj.maxMagnitude);
        write2DIntegerArray(qTransformDataObj.getIntegerMagnitudeValues());
        writeEOSDelimiter();
    }

    /**
     * Method that writes the audio data (<b>section number 2</b>) to file.
     *
     * @param audioDataObj Data object that holds all the audio data.
     */
    public void writeAudioData(AudioDataObject audioDataObj) {
        writeSectionID(2);
        writeString(audioDataObj.audioFilePath);
        writeDouble(audioDataObj.sampleRate);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the GUI data (<b>section number 3</b>) to file.
     *
     * @param guiDataObj Data object that holds all the GUI data.
     */
    public void writeGUIData(GUIDataObject guiDataObj) {
        writeSectionID(3);
        writeInteger(guiDataObj.musicKeyIndex);
        writeInteger(guiDataObj.timeSignatureIndex);
        writeDouble(guiDataObj.bpm);
        writeDouble(guiDataObj.offsetSeconds);
        writeDouble(guiDataObj.playbackVolume);
        writeString(guiDataObj.audioFileName);
        writeInteger(guiDataObj.totalDurationInMS);
        writeInteger(guiDataObj.currTimeInMS);
        writeEOSDelimiter();
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
        FileHandlersHelpers.addBytesIntoBytesList(bytes, byteArray);
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
        FileHandlersHelpers.addBytesIntoBytesList(bytes, byteArray);
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
        FileHandlersHelpers.addBytesIntoBytesList(bytes, byteArray);
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
        byte[] compressedBytes = LZ4.lz4Compress(byteArray, task);

        // Get the number of compressed bytes
        int numCompressedBytes = compressedBytes.length;

        // Write to the byte list
        writeInteger(numCompressedBytes);
        FileHandlersHelpers.addBytesIntoBytesList(bytes, compressedBytes);
    }

    /**
     * Helper method that writes a 2D double array into the byte list.
     *
     * @param array 2D array of doubles.
     */
    private void write2DDoubleArray(double[][] array) throws IOException {
        // Convert the 2D array into its bytes
        byte[] byteArray = IOConverters.twoDimensionalDoubleArrayToBytes(array);

        // Compress the byte array
        byte[] compressedBytes = LZ4.lz4Compress(byteArray, task);

        // Get the number of compressed bytes
        int numCompressedBytes = compressedBytes.length;

        // Write to the byte list
        writeInteger(numCompressedBytes);
        FileHandlersHelpers.addBytesIntoBytesList(bytes, compressedBytes);
    }

    /**
     * Helper method that writes a 2D integer array into the byte list.
     *
     * @param array 2D array of integers.
     */
    private void write2DIntegerArray(int[][] array) throws IOException {
        // Convert the 2D array into its bytes
        byte[] byteArray = IOConverters.twoDimensionalIntegerArrayToBytes(array);

        // Compress the byte array
        byte[] compressedBytes = LZ4.lz4Compress(byteArray, task);

        // Get the number of compressed bytes
        int numCompressedBytes = compressedBytes.length;

        // Write to the byte list
        writeInteger(numCompressedBytes);
        FileHandlersHelpers.addBytesIntoBytesList(bytes, compressedBytes);
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
        FileHandlersHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_SECTION_DELIMITER);
    }

    /**
     * Helper method that writes the end-of-file delimiter.
     */
    private void writeEOFDelimiter() {
        // Write the EOF delimiter bytes
        FileHandlersHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_END_OF_FILE_DELIMITER);

        // Sum all the bytes inside the file as a checksum
        // (The checksum will overflow if the sum exceeds 2^31; this is okay as we only want the bytes' values)
        int checksum = 0;
        for (byte b : bytes) {
            checksum += b;
        }

        // Write the checksum to the file as the last 4 bytes
        writeInteger(checksum);
    }
}
