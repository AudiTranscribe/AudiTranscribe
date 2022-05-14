/*
 * FileHandlersHelpers.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-10
 *
 * Description: Helper methods for writing/reading from files.
 */

package site.overwrite.auditranscribe.io.audt_file.file_handers;

import java.util.List;

/**
 * Helper methods for writing/reading from files.
 */
public class FileHandlersHelpers {
    // Public methods

    /**
     * Method that helps to add bytes into a bytes list.
     *
     * @param byteList  List of bytes to modify.
     * @param byteArray Array of bytes to add.
     */
    public static void addBytesIntoBytesList(List<Byte> byteList, byte[] byteArray) {
        for (byte b : byteArray) {
            byteList.add(b);
        }
    }
}
