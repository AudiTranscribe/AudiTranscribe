/*
 * AUDTFileBaseTest.java
 *
 * Created on 2022-07-15
 * Updated on 2022-07-16
 *
 * Description: Basic tests for the AUDT file reader.
 */

package site.overwrite.auditranscribe.io.audt_file;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.exceptions.io.audt_file.IncorrectFileFormatException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.InvalidFileVersionException;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileReader;
import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileWriter;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class AUDTFileBaseTest {
    @Test
    void fileReaderTestExceptions() {
        // Define files' folder
        String folder = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "testing-files", "audt-test-files"
        );

        // Perform tests
        assertThrowsExactly(IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader("a")  // Too short of a file name
        );
        assertThrowsExactly(IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader("not-audt-file.txt")  // Incorrect extension
        );
        assertThrowsExactly(FileNotFoundException.class, () ->
                AUDTFileReader.getFileReader("abc.audt")  // Non-existent file
        );
        assertThrowsExactly(IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "header-incorrect.audt"))
        );
        assertThrowsExactly(InvalidFileVersionException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "invalid-file-version.audt"))
        );
        assertThrowsExactly(InvalidFileVersionException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "lz4-version-incorrect.audt"))
        );
        assertThrowsExactly(IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "eof-incorrect.audt"))
        );
    }

    @Test
    void fileWriterTestExceptions() {
        assertThrowsExactly(InvalidFileVersionException.class, () ->
                AUDTFileWriter.getWriter(-1, "not-an-audt-file.audt")
        );
        assertThrowsExactly(InvalidFileVersionException.class, () ->
                AUDTFileWriter.getWriter(-1, "not-an-audt-file.audt", -1)
        );
    }
}
