/*
 * AUDTFileReader0x000800.java
 * Description: Handles the reading of the AudiTranscribe file for version 0.8.0.
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

package app.auditranscribe.io.audt_file.v0x000800;

import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.io.audt_file.v0x000700.AUDTFileReader0x000700;
import app.auditranscribe.io.audt_file.v0x000800.data_encapsulators.AudioDataObject0x000800;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the reading of the AudiTranscribe file for version 0.8.0.
 */
public class AUDTFileReader0x000800 extends AUDTFileReader0x000700 {
    /**
     * Initialization method to make an <code>AUDTFileReader0x000800</code> object.
     *
     * @param filepath    Path to the AUDT file. The file name at the end of the file path should
     *                    <b>include</b> the extension of the AUDT file.
     * @param inputStream Input stream of the file.
     * @throws IOException                  If something went wrong when reading the AUDT file.
     * @throws IncorrectFileFormatException If the file was formatted incorrectly.
     */
    public AUDTFileReader0x000800(
            String filepath, InputStream inputStream
    ) throws IOException, IncorrectFileFormatException {
        super(filepath, inputStream);
    }

    // Public methods
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
        byte[] compressedOriginalMP3Bytes = readByteArray();
        byte[] compressedSlowedMP3Bytes = readByteArray();
        double sampleRate = readDouble();
        int totalDurationInMS = readInteger();

        // Check if there is an EOS
        if (!checkEOSDelimiter()) {
            throw new DataReadFailedException("Failed to read audio data; end of section delimiter missing");
        }

        // Create and return an `AudioDataObject`
        return new AudioDataObject0x000800(
                compressedOriginalMP3Bytes, compressedSlowedMP3Bytes, sampleRate, totalDurationInMS
        );
    }
}
