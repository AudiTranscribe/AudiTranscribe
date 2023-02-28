/*
 * AUDTFileReader0x000500.java
 * Description: Handles the reading of the AudiTranscribe file for version 0.5.0.
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

package app.auditranscribe.io.audt_file.v0x000500;

import app.auditranscribe.io.audt_file.base.AUDTFileReader;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.audt_file.v0x000500.data_encapsulators.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the reading of the AudiTranscribe file for version 0.5.0.
 */
public class AUDTFileReader0x000500 extends AUDTFileReader {
    // Attributes
    private String originalFileName;  // To be used as input to the project info data object later

    /**
     * Initialization method to make an <code>AUDTFileReader0x000500</code> object.
     *
     * @param filepath    Path to the AUDT file. The file name at the end of the file path should
     *                    <b>include</b> the extension of the AUDT file.
     * @param inputStream Input stream of the file.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     */
    public AUDTFileReader0x000500(
            String filepath, InputStream inputStream
    ) throws IOException, IncorrectFileFormatException {
        super(filepath, inputStream);
    }

    // Public methods
    @Override
    public UnchangingDataPropertiesObject readUnchangingDataProperties() throws DataReadFailedException {
        // Ensure that the unchanging data properties section ID is correct
        int sectionID = readSectionID();
        if (sectionID != UnchangingDataPropertiesObject.SECTION_ID) {
            throw new DataReadFailedException(
                    "Failed to read the unchanging data properties section; the unchanging data properties section " +
                            "has the incorrect section ID of " + sectionID + " (expected: " +
                            UnchangingDataPropertiesObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data
        int numSkippableBytes = readInteger();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new DataReadFailedException(
                    "Failed to read unchanging data properties; end of section delimiter missing"
            );
        }

        // Create and return a `UnchangingDataPropertiesObject`
        return new UnchangingDataPropertiesObject0x000500(numSkippableBytes);
    }

    @Override
    public QTransformDataObject readQTransformData() throws DataReadFailedException {
        // Ensure that the Q-Transform data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != QTransformDataObject.SECTION_ID) {
            throw new DataReadFailedException(
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
            throw new DataReadFailedException("Failed to read Q-Transform data; end of section delimiter missing");
        }

        // Create and return a `QTransformDataObject`
        return new QTransformDataObject0x000500(qTransformData, minMagnitude, maxMagnitude);
    }

    @Override
    public AudioDataObject readAudioData() throws DataReadFailedException {
        // Ensure that the audio data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != AudioDataObject.SECTION_ID) {
            throw new DataReadFailedException(
                    "Failed to read audio data; the audio data section has the incorrect section ID of " + sectionID +
                            " (expected: " + AudioDataObject.SECTION_ID + ")"
            );
        }

        // Read in the rest of the data
        byte[] compressedMP3Bytes = readByteArray();
        double sampleRate = readDouble();
        int totalDurationInMS = readInteger();
        originalFileName = readString();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new DataReadFailedException("Failed to read audio data; end of section delimiter missing");
        }

        // Create and return an `AudioDataObject`
        return new AudioDataObject0x000500(compressedMP3Bytes, sampleRate, totalDurationInMS, originalFileName);
    }

    @Override
    public ProjectInfoDataObject readProjectInfoData() throws DataReadFailedException {
        // Ensure that the project info data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != ProjectInfoDataObject.SECTION_ID) {
            throw new DataReadFailedException(
                    "Failed to read project info data; the project info data section has the incorrect section ID of " +
                            sectionID + "(expected: " + ProjectInfoDataObject.SECTION_ID + ")"
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
            throw new DataReadFailedException("Failed to read project info data; end of section delimiter missing");
        }

        // Create and return a `ProjectInfoDataObject`
        ProjectInfoDataObject0x000500 obj = new ProjectInfoDataObject0x000500(
                musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, currTimeInMS
        );
        obj.setProjectName(originalFileName);  // Set the superclass' attribute
        return obj;
    }

    @Override
    public MusicNotesDataObject readMusicNotesData() throws DataReadFailedException, IOException {
        // Ensure that the music notes data section ID is correct
        int sectionID = readSectionID();
        if (sectionID != MusicNotesDataObject.SECTION_ID) {
            throw new DataReadFailedException(
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
            throw new DataReadFailedException("Failed to read music notes data; end of section delimiter missing");
        }

        // Create and return a `MusicNotesDataObject`
        return new MusicNotesDataObject0x000500(timesToPlaceRectangles, noteDurations, noteNums);
    }
}
