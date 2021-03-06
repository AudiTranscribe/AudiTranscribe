/*
 * AUDTFileWriter0x00050002.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-12
 *
 * Description: Class that handles the writing of the AudiTranscribe (AUDT) file for file version
 *              0x00050002.
 */

package site.overwrite.auditranscribe.io.audt_file.v0x00050002;

import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileWriter;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.misc.MyLogger;

import java.io.IOException;
import java.util.logging.Level;

public class AUDTFileWriter0x00050002 extends AUDTFileWriter {
    /**
     * Initialization method to make an <code>AUDTFileWriter0x00050002</code> object.
     *
     * @param filepath       Path to the AUDT file. The file name at the end of the file path should
     *                       <b>include</b> the extension of the AUDT file.
     * @param numBytesToSkip Number of bytes to skip at the beginning of the file.
     */
    public AUDTFileWriter0x00050002(String filepath, int numBytesToSkip) {
        super(0x00050002, filepath, numBytesToSkip);
        MyLogger.log(Level.INFO, "Using Version 0x00050002 AUDT file writer", AUDTFileWriter.class.getName());
    }

    /**
     * Initialization method to make an <code>AUDTFileWriter0x00050002</code> object.
     *
     * @param filepath Path to the AUDT file. The file name at the end of the file path should
     *                 <b>include</b> the extension of the AUDT file.
     */
    public AUDTFileWriter0x00050002(String filepath) {
        super(0x00050002, filepath);
        MyLogger.log(Level.INFO, "Using Version 0x00050002 AUDT file writer", AUDTFileWriter.class.getName());
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
