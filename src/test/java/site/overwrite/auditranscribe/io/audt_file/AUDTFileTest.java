/*
 * AUDTFileTest.java
 *
 * Created on 2022-05-01
 * Updated on 2022-06-06
 *
 * Description: Test AUDT file reading and writing.
 */

package site.overwrite.auditranscribe.io.audt_file;

import org.javatuples.Triplet;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.exceptions.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.IncorrectFileFormatException;
import site.overwrite.auditranscribe.utils.TypeConversionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AUDTFileTest {
    // Define the file name
    final String fileName =
            "src/main/resources/site/overwrite/auditranscribe/test-resources/file-io-directory/test-AUDTFileTest.audt";

    // Define Q-Transform magnitude data
    // (This is just an example array, not actual data)
    double[][] qTransformMagnitudes = new double[][]{
            {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
            {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
            {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
    };

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
            44100, "A440.wav"
    );
    GUIDataObject guiDataObject = new GUIDataObject(
            11, 9, 123.45, 0.01, 0.55,
            120000, 9000
    );

    // Initialization method
    AUDTFileTest() throws IOException {
    }

    // Tests
    @Test
    @Order(1)
    void fileWriterTest() throws IOException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = new AUDTFileWriter(fileName);

        // Test writing some data
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeAudioData(audioDataObject);
        fileWriter.writeGUIData(guiDataObject);

        // Write the bytes to file
        fileWriter.writeBytesToFile();
    }

    @Test
    @Order(2)
    void fileReaderTest() throws IOException, IncorrectFileFormatException, FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = new AUDTFileReader(fileName);

        // Test reading some data
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        AudioDataObject readAudioData = fileReader.readAudioData();
        GUIDataObject readGUIData = fileReader.readGUIData();

        // Check if the read data are equal
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(audioDataObject, readAudioData);
        assertEquals(guiDataObject, readGUIData);

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
}