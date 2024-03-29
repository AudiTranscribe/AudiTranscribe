package app.auditranscribe.io.audt_file.v0x000C00;

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.audt_file.InvalidFileVersionException;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.AUDTFileReader;
import app.auditranscribe.io.audt_file.base.AUDTFileWriter;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.audt_file.v0x000500.data_encapsulators.UnchangingDataPropertiesObject0x000500;
import app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators.AudioDataObject0x000B00;
import app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators.ProjectInfoDataObject0x000B00;
import app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators.QTransformDataObject0x000B00;
import app.auditranscribe.io.audt_file.v0x000C00.data_encapsulators.MusicNotesDataObject0x000C00;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.TimeSignature;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AUDTFile0x000C00Test {
    // Define the file path
    static final String FILE_PATH = IOMethods.joinPaths(
            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
            "test-files", "io", "audt_file", "v0x000C00", "test-AUDTFile0x000C00Test.audt"
    );

    // Define helper attributes
    static double[][] qTransformMagnitudes;

    // Define data to be used within the tests
    static QTransformDataObject qTransformDataObject;
    static AudioDataObject audioDataObject;

    static ProjectInfoDataObject projectInfoDataObject1;
    static ProjectInfoDataObject projectInfoDataObject2;

    static MusicNotesDataObject musicNotesDataObject;

    static UnchangingDataPropertiesObject unchangingDataPropertiesObject;

    static ProjectData projectData1;
    static ProjectData projectData2;

    @BeforeAll
    static void beforeAll() throws IOException {
        // Define sample array data
        // (These are example arrays, not actual data)
        qTransformMagnitudes = new double[][]{
                {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
                {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
                {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
        };

        // Define data to be used within the tests
        qTransformDataObject = new QTransformDataObject0x000B00();
        qTransformDataObject.setDataUsingMagnitudes(qTransformMagnitudes, null);
        audioDataObject = new AudioDataObject0x000B00(
                Files.readAllBytes(Paths.get(
                        IOMethods.getAbsoluteFilePath("test-files/general/audio/VeryShortAudio.mp3")
                )), 44100, 200
        );

        projectInfoDataObject1 = new ProjectInfoDataObject0x000B00(
                "Test-1", MusicKey.C_SHARP_MAJOR, TimeSignature.TWO_TWO, 123.45, 0.01,
                0.55, 12
        );
        projectInfoDataObject2 = new ProjectInfoDataObject0x000B00(
                "Test-2", MusicKey.G_FLAT_MAJOR, TimeSignature.SIX_FOUR, 67.89, -1.23,
                0.124, 34
        );

        musicNotesDataObject = new MusicNotesDataObject0x000C00();

        unchangingDataPropertiesObject = new UnchangingDataPropertiesObject0x000500(
                32 +  // Header section
                        UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                        qTransformDataObject.numBytesNeeded() +
                        audioDataObject.numBytesNeeded()
        );

        // Define the overall project data object
        projectData1 = new ProjectData(
                unchangingDataPropertiesObject, qTransformDataObject, audioDataObject, projectInfoDataObject1,
                musicNotesDataObject
        );
        projectData2 = new ProjectData(
                unchangingDataPropertiesObject, qTransformDataObject, audioDataObject, projectInfoDataObject2,
                musicNotesDataObject
        );
    }

    // Tests
    @Test
    @Order(1)
    void fileWriter_initialWrite() throws IOException, InvalidFileVersionException {
        // Create a file writer object
        AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(0x000C0001, FILE_PATH);

        // Test writing some data
        fileWriter.writeUnchangingDataProperties(unchangingDataPropertiesObject);
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeAudioData(audioDataObject);
        fileWriter.writeProjectInfoData(projectInfoDataObject1);
        fileWriter.writeMusicNotesData(musicNotesDataObject);

        // Write the bytes to file
        fileWriter.writeToFile();
    }

    @Test
    @Order(2)
    void fileReader_initialRead() throws IOException, AUDTFileReader.IncorrectFileFormatException,
            InvalidFileVersionException, AUDTFileReader.DataReadFailedException {
        // Create a file reader object
        AUDTFileReader fileReader = AUDTFileReader.getFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        ProjectInfoDataObject readProjectInfoData = fileReader.readProjectInfoData();
        MusicNotesDataObject readMusicData = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(projectInfoDataObject1, readProjectInfoData);
        assertEquals(musicNotesDataObject, readMusicData);

        // Check if the decompressed version of the Q-Transform magnitudes is the same
        double[][] array = qTransformDataObject.obtainMagnitudesFromData();

        assertEquals(array.length, qTransformMagnitudes.length);
        for (int i = 0; i < array.length; i++) {
            assertArrayEquals(array[i], qTransformMagnitudes[i], 1e-5);
        }

        // Check if the project data are equal
        ProjectData readProjectData = new ProjectData(
                readUnchangingDataProperties, readQTransformData, readAudioData, readProjectInfoData, readMusicData
        );

        assertEquals(projectData1, readProjectData);
    }

    @Test
    @Order(2)
    void fileReader_checkBytesMatch() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            InvalidFileVersionException, IOException, AUDTFileReader.IncorrectFileFormatException {
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
    void fileWriter_initialWriteAlt() throws IOException, InvalidFileVersionException {
        // Create a file writer object
        AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(0x000C0001, FILE_PATH, 0);

        // Test writing some data
        fileWriter.writeUnchangingDataProperties(unchangingDataPropertiesObject);
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeAudioData(audioDataObject);
        fileWriter.writeProjectInfoData(projectInfoDataObject1);
        fileWriter.writeMusicNotesData(musicNotesDataObject);

        // Write the bytes to file
        fileWriter.writeToFile();
    }

    @Test
    @Order(4)
    void fileReader_initialReadAlt() throws IOException, AUDTFileReader.IncorrectFileFormatException,
            InvalidFileVersionException, AUDTFileReader.DataReadFailedException {
        // Create a file reader object
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
        assertEquals(musicNotesDataObject, readMusicData);

        // Check if the decompressed version of the Q-Transform magnitudes is the same
        double[][] array = qTransformDataObject.obtainMagnitudesFromData();

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
    void fileWriter_2() throws IOException, InvalidFileVersionException {
        // Create a file writer object
        AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(
                0x000C0001, FILE_PATH, unchangingDataPropertiesObject.numSkippableBytes
        );

        // Test writing only the GUI and music notes data
        fileWriter.writeProjectInfoData(projectInfoDataObject2);
        fileWriter.writeMusicNotesData(musicNotesDataObject);

        // Write the bytes to file
        fileWriter.writeToFile();
    }

    @Test
    @Order(6)
    void fileReader_2() throws IOException, AUDTFileReader.IncorrectFileFormatException, InvalidFileVersionException,
            AUDTFileReader.DataReadFailedException {
        // Create a file reader object
        AUDTFileReader fileReader = AUDTFileReader.getFileReader(FILE_PATH);

        // Test reading some data
        UnchangingDataPropertiesObject readUnchangingDataProperties = fileReader.readUnchangingDataProperties();
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        ProjectInfoDataObject readProjectInfoData = fileReader.readProjectInfoData();
        MusicNotesDataObject readMusicData = fileReader.readMusicNotesData();

        // Check if the read data are equal
        assertEquals(unchangingDataPropertiesObject, readUnchangingDataProperties);
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(projectInfoDataObject2, readProjectInfoData);
        assertEquals(musicNotesDataObject, readMusicData);

        // Check if the decompressed version of the Q-Transform magnitudes is the same
        double[][] array = qTransformDataObject.obtainMagnitudesFromData();

        assertEquals(array.length, qTransformMagnitudes.length);
        for (int i = 0; i < array.length; i++) {
            assertArrayEquals(array[i], qTransformMagnitudes[i], 1e-5);
        }

        // Check if the project data are equal
        ProjectData readProjectData = new ProjectData(
                readUnchangingDataProperties, readQTransformData, readAudioData, readProjectInfoData, readMusicData
        );

        assertEquals(projectData2, readProjectData);
    }

    @Test
    void fileReader_exceptions() {
        // Define files' folder
        String folder = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "test-files", "io", "audt_file", "v0x000C00", "AUDTFile0x000C00Test"
        );

        // Perform tests
        for (int sectionID = 1; sectionID <= 4; sectionID++) {  // 4 sections
            // Define paths
            String idIncorrectFile = IOMethods.joinPaths(
                    folder, "section" + sectionID + "-id-incorrect.audt"
            );
            String eosIncorrectFile = IOMethods.joinPaths(
                    folder, "section" + sectionID + "-eos-incorrect.audt"
            );

            int finalSectionID = sectionID;
            assertThrowsExactly(AUDTFileReader.DataReadFailedException.class, () -> {
                // Define reader
                AUDTFileReader idReader = AUDTFileReader.getFileReader(idIncorrectFile);

                // Call methods
                idReader.readUnchangingDataProperties();
                if (finalSectionID >= 2) idReader.readQTransformData();
                if (finalSectionID >= 3) idReader.readAudioData();
                if (finalSectionID == 4) idReader.readProjectInfoData();

            });
            assertThrowsExactly(AUDTFileReader.DataReadFailedException.class, () -> {
                // Define reader
                AUDTFileReader eosReader = AUDTFileReader.getFileReader(eosIncorrectFile);

                // Call methods
                eosReader.readUnchangingDataProperties();
                if (finalSectionID >= 2) eosReader.readQTransformData();
                if (finalSectionID >= 3) eosReader.readAudioData();
                if (finalSectionID == 4) eosReader.readProjectInfoData();
            });
        }
    }

    @AfterAll
    static void deleteTestingFile() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }
}