/*
 * AUDTFileReader.java
 *
 * Created on 2022-05-02
 * Updated on 2022-07-11
 *
 * Description: Class that handles the reading of the AudiTranscribe (AUDT) file.
 */

package site.overwrite.auditranscribe.io.audt_file.base;

import site.overwrite.auditranscribe.exceptions.io.audt_file.InvalidFileVersionException;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.IOConverters;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileConstants;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.exceptions.io.audt_file.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.IncorrectFileFormatException;
import site.overwrite.auditranscribe.io.audt_file.v401.AUDTFileReader401;
import site.overwrite.auditranscribe.io.audt_file.v501.AUDTFileReader501;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class AUDTFileReader {
    // Attributes
    public final String filepath;
    public int fileFormatVersion;
    public int lz4Version;

    protected final byte[] bytes;
    protected int bytePos = 0;  // Position of the NEXT byte to read

    /**
     * Initialization method to make an <code>AUDTFileReader</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     * @throws InvalidFileVersionException  If the LZ4 version is outdated.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     */
    public AUDTFileReader(String filepath) throws InvalidFileVersionException, IOException,
            IncorrectFileFormatException {
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
     * Method that gets the AUDT file reader that is associated with the requested version.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     * @return An <code>AUDTFileReader</code> object that is used to read the data from an AUDT
     * file.
     * @throws InvalidFileVersionException  If the specified file version is not supported <b>or</b>
     *                                      if the LZ4 version is outdated.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     */
    public static AUDTFileReader getFileReader(String filepath) throws InvalidFileVersionException, IOException,
            IncorrectFileFormatException {
        // Attempt to get file version
        byte[] fileVersionBytes;
        try (InputStream inputStream = new FileInputStream(filepath)) {
            inputStream.skipNBytes(20L);  // First 20 is the header
            fileVersionBytes = inputStream.readNBytes(4);  // 4 bytes per integer
        }

        int fileVersion = IOConverters.bytesToInt(fileVersionBytes);

        // Get the appropriate file reader objects
        return switch (fileVersion) {
            case 401 -> new AUDTFileReader401(filepath);
            case 501 -> new AUDTFileReader501(filepath);
            default -> throw new InvalidFileVersionException("Invalid file version '" + fileVersion + "'.");
        };
    }

    // Abstract methods

    /**
     * Method that reads the unchanging data properties from the file.
     *
     * @return A <code>UnchangingDataPropertiesObject</code> that encapsulates all the unchanging
     * data's properties.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public abstract UnchangingDataPropertiesObject readUnchangingDataProperties() throws FailedToReadDataException;

    /**
     * Method that reads the Q-Transform data from the file.
     *
     * @return A <code>QTransformDataObject</code> that encapsulates all the data that are needed
     * for the Q-Transform matrix.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public abstract QTransformDataObject readQTransformData() throws FailedToReadDataException;

    /**
     * Method that reads the audio data from the file.
     *
     * @return A <code>AudioDataObject</code> that encapsulates all the audio data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public abstract AudioDataObject readAudioData() throws FailedToReadDataException;

    /**
     * Method that reads the GUI data from the file.
     *
     * @return A <code>GUIDataObject</code> that encapsulates all the data that are needed by the
     * GUI data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public abstract GUIDataObject readGUIData() throws FailedToReadDataException;

    /**
     * Method that reads the music notes data from the file.
     *
     * @return A <code>MusicNotesDataObject</code> that encapsulates all the music notes data.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     * @throws IOException               If something went wrong during reading the file.
     */
    public abstract MusicNotesDataObject readMusicNotesData() throws FailedToReadDataException, IOException;

    // Protected methods

    /**
     * Helper method that helps check if two byte arrays are the same.
     *
     * @param orig     Original byte array.
     * @param newBytes New byte array.
     * @return Boolean, where <code>true</code> means that both arrays have the same bytes and
     * <code>false</code> otherwise.
     */
    protected boolean checkBytesMatch(byte[] orig, byte[] newBytes) {
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
     * @throws InvalidFileVersionException If the LZ4 version is not current.
     */
    protected boolean verifyHeaderSection() throws InvalidFileVersionException {
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

        // Check if the LZ4 version is outdated
        if (lz4Version < AUDTFileConstants.LZ4_VERSION_NUMBER) {
            throw new InvalidFileVersionException(
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
    protected int readInteger() {
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
    protected double readDouble() {
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
    protected String readString() {
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
    protected byte[] readByteArray() {
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
    protected int[] read1DIntegerArray() throws IOException {
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
    protected double[] read1DDoubleArray() throws IOException {
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
    protected int readSectionID() {
        // This is just a special case of reading an integer
        return readInteger();
    }

    /**
     * Helper method that checks if the next 4 bytes is an end-of-section (EOS) delimiter.
     *
     * @return Boolean; <code>true</code> if it is an EOS delimiter and <code>false</code>
     * otherwise.
     */
    protected boolean checkEOSDelimiter() {
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
    protected boolean checkEOFDelimiter() {
        // Get the total number of bytes
        int numBytes = bytes.length;

        // Check if the last 8 bytes corresponds to the EOF bytes
        byte[] eofBytes = Arrays.copyOfRange(bytes, numBytes - 8, numBytes);
        return checkBytesMatch(AUDTFileConstants.AUDT_END_OF_FILE_DELIMITER, eofBytes);
    }
}
