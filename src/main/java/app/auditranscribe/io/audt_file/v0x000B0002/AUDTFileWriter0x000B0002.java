/*
 * AUDTFileWriter0x000B0002.java
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file for file version
 *              0x000B0001.
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

package app.auditranscribe.io.audt_file.v0x000B0002;

import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.io.audt_file.v0x00090002.AUDTFileWriter0x00090002;
import app.auditranscribe.io.audt_file.v0x000B0002.data_encapsulators.AudioDataObject0x000B0002;
import app.auditranscribe.io.audt_file.v0x000B0002.data_encapsulators.ProjectInfoDataObject0x000B0002;

public class AUDTFileWriter0x000B0002 extends AUDTFileWriter0x00090002 {
    /**
     * Initialization method to make an <code>AUDTFileWriter0x000B0002</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter0x000B0002(String filepath, int numBytesToSkip) {
        super(filepath, numBytesToSkip);
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter0x000B0002</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter0x000B0002(String filepath) {
        super(filepath);
    }

    // Public methods
    @Override
    public void writeAudioData(AudioDataObject object) {
        // Cast to the correct version of the object
        AudioDataObject0x000B0002 obj = (AudioDataObject0x000B0002) object;

        // Write to file
        writeSectionID(AudioDataObject0x000B0002.SECTION_ID);
        writeByteArray(obj.mp3Bytes);
        writeDouble(obj.sampleRate);
        writeInteger(obj.totalDurationInMS);
        writeEOSDelimiter();
    }

    @Override
    public void writeProjectInfoData(ProjectInfoDataObject object) {
        // Cast to the correct version of the object
        ProjectInfoDataObject0x000B0002 obj = (ProjectInfoDataObject0x000B0002) object;

        // Write to file
        writeSectionID(ProjectInfoDataObject0x000B0002.SECTION_ID);
        writeString(obj.projectName);
        writeShort(obj.musicKey.uuid);
        writeShort((short) obj.timeSignature.beatsPerBar);
        writeShort((short) obj.timeSignature.denominator.numericValue);
        writeDouble(obj.bpm);
        writeDouble(obj.offsetSeconds);
        writeDouble(obj.playbackVolume);
        writeInteger(obj.currTimeInMS);
        writeEOSDelimiter();
    }
}
