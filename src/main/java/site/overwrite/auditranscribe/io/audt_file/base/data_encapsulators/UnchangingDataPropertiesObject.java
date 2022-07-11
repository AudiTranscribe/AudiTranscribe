/*
 * UnchangingDataPropertiesObject.java
 *
 * Created on 2022-06-21
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the unchanging data's properties.
 */

package site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators;

import java.util.Objects;

/**
 * Data object that stores the unchanging data's properties.
 */
public abstract class UnchangingDataPropertiesObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 1;

    public static final int NUM_BYTES_NEEDED =
            4 +   // Section ID
                    4 +  // Number of skippable bytes
                    4;   // EOS delimiter

    // Attributes
    public int numSkippableBytes;

    // Overwritten methods
    @Override
    public int numBytesNeeded() {
        return NUM_BYTES_NEEDED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnchangingDataPropertiesObject that = (UnchangingDataPropertiesObject) o;
        return numSkippableBytes == that.numSkippableBytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numSkippableBytes);
    }
}
