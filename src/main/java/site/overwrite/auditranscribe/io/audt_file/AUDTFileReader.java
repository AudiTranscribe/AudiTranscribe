/*
 * AUDTFileReader.java
 *
 * Created on 2022-05-02
 * Updated on 2022-07-09
 *
 * Description: Class that handles the reading of the AudiTranscribe (AUDT) file.
 */

package site.overwrite.auditranscribe.io.audt_file;

import site.overwrite.auditranscribe.exceptions.io.audt_file.OutdatedFileFormatException;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.IOConverters;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;
import site.overwrite.auditranscribe.exceptions.io.audt_file.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.IncorrectFileFormatException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AUDTFileReader {
    // Attributes
    public final String filepath;
    public int fileFormatVersion;
    public int lz4Version;

    private final byte[] bytes;
    private int bytePos = 0;  // Position of the NEXT byte to read

    /**
     * Initialization method to make an <code>AUDTFileReader</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     * @throws OutdatedFileFormatException  If the file is outdated.
     */
    public AUDTFileReader(String filepath) throws IOException, IncorrectFileFormatException,
            OutdatedFileFormatException {
        // Update attributes
        this.filepath = filepath;

        // Check extension
        // (For simplicity assume the last 5 characters of the file path forms the extension)
        int filepathLength = filepath.length();
        if (filepathLength < 5 ||
                !filepath.substring(filepathLength - 5, filepathLength).equalsIgnoreCase(".audt")) {
            throw new IncorrectFileFormatException("The file is not an AUDT file. Is the extension correct?");
        }

        // Read bytes in from file
        try (InputStream inputStream = new FileInputStream(filepath)) {
            bytes = inputStream.readAllBytes();

            // Verify that the header section is correct
            if (!verifyHeaderSection()) {
                throw new IncorrectFileFormatException("The file is not an AUDT file. Is the header correct?");
            }

            // Verify that the last 4 bytes is the EOF delimiter
            if (!checkEOFDelimiter()) {
                throw new IncorrectFileFormatException(
                        "The file is not an AUDT file. Is the end-of-file delimiter correct?"
                );
            }
        }
    }

    // Public methods

    /**
     * Method that reads the unchanging data properties from the file.
     *
     * @return A <code>UnchangingDataPropertiesObject</code> that encapsulates all the unchanging
     * data's properties.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public UnchangingDataPropertiesObject readUnchangingDataProperties() throws FailedToReadDataException {
        // Ensure that the unchanging data properties section ID is correct
        int sectionID = readSectionID();
        if (sectionID != UnchangingDataPropertiesObject.SECTION_ID) {
            throw new FailedToReadDataException(
                    "Failed to read the unchanging data properties section; the unchanging data properties section " +
                            "has the incorrect section ID of " + sectionID + " (expected: " +
                            UnchangingDataPropertiesObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data
        int numSkippableBytes = readInteger();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException(
                    "Failed to read unchanging data properties; end of section delimiter missing"
            );
        }

        // Create and return a `UnchangingDataPropertiesObject`
        return new UnchangingDataPropertiesObject(numSkippableBytes);
    }

    /**
     * Method that reads the Q-Transform data from the file.
     *
     * @return A <code>QTransformDataObject</code> that encapsulates all the data that are needed
     * for the Q-Transform matrix.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     * @throws IOException               If something went wrong during reading the file.
     */
    public QTransformDataObject readQTransformData() throws FailedToReadDataException, IOException {
        // Ensure that the Q-Transform data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != QTransformDataObject.SECTION_ID) {
            throw new FailedToReadDataException(
                    "Failed to read Q-Transform data; the Q-Transform data section has the incorrect " +
                            "section ID of " + sectionID + " (expected: " + QTransformDataObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data
        double minMagnitude = readDouble();
        double maxMagnitude = readDouble();
        byte[] qTransformData = readByteArray();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException("Failed to read Q-Transform data; end of section delimiter missing");
        }

        // Create and return a `QTransformDataObject`
        return new QTransformDataObject(qTransformData, minMagnitude, maxMagnitude);
    }

    /**
     * Method that reads the audio data from the file.
     *
     * @return A <code>AudioDataObject</code> that encapsulates all the audio data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public AudioDataObject readAudioData() throws FailedToReadDataException {
        // Ensure that the audio data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != AudioDataObject.SECTION_ID) {
            throw new FailedToReadDataException(
                    "Failed to read audio data; the audio data section has the incorrect section ID of " + sectionID +
                            " (expected: " + AudioDataObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data
        byte[] compressedMP3Bytes = readByteArray();
        double sampleRate = readDouble();
        int totalDurationInMS = readInteger();
        String originalFileName = readString();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException("Failed to read audio data; end of section delimiter missing");
        }

        // Create and return an `AudioDataObject`
        return new AudioDataObject(compressedMP3Bytes, sampleRate, totalDurationInMS, originalFileName);
    }

    /**
     * Method that reads the GUI data from the file.
     *
     * @return A <code>GUIDataObject</code> that encapsulates all the data that are needed by the
     * GUI data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public GUIDataObject readGUIData() throws FailedToReadDataException {
        // Ensure that the GUI data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != GUIDataObject.SECTION_ID) {
            throw new FailedToReadDataException(
                    "Failed to read GUI data; the GUI data section has the incorrect section ID of " + sectionID +
                            " (expected: " + GUIDataObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data first
        int musicKeyIndex = readInteger();
        int timeSignatureIndex = readInteger();
        double bpm = readDouble();
        double offsetSeconds = readDouble();
        double playbackVolume = readDouble();
        int currTimeInMS = readInteger();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException("Failed to read GUI data; end of section delimiter missing");
        }

        // Create and return a `GUIDataObject`
        return new GUIDataObject(
                musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, currTimeInMS
        );
    }

    /**
     * Method that reads the music notes data from the file.
     *
     * @return A <code>MusicNotesDataObject</code> that encapsulates all the music notes data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     * @throws IOException               If something went wrong during reading the file.
     */
    public MusicNotesDataObject readMusicNotesData() throws FailedToReadDataException, IOException {
        // Ensure that the GUI data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != MusicNotesDataObject.SECTION_ID) {
            throw new FailedToReadDataException(
                    "Failed to read music notes data; the music notes data section has the incorrect section ID of " +
                            sectionID + " (expected: " + MusicNotesDataObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data first
        double[] timesToPlaceRectangles = read1DDoubleArray();
        double[] noteDurations = read1DDoubleArray();
        int[] noteNums = read1DIntegerArray();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException("Failed to read music notes data; end of section delimiter missing");
        }

        // Create and return a `MusicNotesDataObject`
        return new MusicNotesDataObject(timesToPlaceRectangles, noteDurations, noteNums);
    }

    // Private methods

    /**
     * Helper method that helps check if two byte arrays are the same.
     *
     * @param orig     Original byte array.
     * @param newBytes New byte array.
     * @return Boolean, where <code>true</code> means that both arrays have the same bytes and
     * <code>false</code> otherwise.
     */
    private boolean checkBytesMatch(byte[] orig, byte[] newBytes) {
        // Ensure that both have the same length
        if (orig.length != newBytes.length) return false;

        // Now check if each individual byte matches
        int n = orig.length;
        for (int i = 0; i < n; i++) {
            if (orig[i] != newBytes[i]) {
                return false;
            }
        }

        // Otherwise, both checks passed => bytes match
        return true;
    }

    /**
     * Helper method that verifies that the file format is correct.
     *
     * @return Boolean, where <code>true</code> means that the file format is correct and
     * <code>false</code> otherwise.
     * @throws OutdatedFileFormatException If the file version is not current, or if the LZ4 version
     *                                     is not current.
     */
    private boolean verifyHeaderSection() throws OutdatedFileFormatException {
        // Check if the first 20 bytes follows the AUDT file header
        byte[] first20Bytes = Arrays.copyOfRange(bytes, 0, 20);
        if (!checkBytesMatch(AUDTFileConstants.AUDT_FILE_HEADER, first20Bytes)) {
            return false;
        }

        // Update byte position
        bytePos = 20;

        // Get the file format version and the LZ4 version
        fileFormatVersion = readInteger();
        lz4Version = readInteger();

        // Check if the file format is outdated
        if (fileFormatVersion < AUDTFileConstants.FILE_VERSION_NUMBER) {
            throw new OutdatedFileFormatException(
                    "Reading in outdated AUDT file (file version is " + fileFormatVersion +
                            " but current version is " + AUDTFileConstants.FILE_VERSION_NUMBER + ")"
            );
        }

        // Check if the LZ4 version is outdated
        if (lz4Version < AUDTFileConstants.LZ4_VERSION_NUMBER) {
            throw new OutdatedFileFormatException(
                    "Outdated LZ4 version (file version is " + lz4Version + " but current version is " +
                            AUDTFileConstants.LZ4_VERSION_NUMBER + ")"
            );
        }

        // Verify that the header ends with an end-of-section delimiter
        return checkEOSDelimiter();
    }

    /**
     * Helper method that reads in an integer from the byte array.
     *
     * @return Integer that was read in.
     */
    private int readInteger() {
        // Read the next 4 bytes from the current `bytePos`
        byte[] integerBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 4);
        bytePos += 4;

        // Convert these integer bytes back into an integer and return
        return IOConverters.bytesToInt(integerBytes);
    }

    /**
     * Helper method that reads in a double from the byte array.
     *
     * @return Double that was read in.
     */
    private double readDouble() {
        // Read the next 8 bytes from the current `bytePos`
        byte[] doubleBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 8);
        bytePos += 8;

        // Convert these double bytes back into a double and return
        return IOConverters.bytesToDouble(doubleBytes);
    }

    /**
     * Helper method that reads in a string from the byte array.
     *
     * @return String that was read in.
     */
    private String readString() {
        // Get the number of bytes that stores the string
        int numBytes = readInteger();

        // Read in the string's bytes
        byte[] stringBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + numBytes);
        bytePos += numBytes;

        // Convert these string bytes back into a string and return
        return IOConverters.bytesToString(stringBytes);
    }

    /**
     * Helper method that reads a byte array from the file's byte array.
     *
     * @return Byte array that was read in.
     */
    private byte[] readByteArray() {
        // Get the number of bytes that are present in the byte array
        int numBytes = readInteger();

        // Get the byte array
        byte[] byteArray = Arrays.copyOfRange(bytes, bytePos, bytePos + numBytes);
        bytePos += numBytes;

        // Return the byte array
        return byteArray;
    }

    /**
     * Helper method that reads in a one-dimensional integer array from the byte array.
     *
     * @return One-dimensional integer array that was read in.
     */
    private int[] read1DIntegerArray() throws IOException {
        // Get the number of bytes that stores the compressed 1D integer array
        int numCompressedBytes = readInteger();

        // Read in the compressed array's bytes
        byte[] compressedBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + numCompressedBytes);
        bytePos += numCompressedBytes;

        // Decompress the bytes
        byte[] decompressedBytes = CompressionHandlers.lz4Decompress(compressedBytes);

        // Convert these bytes back into the 1D array and return
        return IOConverters.bytesToOneDimensionalIntegerArray(decompressedBytes);
    }

    /**
     * Helper method that reads in a one-dimensional double array from the byte array.
     *
     * @return One-dimensional double array that was read in.
     */
    private double[] read1DDoubleArray() throws IOException {
        // Get the number of bytes that stores the compressed 1D double array
        int numCompressedBytes = readInteger();

        // Read in the compressed array's bytes
        byte[] compressedBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + numCompressedBytes);
        bytePos += numCompressedBytes;

        // Decompress the bytes
        byte[] decompressedBytes = CompressionHandlers.lz4Decompress(compressedBytes);

        // Convert these bytes back into the 1D array and return
        return IOConverters.bytesToOneDimensionalDoubleArray(decompressedBytes);
    }

    /**
     * Helper methods that reads in the section ID from the byte array.
     *
     * @return Section ID that was read in.
     */
    private int readSectionID() {
        // This is just a special case of reading an integer
        return readInteger();
    }

    /**
     * Helper method that checks if the next 4 bytes is an end-of-section (EOS) delimiter.
     *
     * @return Boolean; <code>true</code> if it is an EOS delimiter and <code>false</code>
     * otherwise.
     */
    private boolean checkEOSDelimiter() {
        // Read the next 4 bytes from the current `bytePos`
        byte[] eosBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 4);
        bytePos += 4;

        // Check if it is the end of section (EOS) bytes
        return checkBytesMatch(AUDTFileConstants.AUDT_SECTION_DELIMITER, eosBytes);
    }

    /**
     * Helper method that checks if the next 8 bytes is an end-of-file (EOF) delimiter.
     *
     * @return Boolean; <code>true</code> if it is an EOF delimiter and <code>false</code>
     * otherwise.
     */
    private boolean checkEOFDelimiter() {
        // Get the total number of bytes
        int numBytes = bytes.length;

        // Check if the last 8 bytes corresponds to the EOF bytes
        byte[] eofBytes = Arrays.copyOfRange(bytes, numBytes - 8, numBytes);
        return checkBytesMatch(AUDTFileConstants.AUDT_END_OF_FILE_DELIMITER, eofBytes);
    }
}
