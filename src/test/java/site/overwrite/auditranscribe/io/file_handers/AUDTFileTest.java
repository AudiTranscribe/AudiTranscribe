/*
 * AUDTFileTest.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-05
 *
 * Description: Test AUDT file reading and writing.
 */

package site.overwrite.auditranscribe.io.file_handers;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.io.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.io.exceptions.FailedToReadDataException;
import site.overwrite.auditranscribe.io.exceptions.IncorrectFileFormatException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AUDTFileTest {
    // Define the file name
    final String fileName = "src/main/resources/site/overwrite/auditranscribe/test-file-io-directory/test-AUDTFileTest";

    // Define data to be used within the tests
    QTransformDataObject qTransformDataObject = new QTransformDataObject(
            new double[][]{
                    {65.43, -123.45, 9876.54321, 3.14159265, -0.000082147128481},
                    {65.43, 9876.54321, 3.14159265, -0.000082147128481, -123.45},
                    {65.43, -123.45, 3.14159265, -0.000082147128481, 9876.54321}
            }
    );  // This is just an example array for the Q-Transform data
    GUIDataObject guiDataObject = new GUIDataObject(11, 9, 123.45, 0.01, 0.55, "Melancholy.wav", 120000, 9000);

    // Tests
    @Test
    @Order(1)
    void fileWriterTest() throws IOException {
        // Create a filewriter object
        AUDTFileWriter fileWriter = new AUDTFileWriter(fileName);

        // Test writing some data
        fileWriter.writeQTransformData(qTransformDataObject);
        fileWriter.writeGUIData(guiDataObject);

        // Write the bytes to file
        try {
            fileWriter.writeBytesToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    void fileReaderTest() throws IOException, IncorrectFileFormatException, FailedToReadDataException {
        // Create a filereader object
        AUDTFileReader fileReader = new AUDTFileReader(fileName + ".audt");  // This needs the extension

        // Test reading some data
        QTransformDataObject readQTransformData = fileReader.readQTransformData();
        GUIDataObject readGUIData = fileReader.readGUIData();

        // Check if the read data are equal
        assertEquals(qTransformDataObject, readQTransformData);
        assertEquals(guiDataObject, readGUIData);
    }
}