/*
 * AUDTFileBaseTest.java
 * Description: Base tests for the AUDT file reader and writer.
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

package app.auditranscribe.io.audt_file;

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.Test;
import app.auditranscribe.io.exceptions.IncorrectFileFormatException;
import app.auditranscribe.io.exceptions.InvalidFileVersionException;
import app.auditranscribe.io.audt_file.base.AUDTFileReader;
import app.auditranscribe.io.audt_file.base.AUDTFileWriter;

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
