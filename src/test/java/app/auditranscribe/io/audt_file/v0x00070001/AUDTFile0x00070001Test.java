/*
 * AUDTFile0x00070001Test.java
 * Description: Test AUDT file reading and writing for file version 0x00070001.
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

package app.auditranscribe.io.audt_file.v0x00070001;

import app.auditranscribe.generic.tuples.Triple;
import app.auditranscribe.io.CompressionHandlers;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.AUDTFileReader;
import app.auditranscribe.io.audt_file.base.AUDTFileWriter;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.audt_file.v0x00070001.data_encapsulators.*;
import app.auditranscribe.io.exceptions.FailedToReadDataException;
import app.auditranscribe.io.exceptions.IncorrectFileFormatException;
import app.auditranscribe.io.exceptions.InvalidFileVersionException;
import app.auditranscribe.utils.TypeConversionUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AUDTFile0x00070001Test {
    // Define the file path
    static final String FILE_PATH = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "testing-files", "misc", "test-AUDTFile0x00070001Test.audt"
    );

    // Define helper attributes
    double[][] qTransformMagnitudes;

    double[] timesToPlaceRectangles1;
    double[] noteDurations1;
    int[] noteNums1;

    double[] timesToPlaceRectangles2;
    double[] noteDurations2;
    int[] noteNums2;

    // Define data to be used within the tests
    QTransformDataObject qTransformDataObject;
    AudioDataObject audioDataObject;

    ProjectInfoDataObject projectInfoDataObject1;
    ProjectInfoDataObject projectInfoDataObject2;

    MusicNotesDataObject musicNotesDataObject1;
    MusicNotesDataObject musicNotesDataObject2;

    UnchangingDataPropertiesObject unchangingDataPropertiesObject;

    ProjectData projectData1;
    ProjectData projectData2;

    // Initialization method
    AUDTFile0x00070001Test() throws IOException {
        // Define sample array data
        // (These are example arrays, not actual data)
        qTransformMagnitudes = new double[][]{
                {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
                {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
                {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
        };

        timesToPlaceRectangles1 = new double[]{1, 2, 3, 4.5, 6.7, 8.9};
        noteDurations1 = new double[]{0.5, 1, 1.5, 2.5, 3.5, 10};
        noteNums1 = new int[]{32, 41, 91, 82, 84, 55};

        timesToPlaceRectangles2 = new double[]{0, 0.9, 1.2, 1.8, 2.4, 3.3, 3.6, 4.2};
        noteDurations2 = new double[]{0.9, 0.3, 0.6, 0.6, 0.9, 0.3, 0.6, 0.6};
        noteNums2 = new int[]{64, 62, 53, 55, 60, 59, 52, 53};

        // Convert the magnitude data to required form
        Triple<Byte[], Double, Double> conversionTuple =
                QTransformDataObject.qTransformMagnitudesToByteData(qTransformMagnitudes, null);
        byte[] qTransformBytes = TypeConversionUtils.toByteArray(conversionTuple.value0());
        double minMagnitude = conversionTuple.value1();
        double maxMagnitude = conversionTuple.value2();

        // Define data to be used within the tests
        qTransformDataObject = new QTransformDataObject0x00070001(
                qTransformBytes, minMagnitude, maxMagnitude
        );
        audioDataObject = new AudioDataObject0x00070001(
                CompressionHandlers.lz4Compress(Files.readAllBytes(Paths.get(
                        IOMethods.getAbsoluteFilePath("testing-files/audio/VeryShortAudio.mp3")
                ))),
                44100, 200);

        projectInfoDataObject1 = new ProjectInfoDataObject0x00070001(
                "Test-1", 1, 0, 123.45, 0.01, 0.55, 12
        );
        projectInfoDataObject2 = new ProjectInfoDataObject0x00070001(
                "Test-2", 8, 7, 67.89, -1.23, 0.124, 34
        );

        musicNotesDataObject1 = new MusicNotesDataObject0x00070001(
                timesToPlaceRectangles1, noteDurations1, noteNums1
        );
        musicNotesDataObject2 = new MusicNotesDataObject0x00070001(
                timesToPlaceRectangles2, noteDurations2, noteNums2
        );

        unchangingDataPropertiesObject = new UnchangingDataPropertiesObject0x00070001(
                32 +  // Header section
                        UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                        qTransformDataObject.numBytesNeeded() +
                        audioDataObject.numBytesNeeded()
        );

        // Define the overall project data object
        projectData1 = new ProjectData(
                unchangingDataPropertiesObject, qTransformDataObject, audioDataObject, projectInfoDataObject1,
                musicNotesDataObject1
        );
        projectData2 = new ProjectData(
                unchangingDataPropertiesObject, qTransformDataObject, audioDataObject, projectInfoDataObject2,
                musicNotesDataObject2
        );
    }

    // Tests
    @Test
    @Order(1)
    void fileWriterTestInitialWrite() throws IOException, InvalidFileVersionException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(0x00070001, FILE_PATH);

        // Test writing some data
        fileWriter.writeUnchangingDataProperties(unchangingDataPropertiesObject);
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeAudioData(audioDataObject);
        fileWriter.writeProjectInfoData(projectInfoDataObject1);
        fileWriter.writeMusicNotesData(musicNotesDataObject1);

        // Write the bytes to file
        fileWriter.writeBytesToFile();
    }

    @Test
    @Order(2)
    void fileReaderTestInitialRead() throws IOException, IncorrectFileFormatException, InvalidFileVersionException,
            FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = AUDTFileReader.getFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        ProjectInfoDataObject readGUIData = fileReader.readProjectInfoData();
        MusicNotesDataObject readMusicData = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(projectInfoDataObject1, readGUIData);
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

        // Check if the project data are equal
        ProjectData readProjectData = new ProjectData(
                readUnchangingDataProperties, readQTransformData, readAudioData, readGUIData, readMusicData
        );

        assertEquals(projectData1, readProjectData);
    }

    @Test
    @Order(2)
    void readerCheckBytesMatch() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            InvalidFileVersionException, IOException, IncorrectFileFormatException {
        // Make the method accessible to this test
        Method mtd = AUDTFileReader.class.getDeclaredMethod("checkBytesMatch", byte[].class, byte[].class);
        mtd.setAccessible(true);

        // Define byte arrays to test
        byte[] bytes1 = new byte[]{
                (byte) 0xe0, (byte) 0x5e, (byte) 0x05, (byte) 0xe5
        };
        byte[] bytes2 = new byte[]{
                (byte) 0xe0, (byte) 0xfe, (byte) 0x0f, (byte) 0xef,
                (byte) 0xe0, (byte) 0xfe, (byte) 0x0f, (byte) 0xef
        };
        byte[] bytes3 = new byte[]{
                (byte) 0xe0, (byte) 0x2e, (byte) 0x4f, (byte) 0xe1,
                (byte) 0x10, (byte) 0xfe, (byte) 0x14, (byte) 0xef
        };

        // Define reader object
        AUDTFileReader reader = AUDTFileReader.getFileReader(FILE_PATH);

        // Run tests
        assertTrue((Boolean) mtd.invoke(reader, bytes1, bytes1));
        assertTrue((Boolean) mtd.invoke(reader, bytes2, bytes2));
        assertTrue((Boolean) mtd.invoke(reader, bytes3, bytes3));

        assertFalse((Boolean) mtd.invoke(reader, bytes1, bytes2));
        assertFalse((Boolean) mtd.invoke(reader, bytes1, bytes3));
        assertFalse((Boolean) mtd.invoke(reader, bytes2, bytes3));
    }

    @Test
    @Order(3)
    void fileWriterTestInitialWriteAlt() throws IOException, InvalidFileVersionException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(0x00070001, FILE_PATH, 0);

        // Test writing some data
        fileWriter.writeUnchangingDataProperties(unchangingDataPropertiesObject);
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeAudioData(audioDataObject);
        fileWriter.writeProjectInfoData(projectInfoDataObject1);
        fileWriter.writeMusicNotesData(musicNotesDataObject1);

        // Write the bytes to file
        fileWriter.writeBytesToFile();
    }

    @Test
    @Order(4)
    void fileReaderTestInitialReadAlt() throws IOException, IncorrectFileFormatException, InvalidFileVersionException,
            FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = AUDTFileReader.getFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        ProjectInfoDataObject readGUIData = fileReader.readProjectInfoData();
        MusicNotesDataObject readMusicData = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(projectInfoDataObject1, readGUIData);
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

        // Check if the project data are equal
        ProjectData readProjectData = new ProjectData(
                readUnchangingDataProperties, readQTransformData, readAudioData, readGUIData, readMusicData
        );

        assertEquals(projectData1, readProjectData);
    }

    @Test
    @Order(5)
    void fileWriterTestTwo() throws IOException, InvalidFileVersionException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(
                0x00070001, FILE_PATH, unchangingDataPropertiesObject.numSkippableBytes
        );

        // Test writing only the GUI and music notes data
        fileWriter.writeProjectInfoData(projectInfoDataObject2);
        fileWriter.writeMusicNotesData(musicNotesDataObject2);

        // Write the bytes to file
        fileWriter.writeBytesToFile();
    }

    @Test
    @Order(6)
    void fileReaderTestTwo() throws IOException, IncorrectFileFormatException, InvalidFileVersionException,
            FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = AUDTFileReader.getFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        ProjectInfoDataObject readGUIData = fileReader.readProjectInfoData();
        MusicNotesDataObject readMusicData = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(projectInfoDataObject2, readGUIData);
        assertEquals(musicNotesDataObject2, readMusicData);

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

        // Check if the project data are equal
        ProjectData readProjectData = new ProjectData(
                readUnchangingDataProperties, readQTransformData, readAudioData, readGUIData, readMusicData
        );

        assertEquals(projectData2, readProjectData);
    }

    @Test
    void fileReaderTestExceptions() {
        // Define files' folder
        String folder = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "audt-test-files", "v0x00070001"
        );

        // Perform tests
        for (int sectionID = 1; sectionID <= 5; sectionID++) {  // 5 sections
            // Define paths
            String idIncorrectFile = IOMethods.joinPaths(
                    folder, "section" + sectionID + "-id-incorrect.audt"
            );
            String eosIncorrectFile = IOMethods.joinPaths(
                    folder, "section" + sectionID + "-eos-incorrect.audt"
            );

            int finalSectionID = sectionID;
            assertThrowsExactly(FailedToReadDataException.class, () -> {
                // Define reader
                AUDTFileReader idReader = AUDTFileReader.getFileReader(idIncorrectFile);

                // Call methods
                idReader.readUnchangingDataProperties();
                if (finalSectionID >= 2) idReader.readQTransformData();
                if (finalSectionID >= 3) idReader.readAudioData();
                if (finalSectionID >= 4) idReader.readProjectInfoData();
                if (finalSectionID == 5) idReader.readMusicNotesData();

            });
            assertThrowsExactly(FailedToReadDataException.class, () -> {
                // Define reader
                AUDTFileReader eosReader = AUDTFileReader.getFileReader(eosIncorrectFile);

                // Call methods
                eosReader.readUnchangingDataProperties();
                if (finalSectionID >= 2) eosReader.readQTransformData();
                if (finalSectionID >= 3) eosReader.readAudioData();
                if (finalSectionID >= 4) eosReader.readProjectInfoData();
                if (finalSectionID == 5) eosReader.readMusicNotesData();
            });
        }
    }

    @AfterAll
    static void deleteTestingFile() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }
}