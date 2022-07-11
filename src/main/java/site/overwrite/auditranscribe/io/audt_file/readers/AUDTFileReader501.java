/*
 * AUDTFileReader501.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Class that handles the reading of the AudiTranscribe (AUDT) file for file version
 *              501.
 */

package site.overwrite.auditranscribe.io.audt_file.readers;

import site.overwrite.auditranscribe.exceptions.io.audt_file.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.IncorrectFileFormatException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.InvalidFileVersionException;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileReader;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.v501.*;

import java.io.IOException;

public class AUDTFileReader501 extends AUDTFileReader {
    /**
     * Initialization method to make an <code>AUDTFileReader</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     * @throws InvalidFileVersionException  If the LZ4 version is outdated.
     */
    public AUDTFileReader501(String filepath) throws IOException, IncorrectFileFormatException,
            InvalidFileVersionException {
        super(filepath);
    }

    // Public methods
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
        return new UnchangingDataPropertiesObject501(numSkippableBytes);
    }

    public QTransformDataObject readQTransformData() throws FailedToReadDataException {
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
        return new QTransformDataObject501(qTransformData, minMagnitude, maxMagnitude);
    }

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
        return new AudioDataObject501(compressedMP3Bytes, sampleRate, totalDurationInMS, originalFileName);
    }

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
        return new GUIDataObject501(
                musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, currTimeInMS
        );
    }

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
        return new MusicNotesDataObject501(timesToPlaceRectangles, noteDurations, noteNums);
    }
}
