/*
 * AUDTFileReader.java
 * Description: Handles the reading of the AudiTranscribe file.
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

package app.auditranscribe.io.audt_file.base;

import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.utils.ByteConversionUtils;
import app.auditranscribe.io.CompressionHandlers;
import app.auditranscribe.io.audt_file.AUDTFileConstants;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.audt_file.v0x000500.AUDTFileReader0x000500;
import app.auditranscribe.io.audt_file.v0x000700.AUDTFileReader0x000700;
import app.auditranscribe.io.audt_file.v0x000800.AUDTFileReader0x000800;
import app.auditranscribe.io.audt_file.v0x000900.AUDTFileReader0x000900;
import app.auditranscribe.io.audt_file.v0x000B00.AUDTFileReader0x000B00;
import app.auditranscribe.io.exceptions.FailedToReadDataException;
import app.auditranscribe.io.exceptions.IncorrectFileFormatException;
import app.auditranscribe.io.exceptions.InvalidFileVersionException;
import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.utils.MiscUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Handles the reading of the AudiTranscribe file.
 */
public abstract class AUDTFileReader extends LoggableClass {
    // Attributes
    public final String filepath;
    public int fileFormatVersion;

    protected final byte[] bytes;
    protected int bytePos = 0;  // Position of the NEXT byte to read

    /**
     * Initialization method to make an <code>AUDTFileReader</code> object.
     *
     * @param filepath    Path to the AUDT file. The file name at the end of the file path should
     *                    <b>include</b> the extension of the AUDT file.
     * @param inputStream Input stream of the file.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     * @throws InvalidFileVersionException  If the LZ4 version is outdated.
     */
    public AUDTFileReader(
            String filepath, InputStream inputStream
    ) throws IOException, IncorrectFileFormatException, InvalidFileVersionException {
        // Update attributes
        this.filepath = filepath;

        // Read bytes in from file
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
        // Check extension
        // (For simplicity assume the last 5 characters of the file path forms the extension)
        int filepathLength = filepath.length();
        if (filepathLength < 5 ||
                !filepath.substring(filepathLength - 5, filepathLength).equalsIgnoreCase(".audt")) {
            throw new IncorrectFileFormatException("The file is not an AUDT file. Is the extension correct?");
        }

        // Attempt to read the file
        int fileVersion;
        try (InputStream inputStream = new FileInputStream(filepath)) {
            // Try and get the file version
            inputStream.skipNBytes(20L);  // First 20 is the header
            byte[] fileVersionBytes = inputStream.readNBytes(4);  // 4 bytes per integer
            fileVersion = ByteConversionUtils.bytesToInt(fileVersionBytes);
        }

        try (InputStream inputStream = new FileInputStream(filepath)) {  // Do this so that the read point is the start
            AUDTFileReader reader = switch (fileVersion) {
                case 0x00050002 -> new AUDTFileReader0x000500(filepath, inputStream);
                case 0x00070001 -> new AUDTFileReader0x000700(filepath, inputStream);
                case 0x00080001 -> new AUDTFileReader0x000800(filepath, inputStream);
                case 0x00090002 -> new AUDTFileReader0x000900(filepath, inputStream);
                case 0x000B0003 -> new AUDTFileReader0x000B00(filepath, inputStream);
                default -> throw new InvalidFileVersionException(
                        "Invalid file version '" + MiscUtils.intAsPaddedHexStr(fileVersion) + "'."
                );
            };

            CustomLogger.log(
                    Level.INFO,
                    "Using version " + MiscUtils.intAsPaddedHexStr(fileVersion) + " AUDT file reader",
                    AUDTFileReader.class.getName()
            );

            return reader;
        }
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
     * Method that reads the project info data from the file.
     *
     * @return A <code>ProjectInfoDataObject</code> that encapsulates all the project's info.
     * @throws FailedToReadDataException If the program failed to read the data from the file.
     */
    public abstract ProjectInfoDataObject readProjectInfoData() throws FailedToReadDataException;

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

        return true;
    }

    /**
     * Helper method that verifies that the file format is correct.
     *
     * @return Boolean, where <code>true</code> means that the file format is correct and
     * <code>false</code> otherwise.
     */
    protected boolean verifyHeaderSection() {
        // Check if the first 16 bytes follows the AUDT file heading
        byte[] first16Bytes = Arrays.copyOfRange(bytes, 0, 16);
        if (!checkBytesMatch(AUDTFileConstants.AUDT_FILE_HEADING, first16Bytes)) {
            return false;
        }

        // Check if the next 4 bytes is the AUDT magic constant
        byte[] next4Bytes = Arrays.copyOfRange(bytes, 16, 20);
        if (!checkBytesMatch(AUDTFileConstants.AUDT_MAGIC_CONSTANT, next4Bytes)) {
            return false;
        }

        // Get the file format version
        bytePos = 20;
        fileFormatVersion = readInteger();
        skipBytes(4);  // Older versions may use compressor version instead of magic constant

        // Verify that the header ends with an end-of-section delimiter
        return checkEOSDelimiter();
    }

    /**
     * Helper method that skips the specified number of bytes.
     *
     * @param numBytesToSkip Number of bytes to skip.
     */
    protected void skipBytes(int numBytesToSkip) {
        bytePos += numBytesToSkip;
    }

    /**
     * Helper method that reads a boolean from the byte array.
     *
     * @return Boolean that was read in.
     */
    protected boolean readBoolean() {
        // Read the next byte from the current `bytePos`
        byte[] booleanBytes = Arrays.copyOfRange(bytes, bytePos, ++bytePos);  // Increment then return

        // Convert these boolean bytes back into a boolean and return
        return ByteConversionUtils.bytesToBoolean(booleanBytes);
    }

    /**
     * Helper method that reads a short from the byte array.
     *
     * @return Short that was read in.
     */
    protected short readShort() {
        // Read the next 2 bytes from the current `bytePos`
        byte[] shortBytes = Arrays.copyOfRange(bytes, bytePos, bytePos + 2);
        bytePos += 2;

        // Convert these short bytes back into a short and return
        return ByteConversionUtils.bytesToShort(shortBytes);
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
        return ByteConversionUtils.bytesToInt(integerBytes);
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
        return ByteConversionUtils.bytesToDouble(doubleBytes);
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
        return ByteConversionUtils.bytesToString(stringBytes);
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
        return ByteConversionUtils.bytesToOneDimensionalIntegerArray(decompressedBytes);
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
        return ByteConversionUtils.bytesToOneDimensionalDoubleArray(decompressedBytes);
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

    @Override
    public void log(Level level, String msg) {
        log(level, msg, AUDTFileReader.class.getName());
    }
}
