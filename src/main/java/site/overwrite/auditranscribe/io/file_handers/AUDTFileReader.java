/*
 * AUDTFileReader.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-05
 *
 * Description: Class that handles the reading of the AudiTranscribe (AUDT) file.
 */

package site.overwrite.auditranscribe.io.file_handers;

import site.overwrite.auditranscribe.io.IOConverters;
import site.overwrite.auditranscribe.io.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.io.exceptions.FailedToReadDataException;
import site.overwrite.auditranscribe.io.exceptions.IncorrectFileFormatException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AUDTFileReader {
    // Attributes
    public final String filename;
    public int fileFormatVersion;
    public int lz4Version;

    private final byte[] bytes;
    private int bytePos = 0;  // Position of the NEXT byte to read

    /**
     * Initialization method to make an <code>AUDTFileReader</code> object.
     *
     * @param filename File name of the AUDT file, <b>including extension</b>.
     */
    public AUDTFileReader(String filename) throws IOException, IncorrectFileFormatException {
        // Update attributes
        this.filename = filename;

        // Read bytes in from file
        try (InputStream inputStream = new FileInputStream(filename)) {
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
     * Method that reads the Q-Transform data from the file.
     *
     * @return A <code>QTransformDataObject</code> that encapsulates all the data that are needed for the Q-Transform matrix.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public QTransformDataObject readQTransformData() throws FailedToReadDataException {
        // Ensure that the Q-Transform data section ID is 1
        int sectionID = readSectionID();
        if (sectionID != 1) {
            throw new FailedToReadDataException(
                    "Failed to read Q-Transform data; the Q-Transform data section has the incorrect " +
                            "section ID of " + sectionID + " (expected: 1)"
            );
        }

        // Read in the rest of the data
        double[][] qTransformMatrix = read2DDoubleArray();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException("Failed to read Q-Transform data; end of section delimiter missing");
        }

        // Create and return a `QTransformDataObject`
        return new QTransformDataObject(qTransformMatrix);
    }

    /**
     * Method that reads the GUI data from the file.
     *
     * @return A <code>GUIDataObject</code> that encapsulates all the data that are needed by the GUI data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public GUIDataObject readGUIData() throws FailedToReadDataException {
        // Ensure that the GUI data section ID is 3
        int sectionID = readSectionID();
        if (sectionID != 3) {
            throw new FailedToReadDataException(
                    "Failed to read GUI data; the GUI data section has the incorrect section ID of " + sectionID +
                            " (expected: 3)"
            );
        }

        // Read in the rest of the data first
        int musicKeyIndex = readInteger();
        int timeSignatureIndex = readInteger();
        double bpm = readDouble();
        double offsetSeconds = readDouble();
        double playbackVolume = readDouble();
        String audioFileName = readString();
        int totalDurationInMS = readInteger();
        int currTimeInMS = readInteger();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new FailedToReadDataException("Failed to read GUI data; end of section delimiter missing");
        }

        // Create and return a `GUIDataObject`
        return new GUIDataObject(
                musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, audioFileName, totalDurationInMS,
                currTimeInMS
        );
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
     */
    private boolean verifyHeaderSection() {
        // Check if the first 20 bytes follows the AUDT file header
        byte[] first20Bytes = Arrays.copyOfRange(bytes, 0, 20);
        if (!checkBytesMatch(AUDTFileConstants.AUDT_FILE_HEADER, first20Bytes)) {
            return false;
        }

        // Update byte position
        bytePos = 20;

        // Get the file format version and the LZ4 version
        fileFormatVersion = readInteger(true);
        lz4Version = readInteger(true);

        // Verify that the header ends with an end-of-section delimiter
        return checkEOSDelimiter();
    }

    /**
     * Helper method that reads in an integer from the byte array.
     *
     * @param updateBytePos Whether to update the byte position after reading.
     * @return Integer that was read in.
     */
    private int readInteger(boolean updateBytePos) {
        // Read the next 4 bytes from the current `bytePos`
        byte[] integerBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 4);
        if (updateBytePos) bytePos += 4;

        // Convert these integer bytes back into an integer and return
        return IOConverters.bytesToInt(integerBytes);
    }

    /**
     * Helper method that reads in an integer from the byte array.
     *
     * @return Integer that was read in.
     */
    private int readInteger() {
        return readInteger(true);
    }

    /**
     * Helper method that reads in a double from the byte array.
     *
     * @param updateBytePos Whether to update the byte position after reading.
     * @return Double that was read in.
     */
    private double readDouble(boolean updateBytePos) {
        // Read the next 8 bytes from the current `bytePos`
        byte[] doubleBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 8);
        if (updateBytePos) bytePos += 8;

        // Convert these double bytes back into a double and return
        return IOConverters.bytesToDouble(doubleBytes);
    }

    /**
     * Helper method that reads in a double from the byte array.
     *
     * @return Double that was read in.
     */
    private double readDouble() {
        return readDouble(true);
    }

    /**
     * Helper method that reads in a string from the byte array.
     *
     * @param updateBytePos Whether to update the byte position after reading.
     * @return String that was read in.
     */
    private String readString(boolean updateBytePos) {
        // Get the number of bytes that stores the string
        int numBytes = readInteger(updateBytePos);

        // Read in the string's bytes
        byte[] stringBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + numBytes);
        if (updateBytePos) bytePos += numBytes;

        // Convert these string bytes back into a string and return
        return IOConverters.bytesToString(stringBytes);
    }

    /**
     * Helper method that reads in a string from the byte array.
     *
     * @return String that was read in.
     */
    private String readString() {
        return readString(true);
    }

    /**
     * Helper method that reads in a one-dimensional double array from the byte array.
     *
     * @param updateBytePos Whether to update the byte position after reading.
     * @return One-dimensional double array that was read in.
     */
    private double[] read1DDoubleArray(boolean updateBytePos) {
        // Get the number of bytes that stores the 1D double array
        int numBytes = readInteger(false);

        // Read in the double array's bytes
        byte[] arrayBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + numBytes + 4);
        if (updateBytePos) bytePos += numBytes + 4;

        // Convert these bytes back into the 1D array and return
        return IOConverters.bytesToOneDimensionalDoubleArray(arrayBytes);
    }

    /**
     * Helper method that reads in a one-dimensional double array from the byte array.
     *
     * @return One-dimensional double array that was read in.
     */
    private double[] read1DDoubleArray() {
        return read1DDoubleArray(true);
    }

    /**
     * Helper method that reads in a two-dimensional double array from the byte array.
     *
     * @param updateBytePos Whether to update the byte position after reading.
     * @return Two-dimensional double array that was read in.
     */
    private double[][] read2DDoubleArray(boolean updateBytePos) {
        // Get the bytes that represent the `numSubarrays` and the `subarrayLength`
        byte[] numSubarraysBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 4);
        byte[] subarrayLengthBytes = Arrays.copyOfRange(bytes, bytePos + 4, bytePos + 8);

        // Get the `numSubarrays` and the `subarrayLength`
        int numSubarrays = IOConverters.bytesToInt(numSubarraysBytes);
        int subarrayLength = IOConverters.bytesToInt(subarrayLengthBytes);

        // Calculate the total number of doubles
        int numDoubles = numSubarrays * subarrayLength;

        // Calculate the total number of bytes needed
        int numBytes = 8 * numDoubles  // Each double takes 8 bytes to store
                + 4                    // Bytes to denote subarray length
                + 4;                   // Bytes to denote number of subarrays

        // Get the bytes that represent the 2D double array
        byte[] arrayBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + numBytes);
        if (updateBytePos) bytePos += numBytes;

        // Convert these bytes back into the 2D array and return
        return IOConverters.bytesToTwoDimensionalDoubleArray(arrayBytes);
    }

    /**
     * Helper method that reads in a two-dimensional double array from the byte array.
     *
     * @return Two-dimensional double array that was read in.
     */
    private double[][] read2DDoubleArray() {
        return read2DDoubleArray(true);
    }

    /**
     * Helper methods that reads in the section ID from the byte array.
     *
     * @return Section ID that was read in.
     */
    private int readSectionID() {
        // This is just a special case of reading an integer
        return readInteger(true);
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
     * Helper method that checks if the next 4 bytes is an end-of-file (EOF) delimiter.
     *
     * @return Boolean; <code>true</code> if it is an EOF delimiter and <code>false</code>
     * otherwise.
     */
    private boolean checkEOFDelimiter() {
        // Get the total number of bytes
        int numBytes = bytes.length;

        // Read the last 4 bytes
        byte[] eofBytes = Arrays.copyOfRange(bytes, numBytes - 4, numBytes);

        // Check if it is the end of section (EOS) bytes
        return checkBytesMatch(AUDTFileConstants.AUDT_END_OF_FILE_DELIMITER, eofBytes);
    }
}
