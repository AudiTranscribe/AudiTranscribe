/*
 * AUDTFileWriter0x00070001.java
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file for file version
 *              0x00070001.
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

package site.overwrite.auditranscribe.io.audt_file.v0x00070001;

import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileWriter;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.v0x00070001.data_encapsulators.*;

import java.io.IOException;
import java.util.logging.Level;

public class AUDTFileWriter0x00070001 extends AUDTFileWriter {
    /**
     * Initialization method to make an <code>AUDTFileWriter0x00070001</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter0x00070001(String filepath, int numBytesToSkip) {
        super(0x00070001, filepath, numBytesToSkip);
        log(Level.INFO, "Using Version 0x00070001 AUDT file writer");
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter0x00070001</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter0x00070001(String filepath) {
        super(0x00070001, filepath);
        log(Level.INFO, "Using Version 0x00070001 AUDT file writer");
    }

    // Public methods
    public void writeUnchangingDataProperties(UnchangingDataPropertiesObject object) {
        // Cast to the correct version of the object
        UnchangingDataPropertiesObject0x00070001 obj = (UnchangingDataPropertiesObject0x00070001) object;

        // Write to file
        writeSectionID(UnchangingDataPropertiesObject0x00070001.SECTION_ID);
        writeInteger(obj.numSkippableBytes);
        writeEOSDelimiter();
    }

    public void writeQTransformData(QTransformDataObject object) {
        // Cast to the correct version of the object
        QTransformDataObject0x00070001 obj = (QTransformDataObject0x00070001) object;

        // Write to file
        writeSectionID(QTransformDataObject0x00070001.SECTION_ID);
        writeDouble(obj.minMagnitude);
        writeDouble(obj.maxMagnitude);
        writeByteArray(obj.qTransformBytes);
        writeEOSDelimiter();
    }

    public void writeAudioData(AudioDataObject object) {
        // Cast to the correct version of the object
        AudioDataObject0x00070001 obj = (AudioDataObject0x00070001) object;

        // Write to file
        writeSectionID(AudioDataObject0x00070001.SECTION_ID);
        writeByteArray(obj.compressedOriginalMP3Bytes);
        writeDouble(obj.sampleRate);
        writeInteger(obj.totalDurationInMS);
        writeEOSDelimiter();
    }

    public void writeProjectInfoData(ProjectInfoDataObject object) {
        // Cast to the correct version of the object
        ProjectInfoDataObject0x00070001 obj = (ProjectInfoDataObject0x00070001) object;

        // Write to file
        writeSectionID(ProjectInfoDataObject0x00070001.SECTION_ID);
        writeString(obj.projectName);
        writeInteger(obj.musicKeyIndex);
        writeInteger(obj.timeSignatureIndex);
        writeDouble(obj.bpm);
        writeDouble(obj.offsetSeconds);
        writeDouble(obj.playbackVolume);
        writeInteger(obj.currTimeInMS);
        writeEOSDelimiter();
    }

    public void writeMusicNotesData(MusicNotesDataObject object) throws IOException {
        // Cast to the correct version of the object
        MusicNotesDataObject0x00070001 obj = (MusicNotesDataObject0x00070001) object;

        // Write to file
        writeSectionID(MusicNotesDataObject0x00070001.SECTION_ID);
        write1DDoubleArray(obj.timesToPlaceRectangles);
        write1DDoubleArray(obj.noteDurations);
        write1DIntegerArray(obj.noteNums);
        writeEOSDelimiter();
    }
}
