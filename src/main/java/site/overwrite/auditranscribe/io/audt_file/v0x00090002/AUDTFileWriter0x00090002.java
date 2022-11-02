/*
 * AUDTFileWriter0x00090002.java
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file for file version
 *              0x00090002.
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

package site.overwrite.auditranscribe.io.audt_file.v0x00090002;

import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileWriter;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.v0x00090002.data_encapsulators.*;

import java.io.IOException;
import java.util.logging.Level;

public class AUDTFileWriter0x00090002 extends AUDTFileWriter {
    /**
     * Initialization method to make an <code>AUDTFileWriter0x00090002</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter0x00090002(String filepath, int numBytesToSkip) {
        super(0x00090002, filepath, numBytesToSkip);
        log(Level.INFO, "Using Version 0x00090002 AUDT file writer");
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter0x00090002</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter0x00090002(String filepath) {
        super(0x00090002, filepath);
        log(Level.INFO, "Using Version 0x00090002 AUDT file writer");
    }

    // Public methods

    /**
     * Method that writes the unchanging data properties to file.
     *
     * @param object Data object that contains the unchanging data's properties.
     */
    public void writeUnchangingDataProperties(UnchangingDataPropertiesObject object) {
        // Cast to the correct version of the object
        UnchangingDataPropertiesObject0x00090002 obj = (UnchangingDataPropertiesObject0x00090002) object;

        // Write to file
        writeSectionID(UnchangingDataPropertiesObject0x00090002.SECTION_ID);
        writeInteger(obj.numSkippableBytes);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the Q-Transform data to file.
     *
     * @param object Data object that holds all the Q-Transform data.
     */
    public void writeQTransformData(QTransformDataObject object) {
        // Cast to the correct version of the object
        QTransformDataObject0x00090002 obj = (QTransformDataObject0x00090002) object;

        // Write to file
        writeSectionID(QTransformDataObject0x00090002.SECTION_ID);
        writeDouble(obj.minMagnitude);
        writeDouble(obj.maxMagnitude);
        writeByteArray(obj.qTransformBytes);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the audio data to file.
     *
     * @param object Data object that holds all the audio data.
     */
    public void writeAudioData(AudioDataObject object) {
        // Cast to the correct version of the object
        AudioDataObject0x00090002 obj = (AudioDataObject0x00090002) object;

        // Write to file
        writeSectionID(AudioDataObject0x00090002.SECTION_ID);
        writeByteArray(obj.compressedOriginalMP3Bytes);
        writeByteArray(obj.compressedSlowedMP3Bytes);
        writeDouble(obj.sampleRate);
        writeInteger(obj.totalDurationInMS);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the project info data to file.
     *
     * @param object Data object that holds all the project info data.
     */
    public void writeProjectInfoData(ProjectInfoDataObject object) {
        // Cast to the correct version of the object
        ProjectInfoDataObject0x00090002 obj = (ProjectInfoDataObject0x00090002) object;

        // Write to file
        writeSectionID(ProjectInfoDataObject0x00090002.SECTION_ID);
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

    /**
     * Method that writes the music notes data to file.
     *
     * @param object Data object that holds all the music notes data.
     * @throws IOException If something went wrong when LZ4 compressing.
     */
    public void writeMusicNotesData(MusicNotesDataObject object) throws IOException {
        // Cast to the correct version of the object
        MusicNotesDataObject0x00090002 obj = (MusicNotesDataObject0x00090002) object;

        // Write to file
        writeSectionID(MusicNotesDataObject0x00090002.SECTION_ID);
        write1DDoubleArray(obj.timesToPlaceRectangles);
        write1DDoubleArray(obj.noteDurations);
        write1DIntegerArray(obj.noteNums);
        writeEOSDelimiter();
    }
}
