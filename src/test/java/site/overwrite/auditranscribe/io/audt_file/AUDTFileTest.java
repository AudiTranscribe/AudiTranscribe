/*
 * AUDTFileTest.java
 *
 * Created on 2022-05-01
 * Updated on 2022-06-24
 *
 * Description: Test AUDT file reading and writing.
 */

package site.overwrite.auditranscribe.io.audt_file;

import org.javatuples.Triplet;
import org.junit.jupiter.api.*;
import site.overwrite.auditranscribe.exceptions.io.audt_file.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.IncorrectFileFormatException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.OutdatedFileFormatException;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AUDTFileTest {
    // Define the file path
    static final String FILE_PATH = IOMethods.joinPaths(
            IOConstants.RESOURCES_FOLDER_PATH, "io-testing-directory", "files",
            "test-AUDTFileTest.audt"
    );

    // Define sample array data
    // (These are example arrays, not actual data)
    double[][] qTransformMagnitudes = new double[][]{
            {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
            {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
            {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
    };

    double[] timesToPlaceRectangles1 = {1, 2, 3, 4.5, 6.7, 8.9};
    double[] noteDurations1 = {0.5, 1, 1.5, 2.5, 3.5, 10};
    int[] noteNums1 = {32, 41, 91, 82, 84, 55};

    double[] timesToPlaceRectangles2 = {0, 0.9, 1.2, 1.8, 2.4, 3.3, 3.6, 4.2};
    double[] noteDurations2 = {0.9, 0.3, 0.6, 0.6, 0.9, 0.3, 0.6, 0.6};
    int[] noteNums2 = {64, 62, 53, 55, 60, 59, 52, 53};

    // Convert the magnitude data to required form
    Triplet<Byte[], Double, Double> conversionTuple =
            QTransformDataObject.qTransformMagnitudesToByteData(qTransformMagnitudes, null);
    byte[] qTransformBytes = TypeConversionUtils.toByteArray(conversionTuple.getValue0());
    double minMagnitude = conversionTuple.getValue1();
    double maxMagnitude = conversionTuple.getValue2();

    // Define data to be used within the tests
    QTransformDataObject qTransformDataObject = new QTransformDataObject(
            qTransformBytes, minMagnitude, maxMagnitude
    );
    AudioDataObject audioDataObject = new AudioDataObject(
            LZ4.lz4Compress(Files.readAllBytes(Path.of(IOMethods.getAbsoluteFilePath("testing-audio-files/A440.mp3")))),
            44100, 120000, "A440.wav");

    GUIDataObject guiDataObject1 = new GUIDataObject(
            11, 9, 123.45, 0.01, 0.55, 9000
    );
    GUIDataObject guiDataObject2 = new GUIDataObject(
            15, 14, 67.89, -1.23, 0.124, 2048
    );

    MusicNotesDataObject musicNotesDataObject1 = new MusicNotesDataObject(
            timesToPlaceRectangles1, noteDurations1, noteNums1
    );
    MusicNotesDataObject musicNotesDataObject2 = new MusicNotesDataObject(
            timesToPlaceRectangles2, noteDurations2, noteNums2
    );

    UnchangingDataPropertiesObject unchangingDataPropertiesObject = new UnchangingDataPropertiesObject(
            32 +  // Header section
                    UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                    qTransformDataObject.numBytesNeeded() +
                    audioDataObject.numBytesNeeded()
    );

    // Initialization method
    AUDTFileTest() throws IOException {
    }

    // Tests
    @Test
    @Order(1)
    void fileWriterTestOne() throws IOException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = new AUDTFileWriter(FILE_PATH);

        // Test writing some data
        fileWriter.writeUnchangingDataProperties(unchangingDataPropertiesObject);
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeAudioData(audioDataObject);
        fileWriter.writeGUIData(guiDataObject1);
        fileWriter.writeMusicNotesData(musicNotesDataObject1);

        // Write the bytes to file
        fileWriter.writeBytesToFile();
    }

    @Test
    @Order(2)
    void fileReaderTestOne() throws IOException, IncorrectFileFormatException, OutdatedFileFormatException,
            FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = new AUDTFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        GUIDataObject readGUIData = fileReader.readGUIData();
        MusicNotesDataObject readMusicData = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(guiDataObject1, readGUIData);
        assertEquals(musicNotesDataObject1, readMusicData);

        // Check if the decompressed version of the Q-Transform magnitudes is the same
        double[][] array = QTransformDataObject.byteDataToQTransformMagnitudes(
                qTransformDataObject.qTransformBytes,
                qTransformDataObject.minMagnitude,
                qTransformDataObject.maxMagnitude
        );

        assertEquals(array.length, qTransformMagnitudes.length);
        for (int i = 0; i < array.length; i++) {
            assertArrayEquals(array[i], qTransformMagnitudes[i], 1e-5);
        }
    }

    @Test
    @Order(3)
    void fileWriterTestTwo() throws IOException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = new AUDTFileWriter(FILE_PATH, unchangingDataPropertiesObject.numSkippableBytes);

        // Test writing only the GUI and music notes data
        fileWriter.writeGUIData(guiDataObject2);
        fileWriter.writeMusicNotesData(musicNotesDataObject2);

        // Write the bytes to file
        fileWriter.writeBytesToFile();
    }

    @Test
    @Order(4)
    void fileReaderTestTwo() throws IOException, IncorrectFileFormatException, OutdatedFileFormatException,
            FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = new AUDTFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        GUIDataObject readGUIData = fileReader.readGUIData();
        MusicNotesDataObject readMusicData2 = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(guiDataObject2, readGUIData);
        assertEquals(musicNotesDataObject2, readMusicData2);

        // Check if the decompressed version of the Q-Transform magnitudes is the same
        double[][] array = QTransformDataObject.byteDataToQTransformMagnitudes(
                qTransformDataObject.qTransformBytes,
                qTransformDataObject.minMagnitude,
                qTransformDataObject.maxMagnitude
        );

        assertEquals(array.length, qTransformMagnitudes.length);
        for (int i = 0; i < array.length; i++) {
            assertArrayEquals(array[i], qTransformMagnitudes[i], 1e-5);
        }
    }

    @AfterAll
    static void deleteTestingFile() throws IOException {
        Files.deleteIfExists(Path.of(FILE_PATH));
    }
}