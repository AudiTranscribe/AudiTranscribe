/*
 * AUDTFileWriter0x000900.java
 * Description: Handles the writing of the AudiTranscribe file for version 0.9.0.
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
import app.auditranscribe.io.audt_file.v0x000800.AUDTFileWriter0x000800;
import app.auditranscribe.io.audt_file.v0x000900.data_encapsulators.ProjectInfoDataObject0x000900;

/**
 * Handles the writing of the AudiTranscribe file for version 0.9.0.
 */
public class AUDTFileWriter0x000900 extends AUDTFileWriter0x000800 {
    /**
     * Initialization method to make an <code>AUDTFileWriter0x000900</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter0x000900(String filepath, int numBytesToSkip) {
        super(filepath, numBytesToSkip);
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter0x000900</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter0x000900(String filepath) {
        super(filepath);
    }

    // Public methods
    @Override
    public void writeProjectInfoData(ProjectInfoDataObject object) {
        // Cast to the correct version of the object
        ProjectInfoDataObject0x000900 obj = (ProjectInfoDataObject0x000900) object;

        // Write to file
        writeSectionID(ProjectInfoDataObject0x000900.SECTION_ID);
        writeString(obj.projectName);
        writeInteger(obj.musicKeyIndex);
        writeShort((short) obj.timeSignature.beatsPerBar);
        writeShort((short) obj.timeSignature.denominator.numericValue);
        writeDouble(obj.bpm);
        writeDouble(obj.offsetSeconds);
        writeDouble(obj.playbackVolume);
        writeInteger(obj.currTimeInMS);
        writeEOSDelimiter();
    }
}
