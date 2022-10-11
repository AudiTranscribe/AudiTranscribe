/*
 * AUDTFileWriter.java
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file.
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

package site.overwrite.auditranscribe.io.audt_file.base;

import site.overwrite.auditranscribe.io.exceptions.InvalidFileVersionException;
import site.overwrite.auditranscribe.io.audt_file.v0x00080001.AUDTFileWriter0x00080001;
import site.overwrite.auditranscribe.utils.ByteConversionUtils;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileConstants;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileHelpers;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.v0x00050002.AUDTFileWriter0x00050002;
import site.overwrite.auditranscribe.io.audt_file.v0x00070001.AUDTFileWriter0x00070001;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class AUDTFileWriter {
    // Attributes
    public final String filepath;

    protected final List<Byte> bytes = new ArrayList<>();
    protected final int numBytesToSkip;

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param fileVersion    AUDT file version.
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter(int fileVersion, String filepath, int numBytesToSkip) {
        // Update attributes
        this.filepath = filepath;
        this.numBytesToSkip = numBytesToSkip;

        // Write the header section if no bytes are to be skipped
        if (numBytesToSkip == 0) writeHeaderSection(fileVersion);
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter</code> object.
     *
     * @param fileVersion AUDT file version.
     * @param filepath    Path to the AUDT file. The file name at the end of the file path should
     *                    <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter(int fileVersion, String filepath) {
        // Update attributes
        this.filepath = filepath;
        this.numBytesToSkip = 0;

        // Write the header section
        writeHeaderSection(fileVersion);
    }

    // Public methods

    /**
     * Method that gets the AUDT file writer that is associated with the requested version.
     *
     * @param fileVersion AUDT file version to get the writer for.
     * @param filepath    Path to the AUDT file.
     * @return An <code>AUDTFileWriter</code> object that is used to write data to the AUDT file.
     * @throws InvalidFileVersionException If the specified file version is not supported.
     */
    public static AUDTFileWriter getWriter(int fileVersion, String filepath) throws InvalidFileVersionException {
        // Get the appropriate file reader objects
        return switch (fileVersion) {
            case 0x00050002 -> new AUDTFileWriter0x00050002(filepath);
            case 0x00070001 -> new AUDTFileWriter0x00070001(filepath);
            case 0x00080001 -> new AUDTFileWriter0x00080001(filepath);
            default -> throw new InvalidFileVersionException("Invalid file version '" + fileVersion + "'.");
        };
    }

    /**
     * Method that gets the AUDT file writer that is associated with the requested version.
     *
     * @param fileVersion AUDT file version to get the writer for.
     * @param filepath    Path to the AUDT file.
     * @return An <code>AUDTFileWriter</code> object that is used to write data to the AUDT file.
     * @throws InvalidFileVersionException If the specified file version is not supported.
     */
    public static AUDTFileWriter getWriter(int fileVersion, String filepath, int numBytesToSkip)
            throws InvalidFileVersionException {
        // Get the appropriate file reader objects
        return switch (fileVersion) {
            case 0x00050002 -> new AUDTFileWriter0x00050002(filepath, numBytesToSkip);
            case 0x00070001 -> new AUDTFileWriter0x00070001(filepath, numBytesToSkip);
            case 0x00080001 -> new AUDTFileWriter0x00080001(filepath, numBytesToSkip);
            default -> throw new InvalidFileVersionException("Invalid file version '" + fileVersion + "'.");
        };
    }

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

                // Set the new length of the file
                raf.setLength(numBytesToSkip + byteArray.length);  // Make sure that the EOF delimiter is set correctly

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
    public abstract void writeUnchangingDataProperties(UnchangingDataPropertiesObject unchangingDataProperties);

    /**
     * Method that writes the Q-Transform data to file.
     *
     * @param qTransformDataObj Data object that holds all the Q-Transform data.
     */
    public abstract void writeQTransformData(QTransformDataObject qTransformDataObj);

    /**
     * Method that writes the audio data to file.
     *
     * @param audioDataObj Data object that holds all the audio data.
     */
    public abstract void writeAudioData(AudioDataObject audioDataObj);

    /**
     * Method that writes the project info data to file.
     *
     * @param projectInfoDataObj Data object that holds all the project info data.
     */
    public abstract void writeProjectInfoData(ProjectInfoDataObject projectInfoDataObj);

    /**
     * Method that writes the music notes data to file.
     *
     * @param musicNotesDataObj Data object that holds all the music notes data.
     * @throws IOException If something went wrong when LZ4 compressing.
     */
    public abstract void writeMusicNotesData(MusicNotesDataObject musicNotesDataObj) throws IOException;

    // Protected methods

    /**
     * Helper method that writes an integer to the byte list.
     *
     * @param integer Integer to write.
     */
    protected void writeInteger(int integer) {
        // Convert the integer into its bytes
        byte[] byteArray = ByteConversionUtils.intToBytes(integer);

        // Write to the byte list
        AUDTFileHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a double to the byte list.
     *
     * @param dbl Double to write.
     */
    protected void writeDouble(double dbl) {
        // Convert the double into its bytes
        byte[] byteArray = ByteConversionUtils.doubleToBytes(dbl);

        // Write to the byte list
        AUDTFileHelpers.addBytesIntoBytesList(bytes, byteArray);
    }

    /**
     * Helper method that writes a string to the byte list.
     *
     * @param str String to write.
     */
    protected void writeString(String str) {
        // Convert the string into its bytes
        byte[] byteArray = ByteConversionUtils.stringToBytes(str);

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
    protected void writeByteArray(byte[] array) {
        writeInteger(array.length);  // Write number of bytes present in the array
        AUDTFileHelpers.addBytesIntoBytesList(bytes, array);
    }

    /**
     * Helper method that writes an 1D integer array into the byte list.
     *
     * @param array 1D array of integers.
     */
    protected void write1DIntegerArray(int[] array) throws IOException {
        // Convert the 1D array into its bytes
        byte[] byteArray = ByteConversionUtils.oneDimensionalIntegerArrayToBytes(array);

        // Compress the byte array
        byte[] compressedBytes = CompressionHandlers.lz4Compress(byteArray);

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
    protected void write1DDoubleArray(double[] array) throws IOException {
        // Convert the 1D array into its bytes
        byte[] byteArray = ByteConversionUtils.oneDimensionalDoubleArrayToBytes(array);

        // Compress the byte array
        byte[] compressedBytes = CompressionHandlers.lz4Compress(byteArray);

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
    protected void writeSectionID(int sectionID) {
        // This is just a special case of writing an integer
        writeInteger(sectionID);
    }

    /**
     * Helper method that writes the end-of-section delimiter.
     */
    protected void writeEOSDelimiter() {
        AUDTFileHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_SECTION_DELIMITER);
    }

    // Private methods

    /**
     * Helper method that writes the header section.
     *
     * @param fileVersion File version to write.
     */
    private void writeHeaderSection(int fileVersion) {
        // Write the file header
        AUDTFileHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_FILE_HEADER);

        // Write version numbers
        byte[] fileVersionBytes = ByteConversionUtils.intToBytes(fileVersion);
        byte[] lz4VersionBytes = ByteConversionUtils.intToBytes(AUDTFileConstants.COMPRESSOR_VERSION_NUMBER);

        AUDTFileHelpers.addBytesIntoBytesList(bytes, fileVersionBytes);
        AUDTFileHelpers.addBytesIntoBytesList(bytes, lz4VersionBytes);

        // Write the end-of-section delimiter
        writeEOSDelimiter();
    }

    /**
     * Helper method that writes the end-of-file delimiter.
     */
    private void writeEOFDelimiter() {
        AUDTFileHelpers.addBytesIntoBytesList(bytes, AUDTFileConstants.AUDT_END_OF_FILE_DELIMITER);
    }
}
