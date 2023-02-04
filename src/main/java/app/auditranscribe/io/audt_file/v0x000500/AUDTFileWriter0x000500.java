/*
 * AUDTFileWriter0x000500.java
 * Description: Handles the writing of the AudiTranscribe file for version 0.5.0.
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

import app.auditranscribe.io.audt_file.base.AUDTFileWriter;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.audt_file.v0x000500.data_encapsulators.*;

import java.io.IOException;

/**
 * Handles the writing of the AudiTranscribe file for version 0.5.0.
 */
public class AUDTFileWriter0x000500 extends AUDTFileWriter {
    /**
     * Initialization method to make an <code>AUDTFileWriter0x000500</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter0x000500(String filepath, int numBytesToSkip) {
        super(filepath, numBytesToSkip);
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter0x000500</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter0x000500(String filepath) {
        super(filepath);
    }

    // Public methods
    @Override
    public void writeUnchangingDataProperties(UnchangingDataPropertiesObject object) {
        // Cast to the correct version of the object
        UnchangingDataPropertiesObject0x000500 obj = (UnchangingDataPropertiesObject0x000500) object;

        // Write to file
        writeSectionID(UnchangingDataPropertiesObject0x000500.SECTION_ID);
        writeInteger(obj.numSkippableBytes);
        writeEOSDelimiter();
    }

    @Override
    public void writeQTransformData(QTransformDataObject object) {
        // Cast to the correct version of the object
        QTransformDataObject0x000500 obj = (QTransformDataObject0x000500) object;

        // Write to file
        writeSectionID(QTransformDataObject0x000500.SECTION_ID);
        writeDouble(obj.minMagnitude);
        writeDouble(obj.maxMagnitude);
        writeByteArray(obj.qTransformBytes);
        writeEOSDelimiter();
    }

    @Override
    public void writeAudioData(AudioDataObject object) {
        // Cast to the correct version of the object
        AudioDataObject0x000500 obj = (AudioDataObject0x000500) object;

        // Write to file
        writeSectionID(AudioDataObject0x000500.SECTION_ID);
        writeByteArray(obj.compressedOriginalMP3Bytes);
        writeDouble(obj.sampleRate);
        writeInteger(obj.totalDurationInMS);
        writeString(obj.getAudioFileName());
        writeEOSDelimiter();
    }

    @Override
    public void writeProjectInfoData(ProjectInfoDataObject object) {
        // Cast to the correct version of the object
        ProjectInfoDataObject0x000500 obj = (ProjectInfoDataObject0x000500) object;

        // Write to file
        writeSectionID(ProjectInfoDataObject0x000500.SECTION_ID);
        writeInteger(obj.musicKeyIndex);
        writeInteger(obj.timeSignatureIndex);
        writeDouble(obj.bpm);
        writeDouble(obj.offsetSeconds);
        writeDouble(obj.playbackVolume);
        writeInteger(obj.currTimeInMS);
        writeEOSDelimiter();
    }

    @Override
    public void writeMusicNotesData(MusicNotesDataObject object) throws IOException {
        // Cast to the correct version of the object
        MusicNotesDataObject0x000500 obj = (MusicNotesDataObject0x000500) object;

        // Write to file
        writeSectionID(MusicNotesDataObject0x000500.SECTION_ID);
        write1DDoubleArray(obj.timesToPlaceRectangles);
        write1DDoubleArray(obj.noteDurations);
        write1DIntegerArray(obj.noteNums);
        writeEOSDelimiter();
    }
}
