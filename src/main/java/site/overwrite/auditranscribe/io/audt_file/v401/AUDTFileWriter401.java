/*
 * AUDTFileWriter401.java
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file for file version
 *              401.
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

package site.overwrite.auditranscribe.io.audt_file.v401;

import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileWriter;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.misc.MyLogger;

import java.io.IOException;
import java.util.logging.Level;

public class AUDTFileWriter401 extends AUDTFileWriter {
    /**
     * Initialization method to make an <code>AUDTFileWriter401</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter401(String filepath, int numBytesToSkip) {
        super(401, filepath, numBytesToSkip);
        MyLogger.log(Level.INFO, "Using Version 401 AUDT file writer", AUDTFileWriter.class.getName());
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter401</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter401(String filepath) {
        super(401, filepath);
        MyLogger.log(Level.INFO, "Using Version 401 AUDT file writer", AUDTFileWriter.class.getName());
    }

    // Public methods

    /**
     * Method that writes the unchanging data properties to file.
     *
     * @param unchangingDataProperties Data object that contains the unchanging data's properties.
     */
    public void writeUnchangingDataProperties(UnchangingDataPropertiesObject unchangingDataProperties) {
        writeSectionID(UnchangingDataPropertiesObject.SECTION_ID);
        writeInteger(unchangingDataProperties.numSkippableBytes);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the Q-Transform data to file.
     *
     * @param qTransformDataObj Data object that holds all the Q-Transform data.
     */
    public void writeQTransformData(QTransformDataObject qTransformDataObj) {
        writeSectionID(QTransformDataObject.SECTION_ID);
        writeDouble(qTransformDataObj.minMagnitude);
        writeDouble(qTransformDataObj.maxMagnitude);
        writeByteArray(qTransformDataObj.qTransformBytes);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the audio data to file.
     *
     * @param audioDataObj Data object that holds all the audio data.
     */
    public void writeAudioData(AudioDataObject audioDataObj) {
        writeSectionID(AudioDataObject.SECTION_ID);
        writeByteArray(audioDataObj.compressedMP3Bytes);
        writeDouble(audioDataObj.sampleRate);
        writeInteger(audioDataObj.totalDurationInMS);
        writeString(audioDataObj.audioFileName);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the GUI data to file.
     *
     * @param guiDataObj Data object that holds all the GUI data.
     */
    public void writeGUIData(GUIDataObject guiDataObj) {
        writeSectionID(GUIDataObject.SECTION_ID);
        writeInteger(guiDataObj.musicKeyIndex);
        writeInteger(guiDataObj.timeSignatureIndex);
        writeDouble(guiDataObj.bpm);
        writeDouble(guiDataObj.offsetSeconds);
        writeDouble(guiDataObj.playbackVolume);
        writeInteger(guiDataObj.currTimeInMS);
        writeEOSDelimiter();
    }

    /**
     * Method that writes the music notes data to file.
     *
     * @param musicNotesDataObj Data object that holds all the music notes data.
     * @throws IOException If something went wrong when LZ4 compressing.
     */
    public void writeMusicNotesData(MusicNotesDataObject musicNotesDataObj) throws IOException {
        writeSectionID(MusicNotesDataObject.SECTION_ID);
        write1DDoubleArray(musicNotesDataObj.timesToPlaceRectangles);
        write1DDoubleArray(musicNotesDataObj.noteDurations);
        write1DIntegerArray(musicNotesDataObj.noteNums);
        writeEOSDelimiter();
    }
}
