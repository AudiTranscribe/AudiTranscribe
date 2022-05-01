/*
 * AUDTFileConstants.java
 *
 * Created on 2022-05-01
 * Updated on 2022-05-01
 *
 * Description: Constants that are needed when processing the AudiTranscribe file format.
 */

package site.overwrite.auditranscribe.io.file_handers;

/**
 * Constants that are needed when processing the AudiTranscribe file format.
 */
public class AUDTFileConstants {
    // Constants
    public static final byte[] AUDT_FILE_HEADER = new byte[] {
            (byte) 0x41, (byte) 0x55, (byte) 0x44, (byte) 0x49,
            (byte) 0x54, (byte) 0x52, (byte) 0x41, (byte) 0x4e,
            (byte) 0x53, (byte) 0x43, (byte) 0x52, (byte) 0x49,
            (byte) 0x42, (byte) 0x45, (byte) 0x0a, (byte) 0x0a,
            (byte) 0xad, (byte) 0x75, (byte) 0xc1, (byte) 0xbe
    };

    public static final int FILE_VERSION_NUMBER = 1;
    public static final int LZ4_VERSION_NUMBER = 1;
}
