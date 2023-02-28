package app.auditranscribe.io.audt_file;

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.audt_file.base.AUDTFileReader;
import app.auditranscribe.io.audt_file.base.AUDTFileWriter;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class AUDTFileBaseTest {
    @Test
    void fileReaderTestExceptions() {
        // Define files' folder
        String folder = IOMethods.joinPaths(
                IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                "test-files", "io", "audt_file", "AUDTFileBaseTest"
        );

        // Perform tests
        assertThrowsExactly(AUDTFileReader.IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader("a")  // Too short of a file name
        );
        assertThrowsExactly(AUDTFileReader.IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader("not-audt-file.txt")  // Incorrect extension
        );
        assertThrowsExactly(FileNotFoundException.class, () ->
                AUDTFileReader.getFileReader("abc.audt")  // Non-existent file
        );

        assertThrowsExactly(AUDTFileReader.IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "header-incorrect.audt"))
        );
        assertThrowsExactly(AUDTFileReader.IncorrectFileFormatException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "magic-constant-incorrect.audt"))
        );
        assertThrowsExactly(InvalidFileVersionException.class, () ->
                AUDTFileReader.getFileReader(IOMethods.joinPaths(folder, "invalid-file-version.audt"))
        );
        assertThrowsExactly(AUDTFileReader.IncorrectFileFormatException.class, () ->
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
