/*
 * AUDTFileReader0x000900.java
 * Description: Handles the reading of the AudiTranscribe file for version 0.9.0.
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

package app.auditranscribe.io.audt_file.v0x000900;

import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.io.audt_file.v0x000800.AUDTFileReader0x000800;
import app.auditranscribe.io.audt_file.v0x000900.data_encapsulators.ProjectInfoDataObject0x000900;
import app.auditranscribe.music.TimeSignature;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the reading of the AudiTranscribe file for version 0.9.0.
 */
public class AUDTFileReader0x000900 extends AUDTFileReader0x000800 {
    /**
     * Initialization method to make an <code>AUDTFileReader0x000900</code> object.
     *
     * @param filepath    Path to the AUDT file. The file name at the end of the file path should
     *                    <b>include</b> the extension of the AUDT file.
     * @param inputStream Input stream of the file.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     */
    public AUDTFileReader0x000900(
            String filepath, InputStream inputStream
    ) throws IOException, IncorrectFileFormatException {
        super(filepath, inputStream);
    }

    // Public methods
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
        String projectName = readString();
        int musicKeyIndex = readInteger();
        short timeSignatureNumerator = readShort();
        short timeSignatureDenominator = readShort();
        double bpm = readDouble();
        double offsetSeconds = readDouble();
        double playbackVolume = readDouble();
        int currTimeInMS = readInteger();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new DataReadFailedException("Failed to read project info data; end of section delimiter missing");
        }

        // Get the time signature
        String timeSignatureDisplayText = timeSignatureNumerator + "/" + timeSignatureDenominator;
        TimeSignature timeSignature = TimeSignature.displayTextToTimeSignature(timeSignatureDisplayText);

        // Create and return a `ProjectInfoDataObject`
        return new ProjectInfoDataObject0x000900(
                projectName, musicKeyIndex, timeSignature, bpm, offsetSeconds, playbackVolume, currTimeInMS
        );
    }
}
