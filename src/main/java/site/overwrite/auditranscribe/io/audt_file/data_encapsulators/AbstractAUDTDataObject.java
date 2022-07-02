/*
 * AbstractAUDTDataObject.java
 *
 * Created on 2022-05-02
 * Updated on 2022-07-02
 *
 * Description: Abstract AUDT data object class that stores the data needed.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

/**
 * Abstract AUDT data object class that stores the data needed.
 */
public abstract class AbstractAUDTDataObject {
    // Abstract methods

    /**
     * Method that returns the number of bytes needed to store the data object.
     *
     * @return The number of bytes needed to store the data object.
     */
    public abstract int numBytesNeeded();
}
